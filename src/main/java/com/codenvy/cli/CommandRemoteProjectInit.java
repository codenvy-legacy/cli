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
@Parameters(separators = " ", commandDescription = "Initializes a new Codenvy project by creating JSON file")
public class CommandRemoteProjectInit implements CommandInterface {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    @ParametersDelegate
	private JSONFileParameterDelegate json_delegate = new JSONFileParameterDelegate();

    public boolean hasSubCommands() {
        return false;
    }

    public boolean hasMandatoryParameters() {
        return true;
    }

    public String getCommandName(){
        return "proj:init";
    }

    public String getParentCommandName(){
        return "remote";
    }

    public String getUsageLongDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Initializes a new Codenvy project by creating a JSON file. This command\n");
		sb.append("does not perform validation of the parameters in the file.  You can check\n");
		sb.append("validation of the parameters by creating a Factory URL using 'remote\n");
		sb.append("factory:create --encoded --launch' to generate a Factory URL and invoke it.\n");
		sb.append("\n");
		sb.append("Precedence of JSON is --param command line and then --in file.  If --out is\n");
		sb.append("specified, the JSON objects will be written to the specified file.\n");		
		sb.append("\n");
        sb.append("Example: Load angular.json, add 'happy:joy' param, and write to sample.json\n");
        sb.append("  codenvy remote proj:init --in angular.json --param happy joy --out sample.json\n");
		sb.append("\n");

		return sb.toString();
	}

    public String getUsageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: codenvy remote proj:init <args>");
        return sb.toString();
    }

    public String getHelpDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        return sb.toString();
    }

	// Create a valid Factory URL with a set of parameters.
	// 1) Look for parameters in default.c5y in local directory.
	// 2) Load (override) any parameters in file specified by --in
	// 3) Load (override) any parameters passed in by --param
	// 4) If --out, then take the resulting URL and output parameters to specified file
    public void execute() {
    
    	// Internal JSON Object to store / override parameters.
    	JSONObject factory_params = new JSONObject();

    	/*
    	 * STEP 1: Load any JSON parameters from file specified by --in, or parameters.
    	 */
    	factory_params = JSONFileHelper.loadJSONFromFileAndParams(json_delegate);


		/* 
    	 * STEP 2: Generate an output file with the contents if outputFile is set.
    	 *         
    	 */ 
   		JSONFileHelper.writeJSONFile(json_delegate.getOutputFile(), factory_params);
    }
}


