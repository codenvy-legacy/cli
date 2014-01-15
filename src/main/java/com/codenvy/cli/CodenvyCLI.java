package com.codenvy.cli;

import com.beust.jcommander.*;
import com.codenvy.cli.*;
import org.apache.commons.lang3.SystemUtils;
import java.lang.StringBuilder;
import java.util.*;

public class CodenvyCLI 
{
	private static double CLI_VERSION = 0.1;

	private static final int PARAMETER_OFFSET = 25;
	private static final int COMMAND_OFFSET = 29;

	private static boolean bad_parameter = false;
	private static boolean bad_command = false;

    public static void main( String[] args )
    {
  
        CodenvyCLIParameters cli = new CodenvyCLIParameters();

        // Setup the JCommander objects for parsing.
        JCommander jc = new JCommander(cli);
        jc.setProgramName("codenvy");

        CommandRemote remote = new CommandRemote();
        CommandAuth auth = new CommandAuth();

        CommandRemoteFactoryCreate remote_factory_create = new CommandRemoteFactoryCreate();
        CommandRemoteFactoryInvoke remote_factory_invoke = new CommandRemoteFactoryInvoke();
        CommandRemoteProjectInit remote_project_init = new CommandRemoteProjectInit();
        CommandRemoteWorkspaceList remote_workspace_list = new CommandRemoteWorkspaceList();


        jc.addCommand("auth", auth);
        jc.addCommand("remote", remote);

        jc.getCommands().get("remote").addCommand("factory:create", remote_factory_create);
        jc.getCommands().get("remote").addCommand("factory:invoke", remote_factory_invoke);
        jc.getCommands().get("remote").addCommand("proj:init", remote_project_init);
        jc.getCommands().get("remote").addCommand("ws:list", remote_workspace_list);

        // Do the parse of the command line parameters.
        try {
 	       jc.parse(args);

 	    } catch (MissingCommandException e) {
 	    	// Happens only if user attempts invalid command w/ no general parameters.
 	    	System.out.println();
 	    	System.out.println("###########################################");
 	    	System.out.println("#### You typed an invalid sub-command. ####");
 	    	System.out.println("###########################################");

 	    	bad_command = true;

  	    } catch (ParameterException e) {
 	    	System.out.println();
 	    	System.out.println("#########################################");
 	    	System.out.println("#### You typed an invalid parameter. ####");
 	    	System.out.println("#########################################");

 	    	bad_parameter = true;

 	    }

       if (args.length == 0) 
        	showUsage(jc, cli);


       if (cli.getVersion()) {
       		showPrintVersion();
       }


 	    // If no proper command, or help flag, or proper non-remote subcommand and bad command
    	if ((jc.getParsedCommand() == null) ||
    		cli.getHelp() ||
    		(jc.getParsedCommand() != "remote" && bad_command))
    			{ showUsage(jc, cli); }

    	// We have a valid first command.
    	// If remote & no 2nd or improper 2nd command, then print remote help.
    	// If bad parameters, print command-specific help.
    	// Otherwise, valid command - execute it.
    	switch (jc.getParsedCommand()) {
 	    	case "remote": {
 	    	
 	    		if (jc.getCommands().get("remote").getParsedCommand() == null ||
 	    			remote.getHelp() ||
 	    			bad_command) { 
 	    				showUsage(jc, remote);
 	    			}

 	    		// We only get here if proper 2nd command exists.
 	    		switch (jc.getCommands().get("remote").getParsedCommand()) {
 	    			case "factory:create": analyzeAndExecuteCommand(remote_factory_create, jc); break;
                    case "factory:invoke": analyzeAndExecuteCommand(remote_factory_invoke, jc); break;
                    case "proj:init": analyzeAndExecuteCommand(remote_project_init, jc); break;
 	    			case "ws:list": analyzeAndExecuteCommand(remote_workspace_list, jc); break;
 	    		}
 	    	
 	    		break;
 	    	}

 	    	case "auth": {

                // For this command, if no parameters, then diplay Usage.  
                // We should probably set the parameter to true as an alternative way.
                if (args.length == 1) {
                    showUsage(jc, auth);
                } else {
                    analyzeAndExecuteCommand(auth, jc);
                }

                break;
            }
 	    }
     }

     private static void analyzeAndExecuteCommand(CommandInterface obj, JCommander jc) {
   		if (obj.getHelp() || bad_parameter) {
   			showUsage(jc, obj);
   		} else {
   			obj.execute(); 
   		}
     }

