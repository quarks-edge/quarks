/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.functional;

import static quarks.function.Functions.closeFunction;

import quarks.function.Function;
import quarks.oplet.core.Pipe;

/**
 * Map an input tuple to 0-1 output tuple
 * 
 *
 * @param <I>
 *            Data container type for input tuples.
 * @param <O>
 *            Data container type for output tuples.
 */
public class Map<I, O> extends Pipe<I, O> {
    private static final long serialVersionUID = 1L;
    private Function<I, O> function;

    public Map(Function<I, O> function) {
        this.function = function;
    }

    @Override
    public void accept(I tuple) {
        O output = function.apply(tuple);
        if (output != null)
            submit(output);
    }

    @Override
    public void close() throws Exception {
        closeFunction(function);
    }
}
