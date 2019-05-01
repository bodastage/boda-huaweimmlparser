/*
 * Parses Huawei managed object tree Configuration Data dump from  XML to csv
 * @version 1.0.0
 * @since 1.0.0
 */
package com.bodastage.boda_huaweimmlparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;

/**
 *
 * @author info@bodastage.com
 */
public class HuaweiMMLParser {

    /**
     * Current release version 
     * 
     * Since 1.3.0
     */
    final static String VERSION = "1.3.0";
    
    
    Logger logger = LoggerFactory.getLogger(HuaweiMMLParser.class);
    
    public HuaweiMMLParser(){
        
    }
    
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
     * The holds the parameters and corresponding values for the moi tag  
     * currently being processed for Modification lines
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private Map<String,Stack> actClassNameAttrsMap 
            = new LinkedHashMap<String, Stack>();
    
    /**
     * The holds the parameters and corresponding values for the moi tag  
     * currently being processed for Deactivation lines
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private Map<String,Stack> deaClassNameAttrsMap 
            = new LinkedHashMap<String, Stack>();
    
    /**
     * The holds the parameters and corresponding values for the moi tag  
     * currently being processed for Blocking lines
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private Map<String,Stack> blkClassNameAttrsMap 
            = new LinkedHashMap<String, Stack>();
    
    /**
     * The holds the parameters and corresponding values for the moi tag  
     * currently being processed for Un-initialize lines
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private Map<String,Stack> uinClassNameAttrsMap 
            = new LinkedHashMap<String, Stack>();
    
    /**
     * The holds the parameters and corresponding values for the moi tag  
     * currently being processed for modification lines
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private Map<String,Stack> modClassNameAttrsMap 
            = new LinkedHashMap<String, Stack>();
    
    /**
     * The holds the parameters and corresponding values for the moi tag  
     * currently being processed for Un-block lines
     * 
     * @since 1.0.0
     * @version 1.0.0
     */
    private Map<String,Stack> ublClassNameAttrsMap 
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

    /**
     * Extract managed objects and their parameters
     */
    private Boolean extractParametersOnly = false;
    
    /**
     * Add meta fields to each MO. FILENAME, DATETIME
     */
    private Boolean extractMetaFields = false;
    
    public void setExtractParametersOnly(Boolean bool){
        extractParametersOnly = bool;
    }
    
    public void setExtractMetaFields(Boolean bool){
        extractMetaFields = bool;
    }
    
