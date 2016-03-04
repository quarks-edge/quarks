/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.tests.connectors.wsclient.javax.websocket;

import java.io.File;

public class KeystorePath {
    
    public static String getStorePath(String storeLeaf) {
        String path = System.getProperty("user.dir");
        // Under eclipse/junit: path to project in repo: <repo>/connectors
        // Under ant/junit: <repo>/connectors/<project>/unittests/testrunxxxxxxx
        if (!path.endsWith("/connectors")) {
            int indx = path.indexOf("/connectors/");
            indx += "/connectors/".length() - 1;
            path = path.substring(0, indx);
        }
        path += "/wsclient-javax.websocket/src/test/keystores/" + storeLeaf;
        if (!new File(path).exists())
            throw new IllegalArgumentException("File does not exist: "+path);
        return path;
    }

}
