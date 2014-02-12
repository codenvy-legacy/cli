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

/**
 * Abstract class with simple implementation of each function
 *
 */ 
public abstract class AbstractCommand implements CommandInterface {

    public boolean hasSubCommands() {
        return false;
    }

    public boolean hasMandatoryParameters() {
        return false;
    }

    public String getCommandName(){
        return "fill_me_in";
    }

    public String getParentCommandName(){
        return "";
    }

    public String getUsageLongDescription() {
		return("INSERT LONG DESCRIPTION FOR HELP");
	}
    
    public String getUsageDescription() {
        return("INSERT USAGE DESCRIPTION FOR HELP");
    }

    public String getHelpDescription() {
        return("INSERT BASIC HELP DESCRIPTION FOR HELP");
    }

    public void execute() {
    	System.out.println("not yet implemented");
    }
}
