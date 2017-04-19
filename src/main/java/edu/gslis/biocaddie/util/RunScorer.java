package edu.gslis.biocaddie.util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import edu.gslis.docaccumulators.ResultAccumulatorUnconstrained;
import edu.gslis.docscoring.QueryDocScorer;
import edu.gslis.docscoring.support.CollectionStats;
import edu.gslis.indexes.IndexWrapper;
import edu.gslis.indexes.IndexWrapperIndriImpl;
import edu.gslis.output.FormattedOutputTrecEval;
import edu.gslis.queries.GQueries;
import edu.gslis.queries.GQueriesIndriImpl;
import edu.gslis.queries.GQueriesJsonImpl;
import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.searchhits.UnscoredSearchHit;
import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.utils.Stopper;



public class RunScorer {
	
	public static void main(String[] args) throws Exception {
		
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cl = parser.parse( options, args);
        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( StemIndriQueries.class.getCanonicalName(), options );
            return;
        }
        
        String queryPath = cl.getOptionValue("queries");
        String outputPath = cl.getOptionValue("output");
        String indexPath = cl.getOptionValue("index");
        int mu = Integer.parseInt(cl.getOptionValue("mu"));
        
        IndexWrapper index = new IndexWrapperIndriImpl(indexPath);
        index.setTimeFieldName(null);
        
        GQueries queries = null;
        if (queryPath.endsWith("json"))
        	queries = new GQueriesJsonImpl();
        else
        	queries = new GQueriesIndriImpl();
        
        queries.read(queryPath);
        
        Stopper stopper = new Stopper();
        if (cl.hasOption("stoplist")) {
            String stopPath = cl.getOptionValue("stoplist");
            stopper = new Stopper(stopPath);
        }
        
		String runId = "gslis";
		
		
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		
		// figure out how we'll be accessing corpus-level stats
		// default
		String corpusStatsClass = "edu.gslis.docscoring.support.IndexBackedCollectionStats";
		
		CollectionStats corpusStats = (CollectionStats)loader.loadClass(corpusStatsClass).newInstance();
		corpusStats.setStatSource(indexPath);
		
		
		String scorerType = "edu.gslis.docscoring.ScorerDirichlet";
		
		QueryDocScorer docScorer = (QueryDocScorer)loader.loadClass(scorerType).newInstance();
		docScorer.setCollectionStats(corpusStats);	
		docScorer.setParameter("mu", mu);
		

		Writer outputWriter;
		if (outputPath != null)
		    outputWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
		else
		    outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));
		
	    FormattedOutputTrecEval output = FormattedOutputTrecEval.getInstance(runId, outputWriter);

	      
		Iterator<GQuery> queryIterator = queries.iterator();
		while(queryIterator.hasNext()) {
			GQuery query = queryIterator.next();
			
			System.err.println(query.getTitle());
			
			FeatureVector surfaceForm = new FeatureVector(stopper);
			Iterator<String> queryTerms = query.getFeatureVector().iterator();
			while(queryTerms.hasNext()) {
				String term = queryTerms.next();
				surfaceForm.addTerm(term, query.getFeatureVector().getFeatureWeight(term));
			}
			query.setFeatureVector(surfaceForm);
			
			docScorer.setQuery(query);


			ResultAccumulatorUnconstrained accumulator = 
	                new ResultAccumulatorUnconstrained((IndexWrapperIndriImpl)index, 
	                        query.getText());
	        accumulator.accumulate();
	        Map<Integer, UnscoredSearchHit> accumulated = 
	                accumulator.getAccumulatedDocs();
	        
	        SearchHits results = new SearchHits();
	        for (UnscoredSearchHit unscoredHit: accumulated.values()) {

	            SearchHit hit = unscoredHit.toSearchHit();
	            double score = docScorer.score(hit);
	            hit.setScore(score);  
	            results.add(hit);
	        }   
	            
	        results.rank();
            output.write(results, query.getTitle(), 1000);
        }
        output.close();
	}
	
    public static Options createOptions()
    {
        Options options = new Options();
        options.addOption("queries", true, "Path to queries");
        options.addOption("output", true, "Path to output file");
        options.addOption("index", true, "Path to index");
        options.addOption("stoplist", true, "Path to stoplist");
        options.addOption("maxResults", true, "Maximum number of results");
        options.addOption("mu", true, "mu");

        return options;
    }
}
