/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.runtime.etiao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import quarks.execution.services.RuntimeServices;
import quarks.function.Consumer;
import quarks.function.Functions;
import quarks.oplet.JobContext;
import quarks.oplet.Oplet;

/**
 * An {@link Oplet} invocation in the context of the 
 * <a href="{@docRoot}/quarks/runtime/etiao/package-summary.html">ETIAO</a> runtime.  
 *
 * @param <T> 
 *            Oplet type.
 * @param <I>
 *            Data container type for input tuples.
 * @param <O>
 *            Data container type for output tuples.
 */
public class Invocation<T extends Oplet<I, O>, I, O> implements AutoCloseable {
    /** Prefix used by oplet unique identifiers. */
    public static final String ID_PREFIX = "OP_";

   /**
    * Runtime unique identifier.
    */
    private final String id; 
    private T oplet;

    private List<Consumer<O>> outputs;
    private List<SettableForwarder<I>> inputs;

    protected Invocation(String id, T oplet, int inputCount, int outputCount) {
    	this.id = id;
        this.oplet = oplet;
        inputs = inputCount == 0 ? Collections.emptyList() : new ArrayList<>(inputCount);
        for (int i = 0; i < inputCount; i++) {
            inputs.add(new SettableForwarder<>());
        }

        outputs = outputCount == 0 ? Collections.emptyList() : new ArrayList<>(outputCount);
        for (int i = 0; i < outputCount; i++) {
            outputs.add(Functions.discard());
        }
    }

    /**
     * Returns the unique identifier associated with this {@code Invocation}.
     * 
     * @return unique identifier
     */
    public String getId() {
        return id;
    }
    
    /**
     * Returns the oplet associated with this {@code Invocation}.
     * 
     * @return the oplet associated with this invocation
     */
    public T getOplet() {
        return oplet;
    }

    /**
     * Returns the number of outputs for this invocation.
     * @return the number of outputs
     */
    public int getOutputCount() {
        return outputs.size();
    }
    
    /**
     * Adds a new output.  By default, the output is connected to a Consumer 
     * that discards all items passed to it.
     * 
     * @return the index of the new output
     */
    public int addOutput() {
        int index = outputs.size();
        outputs.add(Functions.discard());
        return index;
    }

    /**
     * Disconnects the specified port by connecting to a no-op {@code Consumer} implementation.
     * 
     * @param port the port index
     */
    public void disconnect(int port) {
        outputs.set(port, Functions.discard());
    }

    /**
     * Disconnects the specified port and reconnects it to the specified target.
     * 
     * @param port index of the port which is reconnected
     * @param target target the port gets connected to
     */
    public void setTarget(int port, Consumer<O> target) {
        disconnect(port);
        outputs.set(port, target);
    }

    /**
     * Returns the list of input stream forwarders for this invocation.
     */
    public List<? extends Consumer<I>> getInputs() {
        return inputs;
    }

    /**
     * Initialize the invocation.
     * 
     * @param job the context of the current job
     * @param services service provider for this invocation
     */
    public void initialize(JobContext job, RuntimeServices services) {

        InvocationContext<I, O> context = new InvocationContext<I, O>(
        		id, job, services, 
                inputs.size(),
                outputs);

        try {
            oplet.initialize(context);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<? extends Consumer<I>> streamers = oplet.getInputs();
        for (int i = 0; i < inputs.size(); i++)
            inputs.get(i).setDestination(streamers.get(i));
    }

    /**
     * Start the oplet. Oplets must not submit any tuples not derived from
     * input tuples until this method is called.
     */
    public void start() {
        oplet.start();
    }

    @Override
    public void close() throws Exception {
        oplet.close();
    }
}
