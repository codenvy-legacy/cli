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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Set of parameters and help for 'codenvy remote factory:invoke' command
 *
 */ 
@Parameters(commandDescription = "Creates a new temporary developer environment from valid Factory URL")
public class CommandRemoteFactoryInvoke implements CommandInterface {

	String protocol = "https";
	String host = "codenvy.com";
	int port = -1;

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    public String getUsageLongDescription() {
		return("INSERT LONG DESCRIPTION");
	}

    public void execute() {
        try {

            if (!Desktop.getDesktop().isDesktopSupported()) {
                System.out.println("###########################################################################\n");
                System.out.println("### The Codenvy CLI is unable to launch a browser on this client.       ###\n");
                System.out.println("### You must manually copy your Factory or Worspace URL into a browser. ###\n");
                System.out.println("###########################################################################\n");
                System.exit(0);
            }

            // open the default web browser for the HTML page
            URI uri = new URI("https://codenvy.com/");
            Desktop.getDesktop().browse(uri);

        } catch (URISyntaxException e) {
            System.out.println("##############################################################\n");
            System.out.println("### You have passed in an improperly formatted URL string. ###\n");
            System.out.println("##############################################################\n");
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

