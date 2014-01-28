package com.codenvy.cli;

import com.beust.jcommander.*;
import com.codenvy.cli.*;
import java.io.IOException;
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

	private static final int PARAMETER_OFFSET = 25;
	private static final int COMMAND_OFFSET = 29;

	private static boolean bad_parameter = false;
	private static boolean bad_command = false;

    private static ServiceLoader<CommandInterface> cli_command_loader;
    private static Map<String, CommandMap> command_map;
    private static JCommander jc;


    public static void main( String[] args ) {
   
        cli_command_loader = ServiceLoader.load(CommandInterface.class);
        command_map = new HashMap<String, CommandMap>();

        // Load the static command_map with objects.
        // It's essentially a tree set, but manually built up.
        // On this first pass, load each object, create its JCommander peer, and load them into the map
        // On the second pass, then discover the appropriate subcommands to create that list.
        for (CommandInterface ci : cli_command_loader) {
            if (ci.getParentCommandName() == null) {
                jc = new JCommander(ci);
                jc.setProgramName(ci.getCommandName());

                CommandMap map_object = new CommandMap();
                map_object.command_name = ci.getCommandName();
                map_object.subcommands = new ArrayList<CommandMap>();
                map_object.parent_command = null;
                map_object.parameters = jc.getParameters();
                map_object.command_jc = jc;
                map_object.command_object = ci;

                command_map.put(ci.getCommandName(), map_object);

                for (CommandInterface ci2 : cli_command_loader) {        
                     if ((ci2.getParentCommandName() != null) && (ci2.getParentCommandName().equals(ci.getCommandName()))) {
                        jc.addCommand(ci2.getCommandName(), ci2);

                        CommandMap map_object2 = new CommandMap();
                        map_object2.command_name = ci2.getCommandName();
                        map_object2.parent_command = map_object;
                        map_object2.subcommands = new ArrayList<CommandMap>();
                        map_object2.parameters = jc.getCommands().get(ci2.getCommandName()).getParameters();
                        map_object2.command_jc = jc.getCommands().get(ci2.getCommandName());
                        map_object2.command_object = ci2;

                        command_map.put(ci2.getCommandName(), map_object2);

                        if (ci2.hasSubCommands()) {
                            for (CommandInterface ci3 : cli_command_loader) {        
                                if ((ci3.getParentCommandName() != null) && (ci3.getParentCommandName().equals(ci2.getCommandName()))) {
                                    jc.getCommands().get(ci2.getCommandName()).addCommand(ci3.getCommandName(), ci3);

                                    CommandMap map_object3 = new CommandMap();
                                    map_object3.command_name = ci3.getCommandName();
                                    map_object3.parent_command = map_object2;
                                    map_object3.subcommands = new ArrayList<CommandMap>();
                                    map_object3.parameters = jc.getCommands().get(ci2.getCommandName()).getCommands().get(ci3.getCommandName()).getParameters();
                                    map_object3.command_jc = jc.getCommands().get(ci2.getCommandName()).getCommands().get(ci3.getCommandName());
                                    map_object3.command_object = ci3;

                                    command_map.put(ci3.getCommandName(), map_object3);
                                }
                            }
                        }
                    }
                }
            }
        }

        // After initial map created, then update parent and children links appropriately.
        // Second pass
        for (CommandInterface ci : cli_command_loader) {
            if (ci.getCommandName() != null) {
                CommandMap map = command_map.get(ci.getCommandName());
                if (ci.hasSubCommands()) {
                    for (CommandInterface ci2 : cli_command_loader) {
                        if ((ci2.getCommandName() != null) && (ci2.getParentCommandName() != null) && ci2.getParentCommandName().equals(ci.getCommandName())) {
                            map.subcommands.add(command_map.get(ci2.getCommandName()));
                        }
                    }
                } 
                command_map.put(ci.getCommandName(), map);
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

        // Determine which command was effectively loaded and should be the "centered" discussion point.
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

        // If there are no arguments, or if the parsed command is null, or if they designated main help, then provide it.
        if ((args.length == 0) ||
            bad_command ||
            bad_parameter ||
            parsed_command.command_object.getHelp() ||
            (parsed_command.command_object.hasMandatoryParameters() && args.length == depth)) {
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
