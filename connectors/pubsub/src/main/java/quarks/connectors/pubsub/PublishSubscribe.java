/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/

package quarks.connectors.pubsub;

import quarks.connectors.pubsub.oplets.Publish;
import quarks.connectors.pubsub.service.PublishSubscribeService;
import quarks.execution.services.RuntimeServices;
import quarks.function.Consumer;
import quarks.function.Supplier;
import quarks.topology.TSink;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.TopologyElement;

/**
 * Publish subscribe model.
 * <BR>
 * A stream can be {@link #publish(TStream, String, Class) published } to a topic
 * and then {@link #subscribe(TopologyElement, String, Class) subscribed} to by
 * other topologies.
 * 
 * <P>
 * A published topic has a type and subscribers must subscribe using the same
 * topic and type (inheritance matching is not supported).
 * <BR>
 * Multiple streams from different topologies can be published to
 * same topic (and type) and there can be multiple subscribers
 * from different topologies.
 * <BR>
 * A subscriber can exist before a publisher exists, they are connected
 * automatically when the job starts.
 * </P>
 * <P>
 * If no {@link PublishSubscribeService} is registered then published
 * tuples are discarded and subscribers see no tuples.
 * </P>
 */
public class PublishSubscribe {



    /**
     * Publish this stream to a topic.
     * This is a model that allows jobs to subscribe to 
     * streams published by other jobs.
     * 
     * @param topic Topic to publish to.
     * @param streamType Type of objects on the stream.
     * @return sink element representing termination of this stream.
     * 
     * @see #subscribe(TopologyElement, String, Class)
     */
    public static <T> TSink<T> publish(TStream<T> stream, String topic, Class<? super T> streamType) {
        return stream.sink(new Publish<>(topic, streamType));
    }
        
    /**
     * Subscribe to a published topic.
     * This is a model that allows jobs to subscribe to 
     * streams published by other jobs.
     * @param topic Topic to subscribe to.
     * @param streamType Type of the stream.
     * @return Stream containing published tuples.
     * 
     * @see #publish(TStream, String, Class)
     */
    public static <T> TStream<T> subscribe(TopologyElement te, String topic, Class<T> streamType) {
        
        Topology topology = te.topology();
        
        Supplier<RuntimeServices> rts = topology.getRuntimeServiceSupplier();
        
        return te.topology().events(new SubscriberSetup<T>(topic, streamType, rts));
    }
    
    /**
     * Subscriber setup function that adds a subscriber on
     * start up and removes it on close. 
     *
     * @param <T> Type of the tuples.
     */
    private static final class SubscriberSetup<T> implements Consumer<Consumer<T>>, AutoCloseable{
        private static final long serialVersionUID = 1L;
        
        private final Supplier<RuntimeServices> rts;
        private final String topic;
        private final Class<T> streamType;
        private Consumer<T> submitter;
        
        SubscriberSetup(String topic, Class<T> streamType, Supplier<RuntimeServices> rts) {
            this.topic = topic;
            this.streamType = streamType;
            this.rts = rts;
        }
        @Override
        public void accept(Consumer<T> submitter) {
            PublishSubscribeService pubSub = rts.get().getService(PublishSubscribeService.class);
            if (pubSub != null) {
                this.submitter = submitter;
                pubSub.addSubscriber(topic, streamType, submitter);
            }
        }
        @Override
        public void close() throws Exception {
            PublishSubscribeService pubSub = rts.get().getService(PublishSubscribeService.class);
            if (pubSub != null) {
                pubSub.removeSubscriber(topic, submitter);
            }
        }
    }
}
