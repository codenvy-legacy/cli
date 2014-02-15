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

    public static byte[] readImageFile(String input_file) {

    	if (input_file != null) {

			boolean is_url = false;
    		URL url = null;
    		boolean is_readable = false;

    		try {
	    		// Need to figure out if this is a local File or a URL.
	    		// Start by checking to see if URL
	    		url = new URL(input_file);

			} catch (MalformedURLException e) {

				// 
				// If you get here, then not a valid URL, must be a file.
				// Read the file and return
    			try {
   				    Path path = Paths.get((String)input_file);
                    return Files.readAllBytes(path);

				} catch (IOException ex) {
					System.out.println(ex);
		    		System.out.println("###################################################");
		    		System.out.println("### We could not read the image file specified. ###");
		    		System.out.println("###################################################");
		    		return null;
				}
			}
		
			// 
			// If here, then not a file, read from URL
			boolean is_exists = false;

		    try {
      			HttpURLConnection.setFollowRedirects(false);
			    HttpURLConnection con = (HttpURLConnection) new URL(input_file).openConnection();
      			con.setRequestMethod("HEAD");
      			is_exists = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
    		} catch (Exception e) {} 

    		// 
    		// NO FILE
    		// URL FILE - Open stream and pull file contents in
    		if (is_exists) {
    			try {
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();

					int nRead;
					byte[] data = new byte[16384];
					InputStream is = url.openStream();

					while ((nRead = is.read(data, 0, data.length)) != -1) {
					  buffer.write(data, 0, nRead);
					}

					buffer.flush();
					return buffer.toByteArray();

				} catch (Exception e) {
		    		System.out.println("##############################################");
		    		System.out.println("### The image file specified is not valid. ###");
		    		System.out.println("##############################################");
    			}

    		} else {
    			System.out.println("#############################################################");
    			System.out.println("### Could not connect to URL image file.  Does not exist. ###");
    			System.out.println("#############################################################");
    		}

			return null;
		}

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

    	if (input_file != null) {

    		File working_file = null;
			Reader reader = null;
			boolean is_url = false;
    		URL url = null;
    		boolean is_readable = false;

    		try {
	    		// Need to figure out if this is a local File or a URL.
	    		url = new URL(input_file);
	    		is_url = true;

			} catch (MalformedURLException e) {
				// If here, then it is probably a local file.
				working_file = new File(input_file);

				try {
					reader = new FileReader(working_file);
				} catch (Exception ex) {
		    		System.out.println("###########################################");
		    		System.out.println("### The in file specified is not valid. ###");
		    		System.out.println("###########################################");
		    		System.exit(0);

				}
			} 

			if (is_url) {
				boolean is_exists = false;

			    try {
	      			HttpURLConnection.setFollowRedirects(false);
				    HttpURLConnection con = (HttpURLConnection) new URL(input_file).openConnection();
	      			con.setRequestMethod("HEAD");
	      			is_exists = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
	    		} catch (Exception e) {} 

	    		if (is_exists) {
	    			try {
						reader = new BufferedReader(new InputStreamReader(url.openStream()));
						is_readable = true;
	    			} catch (Exception e) {
			    		System.out.println("###########################################");
			    		System.out.println("### The in file specified is not valid. ###");
			    		System.out.println("###########################################");
			    		System.exit(0);
	    			}

	    		} else {
	    			System.out.println("#############################################################");
	    			System.out.println("### Could not connect to URL input file.  Does not exist. ###");
	    			System.out.println("#############################################################");
	    			System.exit(0);
	    		}
			} else {
	    		is_readable = working_file.exists() & working_file.canRead();
	    	}


	    	// We have a valid input file.
	    	// Load it, read in each parameter one at a time into map.
	    	if (is_readable) {
				JSONParser parser = new JSONParser();
    			JSONObject jsonObject = new JSONObject();

    			try {
        			JSONObject input_factory_params = (JSONObject) parser.parse(reader);

			    	Iterator input_iterator = input_factory_params.entrySet().iterator();
	    			while (input_iterator.hasNext()) {
	    				Map.Entry pairs = (Map.Entry)input_iterator.next();
	    				append_to.put(pairs.getKey(), pairs.getValue());
	    			}
        		} catch (IOException e) {
				    e.printStackTrace();
				} catch (ParseException ex) {
			        System.out.println("################################################");
			        System.out.println("#### You have provided an invalid JSON file ####");
			        System.out.println("################################################");
			    } catch (RuntimeException e) {
			        e.printStackTrace();
			    } catch (Exception e) {
			        e.printStackTrace();
				} finally {
    	            if (reader != null) {
                	   	try {
	                    	reader.close();
	                	} catch (IOException e) {}
	            	}
			    }
	    	} else {
	    		System.out.println("###########################################");
	    		System.out.println("### The in file specified is not valid. ###");
	    		System.out.println("###########################################");
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


