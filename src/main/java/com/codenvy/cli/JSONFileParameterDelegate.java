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

    @Parameter(names = "--in", description = ".json file with Codenvy project parameters to import")
    protected String inputFile;
    public String getInputFile() { return inputFile; }
    
    @Parameter(names = "--out", converter = FileConverter.class, description = "Writes loaded project parameters into designated file")
    protected File outputFile;
    public File getOutputFile() { return outputFile; }

    @Parameter(names = "--param", listConverter = JSONPairConverter.class, converter = JSONPairConverter.class, arity = 2, description = "Sets name/value pair separated by space")
    protected List<JSONPair> params = new ArrayList<JSONPair>();
    public List<JSONPair> getParams() { return params; }

    public JSONFileParameterDelegate() {
        super();
    }

    public JSONFileParameterDelegate(String i, File o, ArrayList<JSONPair> a) {
        inputFile = i;
        outputFile = o;
        params = a;
    }

    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("JSON DELEGATE: INPUT FILE : " + inputFile + "\n");
    	sb.append("JSON DELEGATE: OUTPUT FILE: " + outputFile  + "\n");
    	sb.append("JSON DELEGATE: PARAMS: " + params  + "\n");

    	return sb.toString();
    }

}

