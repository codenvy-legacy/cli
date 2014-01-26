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

import java.util.List;
import java.util.ArrayList;

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

    public void execute() {}

    public void showNotYetImplemented() { System.out.println("This command is not yet implemented."); }

 }
