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

import java.io.File;

/**
 * Define some constants used in the commands.
 *
 * @author Florent Benoit
 * @author Stéphane Daviet
 */
public class Constants {

    /**
     * CLI metadata filename
     */
    public static final String METADATA_FILENAME = "cli";

    /**
     * Codenvy foldername.
     */
    public static final String CODENVY_FOLDERNAME = ".codenvy";


    /**
     * Path to the configuration file of Codenvy.
     */
    public static final String CODENVY_CONFIG_FILE =
            System.getProperty("karaf.home") + File.separatorChar + "etc" + File.separatorChar
            + "codenvy.cfg";

    /**
     * Path to the configuration file of Codenvy.
     */
    public static final String PREFERENCES_STORE_FILE =
            System.getProperty("karaf.home") + File.separatorChar + "etc" + File.separatorChar
            + "preferences.json";

    /**
     * Define the <b>host</b> property which is defined in the codenvy configuration file.
     */
    public static final String HOST_PROPERTY = "host";

    /**
     * Define the <b>username</b> property which is defined in the codenvy configuration file.
     */
    public static final String USERNAME_PROPERTY = "username";


    /**
     * Default project type for creating projects.
     */
    public static final String DEFAULT_CREATE_PROJECT_TYPE = "maven";
}
