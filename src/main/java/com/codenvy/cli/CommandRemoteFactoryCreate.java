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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * codenvy remote factory:create command and parameters.
 *
 */ 
@Parameters(separators = " ", commandDescription = "Packages a Codenvy project into a .c5y file or Factory URL")
public class CommandRemoteFactoryCreate implements CommandInterface {

    @Parameter(names = { "-h", "--help" }, description = "Prints this help")
	private boolean help;
    public boolean getHelp() { return help; }

    @Parameter(names = "--launch", description = "If set, will launch a browser session with the URL")
    private boolean launch = false;
    public void setLaunch(boolean l) { launch = l; }

    @Parameter(names = "--encoded", description = "If set, will create hashed Codenvy URL")
    private boolean encoded = false;
    public void setEncoded(boolean e) { encoded = e; }
    
    @Parameter(names = "--rel", description = "Returns a specific Factory reference")
    private String rel = "create-project";
    public void setRel(String s) { rel = s; }

    @ParametersDelegate
	private JSONFileParameterDelegate json_delegate = new JSONFileParameterDelegate();
    public void setJSONDelegate(JSONFileParameterDelegate j) { json_delegate = j; }

    @ParametersDelegate
	private CLIAuthParameterDelegate delegate = new CLIAuthParameterDelegate();
    public void setDelegate(CLIAuthParameterDelegate d) { delegate = d; }

    public boolean hasSubCommands() {
        return false;
    }

    public boolean hasMandatoryParameters() {
        return false;
    }

    public String getCommandName(){
        return "factory:create";
    }

    public String getParentCommandName(){
        return "remote";
    }

    public String getUsageLongDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Generates a Codenvy Factory.  A Codenvy Factory is a URL that, when invoked, creates\n");
		sb.append("a cloned project loaded into a temporary developer environment called a workspace.\n");
		sb.append("Spec is: http://docs.codenvy.com/user/creating-factories/factory-parameter-reference/\n");
		sb.append("\n");
		sb.append("This command will locate the Factory configuration parameters used to generate the\n");
		sb.append("Factory.  If you are building a non-encoded Factory, an internal algorithm to\n");
        sb.append("convert the parameters to a query string is used.  If you are building an encoded\n");
        sb.append("Factory then a Codenvy REST API will be called to generate the URL with the config\n");
        sb.append("parameters.  The configuration parameters can be passed in via CLI or input file.\n");  
		sb.append("Precedence of config parameter loading is '--param' CLI and then --in file.\n");
        sb.append("Files passed with the --in parameter can be local files or downloaded via URL.\n");
		sb.append("If --out is specified, the JSON objects will be written to the specified file.\n");
		sb.append("\n");
        sb.append("If you use --param on the CLI, nested JSON objects have their keys structured as\n");
        sb.append("'key.inner_key=inner_value'.  For example, you may need to do the following:\n");
        sb.append("'--param projectattributes.ptype Java'\n");
        sb.append("\n");
		sb.append("If --encoded is specified, a URL with a hashed code for all of the parameters will be\n");
		sb.append("generated.  You need a valid API token to generate encoded URLs.  Non-encoded URLs\n");
		sb.append("can be generated without a token.  Non-encoded URLs are generated automatically\n");
		sb.append("without making any REST calls to a Codenvy provider.\n");
		sb.append("\n");
        sb.append("If --rel is specified with --encoded, then will return the URL object for the rel\n");
        sb.append("specified.  The default 'rel' is 'create-project' which returns a Factory URL that\n");
        sb.append("can be used by any user.  Codenvy stores images, scripts and other assets tied to\n");
        sb.append("an encoded factory.  This parameter returns different results.  Valid values include\n");
        sb.append("'image', 'self', 'accepted', 'snippet/html', 'snippet/markdown', and 'snippet/url'.\n");
        sb.append("\n");
		sb.append("If '--launch' is specified, then we will launch that new URL into a new Browser\n");
		sb.append("session using the preferred local browser.  You can also use 'codenvy remote\n");
		sb.append("factory:invoke' to launch the URL.\n");
        sb.append("\n");
        sb.append("The short form 'codenvy [file]' is the same as 'codenvy remote factory:create\n");
        sb.append("--in [file]'.  The short form can use --launch and --encoded.");			
		sb.append("\n\n\n");
        sb.append("Example: Generate encoded Factory URL with JSON parameters from input file.\n");
        sb.append("  codenvy remote factory:create --encoded --in \\myfiles\\angular.json\n");
		sb.append("\n");
        sb.append("  The angular.json file:\n");
		sb.append("\n");
        sb.append("  {\n");
		sb.append("     \"v\":\"1.1\",\n");
		sb.append("     \"vcs\":\"git\",\n");
		sb.append("     \"vcsurl\":\"http://codenvy.com/git/04/0f/7f/workspacegcpv6cdxy1q34n1i/Sample-AngularJS\",\n");
		sb.append("     \"idcommit\":\"37a21ef422e7995cbab615431f0f63991a9b314a\",\n");
		sb.append("     \"action\":\"openproject\",\n");
		sb.append("     \"projectattributes\":{\n");
		sb.append("        \"pname\":\"SpringDemo\",\n");
		sb.append("        \"ptype\":\"JavaScript\"\n");
		sb.append("     }\n");
		sb.append("  }\n");
        sb.append("\n");
        sb.append("Example: Generate encoded Factory with JSON parameters passed on command line.\n");
        sb.append("  codenvy remote factory:create --encoded --param v 1.1\n");
        sb.append("          --param projectattributes.pname Sample-Android\n");
        sb.append("          --param projectattributes.ptype Android\n");
        sb.append("          --param vcs git\n");
        sb.append("          --param vcsurl http://codenvy.com/git/04/0f/7f/workspacegcpv6cdxy1q34n1i/Sample-Android\n");
        sb.append("          --param idcommit 2e0a2ca39856d8f5a34c32b2838918a07427ce49\n");
        sb.append("          --param action openproject\n");
        sb.append("          --param openfile AndroidManifest.xml\n");
        sb.append("\n");

