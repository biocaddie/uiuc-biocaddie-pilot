package edu.gslis.biocaddie.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.lemurproject.kstem.KrovetzStemmer;
import org.lemurproject.kstem.Stemmer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.gslis.utils.Stopper;

/**
 * Krovetz stem Indri queries
 */
public class StemIndriQueries 
{
    
    static Stemmer stemmer = new KrovetzStemmer(); 

    public static void main(String[] args) throws ParseException, IOException {
        
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cl = parser.parse( options, args);
        if (cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( StemIndriQueries.class.getCanonicalName(), options );
            return;
        }
        
        String inputPath = cl.getOptionValue("input");
        String outputPath = cl.getOptionValue("output");
        boolean stem = cl.hasOption("stem");
        Stopper stopper = new Stopper();
        if (cl.hasOption("stoplist")) {
            String stopPath = cl.getOptionValue("stoplist");
            stopper = new Stopper(stopPath);
        }
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);
        process(inputFile, outputFile, stem, stopper);
    }
    
    public static void process(File inputFile, File outputFile, boolean stem, Stopper stopper) 
            throws IOException {
       
       FileWriter outputWriter = new FileWriter(outputFile);
                  
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

       try {

           //Using factory get an instance of document builder
           DocumentBuilder db = dbf.newDocumentBuilder();
           String trecText = FileUtils.readFileToString(inputFile);
           Document dom = db.parse(new InputSource(new StringReader(trecText)));
           
           NodeList documents = dom.getElementsByTagName("query");
           outputWriter.write("<parameters>\n");
           if (documents != null && documents.getLength() > 0) {
               
               for (int i = 0; i < documents.getLength(); i++) {
                   outputWriter.write("<query>\n");
                   Node doc = documents.item(i);
                   NodeList elements = doc.getChildNodes();
                   if (elements != null && elements.getLength() > 0) {
                       for (int j = 0; j < elements.getLength(); j++) {

                           Node node = elements.item(j);
                           if (node != null) {
                               String tag = node.getNodeName();
                               if (!tag.equals("#text")) {
                                   if (node.getFirstChild() != null)
                                   {
                                       String content = node.getFirstChild().getNodeValue();
                                       if (tag.equals("text"))
                                           outputWriter.write("<" + tag + ">" + stem(content, stopper) + "</" + tag + ">\n"); 
                                       else
                                           outputWriter.write("<" + tag + ">" + content+ "</" + tag + ">\n");        
                                   }
                               }
                           }
                       }
                   }
                   outputWriter.write("</query>\n");
               }
           }
           outputWriter.write("</parameters>\n");

       } catch (Exception e) {
           e.printStackTrace();
       }
       outputWriter.close();
           
    }
    
    public static String stem(String text, Stopper stopper) {
       //text = text.replaceAll("[^a-zA-Z0-9 ]", " ");
        text = text.toLowerCase();
        String[] tokens = text.split("\\s+");
        String stemmed = "";
        for (String token: tokens) {
            String s = stemmer.stem(token);
            if (!stopper.isStopWord(token))
                stemmed += " " + s;
        }
        return stemmed.trim();
    }
    
    public static Options createOptions()
    {
        Options options = new Options();
        options.addOption("input", true, "Path to Indri formatted topics file");
        options.addOption("output", true, "Path to output file");
        options.addOption("stoplist", true, "Path to stop list");

        return options;
    }

    
}
