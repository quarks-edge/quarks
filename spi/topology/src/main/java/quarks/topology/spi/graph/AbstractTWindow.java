package quarks.topology.spi.graph;

import quarks.function.Function;
import quarks.topology.TStream;
import quarks.topology.TWindow;
import quarks.topology.Topology;

public abstract class AbstractTWindow<T, K> implements TWindow<T, K> {
    private final TStream<T> feed;
    private Function<T, K> keyFunction;
    
    AbstractTWindow(TStream<T> feed, Function<T, K> keyFunction){
        this.feed = feed;
        this.keyFunction = keyFunction;
    } 
    
    @Override
    public Topology topology() {
        return feed.topology();
    }

    @Override
    public Function<T, K> getKeyFunction() {
        return keyFunction;
    }
    @Override
    public TStream<T> feeder() {
        return feed;
    }
}
