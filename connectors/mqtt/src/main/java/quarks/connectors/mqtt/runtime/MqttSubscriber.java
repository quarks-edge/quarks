/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.mqtt.runtime;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;

import quarks.function.BiFunction;
import quarks.function.Consumer;

/**
 * Consumer that subscribes to topics from an MQTT server
 * producing a stream tuple of type {@code T} for each received message.
 *
 * @param <T> stream tuple type
 */
public class MqttSubscriber<T> implements Consumer<Consumer<T>>, AutoCloseable {
    private static final long serialVersionUID = 1L;
    private final Logger logger;
    private transient String id;
    private final MqttConnector connector;
    private final String topicFilter;
    private final int qos;
    private BiFunction<String, byte[], T> message2Tuple;
    private Consumer<T> eventSubmitter;

    public MqttSubscriber(MqttConnector connector, String topicFilter, int qos, BiFunction<String, byte[], T> message2Tuple) {
        this.logger = connector.getLogger();
        this.connector = connector;
        this.topicFilter = topicFilter;
        this.qos = qos;
        this.message2Tuple = message2Tuple;
        connector.setSubscriber(this);
    }
    
    /**
     * To be called on initial connection and reconnects
     * @param client the MqttClient
     * @throws MqttException
     */
    void connected(MqttClient client) throws MqttException {
        logger.info("{} subscribe({}, {})", id(), topicFilter, qos);
        client.subscribe(topicFilter, qos);
    }

    @Override
    public void accept(Consumer<T> eventSubmitter) {
        this.eventSubmitter = eventSubmitter;

        try {
            connector.client();  // induce connecting.
        } catch (Exception e) {
            logger.error("{} setup failed topicFilter:{}", id(), topicFilter, e);
        }
    }

    void messageArrived(String topic, MqttMessage message) throws Exception {
        T tuple = message2Tuple.apply(topic, message.getPayload());
        eventSubmitter.accept(tuple);
    }
    
    protected String id() {
        if (id == null) {
            // use our short object Id (only single subscriber)
            id = connector.id() + " subscriber ";
        }
        return id;
    }

    @Override
    public void close() throws Exception {
        connector.close();
    }
}
