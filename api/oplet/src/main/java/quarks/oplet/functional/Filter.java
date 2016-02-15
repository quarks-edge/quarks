/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.functional;

import static quarks.function.Functions.closeFunction;

import quarks.function.Predicate;
import quarks.oplet.core.Pipe;

public class Filter<T> extends Pipe<T, T> {
    private static final long serialVersionUID = 1L;
    private Predicate<T> filter;

    public Filter(Predicate<T> filter) {
        this.filter = filter;
    }

    @Override
    public void accept(T tuple) {
        if (filter.test(tuple))
            submit(tuple);
    }

    @Override
    public void close() throws Exception {
        closeFunction(filter);
    }
}
