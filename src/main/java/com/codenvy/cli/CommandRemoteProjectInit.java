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

import com.beust.jcommander.*;
import com.beust.jcommander.converters.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Set of codenvy remote proj:init command and parameters.
 *
 */ 
@Parameters(separators = " ", commandDescription = "Initializes a new Codenvy project by creating .c5y file")
public class CommandRemoteProjectInit implements CommandInterface {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    @Parameter(names = "--out", converter = FileConverter.class, description = "File to write project configuration to.  Defaults to default.c5y.")
    private File outputFile = new File("default.c5y");
    
    @Parameter(names = "--param", listConverter = JSONPairConverter.class, converter = JSONPairConverter.class, arity = 2, description = "Sets name/value pair.  First is name.  Second is value.  Can be used multiple times.")
    private List<JSONPair> params = new ArrayList<JSONPair>();

   
    public String getUsageLongDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Initializes a new Codenvy project by created a .c5y file.  This command does not\n");
		sb.append("perform any validation of the parameters inserted into the file.  You can check\n");
		sb.append("validation of the parameters by creating a Factory URL using\n");
		sb.append("'remote proj:factory' to generate a Factory URL and then invoking\n");
		sb.append("'remote tmpws:create' with that URL.  'remote:proj:factory' can also be used to\n");
		sb.append("generate a new .c5y file.\n"); 
		sb.append("\n");
		sb.append("Parameters passed on the command line with '--param' will be added to the newly\n");
		sb.append("created .c5y file.\n");
		return sb.toString();
	}

	// Create a valid Factory URL with a set of parameters.
	// 1) Look for parameters in default.c5y in local directory.
	// 2) Load (override) any parameters in file specified by --file
	// 3) Load (override) any parameters passed in by --param
	// 4) If ! --encoded, then generate a non-encoded URL through string
	// 5) if --encoded, then call REST API to generate Factory URL
	// No test as to whether --provider is valid or not.
	// 6) If --out, then take the resulting URL and output parameters to specified file
    public void execute() {

    
    	// Internal JSON Object to store / override parameters.
    	JSONObject factory_params = new JSONObject();

    	// The dynamically generated Factory URL
    	StringBuilder factory_url = new StringBuilder();
   

		/*
		 *  STEP 1: Add each --param name value to the Map.
		 */
		Iterator<JSONPair> iterator = params.iterator();
		while (iterator.hasNext()) {
			factory_params.put(iterator.next().getPair(), iterator.next().getPair());
		}	    	
    	

		/* 
    	 * STEP 2: Generate an output file with the contents if outputFile is set.
    	 *         Always output the Factory URL to the console.
    	 */ 
    	if (outputFile != null) {
    		try {

    			// Likely will throw an exception if file you specified is in non-existant directory
	    		outputFile.createNewFile();

	    		FileWriter writer = new FileWriter(outputFile);

	    		factory_params.writeJSONString(writer);

	    		writer.flush();
	    		writer.close();

	    	} catch (java.io.IOException e) {
	    		e.printStackTrace();
	    	}
		} 
    }
}


