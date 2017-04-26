package edu.gslis.biocaddie.util;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.gslis.docscoring.ScorerDirichlet;
import edu.gslis.docscoring.support.CollectionStats;
import edu.gslis.docscoring.support.IndexBackedCollectionStats;
import edu.gslis.indexes.IndexWrapper;
import edu.gslis.indexes.IndexWrapperIndriImpl;
import edu.gslis.queries.GQueries;
import edu.gslis.queries.GQueriesIndriImpl;
import edu.gslis.queries.GQueriesJsonImpl;
import edu.gslis.queries.GQuery;
import edu.gslis.queries.expansion.FeedbackRelevanceModel;
import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.utils.Stopper;

/**
 * Given a set of topics and an index, generate
 * RM3 feedback queries and write to an output file.
 */
public class GetFeedbackQueries {

    public static void main(String[] args) throws ParseException, IOException {
        
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cl = parser.parse( options, args);
        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( GetFeedbackQueries.class.getCanonicalName(), options );
            return;
        }
        
        String inputPath = cl.getOptionValue("input");
        String outputPath = cl.getOptionValue("output");
        String indexPath = cl.getOptionValue("index");
        
        IndexWrapper index = new IndexWrapperIndriImpl(indexPath);
        index.setTimeFieldName(null);
        
        int maxResults = Integer.parseInt(cl.getOptionValue("maxResults"));
        int fbDocs = Integer.parseInt(cl.getOptionValue("fbDocs"));
        int fbTerms = Integer.parseInt(cl.getOptionValue("fbTerms"));
        double rmLambda = Double.parseDouble(cl.getOptionValue("rmLambda"));
        int mu = Integer.parseInt(cl.getOptionValue("mu"));

        
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
		
		GQueries rm3Queries = new GQueriesJsonImpl();
	      

		ScorerDirichlet docScorer = new ScorerDirichlet();		
		docScorer.setParameter("mu", mu);
		CollectionStats corpusStats = new IndexBackedCollectionStats();
		corpusStats.setStatSource(indexPath);
		docScorer.setCollectionStats(corpusStats);
		
		/*
		<parameters>
		<query>
		    <number>EA1</number>
		    <text>Find data of all types related to TGF-β signaling pathway across all databases</text>
		</query>
		</parameters>
		*/
		outputWriter.write("<parameters>\n");
		Iterator<GQuery> queryIterator = queries.iterator();
		while(queryIterator.hasNext()) {
			GQuery query = queryIterator.next();
			
			SearchHits results = index.runQuery(query,  maxResults);			
	        docScorer.setQuery(query);
	             
	        Iterator<SearchHit> it = results.iterator();
	        SearchHits rescored = new SearchHits();
	        while (it.hasNext()) {
	            SearchHit hit = it.next();
	            double score = docScorer.score(hit);
	            hit.setScore(score);
	            if (score == Double.NaN || score == Double.NEGATIVE_INFINITY) {
	                System.err.println("Problem with score for " + query.getText() + "," + hit.getDocno() + "," + score);
	            } else if (score != Double.NEGATIVE_INFINITY) {
	                rescored.add(hit);
	            }
	        }
			
	        // Feedback model
	        FeedbackRelevanceModel rm3 = new FeedbackRelevanceModel();
	        rm3.setDocCount(fbDocs);
	        rm3.setTermCount(fbTerms);
	        rm3.setIndex(index);
	        rm3.setStopper(stopper);
	        rm3.setRes(rescored);
	        rm3.build();
	        FeatureVector rmVector = rm3.asFeatureVector();
	        rmVector = cleanModel(rmVector);
	        rmVector.clip(fbTerms);
	        rmVector.normalize();
	        FeatureVector feedbackVector =
	        		FeatureVector.interpolate(query.getFeatureVector(), rmVector, rmLambda);

	        
	        outputWriter.write("<query>\n");
	        outputWriter.write("   <number>" + query.getTitle() + "</number>\n");
	        outputWriter.write("   <text>" + toIndri(feedbackVector) + "</text>\n");
	        outputWriter.write("</query>\n");
	        
			/*
			<parameters>
			<query>
			    <number>EA1</number>
			    <text>Find data of all types related to TGF-β signaling pathway across all databases</text>
			</query>
			</parameters>
			*/
	        
		}
		
		outputWriter.write("</parameters>\n");
		outputWriter.close();
    }
    
    public static String toIndri(FeatureVector fv) {
    	StringBuffer sb = new StringBuffer();
    	DecimalFormat format = new DecimalFormat("#.#########");
    	
    	sb.append("#weight(");
    	for (String feature: fv.getFeatures()) {
    		double w = fv.getFeatureWeight(feature);
    		sb.append(" " + format.format(w) + " " + feature);
    	}
    	sb.append(")");

    	return sb.toString();
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
        options.addOption("rmLambda", true, "RM3 lambda");
        options.addOption("mu", true, "mu");
        options.addOption("maxResults", true, "Maximum number of results");

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
