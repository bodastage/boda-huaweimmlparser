package com.bodastage.boda_huaweimmlparser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import static junit.framework.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HuaweiMMLParser.class);
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

  
    public void testGeneralParsing(){
    
        ClassLoader classLoader = getClass().getClassLoader();
        File inFile = new File(classLoader.getResource("CFGMML1.txt").getFile());
        String inputFile = inFile.getAbsolutePath();
        
        String outputFolder = System.getProperty("java.io.tmpdir");
        
        HuaweiMMLParser parser = new HuaweiMMLParser();
        
        String[] args = { "-i", inputFile, "-o", outputFolder};
        
        parser.main(args);
        
        String expectedResult [] = {
            "FILENAME,DATETIME,BSCID,BAM_VERSION,OMU_IP,MBSC MODE,PARAM1,PARAM2,PARAM3,PARAM4",
            "CFGMML1.txt,2050-22-23 75:68:11,999,V111R050ABCDEFGHIJKLMNOPQRSTUVWXYZ,99.999.9.99,UO,\"VALUE ONE\",\"VALUE2\",NOT_SUPPORTED,\"Some string, with, commas\""
        };
        
        try{
            String csvFile = outputFolder + File.separator + "MONAME.csv";
            
            BufferedReader br = new BufferedReader(new FileReader(csvFile)); 
            String csvResult [] = new String[expectedResult.length];
            
            int i = 0;
            String st; 
            while ((st = br.readLine()) != null) {
                csvResult[i] = st;
                ++i;
            }

            assertTrue(Arrays.equals(expectedResult, csvResult));
            
        }catch(FileNotFoundException ex){
            LOGGER.error(ex.toString());
        }catch(IOException ex){
            LOGGER.error(ex.toString());
        }

    }
}
