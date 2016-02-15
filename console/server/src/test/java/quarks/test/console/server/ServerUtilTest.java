/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.console.server;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import quarks.console.server.ServerUtil;


public class ServerUtilTest {

    @Test
    public void testServerUtil() {
        ServerUtil serverUtil = new ServerUtil();
        assertNotNull("ServerUtil is null", serverUtil);
    }

    @Test
    public void testGetPath() throws IOException {
        ServerUtil serverUtil = new ServerUtil();
        assertNotNull("Get AbsoluteWarFilePath is null", serverUtil.getAbsoluteWarFilePath("console.war"));
    }

}
