package com.codenvy.cli;

import com.beust.jcommander.*;
import com.codenvy.cli.*;
import org.apache.commons.lang3.SystemUtils;
import java.io.IOException;
import java.lang.StringBuilder;
import java.net.URL;
import java.util.*;
import java.io.*;

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

    static class CommandMap {
        String command_name;
        JCommander command_jc;
        List<CommandMap> subcommands;
        CommandMap parent_command;
        List<ParameterDescription> parameters;
        CommandValue command_object;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Loaded Command:   " + command_name + "\n");
            if (parent_command != null) {
                sb.append("Parent:           " + parent_command.command_name + "\n");
            } else {
                sb.append("Parent:           " + "NONE" + "\n");
            }

            sb.append("Children:         " + subcommands + "\n");
            sb.append("Parameters:       " + parameters + "\n");
            return sb.toString();
        }
    }

	private static final int PARAMETER_OFFSET = 25;
	private static final int COMMAND_OFFSET = 29;

	private static boolean bad_parameter = false;
	private static boolean bad_command = false;

    private static ServiceLoader<CommandInterface> cli_command_loader;
    private static Map<String, CommandValue> command_config_map;
    private static Map<String, CommandMap> command_map;
    private static JCommander jc;

    // Load all of the commands dynamically through CI.
    // Create a simple holding object for some of the information needed later.
    static {
        cli_command_loader = ServiceLoader.load(CommandInterface.class);
        command_config_map = new HashMap<String, CommandValue>();
        command_map = new HashMap<String, CommandMap>();

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


    // Load the Parsed Command Linked Map
    public static void loadCommandMap(String command, JCommander jci) {
        CommandMap parsed_command_map = new CommandMap();        
        parsed_command_map.command_name = command;
        parsed_command_map.command_jc = jci;
        parsed_command_map.parent_command = null;
        parsed_command_map.command_object = command_config_map.get(command);
        parsed_command_map.subcommands = new ArrayList<CommandMap>();
        parsed_command_map.parameters = new ArrayList<ParameterDescription>();


        // Get all of the subcommands.
        for (CommandValue cv : command_config_map.values()) {
            if ((cv.parent_command_name != null) && cv.parent_command_name.equals(command)) {
                loadCommandMap(cv.command_name, jci.getCommands().get(cv.command_name));
            }
        }

        // Get all of the parameters for this command.
        parsed_command_map.parameters = jci.getParameters();
        command_map.put(parsed_command_map.command_name, parsed_command_map);
    }


    public static void main( String[] args ) {
   
        CommandCLI cli = new CommandCLI();

        // Setup the JCommander objects for parsing.
        jc = new JCommander(cli);
        jc.setProgramName(cli.getCommandName());


        // Add each command to the JCommander object.
        // If the parent command is "codenvy", then it is a root command.
        // Otherwise, add the command to its parent command.
        for (CommandValue cv : command_config_map.values()) {
            if ((cv.parent_command_name != null) && cv.parent_command_name.equals("codenvy")) {
                jc.addCommand(cv.command_name, cv.command_object);

                if (cv.hasSubCommands) {

                    // After a first run through of tier 1 objects, then do tier 2
                    // We are running through the same list
                    // If the new object's parent matches the name of the outer command, then we have an embedded match
                    for (CommandValue cv2 : command_config_map.values()) {
                        if ((cv2.parent_command_name != null) && cv2.parent_command_name.equals(cv.command_name)) {
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

        if (cli.getVersion()) {
            showPrintVersion();
        }

        // Loads properties of each command into hashmap.
        loadCommandMap("codenvy", jc);

        // After initial map created, then update parent and children links appropriately.
        for (CommandValue cv : command_config_map.values()) {
            if (cv.command_name != null) {
                CommandMap map = command_map.get(cv.command_name);
                map.parent_command = command_map.get(cv.parent_command_name);
                if (cv.hasSubCommands) {
                    for (CommandValue cv2 : command_config_map.values()) {
                        if ((cv2.command_name != null) && (cv2.parent_command_name != null) && cv2.parent_command_name.equals(cv.command_name)) {
                            map.subcommands.add(command_map.get(cv2.command_name));
                        }
                    }
                } 
                command_map.put(cv.command_name, map);
            }
        }

        // Determine which command was effectively loaded and should be the "centered" discussion point.
        String loaded_parsed_command = null;
        if (jc.getParsedCommand() == null) {
           loaded_parsed_command = "codenvy";
        } else {
            loaded_parsed_command = jc.getParsedCommand();
            if (command_map.get(loaded_parsed_command).command_object.hasSubCommands) {
                String sub_parsed_command = jc.getCommands().get(loaded_parsed_command).getParsedCommand();
                if (sub_parsed_command != null) {
                    loaded_parsed_command = sub_parsed_command;
                }
            }   
        }

        CommandMap parsed_command = command_map.get(loaded_parsed_command);
        int depth = 0;
        CommandMap loop_command = parsed_command;
        while (loop_command.parent_command != null) {
            depth++;
            loop_command = loop_command.parent_command;
        }

        // If there are no arguments, or if the parsed command is null, or if they designated main help, then provide it.
        if ((args.length == 0) ||
            (cli.getHelp()) ||
            bad_command ||
            bad_parameter ||
            parsed_command.command_object.command_object.getHelp() ||
            (parsed_command.command_object.command_object.hasMandatoryParameters() && args.length == depth)) {
              showUsage(loaded_parsed_command);
        }

        // Execute the command
        parsed_command.command_object.command_object.execute();

     }

     
     // Will show the pretty format usage.
     // Navigates JCommander object to determine what to print.
     // 1) Get long description from the command
     // 2) Get usage line syntax from the command
     // 3) Iterate through JCommander object to find any parameters, if any
     // 4) Iterate through JCommander object to find any subcommands
     // 5) Get additional help statement from the command
     private static void showUsage(String command_name) {

        StringBuilder sb = new StringBuilder();
        CommandMap command = command_map.get(command_name);
        sb.append(command.command_object.command_object.getUsageLongDescription());
        sb.append("\n\n");
        sb.append(command.command_object.command_object.getUsageDescription());
        sb.append("\n\n");

        // List all of the arguments, sorted for this command.    
        sb.append("Available arguments:\n");

        java.util.Collections.sort(command.parameters,
                                   new Comparator<ParameterDescription>() {
                                       public int compare(ParameterDescription p1, ParameterDescription p2) {
                                            int i = p1.getLongestName().compareTo(p2.getLongestName());
                                            return i; 
                                       }
                                   });

        for (ParameterDescription e : command.parameters) {
            sb.append("   " + e.getNames());

            // PARAMETER_OFFSET is for pretty printing
            for (int i = 0; i< (PARAMETER_OFFSET-e.getNames().length()-4); i++)
                sb.append(" ");

            sb.append(e.getDescription()+ "\n");
        }


        // Loop through all of the subcommands in our master map
        // Pull out the command name for each subcommand
        // Sort the resulting list
        if (command.subcommands.size() > 0) {        
            List<String> subcommand_list = new ArrayList<String>();
            for (CommandMap cm : command.subcommands) {
                subcommand_list.add(cm.command_name);
            }

            java.util.Collections.sort(subcommand_list);

            sb.append("\nAvailable subcommands:\n");
                
            for (String s : subcommand_list) {
                sb.append("   " + s);
                for (int i = 0; i < (COMMAND_OFFSET-s.length()-4); i++) {
                    sb.append(" ");
                }

                // Get the parent JCommander object, and then do a command lookup on the parent to get the right description
                sb.append(command_map.get(s).parent_command.command_jc.getCommandDescription(s) + "\n");
            }
        }

        sb.append("\n");
        sb.append(command.command_object.command_object.getHelpDescription());
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
     	System.out.println(SystemUtils.OS_NAME);
     	System.out.println(SystemUtils.OS_ARCH);
     	System.out.println(SystemUtils.OS_VERSION);
        System.out.println(SystemUtils.USER_HOME);

        URL url = null;
        try {
            Properties props = new Properties();
 
            // Get hold of the path to the properties file
            // (Maven will make sure it's on the class path)
            url = CodenvyCLI.class.getClassLoader().getResource("app.properties");
             
            // Load the file
            props.load(url.openStream());
             
            // Accessing values
            System.out.println(props.getProperty("application.version"));

        } catch (IOException e) {
            System.out.println("### Problem with gaining access to ClassLoader properties for this app. ###");
        } 

     	System.exit(0);
     }
}
