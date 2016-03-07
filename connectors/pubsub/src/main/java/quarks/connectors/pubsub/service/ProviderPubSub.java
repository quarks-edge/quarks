/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.connectors.pubsub.service;

import java.util.HashMap;
import java.util.Map;

import quarks.function.Consumer;

/**
 * Publish subscribe service allowing exchange of streams between jobs in a provider.
 *
 */
public class ProviderPubSub implements PublishSubscribeService {
    
    private final Map<String,TopicHandler<?>> topicHandlers = new HashMap<>();
    
    @Override
    public <T> void addSubscriber(String topic, Class<T> streamType, Consumer<T> subscriber) { 
        getTopicHandler(topic, streamType).addSubscriber(subscriber);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> Consumer<T> getPublishDestination(String topic, Class<? super T> streamType) {
        return (Consumer<T>) getTopicHandler(topic, streamType);      
    }
    
    @Override
    public void removeSubscriber(String topic, Consumer<?> subscriber) {
        TopicHandler<?> topicHandler;
        synchronized (this) {
            topicHandler = topicHandlers.get(topic);
        }
        if (topicHandler != null) {
            topicHandler.removeSubscriber(subscriber);
        }
    }
    
    @SuppressWarnings("unchecked")
    private synchronized <T> TopicHandler<T> getTopicHandler(String topic, Class<T> streamType) {
        TopicHandler<T> topicHandler = (TopicHandler<T>) topicHandlers.get(topic);

        if (topicHandler == null) {
            topicHandlers.put(topic, topicHandler = new TopicHandler<T>(streamType));
        } else {
            topicHandler.checkClass(streamType);
        }
        return topicHandler;
    } 
}