    public static void main( String[] args )
    {
        
       Options options = new Options();
       CommandLine cmd = null;
       String outputDirectory = null;   
       String inputFile = null;
       String parameterConfigFile = null;
       Boolean onlyExtractParameters = false;
       Boolean showHelpMessage = false;
       Boolean showVersion = false;
       Boolean attachMetaFields = false; //Attach mattachMetaFields FILENAME,DATETIME,NE_TECHNOLOGY,NE_VENDOR,NE_VERSION,NE_TYPE
       
       try{ 
            options.addOption( "p", "extract-parameters", false, "extract only the managed objects and parameters" );
            options.addOption( "v", "version", false, "display version" );
//            options.addOption( "m", "meta-fields", false, "add meta fields to extracted parameters. FILENAME,DATETIME" );
            options.addOption( Option.builder("i")
                    .longOpt( "input-file" )
                    .desc( "input file or directory name")
                    .hasArg()
                    .argName( "INPUT_FILE" ).build());
            options.addOption(Option.builder("o")
                    .longOpt( "output-directory" )
                    .desc( "output directory name")
                    .hasArg()
                    .argName( "OUTPUT_DIRECTORY" ).build());
            options.addOption(Option.builder("c")
                    .longOpt( "parameter-config" )
                    .desc( "parameter configuration file")
                    .hasArg()
                    .argName( "PARAMETER_CONFIG" ).build() );
            options.addOption( "h", "help", false, "show help" );
            
            //Parse command line arguments
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse( options, args);

            if( cmd.hasOption("h")){
                showHelpMessage = true;
            }

            if( cmd.hasOption("v")){
                showVersion = true;
            }
            
            if(cmd.hasOption('o')){
                outputDirectory = cmd.getOptionValue("o"); 
            }
            
            if(cmd.hasOption('i')){
                inputFile = cmd.getOptionValue("i"); 
            }
            
            if(cmd.hasOption('c')){
                parameterConfigFile = cmd.getOptionValue("c"); 
            }
            
            if(cmd.hasOption('p')){
                onlyExtractParameters  = true;
            }
            
            if(cmd.hasOption('m')){
                attachMetaFields  = true;
            }
            
       }catch(IllegalArgumentException e){
           
       } catch (ParseException ex) {
//            java.util.logging.Logger.getLogger(HuaweiCMObjectParser.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       
        try{
            
            if(showVersion == true ){
                System.out.println(VERSION);
                System.out.println("Copyright (c) 2019 Bodastage Solutions(http://www.bodastage.com)");
                System.exit(0);
            }
            
            //show help
            if( showHelpMessage == true || 
                inputFile == null || 
                ( outputDirectory == null && onlyExtractParameters == false) ){
                     HelpFormatter formatter = new HelpFormatter();
                     String header = "Parses Huawei CFGMML files to csv\n\n";
                     String footer = "\n";
                     footer += "Examples: \n";
                     footer += "java -jar boda-huaweimmlparser.jar -i cfgmml_dump.txt -o out_folder\n";
                     footer += "java -jar boda-huaweimmlparser.jar -i input_folder -o out_folder\n";
                     footer += "\nCopyright (c) 2019 Bodastage Solutions(http://www.bodastage.com)";
                     formatter.printHelp( "java -jar boda-huaweimmlparser.jar", header, options, footer );
                     System.exit(0);
            }
        
            //Confirm that the output directory is a directory and has write 
            //privileges
            if(outputDirectory != null ){
                File fOutputDir = new File(outputDirectory);
                if (!fOutputDir.isDirectory()) {
                    System.err.println("ERROR: The specified output directory is not a directory!.");
                    System.exit(1);
                }

                if (!fOutputDir.canWrite()) {
                    System.err.println("ERROR: Cannot write to output directory!");
                    System.exit(1);
                }
            }
            
            

            //Get parser instance
            HuaweiMMLParser cmParser = new HuaweiMMLParser();


            if(onlyExtractParameters == true ){
                cmParser.setExtractParametersOnly(true);
            }
            
            if( attachMetaFields == true ){
                cmParser.setExtractMetaFields(true);
            }
            
            if(  parameterConfigFile != null ){
                File f = new File(parameterConfigFile);
                if(f.isFile()){
                    cmParser.setParameterFile(parameterConfigFile);
                    cmParser.getParametersToExtract(parameterConfigFile);
                    cmParser.parserState = ParserStates.EXTRACTING_VALUES;
                }
            }
            
            
            cmParser.setDataSource(inputFile);
            if(outputDirectory != null ) cmParser.setOutputDirectory(outputDirectory);

            cmParser.setOutputDirectory(outputDirectory);
            cmParser.parse();
            cmParser.printExecutionTime();
            
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }


    }
    
    
    /**
     * The file/directory to be parsed.
     *
     * @since 1.1.0
     */
    private String dataSource;
    
    /**
     * Parser states. Currently there are only 2: extraction and parsing
     * 
     * @since 1.1.0
     * @version 1.0.0
     */
    private int parserState = ParserStates.EXTRACTING_PARAMETERS;
    
    private LinkedHashMap<String,String> attrValueMap = new LinkedHashMap<String,String>();
    
    
    /**
     * File with a list of managed objects and parameters to extract.
     * 
     */
    private String parameterFile = null;
    
    /**
     * The parser's entry point.
     * 
     * @param filename 
     */
    public void parseFile( String inputFilename ) throws FileNotFoundException, IOException{

            BufferedReader br = new BufferedReader(new FileReader(this.dataFile));
            for(String line; (line = br.readLine()) != null; ) {
                processLine(line);
            }
    }
       
    /**
     * Set parameter file 
     * 
     * @param filename 
     */
    public void setParameterFile(String filename){
        parameterFile = filename;
    }
    
    
 /**
     * Extract parameter list from  parameter file
     * 
     * @param filename 
     */
    public  void getParametersToExtract(String filename) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new FileReader(filename));
        for(String line; (line = br.readLine()) != null; ) {
           String [] moAndParameters =  line.split(":");
           String mo = moAndParameters[0];
           String [] parameters = moAndParameters[1].split(",");
           
           Stack parameterStack = new Stack();
           for(int i =0; i < parameters.length; i++){
               parameterStack.push(parameters[i]);
           }
           
           classNameAttrsMap.put(mo, parameterStack);
           
           if(mo.endsWith("_ACT")){ 
               actClassNameAttrsMap.put(mo.replace("_ACT", ""), parameterStack);
               continue;
           }
           
           if(mo.endsWith("_BLK")){ 
               blkClassNameAttrsMap.put(mo.replace("_BLK", ""), parameterStack);
               continue;
           }
           
           if(mo.endsWith("_MOD")){ 
               modClassNameAttrsMap.put(mo.replace("_MOD", ""), parameterStack);
               continue;
           }
           
           if(mo.endsWith("_DEA")){ 
               deaClassNameAttrsMap.put(mo.replace("_DEA", ""), parameterStack);
               continue;
           }
           
           if(mo.endsWith("_UBL")){ 
               ublClassNameAttrsMap.put(mo.replace("_UBL", ""), parameterStack);
               continue;
           }
           
           if(mo.endsWith("_UIN")){ 
               uinClassNameAttrsMap.put(mo.replace("_UIN", ""), parameterStack);
               continue;
           }

        }
        
