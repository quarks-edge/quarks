/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.samples.apps;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import quarks.console.server.HttpServer;
import quarks.providers.direct.DirectProvider;
import quarks.samples.apps.mqtt.AbstractMqttApplication;
import quarks.topology.Topology;

/**
 * An Application base class.
 * <p>
 * Application instances need to:
 * <ul>
 * <li>define an implementation for {@link #buildTopology(Topology)}</li>
 * <li>call {@link #run()} to build and submit the topology for execution.</li>
 * </ul>
 * <p>
 * The class provides some common processing needs:
 * <ul>
 * <li>Support for an external configuration file</li>
 * <li>Provides a {@link TopologyProviderFactory}</li>
 * <li>Provides a {@link ApplicationUtilities}</li>
 * </ul>
 * @see AbstractMqttApplication
 */
public abstract class AbstractApplication {
    
    protected final String propsPath;
    protected final Properties props;
    private final ApplicationUtilities applicationUtilities;

    protected Topology t;
    
    public AbstractApplication(String propsPath) throws Exception {
        this.propsPath = propsPath;
        props = new Properties();
        props.load(Files.newBufferedReader(new File(propsPath).toPath()));
        applicationUtilities = new ApplicationUtilities(props);
    }
    
    /**
     * Construct and run the application's topology.
     * @throws Exception
     */
    protected void run() throws Exception {
// TODO need to setup logging to squelch stderr output from the runtime/connectors, 
// including paho output

        TopologyProviderFactory tpFactory = new TopologyProviderFactory(props);
        
        DirectProvider tp = tpFactory.newProvider();
        
        // Create a topology for the application
        t = tp.newTopology(config().getProperty("application.name"));
        
        preBuildTopology(t);
        
        buildTopology(t);
        
        // Run the topology
        HttpServer httpServer = tp.getServices().getService(HttpServer.class);
        if (httpServer != null) {
            System.out.println("Quarks Console URL for the job: "
                                + httpServer.getConsoleUrl());
        }
        tp.submit(t);
    }
    
    /**
     * Get the application's raw configuration information.
     * @return the configuration
     */
    public Properties config() {
        return props;
    }
    
    /**
     * Get the application's 
     * @return the helper
     */
    public ApplicationUtilities utils() {
        return applicationUtilities;
    }

    /**
     * A hook for a subclass to do things prior to the invocation
     * of {@link #buildTopology(Topology)}.
     * <p>
     * The default implementation is a no-op.
     * @param t the application's topology
     */
    protected void preBuildTopology(Topology t) {
        return;
    }
    
    /**
     * Build the application's topology.
     */
    abstract protected void buildTopology(Topology t);
    
    public void handleRuntimeError(String msg, Exception e) {
        // TODO
        System.err.println("A runtime error occurred. " + msg + ":" + e.getLocalizedMessage());
        e.printStackTrace();
    }

}
