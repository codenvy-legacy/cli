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
import com.codenvy.cli.command.builtin.util.ascii.AsciiForm;
import com.codenvy.cli.command.builtin.util.ascii.DefaultAsciiArray;
import com.codenvy.cli.command.builtin.util.ascii.DefaultAsciiForm;
import com.codenvy.cli.command.builtin.util.ascii.FormatterMode;
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.preferences.PreferencesAPI;
import com.codenvy.client.CodenvyAPI;
import com.codenvy.client.CodenvyClient;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.karaf.shell.console.SessionProperties;
import org.fusesource.jansi.Ansi;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
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
     * Manage remotes that can be used.
     */
    private MultiRemoteCodenvy multiRemoteCodenvy;

    /**
     * Global preferences instance
     */
    private Preferences globalPreferences;

    @PostConstruct
    public void init() {
        // Do we have existing preferences ?
        this.globalPreferences = (Preferences)session.get(Preferences.class.getName());
        if (globalPreferences == null) {
            globalPreferences = PreferencesAPI.getPreferences(new File(Constants.PREFERENCES_STORE_FILE).toURI());
            session.put(Preferences.class.getName(), globalPreferences);
        }

        // Do we have multi env ?
        this.multiRemoteCodenvy = (MultiRemoteCodenvy)session.get(MultiRemoteCodenvy.class.getName());
        if (multiRemoteCodenvy == null) {
            // build a new one
            multiRemoteCodenvy = new MultiRemoteCodenvy(getCodenvyClient(), globalPreferences);
            session.put(MultiRemoteCodenvy.class.getName(), multiRemoteCodenvy);
        }

        session.put("PROGRESS_MONITOR", Boolean.TRUE);

    }

    /**
     * @return multi remote
     */
    protected MultiRemoteCodenvy getMultiRemoteCodenvy() {
        return multiRemoteCodenvy;
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
                System.out.println("Unable to load codenvy settings" + e.getMessage());
                throw new IllegalStateException("Unable to load codenvy settings", e);
            }
        }
        return codenvySettings.getProperty(property);
    }

    /**
     * Checks that there are available codenvy remotes
     * @return
     */
    protected boolean checkifAvailableRemotes() {
        if (!multiRemoteCodenvy.hasAvailableRemotes()) {
            System.out.println("There is no Codenvy remote. Manage remotes with remote command.");
            return false;
        }


        return multiRemoteCodenvy.hasAvailableRemotes();
    }
    /**
     * Checks that there are enabled codenvy remotes
     * @return
     */
    protected boolean checkifEnabledRemotes() {
        if (!multiRemoteCodenvy.hasAvailableRemotes()) {
            System.out.println("There is no Codenvy remote. Manage remotes with remote command.");
            return false;
        }

        if (!multiRemoteCodenvy.hasReadyRemotes()) {
            System.out.println("Please login to continue.");
            return false;
        }


        return multiRemoteCodenvy.hasReadyRemotes();
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
     * Build a new Ascii array instance with the selected formatter mode
     * @return a new instance of the ascii array
     */
    protected AsciiForm buildAsciiForm() {
        return new DefaultAsciiForm().withFormatter(getFormatterMode());
    }

    /**
     * Defines the codenvy settings.
     * @param codenvySettings the settings that will replace the existing
     */
    protected void setCodenvySettings(Properties codenvySettings) {
        this.codenvySettings = codenvySettings;
    }



    protected boolean isStackTraceEnabled() {
        Boolean val= (Boolean) session.get(SessionProperties.PRINT_STACK_TRACES);
        if (val != null && val.booleanValue()) {
            return true;
        }
        return false;
    }

    protected boolean isProgressEnabled() {
        Boolean val= (Boolean) session.get("PROGRESS_MONITOR");
        if (val != null && val.booleanValue()) {
            return true;
        }
        return false;
    }


    protected void openURL(String url) {
        if (!Desktop.getDesktop().isDesktopSupported()) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Unable to open the URL of the project '").a(url).a("' as this system is not supported.");
            buffer.reset();
            System.out.println(buffer.toString());
            return;
        }

        // open the default web browser for the HTML page
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Invalid URL of the project found: '").a(url).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return;
        }


        try {
            Desktop.getDesktop().browse(uri);
            Ansi buffer = Ansi.ansi();
            buffer.a("URL '").a(url).a("' has been opened in the web browser");
            System.out.println(buffer.toString());
        } catch (IOException e) {
            Ansi buffer = Ansi.ansi();
            buffer.fg(RED);
            buffer.a("Unable to open URL '").a(url).a("'.");
            buffer.reset();
            System.out.println(buffer.toString());
            return;
        }
    }
}
