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

import edu.gslis.eval.Qrels;
import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;

/** 
 * Re-scores a preliminary retrieval given the following document prior:
 * 
 *  p(D) = p(R=1 | D) = c(R=1,S)/c(R=1)
 *  
 *  source: Path to file containing docno,source
 *  qrels: Path to training qrels
 *  input: Path to preliminary retrieval in TREC format
 *  output: Rescored result set
 *  run: Run name
 */
public class BiocaddiePrior1  
{	

    public static void main(String[] args) throws Exception 
    {
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cl = parser.parse( options, args);
        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( BiocaddiePrior1.class.getCanonicalName(), options );
            return;
        }
        String sourcePath = cl.getOptionValue("source");
        String qrelsPath = cl.getOptionValue("qrels");
        String inputPath = cl.getOptionValue("input");
        String outputPath = cl.getOptionValue("output");
        String runName = cl.getOptionValue("run");
        

        try
        {
        	
        	Map<String, String> sourceMap = readSourceMap(sourcePath);
        	
	        Map<String, Double> sourcePrior = calculateSourcePrior(qrelsPath, sourcePath, sourceMap);
	        
	        rescore(inputPath, outputPath, sourcePrior, sourceMap, runName);
	        

        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    protected static void rescore(String inputPath, String outputPath, Map<String, Double> sourcePrior,
    		Map<String, String> sourceMap, String runName) 
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
        	        	
        	String repo = sourceMap.get(docno);
        	
        	double prior  = sourcePrior.get(repo);
        	score = Math.exp(score)*prior;
        	
        	SearchHit hit = new SearchHit();
        	hit.setDocno(docno);
        	hit.setScore(score);
        	
        	SearchHits hits = new SearchHits();
        	if (results.containsKey(query))
        		hits = results.get(query);
        	
        	hits.add(hit);
        	
        	results.put(query, hits);
        }

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
    protected static Map<String, Double> calculateSourcePrior(String qrelsPath, String sourcePath, 
    		Map<String, String> sourceMap) throws IOException 
    {
        Map<String, Double> repoPrior = new TreeMap<String, Double>();
        
        Qrels qrels = new Qrels(qrelsPath, false, 1);
        
        // Count the number of relevant documents in the training data
        // for each repo.
        int numRel = 0;
        for (String query: qrels.getOrderedQueryList()) {
        	for (String relDoc: qrels.getRelDocs(query)) {
        		String repo = sourceMap.get(relDoc);
        		
        		double count = 0;
        		if (repoPrior.containsKey(repo)) 
        			count += repoPrior.get(repo);
        		
        		repoPrior.put(repo, count);
        		numRel++;
        	}
        }
        
        // Calculate prior with additive smoothing
        double epsilon = 1;
        for (String repo: repoPrior.keySet()) {
        	double pr = (repoPrior.get(repo) + epsilon) / (numRel + epsilon*repoPrior.size());
        	repoPrior.put(repo, pr);
        }
        
        return repoPrior;
    }
    
    protected static Map<String, String> readSourceMap(String sourcePath) throws IOException {
    	Map<String, String> sourceMap = new TreeMap<String, String>();
    	List<String> sourceList = IOUtils.readLines(new FileInputStream(sourcePath));
    	for (String source: sourceList) {
    		String[] fields = source.split(",");
    		sourceMap.put(fields[0], fields[1]);
    	}    	
    	return sourceMap;
    }
    
    public static Options createOptions()
    {
        Options options = new Options();
        options.addOption("source", true, "Path to source training data");
        options.addOption("qrels", true, "Path to training qrels");
        options.addOption("results", true, "Path to results file");
        options.addOption("output", true, "Output path");
        options.addOption("run", true, "Run name");

        return options;
    }
      
}
