/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.connectors.pubsub.oplets;

import quarks.connectors.pubsub.service.PublishSubscribeService;
import quarks.oplet.OpletContext;
import quarks.oplet.core.Sink;

/**
 * Publish a stream to a PublishSubscribeService service.
 * If no such service exists then no tuples are published.
 *
 * @param <T> Type of the tuples.
 */
public class Publish<T> extends Sink<T> {
    
    private final String topic;
    private final Class<? super T> streamType;
    
    public Publish(String topic, Class<? super T> streamType) {
        this.topic = topic;
        this.streamType = streamType;
    }
    
    @Override
    public void initialize(OpletContext<T, Void> context) {
        super.initialize(context);
        
        PublishSubscribeService pubSub = context.getService(PublishSubscribeService.class);
        if (pubSub != null)
            setSinker(pubSub.getPublishDestination(topic, streamType));
    }
}
