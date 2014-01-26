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

/**
 * Set of parameters and help for 'codenvy remote factory:invoke' command
 *
 */ 
@Parameters(commandDescription = "Creates a new temporary developer environment from valid Factory URL")
public class CommandRemoteFactoryInvoke implements CommandInterface {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    @Parameter(names = { "--url" }, description = "The Factory URL to launch in a new browser session")
    private String url = "https://codenvy.com";
    public String getURL() { return url; }

    public boolean hasSubCommands() {
        return false;
    }

    public boolean hasMandatoryParameters() {
        return true;
    }

    public String getCommandName(){
        return "factory:invoke";
    }

    public String getParentCommandName(){
        return "remote";
    }

    public String getUsageLongDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Launches a new browser session and invokes a Factory URL.  The Factory URL is\n");
        sb.append("provided by the --url option.  There is no type checking of parameters in the\n");
        sb.append("URL.  If no URL provided, will launch codenvy.com.\n");
        return sb.toString();
	}

    public String getUsageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: codenvy remote factory:invoke [<args>] url_to_invoke");
        return sb.toString();
    }

    public String getHelpDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        return sb.toString();
    }

    public void execute() {
        BrowserHelper.launchNativeBrowserSession(url);
    }
}