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
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Set of command and CLI parameters for codenvy CLI function.
 *
 */ 
public class CommandCLI implements CommandInterface {
    @Parameter(description = "Unused Parameters")
    private List<String> unused = new ArrayList<String>();

    @Parameter(names = { "-v", "--version" }, description = "Print the version and exit.")
    private boolean version;

    @Parameter(names = { "-h", "--help" }, description = "Print this help.")
    private boolean help;

    public boolean getHelp() { return help; }
    public boolean getVersion() { return version; }

     public boolean hasSubCommands() {
        return true;
    }

    public boolean hasMandatoryParameters() {
        return true;
    }

   public String getCommandName(){
        return "codenvy";
    }

    public String getParentCommandName(){
        return null;
    }

    public String getUsageLongDescription() {
    	return("This is the Codenvy CLI.  You can either interact with the local system or execute remote commands against a Codenvy cloud instance.  For more information see http://docs.codenvy.com/cli.");
    }

    public String getUsageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: codenvy <subcommand> <args>");
        return sb.toString();
    }

    public String getHelpDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("For help on a subcommand run 'codenvy COMMAND --help'");
        return sb.toString();
    }

    public void execute() {
        if (getVersion()) {
            System.out.println(" _____           _                       ");
            System.out.println("/  __ \\         | |                      ");
            System.out.println("| /  \\/ ___   __| | ___ _ ____   ___   _ ");
            System.out.println("| |    / _ \\ / _` |/ _ \\ '_ \\ \\ / / | | |");
            System.out.println("| \\__/\\ (_) | (_| |  __/ | | \\ V /| |_| |");
            System.out.println(" \\____/\\___/ \\__,_|\\___|_| |_|\\_/  \\__, |");
            System.out.println("                                    __/ |");
            System.out.println("                                   |___/ ");
            System.out.println(SystemUtils.OS_NAME);
            System.out.println(SystemUtils.OS_ARCH);
            System.out.println(SystemUtils.OS_VERSION);
            System.out.println(SystemUtils.USER_HOME);

            URL url = null;
            try {
                Properties props = new Properties();
     
                // Get hold of the path to the properties file
                // (Maven will make sure it's on the class path)
                url = CodenvyCLI.class.getClassLoader().getResource("app.properties");
                 
                // Load the file
                props.load(url.openStream());
                 
                // Accessing values
                System.out.println(props.getProperty("application.version"));

            } catch (IOException e) {
                System.out.println("### Problem with gaining access to ClassLoader properties for this app. ###");
            } 

            System.exit(0);
         }
    }
 }