     // Will show the pretty format usage.
     // Navigates JCommander object to determine what to print.
     // All help comments are embedded with JCommander properties.
     // 1) Usage
     // 2) Parameters for this command available.
     // 3) Available subcommands, if any
     // 4) How to get help on a subcommand, if any
     private static void showUsage(JCommander jc, CommandInterface cci) {
     	
     	Map<String, JCommander> map = jc.getCommands();
     	String parsedCommand = jc.getParsedCommand();
     	String subParsedCommand = null;

     	// The level of commands parsed.
     	// Level 0 = no commands passed in.
     	// Level 1 = remote, auth, install_simple, etc.
     	// Level 2 = any valid remote subcommand
 		int level = 0;
     	if (parsedCommand != null) {
     		level++;
     		subParsedCommand = jc.getCommands().get("remote").getParsedCommand();
     		if (subParsedCommand != null)
     			level++;
     	}

     	StringBuilder sb = new StringBuilder();
     	sb.append("\n");
		sb.append(cci.getUsageLongDescription());
		sb.append("\n\n");
     	sb.append("Usage: codenvy ");


     	// Print out the primary usage line.
     	// Include the context sensitive commands detected.

     	if (level == 0) {
     		sb.append("[<subcommand>] ");
   
     	} else if (level == 1) {
     		sb.append(parsedCommand + " ");
     		if (parsedCommand == "remote") sb.append("[<subcommand>] ");

     	} else {

     		sb.append("remote " + subParsedCommand + " ");
       	}

     	sb.append("[<args>]");
     	sb.append("\n\n");
    
        // Display the available arguments for this command.
        // Step 1: Get the right command object.
        // Step 2: Get a List of all the parameters for this object.
        // Step 3: Sort the list alphabetically by name.
        // Step 4: List the parameters in pretty print format.
     	sb.append("Available arguments:\n");

     	JCommander command = jc;

     	if (level == 1)
     		command = map.get(parsedCommand);
     	
     	if (level == 2)
     		command = map.get(parsedCommand).getCommands().get(subParsedCommand);

     	List<ParameterDescription> list = command.getParameters();

        java.util.Collections.sort(list,
                                   new Comparator<ParameterDescription>() {
                                       public int compare(ParameterDescription p1, ParameterDescription p2) {
                                            int i = p1.getLongestName().compareTo(p2.getLongestName());
                                            return i; 
                                       }
                                   });

     	for (ParameterDescription e : list) {
     		sb.append("   " + e.getNames());

     		for (int i = 0; i< (PARAMETER_OFFSET-e.getNames().length()-4); i++)
     			sb.append(" ");

     		sb.append(e.getDescription()+ "\n");
     	}


     	// Display the available subcommands, if any.
       	Map<String, JCommander> sub_map = map;

     	if (parsedCommand == "remote") 
     		sub_map = map.get(parsedCommand).getCommands();

     	if (level == 0 || (level == 1 && parsedCommand == "remote")) {
     		sb.append("\nAvailable subcommands:\n");
     		
     		for (Map.Entry<String,JCommander> entry : sub_map.entrySet()) {
     			sb.append("   " + entry.getKey());
     			for (int i = 0; i < (COMMAND_OFFSET-entry.getKey().length()-4); i++)
     				sb.append(" ");

     			// Tricky.  Not obvious answer here.
     			// If this is the main command, use the main JCommander object.
     			// If this is a remote subcommand, then get the remote JCommander object
     			JCommander obj = jc;
     			if (parsedCommand == "remote") 
     				obj = map.get("remote");

     			sb.append(obj.getCommandDescription(entry.getKey()) + "\n");
     		}

     		sb.append("\nFor help on a subcommand run 'codenvy ");
     		if (parsedCommand == "remote") sb.append("remote ");
     		sb.append("COMMAND --help'\n");
     	}
     	
     	System.out.println(sb.toString());
    	System.exit(0); 	

     }

     private static void showPrintVersion() {
        System.out.println(" _____           _                       ");
        System.out.println("/  __ \\         | |                      ");
        System.out.println("| /  \\/ ___   __| | ___ _ ____   ___   _ ");
        System.out.println("| |    / _ \\ / _` |/ _ \\ '_ \\ \\ / / | | |");
        System.out.println("| \\__/\\ (_) | (_| |  __/ | | \\ V /| |_| |");
        System.out.println(" \\____/\\___/ \\__,_|\\___|_| |_|\\_/  \\__, |");
        System.out.println("                                    __/ |");
        System.out.println("                                   |___/ ");
     	System.out.println(CLI_VERSION);
     	System.out.println(SystemUtils.OS_NAME);
     	System.out.println(SystemUtils.OS_ARCH);
     	System.out.println(SystemUtils.OS_VERSION);
        System.out.println(SystemUtils.USER_HOME);

     	System.exit(0);
     }
}
