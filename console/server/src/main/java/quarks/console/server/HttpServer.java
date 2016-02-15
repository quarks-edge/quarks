/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.console.server;

import java.io.IOException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

public class HttpServer {

	/**
	 * The only constructor.  A private no-argument constructor.  Called only once from the static HttpServerHolder class.
	 */
    private HttpServer() {
    }
    
    /** 
	 * The static class that creates the singleton HttpServer object.
	 */
    private static class HttpServerHolder {
        // use port 0 so we know the server will always start
        private static final Server JETTYSERVER = new Server(0);
        private static final WebAppContext WEBAPP = new WebAppContext();
        private static final HttpServer INSTANCE = new HttpServer();
        private static boolean INITIALIZED = false;
    }

    /**
     * Gets the jetty server associated with this class
     * @return the org.eclipse.jetty.server.Server
     */
    private static Server getJettyServer() {
        return HttpServerHolder.JETTYSERVER;
    }
    /**
     * Initialization of the context path for the web application "/console" occurs in this method
     * and the handler for the web application is set.  This only occurs once.
     * @return HttpServer: the singleton instance of this class
     * @throws IOException
     */
    public static HttpServer getInstance() throws IOException {
        if (!HttpServerHolder.INITIALIZED) {
            HttpServerHolder.WEBAPP.setContextPath("/console");
            ServletContextHandler contextJobs = new ServletContextHandler(ServletContextHandler.SESSIONS);
            contextJobs.setContextPath("/jobs");
            ServletContextHandler contextMetrics = new ServletContextHandler(ServletContextHandler.SESSIONS);
            contextMetrics.setContextPath("/metrics");
            ServerUtil sUtil = new ServerUtil();
            String warFilePath = sUtil.getAbsoluteWarFilePath("console.war");
            HttpServerHolder.WEBAPP.setWar(warFilePath);
            HttpServerHolder.WEBAPP.addAliasCheck(new AllowSymLinkAliasChecker()); 
            ContextHandlerCollection contexts = new ContextHandlerCollection();
            contexts.setHandlers(new Handler[] { contextJobs, contextMetrics, HttpServerHolder.WEBAPP });
            HttpServerHolder.JETTYSERVER.setHandler(contexts);
            HttpServerHolder.INITIALIZED = true;
        }
        return HttpServerHolder.INSTANCE;
    }

    /**
     * 
     * @return the ServerConnector object for the jetty server
     */
    private static ServerConnector getServerConnector() {
        return (ServerConnector) HttpServerHolder.JETTYSERVER.getConnectors()[0];
    }

    /**
     * 
     * @return a String containing the context path to the console web application
     * @throws Exception
     */
    public String getConsoleContextPath() throws Exception {
        return HttpServerHolder.WEBAPP.getContextPath();
    }

    /**
     * Starts the jetty web server
     */
    public void startServer() throws Exception {
        getJettyServer().start();
    }

    /**
     * Stops the jetty web server
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private static void stopServer() throws Exception {
        getJettyServer().stop();
    }

    /**
     * Checks to see if the jetty web server is started
     * @return a boolean: true if the server is started, false if not
     */
    public boolean isServerStarted() {
        if (getJettyServer().isStarted() || getJettyServer().isStarting() || getJettyServer().isRunning()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checks to see if the server is in a "stopping" or "stopped" state
     * @return a boolean: true if the server is stopping or stopped, false otherwise
     */
    public boolean isServerStopped() {
        if (getJettyServer().isStopping() || getJettyServer().isStopped()) {
            return true;
        }
        else {
            return false;
        }
    }
    /**
     * Returns the port number the console is running on.  Each time the console is started a different port number may be returned.
     * @return an int: the port number the jetty server is listening on
     */
    public int getConsolePortNumber() {
        return getServerConnector().getLocalPort();
    }
    
    /**
     * Returns the url for the web application at the "console" context path.  Localhost is always assumed
     * @return the url for the web application at the "console" context path.
     * @throws Exception 
     */
    public String getConsoleUrl() throws Exception {
        return new String("http://localhost" + ":" + getConsolePortNumber() + getConsoleContextPath());
    }
}
