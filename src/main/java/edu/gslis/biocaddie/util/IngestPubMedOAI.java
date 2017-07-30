package edu.gslis.biocaddie.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.Header;
import se.kb.oai.pmh.IdentifiersList;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.ResumptionToken;

/**
 * Application to ingest PubMedCentral articles into an existing ElasticSearch
 * index. Use -oai flag to ingest via OAI-PMH. Otherwise, assumes a directoy
 * containing oa_bulk data.
 * 
 * This can be run under cron to update an existing ElasticSearch index.
 */
public class IngestPubMedOAI {

	static final String PUBMED_OAI_ENDPOINT = "https://www.ncbi.nlm.nih.gov/pmc/oai/oai.cgi";
	static final String PUBMED_INDEX_NAME = "pubmed";
	static final String PUBMED_DOCUMENT_TYPE = "article";

	static Pattern pmidPattern = Pattern.compile("<article-id pub-id-type=\"pmid\">([^<]*)</article-id>");
	static Pattern pmcidPattern = Pattern.compile("<article-id pub-id-type=\"pmcid\">([^<]*)</article-id>");

	TransportClient client = null;

	public static void main(String[] args)
			throws OAIException, IOException, ParseException, InterruptedException, ExecutionException {

		Options options = createOptions();
		CommandLineParser parser = new GnuParser();
		CommandLine cl = parser.parse(options, args);
		if (cl.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(IngestPubMedOAI.class.getCanonicalName(), options);
			return;
		}

		boolean useOai = cl.hasOption("oai");
		String inputPath = cl.getOptionValue("path");
		String fromDate = cl.getOptionValue("fromDate", today());
		String eshost = cl.getOptionValue("eshost", "localhost");
		int esport = Integer.parseInt(cl.getOptionValue("esport", "9300"));

		TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(eshost), esport));

		IngestPubMedOAI ingester = new IngestPubMedOAI(client);

		if (useOai) {
			System.err.println("Ingesting from OAI endpont " + fromDate);
			ingester.ingestOai(client, fromDate);
		} else {
			System.err.println("Ingesting from filesystem " + inputPath);
			ingester.ingestOaBulk(client, inputPath);
		}

		client.close();
	}

	public IngestPubMedOAI(TransportClient client) {
		this.client = client;
	}

	public void ingestOai(TransportClient client, String fromDate)
			throws OAIException, IOException, InterruptedException, ExecutionException {
		OaiPmhServer server = new OaiPmhServer(PUBMED_OAI_ENDPOINT);

		// Get the list of PubMed articles updated since fromDate
		IdentifiersList identifiers = server.listIdentifiers("pmc", fromDate, null, null);
		ResumptionToken token = null;

		int updated = 0;
		do {
			if (token != null)
				identifiers = server.listIdentifiers(token);

			List<Header> headers = identifiers.asList();

			for (Header header : headers) {
				Record record = server.getRecord(header.getIdentifier(), "pmc");
				String metadata = record.getMetadataAsString();

				// Convert metadata to JSON and post to ElasticSearch.
				String id = getId(metadata);
				String content = pmcXmlToString(metadata);
				indexDoc(client, id, content);
				updated++;
			}

			token = identifiers.getResumptionToken();
		} while (token != null);
		System.out.println("Updated/indexed " + updated + " documents");
	}

	public static String today() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		return dateFormat.format(date).toString();
	}

	public void ingestOaBulk(TransportClient client, String inputPath)
			throws FileNotFoundException, IOException, InterruptedException, ExecutionException {

		String[] files = new String[] { "non_comm_use.A-B.xml.tar.gz", "non_comm_use.C-H.xml.tar.gz",
				"non_comm_use.I-N.xml.tar.gz", "non_comm_use.O-Z.xml.tar.gz" };

		for (String file : files) {
			System.err.println("Reading " + file);
			TarArchiveInputStream tarFile = new TarArchiveInputStream(
					new GzipCompressorInputStream(new FileInputStream(inputPath + File.separator + file)));

			TarArchiveEntry currentEntry = tarFile.getNextTarEntry();
			BufferedReader br = null;
			while (currentEntry != null) {

				if (currentEntry.isFile()) {
					br = new BufferedReader(new InputStreamReader(tarFile));
					String line;
					String xml = "";
					while ((line = br.readLine()) != null)
						xml += line + "\n";

					String id = getId(xml);
					String content = pmcXmlToString(xml);
					indexDoc(client, id, content);
				}

				currentEntry = tarFile.getNextTarEntry();
			}
			if (br != null)
				br.close();

			tarFile.close();
		}

	}

	/**
	 * Index the specified document in ElasticSerch
	 * 
	 * @param client
	 *            ES client
	 * @param id
	 *            PMCID or PMID
	 * @param content
	 *            Text content of document
	 * @throws UnknownHostException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void indexDoc(TransportClient client, String id, String content)
			throws UnknownHostException, InterruptedException, ExecutionException {

		IndexRequestBuilder indexRequest = client.prepareIndex(PUBMED_INDEX_NAME, PUBMED_DOCUMENT_TYPE, id);

		Map<String, Object> json = new HashMap<String, Object>();
		json.put("text", content);

		IndexResponse response = indexRequest.setSource(json).get();
		RestStatus status = response.status();
		if (!(status == RestStatus.OK || status == RestStatus.CREATED))
			System.err.println("Non-success status for document " + id + ": " + status.getStatus());
	}

	/**
	 * Get the ID used for indexing this document in ElasticSearch. Default to
	 * PMCID, if present. Otherwise use PMID.
	 * 
	 * @param doc
	 * @return ID or null
	 */
	public String getId(String doc) {

		Matcher m = pmcidPattern.matcher(doc);
		if (m.find())
			return m.group(1);

		m = pmidPattern.matcher(doc);
		if (m.find())
			return m.group(1);

		return null;
	}

	/**
	 * Use Tika to convert PubMed XML to text
	 * 
	 * @param doc
	 * @return Simple text
	 */
	public String pmcXmlToString(String xml) {
		BodyContentHandler handler = new BodyContentHandler(-1);
		XMLParser xmlparser = new XMLParser();
		Metadata metadata = new Metadata();

		ParseContext pcontext = new ParseContext();

		String content = "";
		try {
			xmlparser.parse(new ByteArrayInputStream(xml.getBytes()), handler, metadata, pcontext);
			content = handler.toString();
			content = content.replaceAll("\\s+", " ");
		} catch (Exception e) {
			e.printStackTrace();

		}
		return content;
	}

	/**
	 * Setup the command-line options for this application
	 * 
	 * @return
	 */
	public static Options createOptions() {
		Options options = new Options();
		options.addOption("fromDate", true, "From date for ingest. Only used with -oai flag.");
		options.addOption("oai", false, "Use OAI-PMH endpoint");
		options.addOption("path", true, "Directory containing oa_bulk data");
		options.addOption("eshost", true, "ElasticSearch host");
		options.addOption("esport", true, "ElasticSearch port");
		options.addOption("help", true, "Display this help command");

		return options;
	}
}
