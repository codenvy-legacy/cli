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

import org.apache.commons.lang3.SystemUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import java.io.*;

/**
 * Set of command and CLI parameters for codenvy CLI function.
 *
 */ 
@Parameters(commandDescription = "configure this client to access Codenvy REST APIs")
public class CommandAuth implements CommandInterface {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    @ParametersDelegate
	private CLIAuthParameterDelegate delegate = new CLIAuthParameterDelegate();

	@Parameter(names = { "-d", "--display" }, description = "Display current configuration")
	private boolean display;
    public boolean getDisplay() { return display; }

    public static final String USER_NAME_NAME = "CODENVY_USER_NAME";
    public static final String PASSWORD_NAME = "CODENVY_PASSWORD";
    public static final String TOKEN_NAME = "CODENVY_TOKEN";

    // Helper class used by all CLI commands to retrieve credentials.
    // Step 1: Check default configuration file in USER_HOME\.codenvy\config
    // Step 2: Check environment variables and use values to override.
    // Step 3: Check any parameters and use values to override.
    public static CLICredentials getCredentials() {
    	CLICredentials cred = new CLICredentials();

    	cred.setUser("None Set");
    	cred.setPass("None Set");
    	cred.setToken("None Set");

    	// Check for existence of configuration file.
        File config = new File(SystemUtils.USER_HOME + "\\.codenvy\\config");
    	boolean is_readable = config.exists() & config.canRead();

    	// We have a valid configuration file.
    	// Load it, read in each parameter one at a time into map.
    	if (is_readable) {

    		Properties prop = new Properties();
 
	    	try {
               //load a properties file
    	       prop.load(new FileInputStream(config));
 
	    	} catch (IOException e) {
    			e.printStackTrace();
        	}

        	cred.setUser(prop.getProperty(USER_NAME_NAME));
        	cred.setPass(prop.getProperty(PASSWORD_NAME));
        	cred.setToken(prop.getProperty(TOKEN_NAME));
    	}

    	// Check for environment variables.
    	String env_name = System.getenv(USER_NAME_NAME);
    	String env_pass = System.getenv(PASSWORD_NAME);
    	String env_token = System.getenv(TOKEN_NAME);

    	if (env_name != null) cred.setUser(env_name);
    	if (env_pass != null) cred.setPass(env_pass);
    	if (env_token != null) cred.setToken(env_token);

        return cred;
    } 


    public String getUsageLongDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT STRING");
		return sb.toString();
	}

    public void execute() {
    	if (display) {
    		CLICredentials cred = getCredentials();

    		System.out.println("[CODENVY_USER_NAME]: " + cred.getUser());
    		System.out.println("[CODENVY_PASSWORD]:  " + cred.getPass());
    		System.out.println("[CODENVY_API_TOKEN]: " + cred.getToken());
    		System.exit(0);

    	}

    	/*
    	if (outputFile != null) {
    		try {
	    		outputFile.createNewFile();

	    		FileWriter writer = new FileWriter(outputFile);

	    		factory_params.writeJSONString(writer);

	    		writer.flush();
	    		writer.close();

	    	} catch (java.io.IOException e) {
	    		e.printStackTrace();
	    	}
		} 
		*/
    }

}

