/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.cli;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/**
 * Helper methods for dealing with JSON files
 *
 */ 
public class JSONFileHelper {

	public static HashMap<String, String> detectFile(String input_file) {
		HashMap<String, String> map = new HashMap<String, String>();
        Reader reader = null;
        URL url = null;
        File working_file = null;
        String alternate_input_file = null;

        if (input_file == null)
        	return null;

        try {

            // Need to figure out if this is a local File or a URL.
           url = new URL(input_file);
 
            // If here, then valid URL.
        } catch (MalformedURLException e) {
            // If here, then it is not a valid URL.
            // Test for file

            try {

                reader = new FileReader(new File(input_file));
                // If here then valid file

                map.put("Type", "File");
                map.put("Name", input_file);
                return map;

            } catch (Exception ex1) {
            	// Ok, if we get here, then it is not a file or simple URL.
            	// Check to see if it is a URL without the protocol.
	            try {

	            	alternate_input_file = "http://" + input_file;
	            	url = new URL(alternate_input_file);

	            } catch (MalformedURLException ex) {
	                // If here, then not a valid file or URL.

		    		System.out.println("################################################################");
		    		System.out.println("### We could not read a file.  This is either a JSON file or ###");
		    		System.out.println("### it could be an image referenced inside of a JSON file.   ###");
			   		System.out.println("### We are not completing the operation - abandoning.        ###");
		    		System.out.println("### File: " + input_file);
		    		System.out.println("################################################################");
		    		System.exit(0);
	            }
            }
        }

        // If for some reason we get here, then we have a valid URL format.
        // Time to test the URL to see if the file is there and present.
		boolean is_exists = false;

	    try {
	    	HttpURLConnection con = null;
  			HttpURLConnection.setFollowRedirects(false);

	        map.put("Type", "URL");

  			if (alternate_input_file == null) {
			    con = (HttpURLConnection) new URL(input_file).openConnection();
			    map.put("Name", input_file);
  			} else {
			    con = (HttpURLConnection) new URL(alternate_input_file).openConnection();  				
			    map.put("Name", alternate_input_file);
  			}

  			is_exists = (con.getResponseCode() == HttpURLConnection.HTTP_OK);

			// If we get here, then we have a valid URL, and it was readable.
			if (is_exists)
				return map;

			// If we get here, then we have a vlaid URL, but it was not readable.

			System.out.println("###########################################################################");
			System.out.println("### You provided a file as a valid URL, but we got a connection error.  ###");
	   		System.out.println("### We are not completing the operation - abandoning.                   ###");
			System.out.println("### File: " + input_file);
			System.out.println("### Response Code: " + con.getResponseCode());
			System.out.println("###########################################################################");
    		System.exit(0);

		} catch (Exception e) {

			// If here, we have a valid URL, but it is not readable for some unknown reason.
    		System.out.println("################################################################");
    		System.out.println("### We could not read a file that you passed as a URL.  This ###");
    		System.out.println("### is either a JSON file or it could be an image file       ###");
    		System.out.println("### referenced in a factory JSON file.                       ###");
	   		System.out.println("### We are not completing the operation - abandoning.        ###");
			System.out.println("### File: " + input_file);
    		System.out.println("################################################################");
    		System.exit(0);
		} 

		return null;
	}
	

    public static byte[] readImageFile(String input_file) {

    	HashMap<String, String> map = detectFile(input_file);

    	if (map == null) {
    		return null;
    	}

    	if (map.get("Type").equals("File")) {
			try {
			    Path path = Paths.get((String)map.get("Name"));
                return Files.readAllBytes(path);
			} catch (IOException ex) {
	    		System.out.println("###############################################################");
	    		System.out.println("### Error reading an image file referenced in your JSON.    ###");
	    		System.out.println("### We are not completing the operation - abandoning.       ###");
	    		System.out.println("### File: " + map.get("Name"));
	    		System.out.println("###############################################################");
	    		System.exit(0);
			}

    	} else {

			try {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				int nRead;
				byte[] data = new byte[16384];
				InputStream is = (new URL(map.get("Name"))).openStream();

				while ((nRead = is.read(data, 0, data.length)) != -1) {
				  buffer.write(data, 0, nRead);
				}

				buffer.flush();
				return buffer.toByteArray();

			} catch (Exception e) {
	    		System.out.println("################################################################");
	    		System.out.println("### We could connect to the image referenced via URL, but we ###");
	    		System.out.println("### could not successfully read its contents.                ###");
	    		System.out.println("### We are not completing the operation - abandoning.        ###");
	    		System.out.println("### File: " + map.get("Name"));
	    		System.out.println("################################################################");
	    		System.exit(0);
			}
    	}

		// Should never get here.
		return null;
	}


