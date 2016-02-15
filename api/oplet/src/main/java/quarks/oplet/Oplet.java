/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet;

import java.util.List;

import quarks.function.Consumer;

/**
 * Generic API for an oplet that processes streaming data on 0-N input ports
 * and produces 0-M output streams on its output ports. An input port may be
 * connected with any number of streams from other oplets. An output port may
 * connected to any number of input ports on other oplets.
 *
 * @param <I>
 *            Data container type for input tuples.
 * @param <O>
 *            Data container type for output tuples.
 */
public interface Oplet<I, O> extends AutoCloseable {

    /**
     * Initialize the oplet.
     * 
     * @param context
     * @throws Exception
     */
    void initialize(OpletContext<I, O> context) throws Exception;

    /**
     * Start the oplet. Oplets must not submit any tuples not derived from
     * input tuples until this method is called.
     */
    void start();

    /**
     * Get the input stream data handlers for this oplet. The number of handlers
     * must equal the number of configured input ports. Each tuple
     * arriving on an input port will be sent to the stream handler for that
     * input port.
     * 
     * @return list of consumers
     */
    List<? extends Consumer<I>> getInputs();
}
