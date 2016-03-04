/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.connectors.pubsub.service;

import java.util.HashSet;
import java.util.Set;

import quarks.function.Consumer;

class TopicHandler<T> implements Consumer<T> {
    private static final long serialVersionUID = 1L;

    private final Class<T> streamType;
    private final Set<Consumer<T>> subscribers = new HashSet<>();

    TopicHandler(Class<T> streamType) {
        this.streamType = streamType;
    }

    synchronized void addSubscriber(Consumer<T> subscriber) {
        subscribers.add(subscriber);
    }

    synchronized void removeSubscriber(Consumer<?> subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public synchronized void accept(T tuple) {
        for (Consumer<T> subscriber : subscribers)
            subscriber.accept(tuple);
    }

    void checkClass(Class<T> streamType) {
        if (this.streamType != streamType)
            throw new IllegalArgumentException();
    }
}
