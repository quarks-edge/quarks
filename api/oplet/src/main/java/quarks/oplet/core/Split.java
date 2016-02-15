/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.core;

import static quarks.function.Functions.closeFunction;

import java.util.Collections;
import java.util.List;

import quarks.function.Consumer;
import quarks.function.ToIntFunction;
import quarks.oplet.OpletContext;

/**
 * Split a stream into multiple streams depending
 * on the result of a splitter function.
 * <BR>
 * For each tuple a function is called:
 * <UL>
 * <LI>If the return is negative the tuple is dropped.</LI>
 * <LI>Otherwise the return value is modded by the number of
 * output ports and the result is the output port index
 * the tuple is submitted to.</LI>
 * </UL>
 *
 * @param <T> Type of the tuple.
 */
public class Split<T> extends AbstractOplet<T, T> implements Consumer<T> {

    private static final long serialVersionUID = 1L;
    private final ToIntFunction<T> splitter;
    private List<? extends Consumer<T>> destinations;
    private int n;
    
    public Split(ToIntFunction<T> splitter) {
        this.splitter = splitter;
    }
    

    @Override
    public void initialize(OpletContext<T, T> context) {
        super.initialize(context);

        destinations = context.getOutputs();
        n = destinations.size();
    }

    @Override
    public void start() {
    }

    @Override
    public List<Consumer<T>> getInputs() {
        return Collections.singletonList(this);
    }

    @Override
    public void accept(T tuple) {
        int s = splitter.applyAsInt(tuple);
        if (s >= 0)
            destinations.get(s % n).accept(tuple);
    }

    @Override
    public void close() throws Exception {
        closeFunction(splitter);
    }
}
