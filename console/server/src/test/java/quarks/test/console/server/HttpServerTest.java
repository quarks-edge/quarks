/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.console.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import org.junit.Test;

import quarks.console.server.HttpServer;

public class HttpServerTest {
	
	public static final String consoleWarNotFoundMessage =  
			"console.war not found.  Run 'ant' from the top level quarks directory, or 'ant' from 'console/servlets' to create console.war under the webapps directory.";


    @Test
    public void testGetInstance()  {
    	HttpServer myHttpServer = null;
    	boolean warNotFoundExceptionThrown = false;
        try {
        	myHttpServer = HttpServer.getInstance();
        } catch (Exception warNotFoundException) {
        	System.out.println(warNotFoundException.getMessage());
        	if (warNotFoundException.getMessage().equals(consoleWarNotFoundMessage)) {
        		warNotFoundExceptionThrown = true;
        		assertEquals("", "");
        	}
        } finally {
        	if (warNotFoundExceptionThrown == false) {
        		assertNotNull("HttpServer getInstance is null", myHttpServer);
        	} else {
        		assertNull("HttpServer getInstance is null because console.war could not be found", myHttpServer);
        	}
        }
    }

    @Test
    public void startServer() throws Exception {
    	HttpServer myHttpServer = null;
    	boolean warNotFoundExceptionThrown = false;
        try {
        	myHttpServer = HttpServer.getInstance();
        } catch (Exception warNotFoundException) {
        	System.out.println(warNotFoundException.getMessage());
        	if (warNotFoundException.getMessage().equals(consoleWarNotFoundMessage)) {
        		warNotFoundExceptionThrown = true;
        		assertEquals("", "");
        	}
        } finally {
        	if (warNotFoundExceptionThrown == false) {
                myHttpServer.startServer();
                assertTrue(myHttpServer.isServerStarted());
        	} else {
        		assertNull("HttpServer getInstance is null because console.war could not be found", myHttpServer);
        	}
        }

    }

    @Test
    public void isServerStopped() throws Exception {
    	HttpServer myHttpServer = null;
    	boolean warNotFoundExceptionThrown = false;
        try {
        	myHttpServer = HttpServer.getInstance();
        } catch (Exception warNotFoundException) {
        	System.out.println(warNotFoundException.getMessage());
        	if (warNotFoundException.getMessage().equals(consoleWarNotFoundMessage)) {
        		warNotFoundExceptionThrown = true;
        		assertEquals("", "");
        	}
        } finally {
        	if (warNotFoundExceptionThrown == false) {
                myHttpServer.startServer();
                assertFalse(myHttpServer.isServerStopped());
        	} else {
        		assertNull("HttpServer getInstance is null because console.war could not be found", myHttpServer);
        	}
        }
    }

    @Test
    public void getConsolePath() throws Exception {
    	HttpServer myHttpServer = null;
    	boolean warNotFoundExceptionThrown = false;
        try {
        	myHttpServer = HttpServer.getInstance();
        } catch (Exception warNotFoundException) {
        	System.out.println(warNotFoundException.getMessage());
        	if (warNotFoundException.getMessage().equals(consoleWarNotFoundMessage)) {
        		warNotFoundExceptionThrown = true;
        		assertEquals("", "");
        	}
        } finally {
        	if (warNotFoundExceptionThrown == false) {
        		assertEquals("/console", myHttpServer.getConsoleContextPath());
        	} else {
        		assertNull("HttpServer getInstance is null because console.war could not be found", myHttpServer);
        	}
        }
        
    }

    @Test
    public void getConsoleUrl() throws Exception {
    	
    	HttpServer myHttpServer = null;
    	boolean warNotFoundExceptionThrown = false;
        try {
        	myHttpServer = HttpServer.getInstance();
        } catch (Exception warNotFoundException) {
        	System.out.println(warNotFoundException.getMessage());
        	if (warNotFoundException.getMessage().equals(consoleWarNotFoundMessage)) {
        		warNotFoundExceptionThrown = true;
        		assertEquals("", "");
        	}
        } finally {
        	if (warNotFoundExceptionThrown == false) {
                myHttpServer.startServer();
                int portNum = myHttpServer.getConsolePortNumber();
                String context = myHttpServer.getConsoleContextPath();
                assertEquals("http://localhost:" + portNum + context, myHttpServer.getConsoleUrl());
        	} else {
        		assertNull("HttpServer getInstance is null because console.war could not be found", myHttpServer);
        	}
        }
    }

    @Test
    public void getConsolePortNumber() throws Exception {
    	HttpServer myHttpServer = null;
    	boolean warNotFoundExceptionThrown = false;
        try {
        	myHttpServer = HttpServer.getInstance();
        } catch (Exception warNotFoundException) {
        	System.out.println(warNotFoundException.getMessage());
        	if (warNotFoundException.getMessage().equals(consoleWarNotFoundMessage)) {
        		warNotFoundExceptionThrown = true;
        		assertEquals("", "");
        	}
        } finally {
        	if (warNotFoundExceptionThrown == false) {
                myHttpServer.startServer();
                int portNum = myHttpServer.getConsolePortNumber();
                assertTrue("the port number is not in integer range: " + Integer.toString(portNum),
                        (Integer.MAX_VALUE > portNum) && (0 < portNum));
        	} else {
        		assertNull("HttpServer getInstance is null because console.war could not be found", myHttpServer);
        	}
        }
    }

}
