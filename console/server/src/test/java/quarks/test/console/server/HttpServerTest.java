/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.console.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import quarks.console.server.HttpServer;

public class HttpServerTest {

    @Test
    public void testGetInstance() throws IOException {
        HttpServer myHttpServer = HttpServer.getInstance();
        assertNotNull("HttpServer getInstance is null", myHttpServer);
    }

    @Test
    public void startServer() throws Exception {
        HttpServer myHttpServer = HttpServer.getInstance();
        myHttpServer.startServer();
        assertTrue(myHttpServer.isServerStarted());
    }

    @Test
    public void isServerStopped() throws Exception {
        HttpServer myHttpServer = HttpServer.getInstance();
        myHttpServer.startServer();
        assertFalse(myHttpServer.isServerStopped());
    }

    @Test
    public void getConsolePath() throws Exception {
        HttpServer myHttpServer = HttpServer.getInstance();
        assertEquals("/console", myHttpServer.getConsoleContextPath());
    }

    @Test
    public void getConsoleUrl() throws Exception {
        HttpServer myHttpServer = HttpServer.getInstance();
        myHttpServer.startServer();
        int portNum = myHttpServer.getConsolePortNumber();
        String context = myHttpServer.getConsoleContextPath();
        assertEquals("http://localhost:" + portNum + context, myHttpServer.getConsoleUrl());
    }

    @Test
    public void getConsolePortNumber() throws Exception {
        HttpServer myHttpServer = HttpServer.getInstance();
        myHttpServer.startServer();

        int portNum = myHttpServer.getConsolePortNumber();
        assertTrue("the port number is not in integer range: " + Integer.toString(portNum),
                (Integer.MAX_VALUE > portNum) && (0 < portNum));
    }

}
