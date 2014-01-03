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

        CommandRemoteProjectFactory remote_project_factory = new CommandRemoteProjectFactory();
        CommandRemoteTmpWorkspaceCreate remote_tmp_workspace_create = new CommandRemoteTmpWorkspaceCreate();
        CommandRemoteWorkspaceList remote_workspace_list = new CommandRemoteWorkspaceList();


        jc.addCommand("auth", auth);
        jc.addCommand("remote", remote);

        jc.getCommands().get("remote").addCommand("project_factory", remote_project_factory);
        jc.getCommands().get("remote").addCommand("workspace_list", remote_workspace_list);
        jc.getCommands().get("remote").addCommand("tmp_workspace_create", remote_tmp_workspace_create);

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
 	    			case "project_factory": analyzeAndExecuteCommand(remote_project_factory, jc); break;
 	    			case "workspace_list": analyzeAndExecuteCommand(remote_workspace_list, jc); break;
 	    			case "tmp_workspace_create": analyzeAndExecuteCommand(remote_tmp_workspace_create, jc); break;
 	    		}
 	    	
 	    		break;
 	    	}

 	    	case "auth": analyzeAndExecuteCommand(auth, jc);
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
     	sb.append("Available arguments:\n");

     	JCommander command = jc;

     	if (level == 1)
     		command = map.get(parsedCommand);
     	
     	if (level == 2)
     		command = map.get(parsedCommand).getCommands().get(subParsedCommand);

     	// TODO: Would be nice to sort the list by alphabet.
     	List<ParameterDescription> list = command.getParameters();
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
     	System.out.println(CLI_VERSION);
     	System.out.println(SystemUtils.OS_NAME);
     	System.out.println(SystemUtils.OS_ARCH);
     	System.out.println(SystemUtils.OS_VERSION);

     	System.exit(0);
     }
}