    public static JSONObject loadJSONFromFileAndParams(JSONFileParameterDelegate json_delegate) {

    	JSONObject aggregated_json = new JSONObject();

    	/*
    	 * STEP 1: Search for the file specified by --in
    	 *         If valid file, then Parse it as a JSONObject.
    	 *         Add the resulting parse to the factory_params Map.
    	 */
    	if (json_delegate.getInputFile() != null) {

			aggregated_json = JSONFileHelper.readJSONFileAndOverride(json_delegate.getInputFile(), 
																	 aggregated_json);
		
		}


		/*
		 *  STEP 2: Add each --param name value to the Map.
		 */
		Iterator<JSONPair> iterator = json_delegate.getParams().iterator();
		while (iterator.hasNext()) {
			aggregated_json.put(iterator.next().getPair(), iterator.next().getPair());
		}	    	

		return aggregated_json;
	}



    public static JSONObject readJSONFileAndOverride(String input_file, JSONObject append_to) {

    	HashMap<String, String> map = detectFile(input_file);
		Reader reader = null;
		File working_file = null;
		boolean is_readable = false;

    	if (map == null) {
    		return null;
    	}

    	if (map.get("Type").equals("File")) {
			try {
				working_file = new File(map.get("Name"));
				reader = new FileReader(working_file);
	    		is_readable = working_file.exists() & working_file.canRead();
			} catch (Exception ex) {
	    		System.out.println("#########################################################");
	    		System.out.println("### Error reading a JSON file you referenced.         ###");
	    		System.out.println("### We are not completing the operation - abandoning. ###");
	    		System.out.println("### File: " + map.get("Name"));
	    		System.out.println("#########################################################");
	    		System.exit(0);
			}

    	} else {

			try {
				reader = new BufferedReader(new InputStreamReader((new URL(map.get("Name"))).openStream()));
				is_readable = true;
			} catch (Exception e) {
				System.out.println("#########################################################");
	    		System.out.println("### Error reading a JSON file referenced as a URL.    ###");
	    		System.out.println("### We are not completing the operation - abandoning. ###");
	    		System.out.println("### File: " + map.get("Name"));
	    		System.out.println("#########################################################");
	    		System.exit(0);
			}
    	}

    	// If here, we have a valid reader.  If the file is readable.
    	// We have a valid input file.
    	// Load it, read in each parameter one at a time into map.
    	if (!is_readable) {
			System.out.println("#########################################################");
    		System.out.println("### We ran into problems reading your JSON file.      ###");
    		System.out.println("### We are not completing the operation - abandoning. ###");
    		System.out.println("### File: " + map.get("Name"));
    		System.out.println("#########################################################");
    		System.exit(0);
    	}

		JSONParser parser = new JSONParser();
		JSONObject jsonObject = new JSONObject();

		try {
			JSONObject input_factory_params = (JSONObject) parser.parse(reader);

	    	Iterator input_iterator = input_factory_params.entrySet().iterator();
			while (input_iterator.hasNext()) {
				Map.Entry pairs = (Map.Entry)input_iterator.next();
				append_to.put(pairs.getKey(), pairs.getValue());
			}
		} catch (Exception e) {
			System.out.println("###########################################################");
    		System.out.println("### We ran into problems reading / parsing a JSON file. ###");
    		System.out.println("### The likely issue is a JSON format syntax issue.     ###");
    		System.out.println("### We are not completing the operation - abandoning.   ###");
    		System.out.println("### File: " + map.get("Name"));
    		System.out.println("###########################################################");
		    System.out.println(e);
    		System.exit(0);    		
		} finally {
            if (reader != null) {
        	   	try {
                	reader.close();
            	} catch (IOException e) {}
        	}
	    }

		return append_to;

	}	

		

    public static void writeJSONFile(File write_file, JSONObject objects_to_write) {

    	if (write_file != null) {
    		
    		FileWriter writer = null;
    		boolean is_writeable = false;
    		
    		try {
    			boolean does_exist = write_file.exists();

           		if (!does_exist) {
                	// Cannot put these two statements in an &.
                	// The mkdirs() function will return false if directory already exists.
                	does_exist = write_file.createNewFile(); 
            	}
            
            	if (does_exist) {
                	is_writeable = write_file.canWrite();
            	}            
            
            	if (is_writeable) {

		    		writer = new FileWriter(write_file);

		    		objects_to_write.writeJSONString(writer);

		    		writer.flush();
	    		}

	    	} catch (java.io.IOException e) {
			        System.out.println("##########################################################################");
			        System.out.println("### The file write operation failed.                                   ###");
			        System.out.println("### Issues could be non-existant directory or poorly formed file name. ###");
			        System.out.println("##########################################################################");
	    	} finally {
	            if (writer != null) {
            	   	try {
                    	writer.close();
                	} catch (IOException e) {}
            	}
			}
		} 
    }	    
}


