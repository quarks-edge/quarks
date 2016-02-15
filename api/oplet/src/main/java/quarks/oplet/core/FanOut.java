/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.core;

import java.util.Collections;
import java.util.List;

import quarks.function.Consumer;

public final class FanOut<T> extends AbstractOplet<T, T> implements Consumer<T> {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private List<? extends Consumer<T>> targets;
    private int n;

    @Override
    public void start() {
    }

    @Override
    public List<? extends Consumer<T>> getInputs() {
        targets = getOpletContext().getOutputs();
        n = targets.size();
        return Collections.singletonList(this);
    }
    
    @Override
    public void accept(T tuple) {
        for (int i = 0; i < n; i++)
            targets.get(i).accept(tuple);
    }

    @Override
    public void close() {
    }
}
