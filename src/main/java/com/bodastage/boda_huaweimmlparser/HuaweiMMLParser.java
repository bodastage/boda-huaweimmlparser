/*
 * Parses Huawei managed object tree Configuration Data dump from  XML to csv
 * @version 1.0.0
 * @since 1.0.0
 */
package com.bodastage.boda_huaweimmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author info@bodastage.com
 */
public class HuaweiMMLParser {
    /**
     * This holds a map of the Managed Object Instances (MOIs) to the respective
     * csv print writers.
     * 
     * @since 1.0.0
     */
    private Map<String, PrintWriter> moiPrintWriters 
            = new LinkedHashMap<String, PrintWriter>();
    
    /**
     * 
     * Track parameter with children.
     * 
     * @since 1.0.0
     */
    private Map<String, Stack> parameterChildMap = new LinkedHashMap<String, Stack>();
    
    
    /**
     * Tag data.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    private String tagData = "";
    
    /**
     * Output directory.
     *
     * @since 1.0.0
     */
    private String outputDirectory = "/tmp";
    
    /**
     * Parser start time. 
     * 
     * @since 1.0.4
     * @version 1.0.0
     */
    final long startTime = System.currentTimeMillis();
    
    /**
     * Tracks how deep a class tag is in the hierarch.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private int classDepth = 0;
    
    /**
     * The base file name of the file being parsed.
     * 
     * @since 1.0.0
     */
    private String baseFileName = "";
    
    /**
     * The file to be parsed.
     * 
     * @since 1.0.0
     */
    private String dataFile;
    
    /**
     * The nodename.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private String nodeName;
    
    /**
     * The holds the parameters and corresponding values for the moi tag  
     * currently being processed.
     * 
     * @since 1.0.0
     */
    private Map<String,String> moiParameterValueMap 
            = new LinkedHashMap<String, String>();
    
    /**
     * The holds the parameters and corresponding values for the moi tag  
     * currently being processed.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private Map<String,Stack> classNameAttrsMap 
            = new LinkedHashMap<String, Stack>();
    
    
    /**
     * Current className MO attribute.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private String className = null;

    /**
     * Current attr tag's name attribute.
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private String moAttrName = null;
    
    /**
     * MML generation time. 
     * 
     * @since 1.0.0
     */
    private String dateTime = null;
    
    
    /**
     * The bscId from which the MML was generated in the case of 2G.
     * 
     * @since 1.0.0
     */
    private String bscId = null;
    
    /**
     * The MBSC Mode 
     * 
     * @since 1,0.0
     */
    private String MbscMode = null;
    
    /**
     * version 
     * 
     * @since 1,0.0
     */
    private String version = null;
    
    /**
     * IP 
     * 
     * @since 1,0.0
     */
    private String IP = null;
    
    
    
    private LinkedHashMap<String,String> attrValueMap = new LinkedHashMap<String,String>();
    
    /**
     * The parser's entry point.
     * 
     * @param filename 
     */
    public void parse() throws FileNotFoundException, IOException{

            BufferedReader br = new BufferedReader(new FileReader(this.dataFile));
            for(String line; (line = br.readLine()) != null; ) {
                processLine(line);
            }
            
            closeMOPWMap();
    }
       

