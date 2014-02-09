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

import java.io.File;
import java.util.List;
import java.util.ArrayList;


/**
 * A common set of parameters used across many commands.
 *
 */ 
public class JSONFileParameterDelegate {

    @Parameter(names = "--in", description = ".c5y JSON file with Codenvy project parameters to import.")
//    private File inputFile;
//    public File getInputFile() { return inputFile; }
    private String inputFile;
    public String getInputFile() { return inputFile; }
    
    @Parameter(names = "--out", converter = FileConverter.class, description = "If set, writes all loaded Codenvy project parameters into .c5y file")
    private File outputFile;
    public File getOutputFile() { return outputFile; }

    @Parameter(names = "--param", listConverter = JSONPairConverter.class, converter = JSONPairConverter.class, arity = 2, description = "Sets name/value pair.  First is name.  Second is value.  Can be used multiple times.")
    private List<JSONPair> params = new ArrayList<JSONPair>();
    public List<JSONPair> getParams() { return params; }

}

