/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.functional;

import static quarks.function.Functions.closeFunction;

import quarks.function.Function;
import quarks.oplet.core.Pipe;

/**
 * 
 * Map an input tuple to 0-N output tuples.
 * 
 * Uses a function that returns an iterable
 * to map the input tuple. The return value
 * of the function's apply method is
 * iterated through with each returned
 * value being submitted as an output tuple.
 * 
 * 
 * @param <I>
 *            Data container type for input tuples.
 * @param <O>
 *            Data container type for output tuples.
 */
public class FlatMap<I, O> extends Pipe<I, O> {
	private static final long serialVersionUID = 1L;
	
	private Function<I, Iterable<O>> function;

    public FlatMap(Function<I, Iterable<O>> function) {
        this.function = function;
    }

    @Override
    public void accept(I tuple) {
        Iterable<O> outputs = function.apply(tuple);
        if (outputs != null) {
        	for (O output : outputs) {
        		if (output != null)
                    submit(output);
        	}
        }
    }

    @Override
    public void close() throws Exception {
        closeFunction(function);
    }
}
