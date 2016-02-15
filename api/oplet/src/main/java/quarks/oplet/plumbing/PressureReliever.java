/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.oplet.plumbing;

import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import quarks.function.BiConsumer;
import quarks.function.Function;
import quarks.function.Functions;
import quarks.oplet.OpletContext;
import quarks.oplet.core.Pipe;
import quarks.window.Partition;
import quarks.window.PartitionedState;
import quarks.window.Policies;
import quarks.window.Window;
import quarks.window.Windows;

/**
 * Relieve pressure on upstream oplets by discarding tuples.
 * This oplet ensures that upstream processing is not
 * constrained by any delay in downstream processing,
 * for example by a sink oplet not being able to connect
 * to its external system.
 * When downstream processing cannot keep up with the input rate
 * this oplet maintains a defined window of the most recent
 * tuples and discards any earlier tuples using arrival order.
 * <P>
 * A window partition is maintained for each key seen
 * on the input stream. Any tuple arriving on the input
 * stream is inserted into the window. Asynchronously
 * tuples are taken from the window using FIFO and
 * submitted downstream. The submission of tuples maintains
 * order within a partition but not across partitions.
 * </P>
 * <P>
 * Tuples are  <B>discarded and not</B> submitted to the
 * output port if the downstream processing cannot keep up
 * the incoming tuple rate.
 * <UL>
 * <LI>For a {@link #PressureReliever(int, Function) count}
 * {@code PressureReliever} up to last (most recent) {@code N} tuples
 * are maintained in a window partition.
 * <BR> Asynchronous tuple submission removes the last (oldest) tuple in the partition
 * before submitting it downstream.
 * <BR> If when an input tuple is processed the window partition contains N tuples, then
 * the first (oldest) tuple in the partition is discarded before the input tuple is inserted into the window.
 * </UL>
 * </P>
 * <P>
 * <BR>
 * Insertion of the oplet into a stream disconnects the
 * upstream processing from the downstream processing,
 * so that downstream processing is executed on a different
 * thread to the thread that processed the input tuple.
 * </P>
 * 
 * @param <T> Tuple type.
 * @param <K> Key type.
 */
public class PressureReliever<T, K> extends Pipe<T, T> {
    private static final long serialVersionUID = 1L;

    private ScheduledExecutorService executor;
    private final Window<T, K, LinkedList<T>> window;

    /**
     * Pressure reliever that maintains up to {@code count} most recent tuples per key.
     *
     * @param count Number of tuples to maintain where downstream processing cannot keep up.
     * @param keyFunction Key function for tuples.
     */
    public PressureReliever(int count, Function<T, K> keyFunction) {
        window = Windows.window(
                Policies.alwaysInsert(),
                Policies.countContentsPolicy(count),
                Policies.evictOldest(),
                new FirstSubmitter(),
                keyFunction,
                () -> new LinkedList<T>());

        // No processing of the window takes place
        window.registerPartitionProcessor((tuples, k) -> { });
    }    

    @Override
    public void initialize(OpletContext<T, T> context) {
        super.initialize(context);
        executor = context.getService(ScheduledExecutorService.class);
    }

    @Override
    public void accept(T tuple) {
        window.insert(tuple);
    }

    @Override
    public void close() throws Exception {
    }

    private class FirstSubmitter extends PartitionedState<K, AtomicBoolean>
            implements BiConsumer<Partition<T, K, LinkedList<T>>, T> {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        FirstSubmitter() {
            super(() -> new AtomicBoolean());
        }

        /**
         * Process the window (to consume the oldest tuple in the partition)
         * only if a tuple from this partition is not already being consumed.
         * 
         * @param t
         * @param v
         * @return
         */
        @Override
        public void accept(Partition<T, K, LinkedList<T>> partition, T tuple) {
            submitNextTuple(partition);
        }

        private void submitNextTuple(Partition<T, K, LinkedList<T>> partition) {
            final K key = partition.getKey();
            final AtomicBoolean latch = getState(key);
            if (!latch.compareAndSet(false, true))
                return;
            
            final T firstTuple;
            synchronized (partition) {
                final LinkedList<T> contents = partition.getContents();
                if (contents.isEmpty()) {
                    latch.set(false);
                    return;
                }

                firstTuple = contents.removeFirst();
            }

            Runnable submit = Functions.delayedConsume(getDestination(), firstTuple);
            submit = Functions.runWithFinal(submit, () -> {
                latch.set(false);
                submitNextTuple(partition);
            });

            executor.execute(submit);
        }
    }
}
