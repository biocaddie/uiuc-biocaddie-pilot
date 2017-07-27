package edu.gslis.biocaddie.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.gslis.indexes.IndexWrapper;
import edu.gslis.indexes.IndexWrapperFactory;
import edu.gslis.lucene.expansion.Rocchio;
import edu.gslis.queries.GQueries;
import edu.gslis.queries.GQueriesIndriImpl;
import edu.gslis.queries.GQueriesJsonImpl;
import edu.gslis.queries.GQuery;
import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.utils.Stopper;

/**
 * Given a set of topics and a Lucene index, generate the Rocchio feedback
 * queries and write to an output file.
 */
public class GetFeedbackQueriesRocchio {

    public static void main(String[] args) throws ParseException, IOException {
        
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cl = parser.parse( options, args);
        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( GetFeedbackQueriesRocchio.class.getCanonicalName(), options );
            return;
        }
        
        String inputPath = cl.getOptionValue("input");
        String outputPath = cl.getOptionValue("output");
        String indexPath = cl.getOptionValue("index");
        
        IndexWrapper index = IndexWrapperFactory.getIndexWrapper(indexPath);
        index.setTimeFieldName(null);
        
        int fbDocs = Integer.parseInt(cl.getOptionValue("fbDocs"));
        int fbTerms = Integer.parseInt(cl.getOptionValue("fbTerms"));
        double alpha = Double.parseDouble(cl.getOptionValue("alpha"));
        double beta = Double.parseDouble(cl.getOptionValue("beta"));
        double k1 = Double.parseDouble(cl.getOptionValue("k1"));
        double b = Double.parseDouble(cl.getOptionValue("b"));

        
        Stopper stopper = new Stopper();
        if (cl.hasOption("stoplist")) {
            String stopPath = cl.getOptionValue("stoplist");
            stopper = new Stopper(stopPath);
        }
        
        FileWriter outputWriter = new FileWriter(outputPath);

        GQueries queries = null;
        if (inputPath.endsWith("json"))
        	 queries = new GQueriesJsonImpl();
        else
        	queries = new GQueriesIndriImpl();
        
		queries.read(inputPath);
		

		//outputWriter.write("<parameters>\n");
		GQueries feedbackQueries = new GQueriesJsonImpl();
		
		Iterator<GQuery> queryIterator = queries.iterator();
		while(queryIterator.hasNext()) {
			GQuery query = queryIterator.next();
			
        	
        	Rocchio rocchioFb = new Rocchio(alpha, beta, k1, b);
        	rocchioFb.setStopper(stopper);
        	rocchioFb.expandQuery(index, query, fbDocs, fbTerms);      	
        	  
        	feedbackQueries.addQuery(query);
        	/*
        	String luceneQueryString = ((IndexWrapperLuceneImpl)index).getLuceneQueryString(query);
        	outputWriter.write("<query>\n");
        	outputWriter.write("   <number>" + query.getTitle() + "</number>\n");
        	outputWriter.write("   <text>" + luceneQueryString + "</text>\n");
        	outputWriter.write("</query>\n");
        	*/
	        	        
		}
		
		outputWriter.write(feedbackQueries.toString());
		//outputWriter.write("</parameters>\n");
		outputWriter.close();
    }
    

    
    
    public static Options createOptions()
    {
        Options options = new Options();
        options.addOption("input", true, "Path to Indri formatted topics file");
        options.addOption("output", true, "Path to output file");
        options.addOption("index", true, "Path to index");
        options.addOption("stoplist", true, "Path to stoplist");
        options.addOption("fbDocs", true, "Number of feedback documents");
        options.addOption("fbTerms", true, "Number of feedback terms");
        options.addOption("alpha", true, "Rocchio alpha");
        options.addOption("beta", true, "Rocchio beta");
        options.addOption("k1", true, "BM25 k1");
        options.addOption("b", true, "BM25 b");

        return options;
    }
    
    public static FeatureVector cleanModel(FeatureVector model) {
        FeatureVector cleaned = new FeatureVector(null);
        Iterator<String> it = model.iterator();
        while(it.hasNext()) {
            String term = it.next();
            if(term.length() < 3 || term.matches(".*[0-9].*"))
                continue;
            cleaned.addTerm(term, model.getFeatureWeight(term));
        }
        cleaned.normalize();
        return cleaned;
    }
}
