/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
*/
package quarks.runtime.etiao;

import java.util.List;

import quarks.execution.services.RuntimeServices;
import quarks.function.Consumer;
import quarks.oplet.JobContext;

/**
 * Context information for the {@code Oplet}'s execution context.
 *
 * @param <I>
 *            Data container type for input tuples.
 * @param <O>
 *            Data container type for output tuples.
 */
public class InvocationContext<I, O> extends AbstractContext<I, O> {

	private final String id;
	private final int inputCount;

	/**
	 * Creates an {@code InvocationContext} with the specified parameters.
	 *  
	 * @param id the oplet's unique identifier
	 * @param job the current job's context
	 * @param services service provider for the current job
	 * @param inputCount number of oplet's inputs 
	 * @param outputs list of oplet's outputs
	 */
    public InvocationContext(String id, JobContext job,
            RuntimeServices services,
            int inputCount,
            List<? extends Consumer<O>> outputs) {
        super(job, services);
        this.id = id;
        this.inputCount = inputCount;
        this.outputs = outputs;
    }

    private final List<? extends Consumer<O>> outputs;
    
    @Override
    public String getId() {
    	return id;
    }

    @Override
    public List<? extends Consumer<O>> getOutputs() {
        return outputs;
    }
    @Override
    public int getInputCount() {
        return inputCount;
    }
    @Override
    public int getOutputCount() {
        return getOutputs().size();
    }
}