    public void processLine(String line) throws FileNotFoundException{
        //Handle first line
        if(line.startsWith("//Export start time:")){
            String [] sArray = line.split(":");
            this.dateTime = sArray[1].trim();
            return;
        }
        
        //Extract the version
        if(line.startsWith("//For BAM version:")){
            String [] sArray = line.split(":");
            this.version = sArray[1].trim();
            return;
        }
        
        //Extract the IP
        if(line.startsWith("//OMU IP:")){
            String [] sArray = line.split(":");
            this.IP = sArray[1].trim();
            return;
        }
        
        //Extract the BSCID
        if(line.startsWith("//System BSCID:")){
            String [] sArray = line.split(":");
            this.bscId = sArray[1].trim();
            return;
        }
        
        //Extract the MBSC Mode
        if(line.startsWith("//MBSC Mode:")){
            String [] sArray = line.split(":");
            this.MbscMode = sArray[1].trim();
            return;
        }
        
        //System.out.println(line);
        //Handle lines tarting with SET
        if(line.startsWith("SET ") || line.startsWith("ADD ") ){
            String [] lineArray = line.split(":");
            String moPart = lineArray[0];
            String paramPart = lineArray[1];
            
            //Get the MO
            String [] moPartArray = moPart.split(" ");
            String moName = moPartArray[1].trim();
            
            this.className = moName;
            
            //Get the parameters
            String [] paramPartArray = paramPart.split(", ");
            for(int i = 0, len = paramPartArray.length; i < len; i++){
                String [] sArray = paramPartArray[i].split("=");
                String paramName = sArray[0].trim();
                String paramValue = sArray[1].replaceAll(";$", "");

                attrValueMap.put(paramName, paramValue);
            }

            //Add headers
            if(!moiPrintWriters.containsKey(className)){
                String moiFile = outputDirectory + File.separatorChar + className +  ".csv";
                moiPrintWriters.put(className, new PrintWriter(moiFile));
                
                String pNameStr = "varDateTime,BSCID,BAM_VERSION,OMU_IP,MBSC MODE";
                
                Stack attrStack = new Stack();
                
                Iterator<Map.Entry<String, String>> iter 
                        = attrValueMap.entrySet().iterator();
                
                while(iter.hasNext()){
                    Map.Entry<String, String> me = iter.next();
                    String pName = me.getKey();
                    attrStack.push(pName);
                    
                    //Handle multivalued parameter or parameters with children
                    String tempValue = attrValueMap.get(pName);
                    if(tempValue.matches("([^-]+-[^-]+&).*")){
                        String mvParameter = className + "_" + pName;
                        parameterChildMap.put(mvParameter, null);
                        Stack children = new Stack();
                        
                        
                        String[] valueArray = tempValue.split("&");
                        
                        for(int j = 0; j < valueArray.length; j++){
                            String v = valueArray[j];
                            String[] vArray = v.split("-");
                            String childParameter = vArray[0];
                            pNameStr += "," + pName + "_" + childParameter;
                            children.push(childParameter);
                        }
                        parameterChildMap.put(mvParameter, children);
                        
                        continue;
                    }
                    
                    pNameStr = pNameStr +","+ me.getKey();
                }
                
                //Initialize the MO parameter map hash map
                classNameAttrsMap.put(moName,attrStack);
                moiPrintWriters.get(className).println(pNameStr);
            }
            
            String pValueStr = dateTime +","+bscId+ "," + version + "," + IP + 
                    ","+MbscMode;
            
            //Add the parameter values 
            Stack attrStack = classNameAttrsMap.get(moName);
            Iterator <String> sIter = attrStack.iterator();
            while(sIter.hasNext()){
                String pName = sIter.next();
                String mvParameter = moName + "_" + pName;
                
                String pValue = "";
                if(attrValueMap.containsKey(pName)){

                    if( parameterChildMap.containsKey(mvParameter)){
                        String tempValue = attrValueMap.get(pName);
                        String[] valueArray = tempValue.split("&");
                        
                        for(int j = 0; j < valueArray.length; j++){
                            String v = valueArray[j];
                            String[] vArray = v.split("-");
                            pValueStr += "," + toCSVFormat(vArray[1]);
                        }
                        continue;
                    }
                    
                    pValue = attrValueMap.get(pName);
                }
                
                pValueStr += ","+ toCSVFormat(pValue);
            }

            //System.out.println(pValueStr);
            
            moiPrintWriters.get(className).println(pValueStr);
            attrValueMap.clear();
        }
        
    }
    
           
    /**
     * Handle character events.
     *
     * @param xmlEvent
     * @version 1.0.0
     * @since 1.0.0
     */
    public void characterEvent(XMLEvent xmlEvent) {
        Characters characters = xmlEvent.asCharacters();
        if(!characters.isWhiteSpace()){
            tagData = characters.getData(); 
        }
    }  
    
    /**
     * Get file base name.
     * 
     * @since 1.0.0
     */
     public String getFileBasename(String filename){
        try{
            return new File(filename).getName();
        }catch(Exception e ){
            return filename;
        }
    }
     

    /**
     * Print program's execution time.
     * 
     * @since 1.0.0
     */
    public void printExecutionTime(){
        float runningTime = System.currentTimeMillis() - startTime;
        
        String s = "Parsing completed. ";
        s = s + "Total time:";
        
        //Get hours
        if( runningTime > 1000*60*60 ){
            int hrs = (int) Math.floor(runningTime/(1000*60*60));
            s = s + hrs + " hours ";
            runningTime = runningTime - (hrs*1000*60*60);
        }
        
        //Get minutes
        if(runningTime > 1000*60){
            int mins = (int) Math.floor(runningTime/(1000*60));
            s = s + mins + " minutes ";
            runningTime = runningTime - (mins*1000*60);
        }
        
        //Get seconds
        if(runningTime > 1000){
            int secs = (int) Math.floor(runningTime/(1000));
            s = s + secs + " seconds ";
            runningTime = runningTime - (secs/1000);
        }
        
        //Get milliseconds
        if(runningTime > 0 ){
            int msecs = (int) Math.floor(runningTime/(1000));
            s = s + msecs + " milliseconds ";
            runningTime = runningTime - (msecs/1000);
        }

        
        System.out.println(s);
    }
    
    /**
     * Close file print writers.
     *
     * @since 1.0.0
     * @version 1.0.0
     */
    public void closeMOPWMap() {
        Iterator<Map.Entry<String, PrintWriter>> iter
                = moiPrintWriters.entrySet().iterator();
        while (iter.hasNext()) {
            iter.next().getValue().close();
        }
        moiPrintWriters.clear();
    }
    
    /**
     * Process given string into a format acceptable for CSV format.
     *
     * @since 1.0.0
     * @param s String
     * @return String Formated version of input string
     */
    public String toCSVFormat(String s) {
        String csvValue = s;

        //Check if value contains comma
        if (s.contains(",")) {
            csvValue = "\"" + s + "\"";
        }

        if (s.contains("\"")) {
            csvValue = "\"" + s.replace("\"", "\"\"") + "\"";
        }

        return csvValue;
    }
    
    /**
     * Set the output directory.
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @param directoryName 
     */
    public void setOutputDirectory(String directoryName ){
        this.outputDirectory = directoryName;
    }
     
    /**
     * Set name of file to parser.
     * 
     * @since 1.0.0
     * @version 1.0.0
     * @param directoryName 
     */
    public void setFileName(String filename ){
        this.dataFile = filename;
    }
}
