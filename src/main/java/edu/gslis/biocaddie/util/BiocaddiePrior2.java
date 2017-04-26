package edu.gslis.biocaddie.util;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;

import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;

/** 
 *  Rescore a preliminary retrieval given the following document prior:
 * 
 *  p(S|Q) = (c(S) + epsilon)/(|N| + epsilon*|S|)
 *  
 *  Estimate probability of source based on initial retrieval.
 *  
 *  qrels: Path to training qrels
 *  input: Path to preliminary retrieval in TREC format
 *  output: Rescored result set
 *  run: Run name
 *  k: Number of results to use to estimate prior (default 1000)
 */
public class BiocaddiePrior2 extends BiocaddiePrior1
{	

    public static void main(String[] args) throws Exception 
    {
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cl = parser.parse( options, args);
        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( BiocaddiePrior2.class.getCanonicalName(), options );
            return;
        }
        
        String sourcePath = cl.getOptionValue("source");
        int numDocs = Integer.parseInt(cl.getOptionValue("numDocs"));
        String inputPath = cl.getOptionValue("input");
        String outputPath = cl.getOptionValue("output");
        String runName = cl.getOptionValue("run");
        

        try
        {
        	Map<String, String> sourceMap = readSourceMap(sourcePath);
	        rescore(inputPath, outputPath, sourceMap, numDocs, runName);
	        

        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    private static void rescore(String inputPath, String outputPath, Map<String, String> sourceMap, 
    		int numDocs, String runName) 
    	throws IOException
    {
    	
    	Map<String, SearchHits> results = 
    			new TreeMap<String, SearchHits>();
    	
        // Read trec-formatted results output 
        List<String> inputList = IOUtils.readLines(new FileInputStream(inputPath));
        
        for (String input: inputList) {
        	String[] fields = input.split(" ");
        	String query = fields[0];
        	String docno = fields[2];
        	double score = Double.parseDouble(fields[4]);
        	
        	SearchHit hit = new SearchHit();
        	hit.setDocno(docno);
        	hit.setScore(score);
        	
        	SearchHits hits = new SearchHits();
        	if (results.containsKey(query))
        		hits = results.get(query);
        	
        	hits.add(hit);
        	
        	results.put(query, hits);
        }
        
        applyPrior(results, sourceMap, numDocs);
        
        FileWriter outputWriter  = new FileWriter(outputPath);
        
        for (String query: results.keySet()) {
        	SearchHits hits = results.get(query);
        	hits.rank();
        	
        	for (int i=0; i< hits.size(); i++) {
        		SearchHit hit = hits.getHit(i);
            	outputWriter.write(query + " Q0 " + hit.getDocno() 
            			+ " " + (i+1) + " " + hit.getScore() + " " + runName);      		
        	}
        	outputWriter.close();
        }
    }

    
    protected static void applyPrior(Map<String, SearchHits> results, Map<String, String> sourceMap, 
    		int numDocs) {
    	
    	
    	for (String query: results.keySet()) {
    		SearchHits hits = results.get(query);

        	Map<String, Double> repoPrior = new TreeMap<String, Double>();

    		if (hits.size() < numDocs)
    			numDocs = hits.size();
    		
    		// Count the number of times each repo occurs w.r.t. this query.
    		for (int i=0; i<numDocs; i++) {
    			SearchHit hit = hits.getHit(i);
    			String repo = sourceMap.get(hit.getDocno());
    			
        		double count = 0;
        		if (repoPrior.containsKey(repo)) 
        			count += repoPrior.get(repo);
        		
        		repoPrior.put(repo, count);
    		}    		
    		
            // Calculate prior with additive smoothing
            double epsilon = 1;
            for (String repo: repoPrior.keySet()) {
            	double pr = (repoPrior.get(repo) + epsilon) / ( numDocs + epsilon*repoPrior.size());
            	repoPrior.put(repo, pr);
            }
            
            // Rescore the results for this query
    		for (SearchHit hit: hits.hits()) {
    			String repo = sourceMap.get(hit.getDocno());
    			double pr = repoPrior.get(repo);
    			
    			double score = Math.exp(hit.getScore())*pr;
    			hit.setScore(score);
    		}    		
    		hits.rank();

    		results.put(query, hits);
    	}
    }
    public static Options createOptions()
    {
        Options options = new Options();
        options.addOption("source", true, "Path to source training data");
        options.addOption("numDocs", true, "Number of pseudo-feedback documents");
        options.addOption("results", true, "Path to results file");
        options.addOption("output", true, "Output path");
        options.addOption("run", true, "Run name");

        return options;
    }
      
}
