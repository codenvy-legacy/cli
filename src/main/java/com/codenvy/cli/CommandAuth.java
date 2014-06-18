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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.file.*;
import java.nio.file.attribute.*;
import java.net.*;

import java.security.*;

/**
 * Set of command and CLI parameters for codenvy CLI function.
 *
 */ 
@Parameters(commandDescription = "Configure this client to access Codenvy REST APIs")
public class CommandAuth implements CommandInterface {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    @Parameter(names = { "--save" }, description = "Writes properties to a profile configuration file")
    private boolean configure;
    public boolean getConfigure() { return configure; }

    @ParametersDelegate
	private CLIAuthParameterDelegate delegate = new CLIAuthParameterDelegate();

	@Parameter(names = { "-d", "--display" }, description = "Display current configuration")
	private boolean display;
    public boolean getDisplay() { return display; }

    @Parameter(names = { "--newToken" }, description = "Generate new API token from remote Codenvy system")
    private boolean newToken;
    public boolean getNewToken() { return newToken; }

    public static final String PROVIDER_NAME = "CODENVY_PROVIDER_NAME";
    public static final String USER_NAME_NAME = "CODENVY_USER_NAME";
    public static final String PASSWORD_NAME = "CODENVY_PASSWORD";
    public static final String TOKEN_NAME = "CODENVY_TOKEN";

