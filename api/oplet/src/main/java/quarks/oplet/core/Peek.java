/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.core;

/**
 * Oplet that allows a peek at each tuple and always forwards a tuple onto
 * its single output port.
 * 
 * {@link #peek(Object)} is called before the tuple is forwarded
 * and it is intended that the peek be a low cost operation
 * such as increasing a metric.
 *
 * @param <T>
 *            Type of the tuple.
 */
public abstract class Peek<T> extends Pipe<T, T> {
    private static final long serialVersionUID = 1L;

    @Override
    public final void accept(T tuple) {
        peek(tuple);
        submit(tuple);
    }

    protected abstract void peek(T tuple);
}
