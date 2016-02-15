/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.kafka.runtime;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConnector implements Serializable {
    private static final long serialVersionUID = 1L;
    protected static final Logger trace = LoggerFactory.getLogger(KafkaConnector.class);
    
    static Logger getTrace() {
        return trace;
    }
}
