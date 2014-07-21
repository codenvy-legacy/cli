/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package com.codenvy.cli.command.builtin;

import org.apache.karaf.shell.commands.Argument;

/**
 * This command allows to use a given ID in its command to specify the project
 * @author Florent Benoit
 */
public abstract class ScopedIDCommand extends AbsCommand {

    @Argument(name = "id", description = "Specify the project ID to use", required = false, multiValued = false)
    private String projectShortId;


    /**
     * @return the scoped ID of this command
     */
    protected String getScopedProjectId() {
        return projectShortId;
    }

}
