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
 * Set of parameters and help for 'codenvy remote tmp_workspace_create' command
 *
 */ 
@Parameters(commandDescription = "Creates a new temporary developer environment from valid Factory URL")
public class CommandRemoteTmpWorkspaceCreate implements CommandInterface {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    public String getUsageLongDescription() {
		return("INSERT LONG DESCRIPTION");
	}

    public void execute() {
    	System.out.println("not yet implemented");
    }
}

