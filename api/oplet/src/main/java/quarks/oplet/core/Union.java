/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.core;

import java.util.Collections;
import java.util.List;

import quarks.function.Consumer;

/**
 * Union oplet, merges multiple input ports
 * into a single output port.
 * 
 * Processing for each input is identical
 * and just submits the tuple to the single output.
 */
public final class Union<T> extends AbstractOplet<T, T> {

    @Override
    public void start() {
    }

    /**
     * For each input set the output directly to the only output.
     */
    @Override
    public List<? extends Consumer<T>> getInputs() {
        Consumer<T> output = getOpletContext().getOutputs().get(0);
        return Collections.nCopies(getOpletContext().getInputCount(), output);
    }

    @Override
    public void close() {
    }
}
