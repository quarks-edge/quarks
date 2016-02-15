/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.core;

import java.util.Collections;
import java.util.List;

import quarks.function.Consumer;
import quarks.oplet.OpletContext;

public abstract class Source<T> extends AbstractOplet<Void, T> {

    private Consumer<T> destination;

    @Override
    public void initialize(OpletContext<Void, T> context) {
        super.initialize(context);

        destination = context.getOutputs().get(0);
    }

    protected Consumer<T> getDestination() {
        return destination;
    }
    
    /**
     * Submit a tuple to single output.
     * @param tuple Tuple to be submitted.
     */
    protected void submit(T tuple) {
        getDestination().accept(tuple);
    }

    @Override
    public final List<Consumer<Void>> getInputs() {
        return Collections.emptyList();
    }
}
