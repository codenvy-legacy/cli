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

import com.codenvy.cli.command.builtin.util.ascii.AsciiArray;
import com.codenvy.cli.command.builtin.util.ascii.DefaultAsciiArray;
import com.codenvy.cli.command.builtin.util.ascii.FormatterMode;
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.preferences.PreferencesAPI;
import com.codenvy.client.Codenvy;
import com.codenvy.client.CodenvyAPI;
import com.codenvy.client.CodenvyClient;
import com.codenvy.client.model.Project;
import com.codenvy.client.model.Workspace;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.fusesource.jansi.Ansi;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;

import static com.codenvy.cli.command.builtin.Constants.CODENVY_CONFIG_FILE;
import static com.codenvy.cli.command.builtin.util.ascii.FormatterMode.MODERN;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 * Abstract command which should be extended by all Codenvy commands.
 * @author Florent Benoit
 */
public abstract class AbsCommand extends OsgiCommandSupport {

    /**
     * Codenvy settings read from its configuration file.
     */
    private Properties codenvySettings;

    /**
     * Codenvy client instance.
     */
    private CodenvyClient codenvyClient;

    /**
     * Manage environments that can be used.
     */
    private MultiEnvCodenvy multiEnvCodenvy;

    /**
     * Global preferences instance
     */
    private Preferences globalPreferences;

    @PostConstruct
    public void init() {
        // Do we have existing preferences ?
        this.globalPreferences = (Preferences) session.get(Preferences.class.getName());
        if (globalPreferences == null) {
            globalPreferences = PreferencesAPI.getPreferences(new File(Constants.PREFERENCES_STORE_FILE).toURI());
            session.put(Preferences.class.getName(), globalPreferences);
        }

        // Do we have multi env ?
        this.multiEnvCodenvy = (MultiEnvCodenvy)session.get(MultiEnvCodenvy.class.getName());
        if (multiEnvCodenvy == null) {
            // build a new one
            multiEnvCodenvy = new MultiEnvCodenvy(getCodenvyClient(), globalPreferences);
            session.put(MultiEnvCodenvy.class.getName(), multiEnvCodenvy);
        }
    }

    /**
     * @return multi environment
     */
    protected MultiEnvCodenvy getMultiEnvCodenvy() {
        return multiEnvCodenvy;
    }


    /**
     * Get a configuration property from the Codenvy configuration file stored in KARAF_HOME/etc folder.
     * @param property the name of the property
     * @return the value or null if not found
     */
    protected String getCodenvyProperty(String property) {
        // Load the settings if not yet loaded
        if (codenvySettings == null) {
            // load the codenvy setting
            try (Reader reader = new InputStreamReader(new FileInputStream(CODENVY_CONFIG_FILE), Charset.defaultCharset())) {
                codenvySettings = new Properties();
                codenvySettings.load(reader);
            } catch (IOException e) {
                System.out.println("Unable to load condenvy settings" + e.getMessage());
                throw new IllegalStateException("Unable to load codenvy settings", e);
            }
        }
        return codenvySettings.getProperty(property);
    }

    protected boolean checkifEnvironments() {
        if (!multiEnvCodenvy.hasEnvironments()) {
            System.out.println("There is no Codenvy environment. Manage environments with env command.");
        }


        return multiEnvCodenvy.hasEnvironments();
    }

    /**
     * @return the current workspace used at runtime
     */
    protected CodenvyClient getCodenvyClient() {
        if (codenvyClient == null) {
            codenvyClient = CodenvyAPI.getClient();
        }
        return codenvyClient;
    }

    /**
     * Defines the codenvy Client to use.
     */
    protected void setCodenvyClient(CodenvyClient codenvyClient) {
        this.codenvyClient = codenvyClient;
    }


    /**
     * @return the current formatter mode used at runtime
     */
    protected FormatterMode getFormatterMode() {
        FormatterMode formatterMode = (FormatterMode) session.get(FormatterMode.class.getName());
        if (formatterMode == null) {
            formatterMode = MODERN;
        }
        return formatterMode;
    }

    /**
     * Build a new Ascii array instance with the selected formatter mode
     * @return a new instance of the ascii array
     */
    protected AsciiArray buildAsciiArray() {
        return new DefaultAsciiArray().withFormatter(getFormatterMode());
    }


    /**
     * Defines the codenvy settings.
     * @param codenvySettings the settings that will replace the existing
     */
    protected void setCodenvySettings(Properties codenvySettings) {
        this.codenvySettings = codenvySettings;
    }



}
