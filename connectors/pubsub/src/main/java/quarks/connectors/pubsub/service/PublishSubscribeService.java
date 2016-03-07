/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.connectors.pubsub.service;

import quarks.function.Consumer;

/**
 * Publish subscribe service.
 * <BR>
 * Service that allows jobs to subscribe to 
 * streams published by other jobs.
 * <BR>
 * This is an optional service that allows streams
 * to be published by topic between jobs.
 * <P>
 * When an instance of this service is not available
 * then {@link quarks.connectors.pubsub.PublishSubscribe#publish(quarks.topology.TStream, String, Class) publish}
 * is a no-op, a sink that discards all tuples on the stream.
 * <BR>
 * A {@link quarks.connectors.pubsub.PublishSubscribe#subscribe(quarks.topology.TopologyElement, String, Class) subscribe} 
 * will have no tuples when an instance of this service is not available.
 * </P>
 * 
 * @see quarks.connectors.pubsub.PublishSubscribe#publish(quarks.topology.TStream, String, Class)
 * @see quarks.connectors.pubsub.PublishSubscribe#subscribe(quarks.topology.TopologyElement, String, Class)
 */
public interface PublishSubscribeService {
    
    /**
     * Add a subscriber to a published topic.
     * 
     * @param topic Topic to subscribe to.
     * @param streamType Type of the stream.
     * @param subscriber How to deliver published tuples to the subscriber.
     */
    <T> void addSubscriber(String topic, Class<T> streamType, Consumer<T> subscriber);
    
    void removeSubscriber(String topic, Consumer<?> subscriber);
    
    /**
     * Get the destination for a publisher.
     * A publisher calls {@code destination.accept(tuple)} to publish
     * {@code tuple} to the topic.
     * 
     * @param topic Topic tuples will be published to.
     * @param streamType Type of the stream
     * @return Consumer that is used to publish tuples.
     */
    <T> Consumer<T> getPublishDestination(String topic, Class<? super T> streamType);
}