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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

import com.codenvy.commons.lang.IoUtil;

/**
 * Set of parameters and help for 'codenvy remote tmp_workspace_create' command
 *
 */ 
@Parameters(commandDescription = "Creates a new temporary developer environment from valid Factory URL")
public class CommandRemoteTmpWorkspaceCreate implements CommandInterface {

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

    	HttpURLConnection conn = null;

        try {

            conn = (HttpURLConnection)new URL(protocol, host, port, "/api/factory").openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);

            int responseCode = conn.getResponseCode();
            System.out.println(responseCode);

            if (responseCode / 100 != 2) {
                InputStream errorStream = conn.getErrorStream();
                String message = errorStream != null ? IoUtil.readAndCloseQuietly(errorStream) : "";
                System.out.println(message);

            }

            InputStreamReader in = new InputStreamReader((InputStream) conn.getInputStream());
			JSONParser parser = new JSONParser();
   			JSONObject jsonObject = (JSONObject) parser.parse(in);
   			System.out.println(jsonObject.toString());

        } catch (IOException | ParseException e ) {
            System.out.println(e.getLocalizedMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }
}