    // Helper class used by all CLI commands to retrieve credentials.
    // Step 1: Check default configuration file in USER_HOME\.codenvy\config
    // Step 1a: Check for profile configuration file, if specified.
    // Step 2: Check environment variables and use values to override.
    // Step 3: Check any parameters and use values to override.
    public static CLICredentials getCredentials(String param_profile,
                                                String param_provider, 
                                                String param_user, 
                                                String param_pass, 
                                                String param_token) {

    	CLICredentials cred = new CLICredentials();

        cred.setProvider(null);
    	cred.setUser(null);
    	cred.setPass(null);
    	cred.setToken(null);

    	// If param_profile is null, then use default config
        // If param_profile is !null, then use profile name for config
        String config_file_name = getCLIDir() + "/conf/";

        File config;
        if (param_profile == null) {
            config_file_name = config_file_name + "config";
        } else {
            config_file_name = config_file_name + param_profile;
        }
  
        config = new File(config_file_name);

    	boolean is_readable = config.exists() & config.canRead();

    	// We have a valid configuration file.
    	// Load it, read in each parameter one at a time into map.
    	if (is_readable) {

    		Properties prop = new Properties();
            FileInputStream fis = null;

	    	try {
                fis = new FileInputStream(config);
                prop.load(fis);
	    	} catch (IOException e) {
    			e.printStackTrace();
        	} finally {
                try { fis.close(); } catch (IOException e) { e.printStackTrace(); }
            }

            cred.setProvider(prop.getProperty(PROVIDER_NAME));
        	cred.setUser(prop.getProperty(USER_NAME_NAME));
        	cred.setPass(prop.getProperty(PASSWORD_NAME));
        	cred.setToken(prop.getProperty(TOKEN_NAME));
    	}

    	// Check for environment variables.
        String provider_name = System.getenv(PROVIDER_NAME);
    	String env_name = System.getenv(USER_NAME_NAME);
    	String env_pass = System.getenv(PASSWORD_NAME);
    	String env_token = System.getenv(TOKEN_NAME);

        if (provider_name != null) cred.setProvider(provider_name);
    	if (env_name != null) cred.setUser(env_name);
    	if (env_pass != null) cred.setPass(env_pass);
    	if (env_token != null) cred.setToken(env_token);

        // Check for parameter overrides.
        if (param_provider != null) cred.setProvider(param_provider);
        if (param_user != null) cred.setUser(param_user);
        if (param_pass != null) cred.setPass(param_pass);
        if (param_token != null) cred.setToken(param_token);

        if (cred.getProvider() == null || cred.getProvider().equals("")) cred.setProvider("https://codenvy.com");
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
        String config_file_name = getCLIDir() + "/conf/";

        File config;
        if (param_profile == null) {
            config_file_name = config_file_name + "config";
        } else {
            config_file_name = config_file_name + param_profile;
        }

        config = new File(config_file_name);

        boolean does_exist = config.exists();        
        boolean is_writeable = false;
        FileOutputStream fos = null;

        try {

            if (!does_exist) {
/*
        Path config_file = Paths.get(config_file_name);
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwxrwx");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
        Files.setPosixFilePermissions(config_file, perms);
*/

                // Cannot put this in an &.
                // The mkdirs() function will return false if directory already exists.
                does_exist = config.getParentFile().mkdirs();
                does_exist = config.createNewFile(); 
            }
            
            if (does_exist) {
                is_writeable = config.canWrite();
            }            
            
            // We have a valid configuration file.
            if (is_writeable) {

                // We write the file in a Properties format.
                Properties prop = new Properties();
                prop.setProperty(PROVIDER_NAME, cred.getProvider());
                prop.setProperty(USER_NAME_NAME, cred.getUser());
                prop.setProperty(PASSWORD_NAME, cred.getPass());
                prop.setProperty(TOKEN_NAME, cred.getToken());
                
               //write the properties file out
               fos = new FileOutputStream(config);
               prop.store(fos, null);

            }
     
        } catch (IOException e) {
            System.out.println("############################################################################");
            System.out.println("### Write issue.  CLI may not have write permission to ~/.codenvy/config ###");
            System.out.println("############################################################################");
            e.printStackTrace();
        } finally {
           try { fos.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static String getCLIDir() {
        CodeSource codeSource;
        File jarFile;
        String cliDir = null;

        try {
            codeSource = CodenvyCLI.class.getProtectionDomain().getCodeSource();
            jarFile = new File(codeSource.getLocation().toURI().getPath());
            cliDir = jarFile.getParentFile().getParentFile().getPath();
        } catch (URISyntaxException e) {
            System.out.println("### Problem with the files system and our JAR file. ###");                
        }

        return cliDir;
    }


    // Helper method to display a set of credentials against a particular profile
    public static void displayCredentials(String param_profile, 
                                      CLICredentials cred) {
        System.out.print("PROFILE:               ");
        if (param_profile == null) {
            System.out.println("Default");
        } else {
            System.out.println(param_profile);
        }

        System.out.println(PROVIDER_NAME + ": " + cred.getProvider());
        System.out.println(USER_NAME_NAME + ":     " + cred.getUser());
        System.out.println(PASSWORD_NAME + ":      " + cred.getPass());
        System.out.println(TOKEN_NAME + ":         " + cred.getToken());
    }


    public boolean hasSubCommands() {
        return false;
    }

    public boolean hasMandatoryParameters() {
        return true;
    }

    public String getCommandName(){
        return "auth";
    }

    public String getParentCommandName(){
        return "codenvy";
    }

    public String getUsageLongDescription() {
		StringBuilder sb = new StringBuilder();
        sb.append("Manages this client's local configuration for working with a remote Codenvy\n");
        sb.append("cloud.  The CLI provides many settings, and these settings are specified\n");
        sb.append("in multiple ways.  Configuration items can be set in a configuration file, \n");
        sb.append("in an environment variable, or a command line option.\n");
        sb.append("\n");
        sb.append("When a configuration property is specified in multiple ways, the precedence\n");
        sb.append("is: Command Line, Environment Variable, Configuration File.\n");
        sb.append("\n");
        sb.append("The default config file is stored at CLI_HOME/conf/config where CLI_HOME is\n");
        sb.append("the directory where the CLI has been installed into.\n");
        sb.append("\n");
        sb.append("Codenvy supports multiple profiles that can be used against different\n");
        sb.append("clouds.  Use the --profile option to specify which profile should be loaded\n");
        sb.append("or set.  We store each profile in its own configuration file in the same\n");
        sb.append(" location as the default configuration file.\n");
        sb.append("\n");
        sb.append("The following environment variables are set as part of a profile, whether\n");
        sb.append("stored in a configuration file, environment variable, or as a command\n");
        sb.append("line parameter: CODENVY_PROVIDER_NAME, CODENVY_USER_NAME, CODENVY_PASSWORD,\n");
        sb.append("and CODENVY_TOKEN. You should only need to pass the user name and password\n");
        sb.append("when acquiring a token.  For most other Codenvy functions, the CLI will use\n");
        sb.append("the token associated with the profile to gain authorized access.\n");
        sb.append("\n");
        sb.append("Specifying the --newToken parameter will generate a new API token from a\n");
        sb.append("remote Codenvy installation. REST API calls generate the token. You must\n");
        sb.append("provide user name and password credentials to generate the token. Tokens\n");
        sb.append("can expire, so check the stored is still valid after inactivity. If you\n");
        sb.append("specify --newToken then the newly generated token takes precedence over\n");
        sb.append("any stored in a configuration file, environment variable, or command line\n");
        sb.append("parameter. Use --provider to specificy which Codenvy system to generate a\n");
        sb.append("token from.\n");
        sb.append("\n");
        sb.append("Use --display to see how the CLI is loading your properties.  Use --save\n");
        sb.append("to write any loaded parameters to your config file.\n");
        sb.append("\n");
        sb.append("Example: Display loaded configuration.\n");
        sb.append("  codenvy auth -d\n");
        sb.append("\n");
        sb.append("Example: Display configuration from the c2 profile.\n");
        sb.append("  codenvy auth -d --profile c2\n");
        sb.append("\n");
        sb.append("Example: Set User Name to john@codenvy.com and store in default profile.\n");
        sb.append("  codenvy auth -u john@codenvy.com --save\n");
        sb.append("\n");
        sb.append("Example: Set User Name to john@codenvy.com. Set Password to krusty.\n");
        sb.append("         Generate a new token by with the default. Store in c2 profile.\n");
        sb.append("  codenvy auth -u john@codenvy.com -p krusty --newToken --profile c2 --save\n");
       
		return sb.toString();
	}

    public String getUsageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: codenvy auth <args>");
        return sb.toString();
    }

    public String getHelpDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        return sb.toString();
    }

    public void execute() {

        // Step 1: Check to see if we are in display or configure mode.
        // Step 2: Find parameters for the specified profile.
        // Step 3: If the user has asked for a new token, call REST API to get it.
        // Step 4: If in configure mode, write the parameters out to a config file
        // Step 5: Display the parameters if that is the ask.
    	if (display | configure | newToken) {
    		CLICredentials cred = getCredentials(delegate.getProfile(),
                                                 delegate.getProvider(),
                                                 delegate.getUser(),
                                                 delegate.getPassword(),
                                                 delegate.getToken());

            if (newToken) {
                cred.setToken(getNewToken(cred));
            }

            if (configure) {
                setCredentials(delegate.getProfile(), cred);
            }
    		
            if (display) {
                displayCredentials(delegate.getProfile(), cred);
                System.exit(0);
            }
    	}
    }

    private String getNewToken(CLICredentials cred) {

        JSONObject api_return_data = null;
        JSONObject api_input_data = new JSONObject();

        // Format the appropriate input data for this REST command.
        // Pass the input data into helper command, invoke REST command, and get response.
        // Parse the response appropriately.
      
        if (cred.getUser() == null || cred.getUser().isEmpty() || cred.getPass() == null || cred.getPass().isEmpty()) {
            System.out.println("###############################################################################");
            System.out.println("### No user or password set.  You cannot generate a new token without this. ###");
            System.out.println("###############################################################################");
            System.exit(0);
        }
        
        api_input_data.put("username", cred.getUser());
        api_input_data.put("password", cred.getPass());
        
        api_return_data = RESTAPIHelper.callRESTAPIAndRetrieveResponse(cred, api_input_data, RESTAPIHelper.REST_API_AUTH_LOGIN_JSON);
        
        if (api_return_data == null)
            return "";
        else
            return (String)api_return_data.get("value");

    }
}