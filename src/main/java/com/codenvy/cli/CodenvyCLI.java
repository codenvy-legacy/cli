package com.codenvy.cli;

import com.beust.jcommander.*;
import com.codenvy.cli.*;
import org.apache.commons.lang3.SystemUtils;
import java.lang.StringBuilder;
import java.util.*;

public class CodenvyCLI 
{

   static class CommandValue {
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


	private static double CLI_VERSION = 0.1;

	private static final int PARAMETER_OFFSET = 25;
	private static final int COMMAND_OFFSET = 29;

	private static boolean bad_parameter = false;
	private static boolean bad_command = false;

    private static ServiceLoader<CommandInterface> cli_command_loader;
    private static Map<String, CommandValue> command_config_map;

    static {
        cli_command_loader = ServiceLoader.load(CommandInterface.class);
        command_config_map = new HashMap<String, CommandValue>();

        // STEP 1: Pull out all of the commands in the Service Loader.
        // STEP 2: Create a HashMap of each loaded command & its configuration options
        for (CommandInterface ci : cli_command_loader) {
            command_config_map.put(ci.getCommandName(), 
                                   new CommandValue(ci,
                                                    ci.getCommandName(),
                                                    ci.getParentCommandName(),
                                                    ci.hasSubCommands(),
                                                    ci.hasMandatoryParameters()));
        }
    }


    public static void main( String[] args ) {
 
        CommandCLI cli = new CommandCLI();

        // Setup the JCommander objects for parsing.
        JCommander jc = new JCommander(cli);
        jc.setProgramName(cli.getCommandName());


        // Add each command to the JCommander object.
        // If the parent command is "codenvy", then it is a root command.
        // Otherwise, add the command to its parent command.
        for (CommandValue cv : command_config_map.values()) {
            if (cv.parent_command_name.equals("codenvy")) {
                jc.addCommand(cv.command_name, cv.command_object);

                if (cv.hasSubCommands) {

                    // After a first run through of tier 1 objects, then do tier 2
                    // We are running through the same list
                    // If the new object's parent matches the name of the outer command, then we have an embedded match
                    for (CommandValue cv2 : command_config_map.values()) {
                        if (cv2.parent_command_name.equals(cv.command_name)) {
                            jc.getCommands().get(cv.command_name).addCommand(cv2.command_name, cv2.command_object);
                        }
                    }
                }
            }
        }


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


        // Execute each command, or show help if there is a more elaborate error.
        for (CommandValue cv : command_config_map.values()) {
            if (jc.getParsedCommand().equals(cv.command_name)) {

                // We have a match.
                // If the help flag was explicitly set, then print help.
                // If the command requires parameters and has none, then print help.
                // If there was a bad parameter & there is no sub parsed command, then print help
                if (cv.command_object.getHelp() || 
                   (cv.hasMandatoryParameters && (args.length == 1)) ||
                   (bad_parameter && jc.getCommands().get(cv.command_name).getParsedCommand() == null)) {
                        showUsage(jc, cv.command_object);
                }

                // Execute the command
                cv.command_object.execute();

                // If the command object has sub commands, then we need to perform the same function.
                if (cv.hasSubCommands) {

                    if (jc.getCommands().get(cv.command_name).getParsedCommand() == null) {
                        showUsage(jc, cv.command_object);
                    }


                    // After a first run through of tier 1 objects, then do tier 2
                    for (CommandValue cv2 : command_config_map.values()) {
                        if (cv2.parent_command_name.equals(cv.command_name) &&
                            jc.getCommands().get(cv.command_name).getParsedCommand().equals(cv2.command_name)) {

                                // We have a match on the subcommand.
                                // If there was a bad parameter, then print help.
                                // If there was a help flag set, then print help.
                                // If the command requires a parameter and there is none, then print help.
                                if (cv2.command_object.getHelp() ||
                                   (cv2.hasMandatoryParameters && (args.length == 2)) ||
                                    bad_parameter) {
                                        showUsage(jc, cv2.command_object);
                                }

                                cv2.command_object.execute();
                        }
                    }
                }
            }
        }
     }

     
     // Will show the pretty format usage.
     // Navigates JCommander object to determine what to print.
     // 1) Get long description from the command
     // 2) Get usage line syntax from the command
     // 3) Iterate through JCommander object to find any parameters, if any
     // 4) Iterate through JCommander object to find any subcommands
     // 5) Get additional help statement from the command
     private static void showUsage(JCommander jc, CommandInterface cci) {
     	
     	Map<String, JCommander> map = jc.getCommands();
     	String parsed_command = jc.getParsedCommand();
     	String sub_parsed_command = null;

     	// The level of commands parsed.
     	// Level 0 = no commands passed in.
     	// Level 1 = remote, auth, install_simple, etc.
     	// Level 2 = any valid remote subcommand
 		int level = 0;
     	if (parsed_command != null) {
     		level++;

            for (CommandValue cv : command_config_map.values()) {
                if (parsed_command.equals(cv.command_name)) {
                    if (cv.hasSubCommands) {
                        sub_parsed_command = jc.getCommands().get(cv.command_name).getParsedCommand();
                        if (sub_parsed_command != null) {
                            level++;
                        }
                    }
                }
            }
     	}


     	StringBuilder sb = new StringBuilder();
		sb.append(cci.getUsageLongDescription());
		sb.append("\n\n");
        sb.append(cci.getUsageDescription());
     	sb.append("\n\n");
    
        // Display the available arguments for this command.
        // Step 1: Get the right command object.
        // Step 2: Get a List of all the parameters for this object.
        // Step 3: Sort the list alphabetically by name.
        // Step 4: List the parameters in pretty print format.
     	sb.append("Available arguments:\n");

     	JCommander command = jc;

     	if (level == 1)
     		command = map.get(parsed_command);
     	
     	if (level == 2)
     		command = map.get(parsed_command).getCommands().get(sub_parsed_command);

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

            // PARAMETER_OFFSET is for pretty printing
     		for (int i = 0; i< (PARAMETER_OFFSET-e.getNames().length()-4); i++)
     			sb.append(" ");

     		sb.append(e.getDescription()+ "\n");
     	}


