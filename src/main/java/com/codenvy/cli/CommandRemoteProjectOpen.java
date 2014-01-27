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

import java.util.*;

/**
 * Set of parameters and help for 'codenvy remote project:open' command
 *
 */ 
@Parameters(commandDescription = "Opens a browser session and loads a project")
public class CommandRemoteProjectOpen implements CommandInterface {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    @Parameter(description = "The Project URL to launch in a new browser session")
    private List<String> url = new ArrayList<String>(Arrays.asList("https://codenvy.com"));
    public String getURL() { return url.get(1); }

    public boolean hasSubCommands() {
        return false;
    }

    public boolean hasMandatoryParameters() {
        return true;
    }

    public String getCommandName(){
        return "proj:open";
    }
    
    public String getParentCommandName(){
        return "remote";
    }

    public String getUsageLongDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Launches a new browser session and opens a Project URL. The Project URL is\n");
        sb.append("provided by a default option.\n");
        return sb.toString();
	}

    public String getUsageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: codenvy remote proj:open [<args>] url_to_invoke");
        return sb.toString();
    }

    public String getHelpDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        return sb.toString();
    }

    public void execute() {
        BrowserHelper.launchNativeBrowserSession(url.get(1));
    }
}