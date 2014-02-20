package com.codenvy.cli;

import org.apache.commons.lang3.SystemUtils;
import com.beust.jcommander.*;
import com.codenvy.cli.*;
import java.lang.StringBuilder;
import java.util.*;

public class CodenvyCLI 
{

    static class CommandMap {
        String command_name;
        JCommander command_jc;
        List<CommandMap> subcommands;
        CommandMap parent_command;
        List<ParameterDescription> parameters;
        CommandInterface command_object;

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

    static class CommandMapHash extends HashMap<String, CommandMap> {
        CommandMap root_node = null;

        public void setRootNode(CommandMap root) {
            root.parent_command = null;
            root.subcommands = new ArrayList<CommandMap> ();
            jc = new JCommander(root.command_object);
            jc.setProgramName(root.command_name);
            root.command_jc = jc;
            root.parameters = jc.getParameters();

            root_node = root;
            put(root.command_name, root);
        }

        public CommandMap getRootNode() {
            return root_node;
        }

        public void addChild(CommandMap parent, CommandMap child) {
            child.parent_command = parent;
            child.subcommands = new ArrayList<CommandMap> ();
            parent.subcommands.add(child);

            parent.command_jc.addCommand(child.command_name, child.command_object);
            child.command_jc = parent.command_jc.getCommands().get(child.command_name);
            child.parameters = child.command_jc.getParameters();

            // Find the JCommander object.  Recursively loop
            put(child.command_name, child);
        }

    }

	private static final int PARAMETER_OFFSET = 25;
	private static final int COMMAND_OFFSET = 25;

	private static boolean bad_parameter = false;
	private static boolean bad_command = false;

    private static ServiceLoader<CommandInterface> cli_command_loader;
    private static Map<String, CommandMap> shadow;
    private static JCommander jc;
    private static Map<String, CommandMap> loaded;
    private static CommandMapHash command_map;


    public static void findAndAddChildrenCommands(CommandMap parent) {
        for (CommandInterface ci : cli_command_loader) {
            if ((ci.getParentCommandName() != null) && (ci.getParentCommandName() == parent.command_name)) {
                CommandMap child_cm = loaded.get(ci.getCommandName());
                command_map.addChild(parent, child_cm);
                findAndAddChildrenCommands(child_cm);
            }
        }
    }

    public static void main( String[] args ) {
   
        String version = System.getProperty("java.version");
        char minor = version.charAt(2);
        if(minor < '7') {
            throw new RuntimeException("JAVA 7 or higher is required.  Please upgrade your version of Java.");
        }

        cli_command_loader = ServiceLoader.load(CommandInterface.class);
        command_map = new CommandMapHash();
        loaded = new HashMap<String, CommandMap>();

        // Load all of the empty Command Objects
        for (CommandInterface ci : cli_command_loader) {
            CommandMap cm = new CommandMap();
            cm.command_name = ci.getCommandName();
            cm.command_object = ci;
            loaded.put(ci.getCommandName(), cm);

            if (ci.getParentCommandName() == null) {
                command_map.setRootNode(cm);
            }
        }

        // 1) Get root node.
        // 2) Find all of its children in loaded commands.
        // 3) Add the child to the parent
        // 4) Repeat for each child iteratively.
        findAndAddChildrenCommands(command_map.getRootNode());

        // Do the parse of the command line parameters.
        try {
 	       jc.parse(args);

 	    } catch (MissingCommandException e) {
 	    	bad_command = true;

  	    } catch (ParameterException e) {
 	    	bad_parameter = true;

 	    }

        // Determine which command was effectively loaded and should be the "centered" discussion point.
        // If you have a tiered set of commands: codenvy->remote->factory:create then JComander will 
        // have multiple parsed commands.  getParsedCommand() will return true for "remote" and also 
        // true if you get the remote JC and test "factory:create".  A simple iteration through to 
        // see which parsed commands are returned is not sufficient.  
        int depth = 0;
        String loaded_parsed_command = null;
        if (jc.getParsedCommand() == null) {
           loaded_parsed_command = "codenvy";
        } else {
            depth++;
            loaded_parsed_command = jc.getParsedCommand();
            if (command_map.get(loaded_parsed_command).command_object.hasSubCommands()) {
                String sub_parsed_command = jc.getCommands().get(loaded_parsed_command).getParsedCommand();
                if (sub_parsed_command != null) {
                    depth++;
                    loaded_parsed_command = sub_parsed_command;
                }
            }   
        }

        CommandMap parsed_command = command_map.get(loaded_parsed_command);

        // If there is a bad_command, then:
        // Check to see if the parameters passed are files.
        boolean valid_json_file = false;
        if (bad_command && (loaded_parsed_command.equals("codenvy")) && args.length >= 1) {
            CommandCLI object = (CommandCLI)parsed_command.command_object;
            valid_json_file = object.executeShortcut(args);
        }

        // If there are no arguments, or if the parsed command is null, or if they designated main help, then provide it.
        if ((args.length == 0) || (parsed_command.command_object.getHelp()) || (parsed_command.command_object.hasMandatoryParameters() && args.length == depth)) {
            showUsage(loaded_parsed_command); 
        }

        if (bad_command && !valid_json_file) {
            // Happens only if user attempts invalid command w/ no general parameters.
            System.out.println();
            System.out.println("########################################################################");
            System.out.println("#### You typed an invalid sub-command or passed in an invalid file. ####");
            System.out.println("########################################################################");
            System.out.println();

            showUsage(loaded_parsed_command);
        }

        if (bad_parameter) {
            System.out.println();
            System.out.println("#########################################");
            System.out.println("#### You typed an invalid parameter. ####");
            System.out.println("#########################################");

            showUsage(loaded_parsed_command);
        }

        // Execute the command
        parsed_command.command_object.execute();

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
        sb.append(command.command_object.getUsageLongDescription());
        sb.append("\n\n");
        sb.append(command.command_object.getUsageDescription());
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
        sb.append(command.command_object.getHelpDescription());
        System.out.println(sb.toString());
        System.exit(0);     

     }
}