     	// Display the available subcommands, if any.
        // If level 0, then there are definitely subcommands, we will use the main map.
        // If level 1, then we need to get a sub_map of JCommander objects.
        Map<String, JCommander> sub_map = map;
        boolean print_subcommands = false;

        if (level == 0)
            print_subcommands = true;

        if (parsed_command != null) {
            for (CommandValue cv : command_config_map.values()) {
                if (parsed_command.equals(cv.command_name)) {
                    if (cv.hasSubCommands && (level == 1)) {
                        print_subcommands = true;
                        sub_map = map.get(parsed_command).getCommands();
                    }
                }
            }
        }

        if (print_subcommands) {
            // Sort the sub_map
            List<String> subcommand_list = new ArrayList<String>(sub_map.keySet());
            java.util.Collections.sort(subcommand_list);

            sb.append("\nAvailable subcommands:\n");
                
            for (String s : subcommand_list) {
                sb.append("   " + s);
                for (int i = 0; i < (COMMAND_OFFSET-s.length()-4); i++)
                    sb.append(" ");

                // Tricky.  Not obvious answer here.
                // If this is the main command, use the main JCommander object.
                // If this is a remote subcommand, then get the remote JCommander object
                JCommander obj = jc;
                if (parsed_command != null) {
                    for (CommandValue cv : command_config_map.values()) {
                        if (parsed_command.equals(cv.command_name)) {
                            if (cv.hasSubCommands) {
                                obj = map.get(parsed_command);
                            }
                        }
                    }
                }

                sb.append(obj.getCommandDescription(s) + "\n");

            }
        }

        sb.append("\n");
        sb.append(cci.getHelpDescription());
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
