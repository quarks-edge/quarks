/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.core;

import java.util.Collections;
import java.util.List;

import quarks.function.Consumer;
import quarks.oplet.OpletContext;

/**
 * Pipe oplet with a single input and output. 
 *
 * @param <I>
 *            Data container type for input tuples.
 * @param <O>
 *            Data container type for output tuples.
 */
public abstract class Pipe<I, O> extends AbstractOplet<I, O>implements Consumer<I> {
    private static final long serialVersionUID = 1L;

    private Consumer<O> destination;

    @Override
    public void initialize(OpletContext<I, O> context) {
        super.initialize(context);

        destination = context.getOutputs().get(0);
    }

    @Override
    public void start() {
    }

    @Override
    public List<Consumer<I>> getInputs() {
        return Collections.singletonList(this);
    }

    protected Consumer<O> getDestination() {
        return destination;
    }
    
    /**
     * Submit a tuple to single output.
     * @param tuple Tuple to be submitted.
     */
    protected void submit(O tuple) {
        getDestination().accept(tuple);
    }
}