		return sb.toString();
	}

    public String getUsageDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: codenvy remote factory:create [<args>]");
        return sb.toString();
    }

    public String getHelpDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        return sb.toString();
    }

	// Create a valid Factory URL with a set of parameters.
	// 1) Load (override) any parameters in file specified by --in
	// 2) Load (override) any parameters passed in by --param
	// 3a) If !encoded, then generate a non-encoded URL through string
	// 3b) If encoded, then call REST API to generate Factory URL
	// 4) If --out, then take the resulting URL and output parameters to specified file
	// 5) If --launch, then open a browser session with the new URL
    public void execute() {

    	// The default Codenvy Factory reference.
    	final String default_reference = "/factory?";

    	// Internal JSON Object to store / override parameters.
    	JSONObject factory_params = new JSONObject();

    	// The dynamically generated Factory URL
    	StringBuilder factory_url = new StringBuilder();


        // Found bug.  Need to get credentials whether this is encoded or not.
        // If non-encoded, the URL of the system to call must come from Provider.
        CLICredentials cred = CommandAuth.getCredentials(delegate.getProfile(),
                                                         delegate.getProvider(),
                                                         delegate.getUser(),
                                                         delegate.getPassword(),
                                                         delegate.getToken());

    	/*
    	 * STEP 1-2: Load any JSON parameters from default.c5y, file specified by --in, or parameters.
    	 */
    	factory_params = JSONFileHelper.loadJSONFromFileAndParams(json_delegate);

    	
    	/* 
    	 * STEP 3: Generate a valid Factory URL.
    	 *         If not encoded, then build manually.
    	 *         If --encoded, then call REST API to generate URL.
    	 */ 
    	if (!encoded) {

	    	factory_url.append(cred.getProvider() + default_reference);
            factory_url.append(createJSONParamSet("", factory_params));  

	    } else {
   		
		    JSONObject api_return_data = null;
	        JSONObject api_input_data = factory_params;

    		// Format the appropriate input data for this REST command.
    		// Pass the input data into helper command, invoke REST command, and get response.
    		// Parse the response appropriately.
	        api_return_data = RESTAPIHelper.callRESTAPIAndRetrieveResponse(cred, api_input_data, RESTAPIHelper.REST_API_FACTORY_MULTI_PART);

    		if (api_return_data == null)
        		factory_url.append("");
    		else {
        		JSONArray list_of_urls = (JSONArray) api_return_data.get("links");
				Iterator<JSONObject> it = list_of_urls.iterator();
			
				while (it.hasNext()) {

					JSONObject link = (JSONObject) it.next();
					if (link.get("rel").equals(rel)) {
						factory_url.append(link.get("href"));
					}

				}
    		}
	    }
		

		/* 
    	 * STEP 4: Generate an output file with the contents if outputFile is set.
    	 *         Always output the Factory URL to the console.
    	 */ 
   		JSONFileHelper.writeJSONFile(json_delegate.getOutputFile(), factory_params);
	    

		/* 
    	 * STEP 5: If launch is set, call the native browser with new URL
    	 */ 
    	if (launch) {
	        BrowserHelper.launchNativeBrowserSession(factory_url.toString());
		}

	    System.out.println(factory_url.toString());

    }

    // Returns a new HTML Query String that matches the JSON object.
    // Recursively parses inner JSON objects to create format of key.inner_key = inner_value.
    // We did this to simplify the non-encoded URL format.
    public static String createJSONParamSet(String in_key, JSONObject in_json) {

        StringBuilder sb = new StringBuilder();

        Iterator json_iterator = in_json.entrySet().iterator();
        while (json_iterator.hasNext()) {
            Map.Entry pairs = (Map.Entry)json_iterator.next();

            // If the key / value pair is a JSON combination, then we have special creator.
            String key = pairs.getKey().toString();
            String value = pairs.getValue().toString();

            if (value.charAt(0) == '{') {
                // Convert the value into a JSON object.

                JSONObject inner = null;
                try{
                    inner = (JSONObject) new JSONParser().parse(value);
                } catch (ParseException e) {
                    System.out.println("################################################################");
                    System.out.println("### We found an error in your JSON formatting.               ###");
                    System.out.println("### This error occurs on an inner JSON string in your file.  ###");
                    System.out.println("################################################################");
                }

                sb.append(createJSONParamSet(key, inner));
            } else {

                sb.append(in_key);
                if (in_key != "") sb.append(".");
                sb.append(key);
                sb.append("=");
                sb.append(URLEncoder.encode(value));

            }

            if (json_iterator.hasNext())
                sb.append("&");
        }

        return sb.toString();
    }
}


