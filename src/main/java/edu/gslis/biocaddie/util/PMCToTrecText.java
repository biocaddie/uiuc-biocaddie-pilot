package edu.gslis.biocaddie.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;


public class PMCToTrecText {
    

    public static void main(String[] args) throws Exception 
    {
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cl = parser.parse( options, args);
        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( PMCToTrecText.class.getCanonicalName(), options );
            return;
        }
        String inputPath = cl.getOptionValue("input");
        String outputPath = cl.getOptionValue("output");
        
        FileWriter outputWriter = new FileWriter(outputPath);

        TarArchiveInputStream tarFile = 
        		new TarArchiveInputStream(new GzipCompressorInputStream(
        				new FileInputStream(inputPath)));
        
        TarArchiveEntry currentEntry = tarFile.getNextTarEntry();
        BufferedReader br = null;
        while (currentEntry != null) {
        	
        	if (currentEntry.isFile()) {
	            br = new BufferedReader(new InputStreamReader(tarFile)); // Read directly from tarInput
	            //System.out.println("For File = " + currentEntry.getName());
	            String line;
	            String text ="";
	            while ((line = br.readLine()) != null) {
	                text+= line + "\n";
	            }
	            
	            String docno = UUID.randomUUID().toString();
	            outputWriter.write("<DOC>\n");
	            outputWriter.write("<DOCNO>" + docno + "</DOCNO>\n");
	    		outputWriter.write("<TEXT>\n" + text + "\n</TEXT>\n");        		
	    		outputWriter.write("</DOC>\n");	 
        	}
    		
        	currentEntry = tarFile.getNextTarEntry();
        }
        if (br!=null) {
            br.close();
        }
       
        tarFile.close();
        outputWriter.close();
    }
    public static Options createOptions()
    {
        Options options = new Options();
        options.addOption("input", true, "Path to input gz");
        options.addOption("output", true, "Path to output file");
        //options.addOption("date", true, "Default date");
        options.addOption("startTime", true, "Collection start date");
        options.addOption("endTime", true, "Collection end date");
        return options;
    }
}
