/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology.spi.graph;

import java.util.concurrent.TimeUnit;

import quarks.function.Consumer;
import quarks.function.Functions;
import quarks.function.Supplier;
import quarks.oplet.core.Source;
import quarks.oplet.functional.Events;
import quarks.oplet.functional.SupplierPeriodicSource;
import quarks.oplet.functional.SupplierSource;
import quarks.topology.TStream;
import quarks.topology.plumbing.PlumbingStreams;
import quarks.topology.spi.AbstractTopology;
import quarks.topology.tester.Tester;

public abstract class GraphTopology<X extends Tester> extends AbstractTopology<X> {

    protected GraphTopology(String name) {
        super(name);
    }

    protected <N extends Source<T>, T> TStream<T> sourceStream(N sourceOp) {
        return new ConnectorStream<GraphTopology<X>, T>(this, graph().source(sourceOp));
    }

    @Override
    public <T> TStream<T> source(Supplier<Iterable<T>> data) {
        data = Functions.synchronizedSupplier(data);
        return sourceStream(new SupplierSource<>(data));
    }

    @Override
    public <T> TStream<T> poll(Supplier<T> data, long period, TimeUnit unit) {
        data = Functions.synchronizedSupplier(data);
        return sourceStream(new SupplierPeriodicSource<>(period, unit, data));
    }

    @Override
    public <T> TStream<T> events(Consumer<Consumer<T>> eventSetup) {
        TStream<T> rawEvents = sourceStream(new Events<>(eventSetup));
        return PlumbingStreams.isolate(rawEvents, true);
    }
}
