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

    @Parameter(names = { "--configure" }, description = "Updates your User configuration file with parameter overrides")
    private boolean configure;
    public boolean getConfigure() { return configure; }

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
    public static CLICredentials getCredentials(String param_user, String param_pass, String param_token) {
    	CLICredentials cred = new CLICredentials();

    	cred.setUser(null);
    	cred.setPass(null);
    	cred.setToken(null);

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

        // Check for parameter overrides.
        if (param_user != null) cred.setUser(param_user);
        if (param_pass != null) cred.setPass(param_pass);
        if (param_token != null) cred.setToken(param_token);

        return cred;
    } 


    public String getUsageLongDescription() {
		StringBuilder sb = new StringBuilder();
        sb.append("Manages this client's local configuration for working with a remote Codenvy cloud.\n");
        sb.append("The Codenvy CLI provides a number of configurable settings, and these settings can \n");
        sb.append("specified in multiple ways.  Configuration items can be set in a configuration file, \n");
        sb.append("in an environment variable, or a command line option.\n");
        sb.append("\n");
        sb.append("When a configuration property is specified in multiple ways, the precedence from\n");
        sb.append("highest to lowest is: Command Line, Environment Variable, Configuration File.\n");
        sb.append("\n");
        sb.append("The configuration file is stored at YOUR_HOME\\.codenvy\\config. On a Windows system\n");
        sb.append("this would look like C:\\Users\\USERNAME\\.codenvy\\config.  On a Linux system this\n");
        sb.append("would look like ~/.codenvy/config.\n");
        sb.append("\n");
        sb.append("Use the --display option to see how the CLI is loading your properties.  Use the\n");
        sb.append("--configure option to take the parameters loaded and write them to your config file.\n");
		return sb.toString();
	}

    public void execute() {

    	if (display | configure) {
    		CLICredentials cred = getCredentials(delegate.getUser(),
                                                 delegate.getPassword(),
                                                 delegate.getToken());

            if (configure) {
                
                // Check for existence of configuration file.
                File config = new File(SystemUtils.USER_HOME + "\\.codenvy\\config");
                
                boolean is_writeable = false;

                try {

                    if (!config.exists()) {
                        is_writeable = config.getParentFile().mkdirs() & config.createNewFile() & config.canWrite();
                    }
                    
                    // We have a valid configuration file.
                    if (is_writeable) {

                        // We write the file in a Properties format.
                        Properties prop = new Properties();
                        prop.setProperty(USER_NAME_NAME, cred.getUser());
                        prop.setProperty(PASSWORD_NAME, cred.getPass());
                        prop.setProperty(TOKEN_NAME, cred.getToken());
                        
                       //write the properties file out
                       prop.store(new FileOutputStream(config), null);

                    }
             
                } catch (IOException e) {
                    e.printStackTrace();
                }
            
            }
    		
            if (display) {
                System.out.println(USER_NAME_NAME + ": " + cred.getUser());
                System.out.println(PASSWORD_NAME + ":  " + cred.getPass());
                System.out.println(TOKEN_NAME + ":     " + cred.getToken());
                System.exit(0);
            }
    	}

    }

}

