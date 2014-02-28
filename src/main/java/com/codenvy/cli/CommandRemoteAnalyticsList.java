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
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Parameters and execution of 'codenvy remote analytics:list' command.
 *
 */ 
@Parameters(commandDescription = "List all metrics in the analytics system")
public class CommandRemoteAnalyticsList extends AbstractCommand {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    @ParametersDelegate
    private CLIAuthParameterDelegate delegate = new CLIAuthParameterDelegate();

    public boolean hasSubCommands() {
        return false;
    }

    public boolean hasMandatoryParameters() {
        return false;
    }

    public String getCommandName(){
        return "analytics:list";
    }

    public String getParentCommandName(){
        return "remote";
    }

    public String getUsageLongDescription() {
		return("INSERT LONG DESCRIPTION FOR HELP");
	}
    
    public String getUsageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: codenvy remote analytics:list [<args>]");
        return sb.toString();
    }

    public String getHelpDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        return sb.toString();
    }

    public void execute() {

        
        CLICredentials cred = CommandAuth.getCredentials(delegate.getProfile(),
                                                         delegate.getProvider(),
                                                         delegate.getUser(),
                                                         delegate.getPassword(),
                                                         delegate.getToken());

        JSONObject api_return_data = null;
        JSONObject api_input_data = null;

        // Format the appropriate input data for this REST command.
        // Pass the input data into helper command, invoke REST command, and get response.
        // Parse the response appropriately.
        api_return_data = RESTAPIHelper.callRESTAPIAndRetrieveResponse(cred, api_input_data, RESTAPIHelper.REST_API_ANALYTICS_JSON);

        if (api_return_data == null) {
            System.out.println("#####################################################################");
            System.out.println("### There was a problem retrieving the list of metrics.  Exiting. ###");
            System.out.println("#####################################################################");
        }
        else {
            JSONArray list_of_metrics = (JSONArray) api_return_data.get("metrics");
            Iterator<JSONObject> it = list_of_metrics.iterator();
            List<String> list = new ArrayList<String>();

            while (it.hasNext()) {

                JSONObject link = (JSONObject) it.next();
                list.add((String)link.get("name"));
            }

            Collections.sort(list, new Comparator<String>(){
                public int compare(String s1, String s2) {
                    return s1.length() - s2.length();
                }
            });

            for (String s : list) {
                System.out.println(s);
            }
        }
    }
}
