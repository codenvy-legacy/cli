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

    @Parameter(names = { "--configure" }, description = "Writes loaded properties to a profile configuration file")
    private boolean configure;
    public boolean getConfigure() { return configure; }

    @ParametersDelegate
	private CLIAuthParameterDelegate delegate = new CLIAuthParameterDelegate();

	@Parameter(names = { "-d", "--display" }, description = "Display current configuration")
	private boolean display;
    public boolean getDisplay() { return display; }

    @Parameter(names = { "--newToken" }, description = "Calls remote Codenvy instance to generate new API token")
    private boolean newToken;
    public boolean getNewToken() { return newToken; }

    @Parameter(names = { "--provider" }, description = "Domain of Codenvy cloud where remote commands will be executed.  Default is https://codenvy.com.")
    private String provider;
    public String getProvider() { return provider; }

    public static final String USER_NAME_NAME = "CODENVY_USER_NAME";
    public static final String PASSWORD_NAME = "CODENVY_PASSWORD";
    public static final String TOKEN_NAME = "CODENVY_TOKEN";

    // Helper class used by all CLI commands to retrieve credentials.
    // Step 1: Check default configuration file in USER_HOME\.codenvy\config
    // Step 1a: Check for profile configuration file, if specified.
    // Step 2: Check environment variables and use values to override.
    // Step 3: Check any parameters and use values to override.
    public static CLICredentials getCredentials(String param_profile, 
                                                String param_user, 
                                                String param_pass, 
                                                String param_token) {

    	CLICredentials cred = new CLICredentials();

    	cred.setUser(null);
    	cred.setPass(null);
    	cred.setToken(null);

    	// If param_profile is null, then use default config
        // If param_profile is !null, then use profile name for config
        File config;
        if (param_profile == null) {
            config = new File(SystemUtils.USER_HOME + 
                              "\\.codenvy\\config");
        } else {
            config = new File (SystemUtils.USER_HOME +
                               "\\.codenvy\\" + param_profile);
        }

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

        if (cred.getUser() == null) cred.setUser("");
        if (cred.getPass() == null) cred.setPass("");
        if (cred.getToken() == null) cred.setToken("");
        
        return cred;
    } 


    // Helper class to write credentials to the correct property file.
    public static void setCredentials(String param_profile, 
                                      CLICredentials cred) {


        // If param_profile is null, then use default config
        // If param_profile is !null, then use profile name for config
        File config;

        if (param_profile == null) {
            config = new File(SystemUtils.USER_HOME + 
                              "\\.codenvy\\config");
        } else {
            config = new File(SystemUtils.USER_HOME +
                              "\\.codenvy\\" + param_profile);
        }

        boolean does_exist = config.exists();        
        boolean is_writeable = false;


        try {

            System.out.println (config.exists());
            
            if (!does_exist) {
                does_exist = config.getParentFile().mkdirs() & 
                             config.createNewFile(); 
            }

            if (does_exist) {
                is_writeable = config.canWrite();
            }            
            
            System.out.println (is_writeable);
            
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

    public String getUsageLongDescription() {
		StringBuilder sb = new StringBuilder();
        sb.append("Manages this client's local configuration for working with a remote Codenvy cloud.\n");
        sb.append("The Codenvy CLI provides a number of configurable settings, and these settings are \n");
        sb.append("specified in multiple ways.  Configuration items can be set in a configuration file, \n");
        sb.append("in an environment variable, or a command line option.\n");
        sb.append("\n");
        sb.append("When a configuration property is specified in multiple ways, the precedence from\n");
        sb.append("highest to lowest is: Command Line, Environment Variable, Configuration File.\n");
        sb.append("\n");
        sb.append("The default config file is stored at YOUR_HOME\\.codenvy\\config. On Windows this\n");
        sb.append("would look like C:\\Users\\USERNAME\\.codenvy\\config.  On Linux this would look\n");
        sb.append("like ~/.codenvy/config.\n");
        sb.append("\n");
        sb.append("Codenvy support multiple profiles that can be stored to be used against different\n");
        sb.append("Codenvy clouds.  Use the --profile option to specify which profile should be\n");
        sb.append("loaded or set.  We store each profile in its own configuration file in the same\n");
        sb.append("location as the default configuration file.\n");
        sb.append("\n");
        sb.append("Specifying the --newToken parameter will generate a new API token from a remote\n");
        sb.append("Codenvy installation.  We use REST API calls to generate the token.  You must\n");
        sb.append("provide user name and password credentials for the remote system to generate\n");
        sb.append("the token.  Tokens can expire with Codenvy, so check to make sure the stored\n");
        sb.append("token is still valid when you use it on other calls.  If you specify this\n");
        sb.append("parameter then the new generated token will take precedence over one used on\n");
        sb.append("--token command line.  Use the --provider parameter to specify which remote\n");
        sb.append("Codenvy environment to generate the token from.\n");
        sb.append("\n");
        sb.append("Use the --display option to see how the CLI is loading your properties.  Use the\n");
        sb.append("--configure option to take the parameters loaded and write them to your config file.\n");
		return sb.toString();
	}

    public void execute() {

        // Step 1: Check to see if we are in display or configure mode.
        // Step 2: Find parameters for the specified profile.
        // Step 3: If in configure mode, call authentication API to get Token
        // Step 4: Write out properties to the appropriate profile file.
    	if (display | configure) {
    		CLICredentials cred = getCredentials(delegate.getProfile(),
                                                 delegate.getUser(),
                                                 delegate.getPassword(),
                                                 delegate.getToken());

            if (newToken) {

            }

            if (configure) {
                setCredentials(delegate.getProfile(), cred);
            }
    		
            if (display) {
                System.out.print("PROFILE: ");
                if (delegate.getProfile() == null) {
                    System.out.println("Default");
                } else {
                    System.out.println(delegate.getProfile());
                }

                System.out.println(USER_NAME_NAME + ": " + cred.getUser());
                System.out.println(PASSWORD_NAME + ":  " + cred.getPass());
                System.out.println(TOKEN_NAME + ":     " + cred.getToken());
                System.exit(0);
            }
    	}
    }
}