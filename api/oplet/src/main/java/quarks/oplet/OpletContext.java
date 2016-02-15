/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
*/
package quarks.oplet;

import java.util.List;

import quarks.execution.services.RuntimeServices;
import quarks.function.Consumer;

/**
 * Context information for the {@code Oplet}'s invocation context.
 * <P>
 * At execution time an oplet uses its invocation context to retrieve 
 * provided {@link #getService(Class) services}, 
 * {@link #getOutputs() output ports} for tuple submission
 * and {@link #getJobContext() job} information. 
 *
 * @param <I>
 * @param <O>
 */
public interface OpletContext<I, O> extends RuntimeServices {

	/**
	 * Get the unique identifier (within the running job)
	 * for this oplet.
	 * @return unique identifier for this oplet
	 */
	String getId();

    /**
     * {@inheritDoc}
     * <P>
     * Get a service for this oplet invocation.
     * 
     * An invocation of an oplet may get access to services,
     * which provide specific functionality, such as metrics.
     * </P>
     * 
     */
	@Override
    <T> T getService(Class<T> serviceClass);
    
    /**
     * Get the number of connected inputs to this oplet.
     * @return number of connected inputs to this oplet.
     */
    int getInputCount();
    
    /**
     * Get the number of connected outputs to this oplet.
     * @return number of connected outputs to this oplet.
     */
    int getOutputCount();

    /**
     * Get the mechanism to submit tuples on an output port.
     * 
     * @return list of consumers
     */
    List<? extends Consumer<O>> getOutputs();

    /**
     * Get the job hosting this oplet. 
     * @return {@link JobContext} hosting this oplet invocation.
     */
    JobContext getJobContext();
    
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
    String uniquify(String name);
}
