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
 * Parameters and execution of 'codenvy remote analytics:list' command.
 *
 */ 
@Parameters(commandDescription = "List all metrics in the analytics system")
public class CommandRemoteAnalyticsList extends AbstractCommand {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    @Parameter(names = { "-r", "--regex" }, description = "Perl regex filter to narrow list of returned metrics")
    private String regex;

    @Parameter(names = { "-i", "--title" }, description = "Include title and header row; defaults to false")
    private boolean title = false;

    @Parameter(names = { "-y", "--type" }, description = "Include type of metric; defaults to false")
    private boolean type = false;

    @Parameter(names = { "-d", "--desc" }, description = "Include description of metric; defaults to false")
    private boolean description = false;

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
        StringBuilder sb = new StringBuilder();
        sb.append("Generates a list of metrics that can be queried from the underlying\n");
        sb.append("analytics system. The analytics system collects events from actual usage\n");
        sb.append("and then correlates into a warehouse of statistics. Aggregation occurs a\n");
        sb.append("couple times each day.\n");
        sb.append("\n");
        sb.append("Metrics in the system can be complex types. As of Codenvy 2.11, there were\n");
        sb.append("130 metrics that can be pulled depending upon access rights. Use 'codenvy\n");
        sb.append(" remote analytics:metric' to see the value of a single metric.\n");
        sb.append("\n");
        sb.append("To filter the returned metrics, you can pass in a regular expresssion with\n");
        sb.append("--regex.  Cheat sheet:\n");
        sb.append("http://www.cheatography.com/davechild/cheat-sheets/regular-expressions/\n");
        sb.append("\n");
        sb.append("\n\n");
        sb.append("Example: Generate all metrics with \"users_\" in the name\n");
        sb.append("  codenvy remote analytics:list --regex users_[a..z]\n");
        sb.append("\n");
        return sb.toString();
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
            HashMap<String, Object> list = new HashMap<String, Object>();
            int max_metric_length = 0;
            
            while (it.hasNext()) {
                JSONObject link = (JSONObject) it.next();
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("Name",(String)link.get("name"));
                map.put("Type",(String)link.get("type"));
                map.put("Desc",(String)link.get("description"));
                map.put("Roles",(JSONArray)link.get("rolesAllowed"));
                list.put((String)link.get("name"), map);

                if (((String)link.get("name")).length() > max_metric_length) {
                    max_metric_length = ((String)link.get("name")).length();
                }
            }


            // Now execute display commands.
            // First, apply any filters from regex
            // Second, apply any sort to the list
            // Third, apply any ordering algorith
            // Finally, Print according to specs

            List<String> matched_list = new ArrayList<String>();

            if (regex != null) {

                Pattern pattern = Pattern.compile(regex);

                Iterator list_it = list.entrySet().iterator();
                while (list_it.hasNext()) {
                    Map.Entry pair = (Map.Entry)list_it.next();
                    String key = (String)pair.getKey();

                    Matcher matcher = pattern.matcher(key);
                    if (matcher.find()) {
                        matched_list.add(key);
                    }

                }

            } else {
                Iterator list_it = list.entrySet().iterator();
                while (list_it.hasNext()) {
                    Map.Entry pair = (Map.Entry)list_it.next();
                    String key = (String)pair.getKey();
                    matched_list.add(key);
                }
            }

            Collections.sort(matched_list);
            /*
            // Apply sort algorithms to the matched algorithm
            Collections.sort(matched_list, new Comparator<String>(){
                public int compare(String s1, String s2) {
                    return s1.length() - s2.length();
                }
            });
            */

            int NAME_OFFSET = max_metric_length + 2;
            int TYPE_OFFSET = 10;
            int ROLE_OFFSET = 30;


            // Apply print algorithms
            StringBuilder sb = new StringBuilder();

            if (title) {             
                sb.append("NAME");
                for (int i = 0; i<NAME_OFFSET-4; i++) {
                    sb.append(" ");
                }

                if (type) {
                    sb.append("TYPE");
                    for (int i = 0; i<TYPE_OFFSET-4; i++) {
                        sb.append(" ");
                    }                    
                }

                if (description) {
                    //sb.append("Roles");
                    sb.append("DESCRIPTION\n\n");                    
                }
            }

            for (String s : matched_list) {
                HashMap<String, Object> map = (HashMap<String, Object>)list.get(s);

                sb.append(s);
                for (int i = 0; i<(NAME_OFFSET-s.length()); i++) {
                    sb.append(" ");
                }

                if (type) {
                    sb.append(map.get("Type"));
                    for (int i = 0; i<(TYPE_OFFSET-map.get("Type").toString().length()); i++) {
                        sb.append(" ");
                    }                    
                }

/*              sb.append(map.get("Roles"));
                for (int i = 0; i<(ROLE_OFFSET-map.get("Roles").toString().length()); i++) {
                    sb.append(" ");
                }
*/
                if (description) {
                    sb.append(map.get("Desc"));
                }
                
                sb.append("\n");
            }

            System.out.println(sb.toString());
        }
    }
}
