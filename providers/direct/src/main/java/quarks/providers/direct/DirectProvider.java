/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.providers.direct;

import java.util.concurrent.Future;

import com.google.gson.JsonObject;

import quarks.execution.DirectSubmitter;
import quarks.execution.Job;
import quarks.execution.services.ServiceContainer;
import quarks.topology.Topology;
import quarks.topology.TopologyProvider;
import quarks.topology.spi.AbstractTopologyProvider;

/**
 * {@code DirectProvider} is a {@link TopologyProvider} that
 * runs a submitted topology as a {@link Job} in threads
 * in the current virtual machine.
 * <P> 
 * A job (execution of a topology) continues to execute
 * while any of its elements have remaining work,
 * such as any of the topology's source streams are capable
 * of generating tuples.
 * <BR>
 * "Endless" source streams never terminate - e.g., a stream
 * created by {@link Topology#generate(quarks.function.Supplier) generate()},
 * {@link Topology#poll(quarks.function.Supplier, long, java.util.concurrent.TimeUnit) poll()},
 * or {@link Topology#events(quarks.function.Consumer) events()}.
 * Hence a job with such sources runs until either it or some other
 * entity terminates it.
 * </P>
 */
public class DirectProvider extends AbstractTopologyProvider<DirectTopology>
        implements DirectSubmitter<Topology, Job> {

    private final ServiceContainer services;
    
    /**
     * CONFIGURATION_JOB_NAME is used to identify the submission configuration property containing the job name.
     * The value is {@value}.
     * @see #submit(Topology, JsonObject)
     */
    public static final String CONFIGURATION_JOB_NAME = "jobName";

    public DirectProvider() {
        this.services = new ServiceContainer();
    }

    /**
     * {@inheritDoc}
     * <P>
     * The returned services instance is shared
     * across all jobs submitted to this provider. 
     * </P>
     */
    @Override
    public ServiceContainer getServices() {
        return services;
    }

    @Override
    public DirectTopology newTopology(String name) {
        return new DirectTopology(name, services);
    }

    @Override
    public Future<Job> submit(Topology topology) {
        return submit(topology, new JsonObject());
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * The configuration JSON representation is summarized in the following table:
     * 
     * <table border=1 cellpadding=3 cellspacing=1>
     * <caption>Summary of configuration properties</caption>
     * <tr>
     *    <td align=center><b>Attribute</b></td>
     *    <td align=center><b>Type</b></td>
     *    <td align=center><b>Description</b></td>
     *  </tr>
     * <tr>
     *    <td>jobName</td>
     *    <td>String</td>
     *    <td>The name of the job.</td>
     *  </tr>
     * </table>
     * 
     */
    @Override
    public Future<Job> submit(Topology topology, JsonObject config) {
        return ((DirectTopology) topology).executeCallable(config);
    }
}
