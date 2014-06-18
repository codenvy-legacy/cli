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

import java.util.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Parameters and execution of 'codenvy remote analytics:metric' command.
 *
 */ 
@Parameters(commandDescription = "Retrieve the value of a metric from analytics system")
public class CommandRemoteAnalyticsMetric extends AbstractCommand {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    @Parameter(names = { "-n", "--name" }, description = "The name of the metric to query; defaults to workspaces")
    private String name = "workspaces";

    @Parameter(names = "--filter", arity = 2, description = "Filters the query. Two values separated by spaces")
    protected List<String> filters = new ArrayList<String>();
    public List<String> getFilters() { return filters; }

    @ParametersDelegate
    private CLIAuthParameterDelegate delegate = new CLIAuthParameterDelegate();

    public boolean hasSubCommands() {
        return false;
    }

    public boolean hasMandatoryParameters() {
        return false;
    }

    public String getCommandName(){
        return "analytics:metric";
    }

    public String getParentCommandName(){
        return "remote";
    }

    public String getUsageLongDescription() {
        StringBuilder sb = new StringBuilder();
/*        sb.append("Generates a list of metrics that can be queried from the underlying analytics system.\n");
        sb.append("The analytics system collects events across the system from actual usage and then \n");
        sb.append("correlates them into a data warehouse of statistics. Aggregation occurs a couple\n");
        sb.append("times each day.\n");
        sb.append("\n");
        sb.append("Metrics in the system can be complex types. As of Codenvy 2.11, there were 130\n");
        sb.append("metrics that can be pulled depending upon access rights. Use 'codenvy remote\n");
        sb.append("analytics:metric' to see the value of a single metric.\n");
        sb.append("\n");
        sb.append("To filter the list of returned metrics, you can pass in a regular expresssion with\n");
        sb.append("--regex.  Cheat sheet:\n");
        sb.append("http://www.cheatography.com/davechild/cheat-sheets/regular-expressions/\n");
        sb.append("\n");
        sb.append("\n\n");
        sb.append("Example: Generate all metrics with \"users_\" in the name\n");
        sb.append("  codenvy remote analytics:list --regex users_[a..z]\n");
  */      sb.append("\n");
        return sb.toString();
	}
    
    public String getUsageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: codenvy remote analytics:metric [<args>]");
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
        JSONObject api_input_data = new JSONObject();

        // This command takes an ExtraPath
        // The name of the extra path is the name of the variable to search
        api_input_data.put("ExtraPath", name);

        // We will then insert all of the name / value pairs to be used as filters
        // into the same input data object.
        Iterator<String> iterator = filters.iterator();
        while (iterator.hasNext()) {
            api_input_data.put(iterator.next(), iterator.next());
        }

        // Format the appropriate input data for this REST command.
        // Pass the input data into helper command, invoke REST command, and get response.
        // Parse the response appropriately.
        api_return_data = RESTAPIHelper.callRESTAPIAndRetrieveResponse(cred, api_input_data, RESTAPIHelper.REST_API_ANALYTICS_METRIC_JSON);

        if (api_return_data == null) {
            System.out.println("############################################################");
            System.out.println("### There was a problem retrieving metric information.   ###");
            System.out.println("### Metric Name: " + name);
            System.out.println("############################################################");
        }
        else {
            String value = (String)api_return_data.get("value");
            String type = (String)api_return_data.get("type");

            // Apply print algorithms
            StringBuilder sb = new StringBuilder();

            System.out.println(api_return_data);
        }
    }
}
