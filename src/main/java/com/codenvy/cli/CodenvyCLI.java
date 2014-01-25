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

    private static ServiceLoader<CommandInterface> cli_command_loader = ServiceLoader.load(CommandInterface.class);

    public static void main( String[] args )
    {
  
        class CommandValue {
            CommandInterface command_object;
            String command_name;
            String parent_command_name;
            boolean hasSubCommands;
            boolean hasMandatoryParameters;

            public CommandValue (CommandInterface o, String a, String p, boolean b, boolean c ) {
                command_object = o;
                command_name = a;
                parent_command_name = p;
                hasSubCommands = b;
                hasMandatoryParameters = c;
            }

        }

        Map<String, CommandValue> command_config_map = new HashMap<String, CommandValue>();

        // STEP 1: Pull out all of the commands in the Service Loader.
        // STEP 2: Create a HashMap of the configuration options
        for (CommandInterface ci : cli_command_loader) {
            command_config_map.put(ci.getCommandName(), 
                                   new CommandValue(ci,
                                                    ci.getCommandName(),
                                                    ci.getParentCommandName(),
                                                    ci.hasSubCommands(),
                                                    ci.hasMandatoryParameters()));
        }

        CommandCLI cli = new CommandCLI();

        // Setup the JCommander objects for parsing.
        JCommander jc = new JCommander(cli);
        jc.setProgramName(cli.getCommandName());

/*
        CommandRemote remote = new CommandRemote();
        CommandAuth auth = new CommandAuth();

        CommandRemoteFactoryCreate remote_factory_create = new CommandRemoteFactoryCreate();
        CommandRemoteFactoryInvoke remote_factory_invoke = new CommandRemoteFactoryInvoke();
        CommandRemoteProjectInit remote_project_init = new CommandRemoteProjectInit();
        CommandRemoteProjectCreate remote_project_create = new CommandRemoteProjectCreate();
        CommandRemoteProjectOpen remote_project_open = new CommandRemoteProjectOpen();
        CommandRemoteWorkspaceList remote_workspace_list = new CommandRemoteWorkspaceList();

*/

        // Add each command to the JCommander object.
        // If the parent command is "codenvy", then it is a root command.
        // Otherwise, add the command to its parent command.
        for (CommandValue cv : command_config_map.values()) {
            if (cv.parent_command_name.equals("codenvy")) {
                jc.addCommand(cv.command_name, cv.command_object);

                if (cv.hasSubCommands) {

                    // After a first run through of tier 1 objects, then do tier 2
                    for (CommandValue cv2 : command_config_map.values()) {
                        if (cv2.parent_command_name.equals(cv.command_name)) {
                            jc.getCommands().get(cv.command_name).addCommand(cv2.command_name, cv2.command_object);
                        }
                    }
                }
            }
        }


/*
        jc.addCommand("auth", auth);
        jc.addCommand("remote", remote);

        jc.getCommands().get("remote").addCommand("factory:create", remote_factory_create);
        jc.getCommands().get("remote").addCommand("factory:invoke", remote_factory_invoke);
        jc.getCommands().get("remote").addCommand("proj:init", remote_project_init);
        jc.getCommands().get("remote").addCommand("proj:create", remote_project_create);
        jc.getCommands().get("remote").addCommand("proj:open", remote_project_open);
        jc.getCommands().get("remote").addCommand("ws:list", remote_workspace_list);

*/

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

        // If there are no arguments, or if the parsed command is null, or if they designated main help, then provide it.
        if ((args.length == 0) ||
            (jc.getParsedCommand() == null) ||
            (cli.getHelp())) {
                showUsage(jc, cli);
        } 

        // If there is a bad command provided, then also show help
        // Provide the right help.  If the root level bad command, then provide root level help.
        // If it's a bad subcommand, then provide subcommand help.
        if (bad_command) {
            // If the parsed command matches any key in the configuration map, then it's a subcommand.
            // Otherwise, it's a bad main level command.
            if (command_config_map.containsKey(jc.getParsedCommand())) {
                showUsage(jc, command_config_map.get(jc.getParsedCommand()).command_object);
            } else {
                showUsage(jc, cli);
            }
        }


       if (cli.getVersion()) {
       		showPrintVersion();
       }

/*
 	    // If no proper command, or help flag, or proper non-remote subcommand and bad command
    	if ((jc.getParsedCommand() == null) ||
    		cli.getHelp() ||
    		(jc.getParsedCommand() != "remote" && bad_command))
    			{ showUsage(jc, cli); }
*/


        // Execute each command, or show help if there is a more elaborate error.
        for (CommandValue cv : command_config_map.values()) {
            if (jc.getParsedCommand().equals(cv.command_name)) {

                // We have a match.
                // If the -h flag was set, then print help
                // If the command requires parameters and has none, then print help.
                if (cv.command_object.getHelp()) {
                    showUsage(jc, cv.command_object);
                }

                // Execute the command or show help.
                analyzeAndExecuteCommand(jc, cv.command_object);

                // If the command object has sub commands, then we need to perform the same function.
                if (cv.hasSubCommands) {

                    if (jc.getCommands().get(cv.command_name).getParsedCommand() == null) {
                        showUsage(jc, cv.command_object);
                    }


                    // After a first run through of tier 1 objects, then do tier 2
                    for (CommandValue cv2 : command_config_map.values()) {
                        if (cv2.parent_command_name.equals(cv.command_name) &&
                            jc.getCommands().get(cv.command_name).getParsedCommand().equals(cv2.command_name)) {
                                analyzeAndExecuteCommand(jc, cv2.command_object);
                        }
                    }
                }
            }
        }

/*

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
                    case "proj:create": analyzeAndExecuteCommand(remote_project_create, jc); break;
                    case "proj:open": analyzeAndExecuteCommand(remote_project_open, jc); break;
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

        */
     }

     private static void analyzeAndExecuteCommand(JCommander jc, CommandInterface obj) {
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
