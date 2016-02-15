/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.providers.development;

import java.util.Hashtable;
import java.util.concurrent.Future;

import com.codahale.metrics.MetricRegistry;
import com.google.gson.JsonObject;

import quarks.console.server.HttpServer;
import quarks.execution.Job;
import quarks.execution.services.ControlService;
import quarks.metrics.Metrics;
import quarks.metrics.MetricsSetup;
import quarks.metrics.oplets.CounterOp;
import quarks.providers.direct.DirectProvider;
import quarks.runtime.jmxcontrol.JMXControlService;
import quarks.topology.Topology;

/**
 * Provider intended for development.
 * This provider executes topologies using {@code DirectProvider}
 * and extends it by:
 * <UL>
 * <LI>
 * starting an embedded web-server providing the Quarks development console
 * that shows live graphs for running applications.
 * </LI>
 * <LI>
 * Creating a metrics registry with metrics registered
 * in the platform MBean server.
 * </LI>
 * <LI>
 * Add a {@link ControlService} that registers control management
 * beans in the platform MBean server.
 * </LI>
 * <LI>
 * Add tuple count metrics on all the streams before submitting a topology.
 * The implementation calls {@link Metrics#counter(Topology)} to insert 
 * {@link CounterOp} oplets into each stream.
 * </LI>
 * </UL>
 */
public class DevelopmentProvider extends DirectProvider {
    
    /**
     * JMX domains that this provider uses to register MBeans.
     * Set to {@value}.
     */
    public static final String JMX_DOMAIN = "quarks.providers.development";
    
    public DevelopmentProvider() throws Exception {
        
        MetricsSetup.withRegistry(getServices(), new MetricRegistry()).
                startJMXReporter(JMX_DOMAIN);
        
        getServices().addService(ControlService.class,
                new JMXControlService(JMX_DOMAIN, new Hashtable<>()));

        HttpServer server = HttpServer.getInstance();
        getServices().addService(HttpServer.class, server);   
        server.startServer();
    }

    @Override
    public Future<Job> submit(Topology topology, JsonObject config) {
        Metrics.counter(topology);
        return super.submit(topology, config);
    }
}
