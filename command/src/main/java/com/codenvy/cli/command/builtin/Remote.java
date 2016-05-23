/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.command.builtin;

/**
 * @author Florent Benoit
 */
public class Remote {

    private boolean defaultRemote = false;
    public String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDefaultRemote() {
        return defaultRemote;
    }

    public void setDefaultRemote(boolean defaultRemote) {
        this.defaultRemote = defaultRemote;
    }
}
