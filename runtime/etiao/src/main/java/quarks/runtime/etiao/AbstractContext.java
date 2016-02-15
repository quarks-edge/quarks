/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
*/
package quarks.runtime.etiao;

import quarks.execution.services.RuntimeServices;
import quarks.oplet.JobContext;
import quarks.oplet.OpletContext;

/**
 * Provides a skeletal implementation of the {@link OpletContext}
 * interface.
 */
public abstract class AbstractContext<I, O> implements OpletContext<I, O> {

    private final JobContext job;
    private final RuntimeServices services;

    public AbstractContext(JobContext job, RuntimeServices services) {
        this.job = job;
        this.services = services;
    }
    
    @Override
    public <T> T getService(Class<T> serviceClass) {
        return services.getService(serviceClass);
    }
    
    @Override
    public JobContext getJobContext() {
        return job;
    }
    /**
     * Creates a unique name within the context of the current runtime.
     * <p>
     * The default implementation adds a suffix composed of the package 
     * name of this interface, the current job and oplet identifiers, 
     * all separated by periods ({@code '.'}).  Developers should use this 
     * method to avoid name clashes when they store or register the name in 
     * an external container or registry.
     *
     * @param name name (possibly non-unique)
     * @return unique name within the context of the current runtime.
     */
    @Override
    public String uniquify(String name) {
        return new StringBuilder(name).
                append('.').append(OpletContext.class.getPackage().getName()).
                append('.').append(getJobContext().getJobId()).
                append('.').append(getId()).toString();
    }
}
