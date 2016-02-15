/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.file.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConnector {
    @SuppressWarnings("unused")
    private static final FileConnector forCodeCoverage = new FileConnector();
    private static final Logger TRACER = LoggerFactory.getLogger(FileConnector.class);
    
    private FileConnector() {}
    
    public static Logger getTrace() {
        return TRACER;
    }

}
