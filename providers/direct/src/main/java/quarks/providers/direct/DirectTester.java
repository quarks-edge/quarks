/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.providers.direct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import quarks.topology.TStream;
import quarks.topology.spi.tester.AbstractTester;
import quarks.topology.tester.Condition;

class DirectTester extends AbstractTester {

    private final DirectTopology topology;

    DirectTester(DirectTopology topology) {
        this.topology = topology;
    }

    @Override
    public DirectTopology topology() {
        return topology;
    }

    @Override
    public Condition<Long> tupleCount(TStream<?> stream, final long expectedCount) {
        AtomicLong count = new AtomicLong();
        stream.sink(t -> {
            count.incrementAndGet();
        });
        return new Condition<Long>() {

            @Override
            public boolean valid() {
                return count.get() == expectedCount;
            }

            @Override
            public Long getResult() {
                return count.get();
            }
        };
    }

    @Override
    public <T> Condition<List<T>> streamContents(TStream<T> stream, @SuppressWarnings("unchecked") T... values) {
        List<T> contents = Collections.synchronizedList(new ArrayList<>());
        stream.sink(t -> contents.add(t));
        return new Condition<List<T>>() {

            @Override
            public boolean valid() {
                synchronized (contents) {
                    return Arrays.asList(values).equals(contents);
                }
            }

            @Override
            public List<T> getResult() {
                return contents;
            }
        };
    }

    @Override
    public Condition<Long> atLeastTupleCount(TStream<?> stream, long expectedCount) {
        AtomicLong count = new AtomicLong();
        stream.sink(t -> {
            count.incrementAndGet();
        });
        return new Condition<Long>() {

            @Override
            public boolean valid() {
                return count.get() >= expectedCount;
            }

            @Override
            public Long getResult() {
                return count.get();
            }
        };
    }

    @Override
    public <T> Condition<List<T>> contentsUnordered(TStream<T> stream, @SuppressWarnings("unchecked") T... values) {
        List<T> contents = Collections.synchronizedList(new ArrayList<>());
        stream.sink(t -> contents.add(t));
        return new Condition<List<T>>() {

            @Override
            public boolean valid() {
                synchronized (contents) {
                    if (contents.size() != values.length)
                        return false;
                    List<T> copy = new ArrayList<T>(contents);
                    for (T expected : values) {
                        if (!copy.remove(expected))
                            return false;
                    }
                    return copy.isEmpty();
                }
            }

            @Override
            public List<T> getResult() {
                return contents;
            }
        };
    }
}