        //Move to the parameter value extraction stage
        parserState = ParserStates.EXTRACTING_VALUES;
    }
    
    
    /**
     * Parser entry point 
     * 
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     * 
     * @since 1.1.1
     */
    public void parse() throws IOException {
        //Extract parameters
        if (parserState == ParserStates.EXTRACTING_PARAMETERS) {
            processFileOrDirectory();

            parserState = ParserStates.EXTRACTING_VALUES;
        }

        //Extracting values
        if (parserState == ParserStates.EXTRACTING_VALUES) {
            processFileOrDirectory();
            parserState = ParserStates.EXTRACTING_DONE;
        }
        
        closeMOPWMap();
    }
    
    /**
     * Determines if the source data file is a regular file or a directory and 
     * parses it accordingly
     * 
     * @since 1.1.0
     * @version 1.0.0
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public void processFileOrDirectory() throws IOException {
        //logger.info("processFileOrDirectory");
        //this.dataFILe;
        Path file = Paths.get(this.dataSource);
        boolean isRegularExecutableFile = Files.isRegularFile(file)
                & Files.isReadable(file);

        boolean isReadableDirectory = Files.isDirectory(file)
                & Files.isReadable(file);

        if (isRegularExecutableFile) {
            this.setFileName(this.dataSource);
            baseFileName =  getFileBasename(this.dataFile);
            
            if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                System.out.print("Extracting parameters from " + this.baseFileName + "...");
            }else{
                System.out.print("Parsing " + this.baseFileName + "...");
            }
                    
            this.parseFile(this.dataSource);
            
            if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                 System.out.println("Done.");
            }else{
                System.out.println("Done.");
                //System.out.println(this.baseFileName + " successfully parsed.\n");
            }
        }

        if (isReadableDirectory) {

            File directory = new File(this.dataSource);

            //get all the files from a directory
            File[] fList = directory.listFiles();

            for (File f : fList) {
                this.setFileName(f.getAbsolutePath());
                try {
                    baseFileName =  getFileBasename(this.dataFile);
                    if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                        System.out.print("Extracting parameters from " + this.baseFileName + "...");
                    }else{
                        System.out.print("Parsing " + this.baseFileName + "...");
                    }
                    
                    //Parse
                    this.parseFile(f.getAbsolutePath());
                    if( parserState == ParserStates.EXTRACTING_PARAMETERS){
                         System.out.println("Done.");
                    }else{
                        System.out.println("Done.");
                        //System.out.println(this.baseFileName + " successfully parsed.\n");
                    }
                   
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Skipping file: " + this.baseFileName + "\n");
                }
            }
        }

    }

    public void processLine(String line) throws FileNotFoundException{
        //logger.debug("processLine");
        //Handle first line
        if(line.startsWith("//Export start time:")){
            String [] sArray = line.split("time:");
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
            
            //Parameter Extraction Stage
            if(ParserStates.EXTRACTING_PARAMETERS == parserState){
                
                if( parameterFile != null && !classNameAttrsMap.containsKey(className) ){
                    attrValueMap.clear();
                    return;
                }
                
                Stack attrStack = new Stack();
                
                if(classNameAttrsMap.containsKey(moName)){ //this.className
                      attrStack = classNameAttrsMap.get(moName);
                }
               
                //Get the parameters
                String [] paramPartArray = paramPart.split(", ");
                for(int i = 0, len = paramPartArray.length; i < len; i++){
                    String [] sArray = paramPartArray[i].split("=");
                    String paramName = sArray[0].trim();
                    
                    //Skip if the parameter is not in the pFile
                    if( !attrStack.contains(paramName) && parameterFile != null ){
                        continue;
                    }
                    
                    if( !attrStack.contains(paramName) ){
                        attrStack.push(paramName);
                    }
                    
                    //Collect multivalue parameters 
                     String tempValue  = sArray[1].trim();
                     if(tempValue.matches("([^-]+-[^-]+&).*")  ){
                         String mvParameter = className + "_" + paramName;
                        //logger.debug("mvParameter: " + mvParameter);
                        //System.out.println("mvParameter:" + mvParameter);
                        
                         Stack children = new Stack();
                         if(parameterChildMap.containsKey(mvParameter)){ 
                             children = parameterChildMap.get(mvParameter);
                         }else{
                             parameterChildMap.put(mvParameter, null);
                         }

                         String[] valueArray = tempValue.split("&");

                         for(int j = 0; j < valueArray.length; j++){
                             String v = valueArray[j];
                             String[] vArray = v.split("-");
                             String childParameter = vArray[0];
                             if( !children.contains(childParameter)){
                                 children.push(childParameter);
                             }
                             
                         }
                         
                         parameterChildMap.put(mvParameter, children);
                     }
                     //EOF: MV Parameters

                }
            
                classNameAttrsMap.put(moName,attrStack);
                attrValueMap.clear();
                return; //Stop here if we on the parameter extraction stage
            }
            
            
            if(ParserStates.EXTRACTING_VALUES == parserState){
                //Get the parameters
                String [] paramPartArray = paramPart.split(", ");
                for(int i = 0, len = paramPartArray.length; i < len; i++){
                    String [] sArray = paramPartArray[i].split("=");
                    String paramName = sArray[0].trim();
                    String paramValue = sArray[1].replaceAll(";$", "");

                    attrValueMap.put(paramName, paramValue);
                }   
            }
            
            //Continue to value extraction stage
            if(parameterFile != null && !classNameAttrsMap.containsKey(className) ){
                 attrValueMap.clear();
                return;
            }

            //Add headers
            //If there is no parameterFile or if the parameter file exists and the mo is in the classNameAttrsMap
            if( !moiPrintWriters.containsKey(className) ) {
                String moiFile = outputDirectory + File.separatorChar + className +  ".csv";
                moiPrintWriters.put(className, new PrintWriter(moiFile));
                
                String pNameStr = "FILENAME,DATETIME,BSCID,BAM_VERSION,OMU_IP,MBSC MODE";
                
                //This list of parameters are added by default. Ignore to prevent duplicates
                //String ignoreList = "FileName,varDateTime,BSCID,BAM_VERSION,OMU_IP,MBSC MODE";
                
                Stack attrStack =classNameAttrsMap.get(className);
                
                for(int y =0; y < attrStack.size(); y++){
                    String pName = (String)attrStack.get(y);
                    
                    //@TODO: Skip parameter that are added by default
                    if( pName.toLowerCase().equals("filename") || 
                        pName.toLowerCase().equals("datetime") || 
                        pName.toLowerCase().equals("bscid") || 
                        pName.toLowerCase().equals("bam_version") ||
                        pName.toLowerCase().equals("omu_ip") || 
                        pName.toLowerCase().equals("mbsc mode") ) continue;
                    //if(ignoreList.contains(pName)) continue;
                    
                    String mvParameter = moName + "_" + pName;
                    
                    //Handle multivalued parameter (parameters with children)
                    if( parameterChildMap.containsKey(mvParameter)){
                        //Get the child parameters 
                        Stack childParameters = parameterChildMap.get(mvParameter);
                        for(int idx =0; idx < childParameters.size(); idx++){
                            String childParam = (String)childParameters.get(idx);
                            pNameStr = pNameStr +","+ pName + "_" + childParam;
                        }
                        continue;
                    }

                    pNameStr = pNameStr +","+ pName;
                }
                
                //Initialize the MO parameter map hash map
                //classNameAttrsMap.put(moName,attrStack);
                moiPrintWriters.get(className).println(pNameStr);
            }
            
            String pValueStr = baseFileName + "," + dateTime +","+bscId+ "," + version + "," + IP + 
                    ","+MbscMode;
            
            //Add the parameter values 
            Stack attrStack;
            attrStack = classNameAttrsMap.get(moName);
            
            Iterator <String> sIter = attrStack.iterator();
            while(sIter.hasNext()){
                String pName = sIter.next();
                
                //@TODO: Skip parameter that are added by default
                if( pName.toLowerCase().equals("filename") || 
                    pName.toLowerCase().equals("datetime") || 
                    pName.toLowerCase().equals("bscid") || 
                    pName.toLowerCase().equals("bam_version") ||
                    pName.toLowerCase().equals("omu_ip") || 
                    pName.toLowerCase().equals("mbsc mode") ) continue;

                String mvParameter = moName + "_" + pName;
                
                String pValue = "";
                
                if( parameterChildMap.containsKey(mvParameter)){
                    
                    //Fix for bug where parser can't tell if parametr is multivalued or not
                    //ADD CLKSRC:SRCGRD=1, SRCT=LINE1_8KHZ;
                    //ADD CLKSRC:SRCGRD=2, SRCT=BITS1-2MHZ;
                    
                    ///if(!attrValueMap.containsKey(pName)){
                    //    pValueStr += ",";
                    //    continue;
                    //}
                    
                    String tempValue = "";
                    String[] valueArray = {};
                    if(attrValueMap.containsKey(pName)){
                        tempValue = attrValueMap.get(pName);
                         valueArray = tempValue.split("&");
                    }

                    Map<String, String> paramValueMap = new LinkedHashMap<String, String>();

                    //Iterate over the values in parameterChildMap
                    for(int j = 0; j < valueArray.length; j++){
                       String v = valueArray[j];
                        String[] vArray = v.split("-");
                        paramValueMap.put(vArray[0], vArray[1]);
                    }

                    //Get the child parameters 
                    Stack childParameters = parameterChildMap.get(mvParameter);
                    for(int idx =0; idx < childParameters.size(); idx++){
                        String childParam = (String)childParameters.get(idx);

                        if(paramValueMap.containsKey(childParam)){
                            String mvValue = (String)paramValueMap.get(childParam);
                            pValueStr += "," + toCSVFormat(mvValue);
                        }else{
                            pValueStr += ",";
                        }

                    }

                    continue;
                }
                
                if(attrValueMap.containsKey(pName)) { 
                    pValue = attrValueMap.get(pName);
                }
                pValueStr += ","+ toCSVFormat(pValue);
            }
            
            moiPrintWriters.get(className).println(pValueStr);
            
            attrValueMap.clear();
        }//eof:SET
        
        
        //ACT
        if(line.startsWith("ACT ") ){
            extactParameterAndValues(line, "ACT");
            return;
        }
        //ACT
        if(line.startsWith("MOD ") ){
            extactParameterAndValues(line, "MOD");
            return;
        }
        //ACT
        if(line.startsWith("DEA ") ){
            extactParameterAndValues(line, "DEA");
            return;
        }
        //ACT
        if(line.startsWith("BLK ") ){
            extactParameterAndValues(line, "BLK");
            return;
        }
        //ACT
        if(line.startsWith("UBL ") ){
            extactParameterAndValues(line, "UBL");
            return;
        }
        //ACT
        if(line.startsWith("UIN ") ){
            extactParameterAndValues(line, "UIN");
            return;
        }
        
        //
    }
    
    /**
     * 
     * @param line
     * @param keyWord ACT,BLK,UBK,DEA,UIN
     */
    private void extactParameterAndValues(String line, String keyWord) throws FileNotFoundException{
            if( !keyWord.equals("ACT") && !keyWord.equals("BLK") &&
                    !keyWord.equals("MOD") && !keyWord.equals("DEA") &&
                    !keyWord.equals("UBL") &&  !keyWord.equals("UIN")
                    ){
                return;
            }
            String [] lineArray = line.split(":");
            String moPart = lineArray[0];
            String paramPart = lineArray[1];


            //Get the MO
            String [] moPartArray = moPart.split(" ");
            String moName = moPartArray[1].trim();
            
            this.className = moName;
            
            String printWriterClassName = className + "_" + keyWord ;
            
            //Parameter Extraction Stage
            if(ParserStates.EXTRACTING_PARAMETERS == parserState){
                
                if( parameterFile != null && !classNameAttrsMap.containsKey(printWriterClassName) ){
                    attrValueMap.clear();
                    return;
                }
                
                Stack attrStack = new Stack();
                
                //Get the parameters
                String [] paramPartArray = paramPart.split(", ");
                for(int i = 0, len = paramPartArray.length; i < len; i++){
                    String [] sArray = paramPartArray[i].split("=");
                    String paramName = sArray[0].trim();
                    
                    //Skip if the parameter is not in the pFile
                    if( !attrStack.contains(paramName) && parameterFile != null ){
                        continue;
                    }
                    
                    if( !attrStack.contains(paramName)){
                        attrStack.push(paramName);
                    }
                    
                    //Collect multivalue parameters 
                    String tempValue  = sArray[1].trim();
                    if(tempValue.matches("([^-]+-[^-]+&).*")  ){
                        String mvParameter = className + "_" + paramName;
                        
                        
                        Stack children = new Stack();
                        if(parameterChildMap.containsKey(mvParameter)){ 
                            children = parameterChildMap.get(mvParameter);
                        }else{
                            parameterChildMap.put(mvParameter, null);
                        }
                        
                        
                        String[] valueArray = tempValue.split("&");
                        
                        for(int j = 0; j < valueArray.length; j++){
                            String v = valueArray[j];
                            String[] vArray = v.split("-");
                            String childParameter = vArray[0];
                            children.push(childParameter);
                        }
                        parameterChildMap.put(mvParameter, children);
                    }
                }
            
                if(parameterFile == null ){
                    if(keyWord.equals("ACT")){
                        actClassNameAttrsMap.put(moName,attrStack);
                    }
                    if(keyWord.equals("BLK")){
                        blkClassNameAttrsMap.put(moName,attrStack);
                    }
                    if(keyWord.equals("MOD")){
                        modClassNameAttrsMap.put(moName,attrStack);
                    }
                    if(keyWord.equals("DEA")){
                        deaClassNameAttrsMap.put(moName,attrStack);
                    }
                    if(keyWord.equals("UBL")){
                        ublClassNameAttrsMap.put(moName,attrStack);
                    }
                    if(keyWord.equals("UIN")){
                        uinClassNameAttrsMap.put(moName,attrStack);
                    }
                }
                

                
                return; //Stop here if we on the parameter extraction stage
            }
            
            if(ParserStates.EXTRACTING_VALUES == parserState){
                //Get the parameters
                String [] paramPartArray = paramPart.split(", ");
                for(int i = 0, len = paramPartArray.length; i < len; i++){
                    String [] sArray = paramPartArray[i].split("=");
                    String paramName = sArray[0].trim();
                    String paramValue = sArray[1].replaceAll(";$", "");

                    attrValueMap.put(paramName, paramValue);
                }
                
            }
            
            //Continue to value extraction stage
            //printWriterClassName=className_<MOD}|BLK|...>
            if(parameterFile != null && !classNameAttrsMap.containsKey(printWriterClassName) ){
                 attrValueMap.clear();
                return;
            }
            
            //Add headers
            if(!moiPrintWriters.containsKey(printWriterClassName)){
                String moiFile = outputDirectory + File.separatorChar + printWriterClassName +  ".csv";
                moiPrintWriters.put(printWriterClassName, new PrintWriter(moiFile));
                
                String pNameStr = "FILENAME,DATETIME,BSCID,BAM_VERSION,OMU_IP,MBSC MODE";
                
                Stack attrStack = new Stack();
                
                if( parameterFile == null ){
                    Iterator<Map.Entry<String, String>> iter 
                            = attrValueMap.entrySet().iterator();

                    while(iter.hasNext()){
                        Map.Entry<String, String> me = iter.next();
                        String pName = me.getKey();
                        attrStack.push(pName);

                        //Handle multivalued parameter or parameters with children
                        String tempValue = attrValueMap.get(pName);
                        if(tempValue.matches("([^-]+-[^-]+&).*") ){
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
                
                }else{
                    
                    //Cases where the parameter list is provided in the parameter file
                    attrStack =classNameAttrsMap.get(printWriterClassName);
                    
                    for(int y =0; y < attrStack.size(); y++){
                        String pName = (String)attrStack.get(y);

                        //@TODO: Skip parameter that are added by default
                        if( pName.toLowerCase().equals("filename") || 
                            pName.toLowerCase().equals("datetime") || 
                            pName.toLowerCase().equals("bscid") || 
                            pName.toLowerCase().equals("bam_version") ||
                            pName.toLowerCase().equals("omu_ip") || 
                            pName.toLowerCase().equals("mbsc mode") ) continue;
                        //if(ignoreList.contains(pName)) continue;

                        String mvParameter = moName + "_" + pName;

                        //Handle multivalued parameter (parameters with children)
                        if( parameterChildMap.containsKey(mvParameter)){
                            //Get the child parameters 
                            Stack childParameters = parameterChildMap.get(mvParameter);
                            for(int idx =0; idx < childParameters.size(); idx++){
                                String childParam = (String)childParameters.get(idx);
                                pNameStr = pNameStr +","+ pName + "_" + childParam;
                            }
                            continue;
                        }

                        pNameStr = pNameStr +","+ pName;
                    }
                }
                

                
                if( parameterFile == null ){
                    //Initialize the MO parameter map hash map
                   if(keyWord.equals("ACT")){
                        actClassNameAttrsMap.put(moName,attrStack);
                    }
                    if(keyWord.equals("BLK")){
                        blkClassNameAttrsMap.put(moName,attrStack);
                    }
                    if(keyWord.equals("MOD")){
                        modClassNameAttrsMap.put(moName,attrStack);
                    }
                    if(keyWord.equals("DEA")){
                        deaClassNameAttrsMap.put(moName,attrStack);
                    }
                    if(keyWord.equals("UBL")){
                        ublClassNameAttrsMap.put(moName,attrStack);
                    }
                    if(keyWord.equals("UIN")){
                        uinClassNameAttrsMap.put(moName,attrStack);
                    }
                }

                
                moiPrintWriters.get(printWriterClassName).println(pNameStr);
            }
            
            String pValueStr = baseFileName + "," + dateTime +","+bscId+ "," + version + "," + IP + 
                    ","+MbscMode;
            
            //Add the parameter values 
            Stack attrStack = new Stack();

               if(keyWord.equals("ACT")){
                    attrStack = actClassNameAttrsMap.get(moName);
                }
                if(keyWord.equals("BLK")){
                    attrStack = blkClassNameAttrsMap.get(moName);
                }
                if(keyWord.equals("MOD")){
                    attrStack = modClassNameAttrsMap.get(moName);
                }
                if(keyWord.equals("DEA")){
                    attrStack = deaClassNameAttrsMap.get(moName);
                }
                if(keyWord.equals("UBL")){
                    attrStack = ublClassNameAttrsMap.get(moName);
                }
                if(keyWord.equals("UIN")){
                    attrStack = uinClassNameAttrsMap.get(moName);
                }
                
               
            Iterator <String> sIter = attrStack.iterator();
            while(sIter.hasNext()){
                String pName = sIter.next();
                
                if( pName.toLowerCase().equals("filename") || 
                    pName.toLowerCase().equals("datetime") || 
                    pName.toLowerCase().equals("bscid") || 
                    pName.toLowerCase().equals("bam_version") ||
                    pName.toLowerCase().equals("omu_ip") || 
                    pName.toLowerCase().equals("mbsc mode") ) continue;

                String mvParameter = moName + "_" + pName;
                
                String pValue = "";
                if(attrValueMap.containsKey(pName)){
                    
                    //Check whether the multi value parameter was detected
                    if( parameterChildMap.containsKey(mvParameter)){
                        String tempValue = attrValueMap.get(pName);
                        String[] valueArray = tempValue.split("&");
                        
                        Map<String, String> paramValueMap = new LinkedHashMap<String, String>();
                        
                        //Iterate over the values in parameterChildMap
                        for(int j = 0; j < valueArray.length; j++){
                           String v = valueArray[j];
                            String[] vArray = v.split("-");
                            paramValueMap.put(vArray[0], toCSVFormat(vArray[1]));
                        }
                        
                        //Get the child parameters 
                        Stack childParameters = parameterChildMap.get(mvParameter);
                        for(int idx =0; idx < childParameters.size(); idx++){
                            String childParam = (String)childParameters.get(idx);
                            
                            if(paramValueMap.containsKey(childParam)){
                                String mvValue = (String)paramValueMap.get(childParam);
                                pValueStr += "," + toCSVFormat(mvValue);
                            }else{
                                pValueStr += ",";
                            }
                            
                        }

                        /*
                        for(int j = 0; j < valueArray.length; j++){
                            String v = valueArray[j];
                            String[] vArray = v.split("-");
                            pValueStr += "," + toCSVFormat(vArray[1]);
                        }*/
                        continue;
                    }
                    
                    pValue = attrValueMap.get(pName);
                }
                
                pValueStr += ","+ toCSVFormat(pValue);
            }
            
            moiPrintWriters.get(printWriterClassName).println(pValueStr);
            
            
            attrValueMap.clear();
            className = null;
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

        //Strip start and end quotes
        s = s.replaceAll("^\"|\"$", "");
        
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
    
    
    /**
     * Set name of file to parser.
     * 
     * @since 1.0.1
     * @version 1.0.0
     * @param dataSource 
     */
    public void setDataSource(String dataSource ){
        this.dataSource = dataSource;
    }
}
