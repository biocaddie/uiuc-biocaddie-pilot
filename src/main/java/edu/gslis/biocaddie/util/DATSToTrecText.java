package edu.gslis.biocaddie.util;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Converts the BioCADDIE DATS format to TREC Text format for indexing with indri
 * @author cwillis
 *
 */
public class DATSToTrecText {
    

    public static void main(String[] args) throws Exception 
    {
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cl = parser.parse( options, args);
        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( DATSToTrecText.class.getCanonicalName(), options );
            return;
        }
        // Input Zip file
        String inputPath = cl.getOptionValue("input");
        // Output TREC Text file
        String outputPath = cl.getOptionValue("output");
        
        boolean allFields = false;
        if (cl.hasOption("all")) {
        	allFields = true;
        }
        
        ZipFile zipFile = new ZipFile(inputPath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        FileWriter outputWriter = new FileWriter(outputPath);

        while(entries.hasMoreElements())
        {
            try
            {
                ZipEntry entry = entries.nextElement();
                InputStream is = zipFile.getInputStream(entry);
	            
	            JsonParser jsonParser = new JsonParser();
	            JsonObject json = (JsonObject) jsonParser.parse(new InputStreamReader(is));
	            // DATS objects appear to have either dataItem or dataSet.
	            String docno = json.get("DOCNO").getAsString();
	            outputWriter.write("<DOC>\n");
	            outputWriter.write("<DOCNO>" + docno + "</DOCNO>\n");
	            String repository = json.get("REPOSITORY").getAsString();
	            if (repository.contains("_"))
	            	repository = repository.substring(0, repository.indexOf("_"));
	            outputWriter.write("<REPOSITORY>" + repository + "</REPOSITORY>\n");
	            JsonObject metadata = json.get("METADATA").getAsJsonObject();

	            if (allFields) {
	            	// Output the full JSON document
	         		outputWriter.write("<TEXT>\n" + json.toString() + "\n</TEXT>\n");	                
	            }
	            else {
		            String title = "";
		            String text = "";
		            
		            // Most repositories seem to have at least the dataItem field
		            // except phenodisc
		            if (metadata.get("dataItem") != null) {
			            JsonElement dataItem = metadata.get("dataItem");
		            	
			            // Some repositories have a dataItem.title field
		            	if (dataItem.getAsJsonObject().get("title") != null) {  
		            		title += " " + dataItem.getAsJsonObject().get("title").getAsString(); 
	
		            	}
		            	
		            	// Some repositories have a dataItem.keyworkds field
		            	JsonElement keywords = dataItem.getAsJsonObject().get("keywords");
		            	if ( keywords != null) {  
		            		for (JsonElement kw : (JsonArray)keywords) {
		            			text += " " + kw.getAsString();
		            		}
		            	}	            	
		            	
			            // Some repositories have a dataItem.description field
		            	if (dataItem.getAsJsonObject().get("description") != null) {  
		            		text += " " +dataItem.getAsJsonObject().get("description").getAsString(); 
		            	}
	
		            	// For other repositories, title, keywords and description are on the
		            	// dataset object
		            	if (dataItem.getAsJsonObject().get("dataTypes") != null) {
			            	JsonArray dataTypes = dataItem.getAsJsonObject().get("dataTypes").getAsJsonArray();
			            	for (JsonElement elem: dataTypes) {
			            		String name = elem.getAsString();
			            		if (name.toLowerCase().equals("dataset")) {
			        	            JsonElement dataSet = json.get("METADATA").getAsJsonObject().get(name);
			    	            	if (dataSet.getAsJsonObject().get("title") != null) {  
			    	            		title += " " +  dataSet.getAsJsonObject().get("title").getAsString(); 
			    	            	}
			    	            	keywords = dataSet.getAsJsonObject().get("keywords");
			    	            	if ( keywords != null) {  
			    	            		for (JsonElement kw : (JsonArray)keywords) {
			    	            			text += " " + kw.getAsString();
			    	            		}
			    	            	}	         	
			    	            	if (dataSet.getAsJsonObject().get("description") != null) {  
			    	            		text += " "  + dataSet.getAsJsonObject().get("description").getAsString(); 
			    	            	}
			            		}       
			            	}
		            	}
		            }
		            else
		            {
		            	//phenodisc does not have dataItem or dataSet objects
		             	if (metadata.get("title") != null) {
		            		title += " " + metadata.get("title").getAsString();
		            	}	            	
		             	if (metadata.get("desc") != null) {
		            		text += " " + metadata.get("desc").getAsString();
		            	}
		            }
		            
	        		outputWriter.write("<TITLE>" + title + "</TITLE>\n");
	        		outputWriter.write("<TEXT>\n" + text + "\n</TEXT>\n");        		
           
	        		System.out.println(docno + "," +  repository + "," + title.split(" ").length + "," + text.split(" ").length);
            	}
        		outputWriter.write("</DOC>\n");	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        }
        zipFile.close();        
        outputWriter.close();
    }
    public static Options createOptions()
    {
        Options options = new Options();
        options.addOption("input", true, "Path to input gz");
        options.addOption("output", true, "Path to output file");
        options.addOption("all", false, "Whether to index all fields");
        return options;
    }
}
