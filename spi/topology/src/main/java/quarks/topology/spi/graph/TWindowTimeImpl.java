package quarks.topology.spi.graph;

import static quarks.window.Policies.alwaysInsert;
import static quarks.window.Policies.evictOlderWithProcess;
import static quarks.window.Policies.insertionTimeList;
import static quarks.window.Policies.processOnInsert;
import static quarks.window.Policies.scheduleEvictIfEmpty;

import java.util.List;
import java.util.concurrent.TimeUnit;

import quarks.function.BiFunction;
import quarks.function.Function;
import quarks.function.Functions;
import quarks.oplet.window.Aggregate;
import quarks.topology.TStream;
import quarks.window.InsertionTimeList;
import quarks.window.Window;
import quarks.window.Windows;

public class TWindowTimeImpl<T, K> extends AbstractTWindow<T, K> {
    private long time;
    private TimeUnit unit;
    
    TWindowTimeImpl(long time, TimeUnit unit, TStream<T> feed, Function<T, K> keyFunction){
        super(feed, keyFunction);
        this.time = time;
        this.unit = unit;
    }

    /**
     * Window behaviour here is that:
     * 
     * Tuples are always inserted into the partition.
     * 
     * If before insertion the partition is empty the
     * a eviction is scheduled 
     * 
     * After insertion the window is processed so that
     * any change triggers this continuous aggregation.
     * 
     * The evict determiner evicts any tuples that have
     * been in the window longer that the configured
     * size, and invokes the processing on any eviction.
     * Multiple tuples may have been evicted before processing.
     */
    @Override
    public <U> TStream<U> aggregate(BiFunction<List<T>,K, U> processor) {    
        processor = Functions.synchronizedBiFunction(processor);
        Window<T, K, InsertionTimeList<T>> window =
                Windows.window(
                        alwaysInsert(),
                        scheduleEvictIfEmpty(time, unit),
                        evictOlderWithProcess(time, unit),
                        processOnInsert(),
                        getKeyFunction(),
                        insertionTimeList());
        
        Aggregate<T,U,K> op = new Aggregate<T,U,K>(window, processor);
        return feeder().pipe(op); 
    } 
}
