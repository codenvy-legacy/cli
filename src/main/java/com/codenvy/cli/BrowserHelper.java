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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Helper methods for launching browser sessions
 *
 */ 
public class BrowserHelper {

    public static void launchNativeBrowserSession(String url) {
        try {

            if (!Desktop.getDesktop().isDesktopSupported()) {
                System.out.println("###########################################################################");
                System.out.println("### The Codenvy CLI is unable to launch a browser on this client.       ###");
                System.out.println("### You must manually copy your Factory or Worspace URL into a browser. ###");
                System.out.println("###########################################################################");
                System.exit(0);
            }

            // open the default web browser for the HTML page
            URI uri = new URI(url);
            Desktop.getDesktop().browse(uri);

        } catch (URISyntaxException e) {
            System.out.println(e);
            System.out.println("##############################################################");
            System.out.println("### You have passed in an improperly formatted URL string. ###");
            System.out.println("##############################################################");
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}