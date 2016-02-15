/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.kafka.runtime;

import java.util.Map;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import quarks.function.Supplier;

/**
 * A connector for producing/publishing Kafka key/value records.
 */
public class KafkaProducerConnector extends KafkaConnector implements AutoCloseable {
    private static final long serialVersionUID = 1L;
    private final Supplier<Map<String,Object>> configFn;
    private String id;
    private KafkaProducer<byte[],byte[]> producer;

    public KafkaProducerConnector(Supplier<Map<String, Object>> configFn) {
        this.configFn = configFn;
    }
    
    synchronized KafkaProducer<byte[],byte[]> client() {
        if (producer == null)
            producer = new KafkaProducer<byte[],byte[]>(configFn.get(),
                    new ByteArraySerializer(), new ByteArraySerializer());
        return producer;
    }

    @Override
    public synchronized void close() throws Exception {
        if (producer != null)
            producer.close();
    }
    
    String id() {
        if (id == null) {
            // include our short object Id
            id = "Kafka " + toString().substring(toString().indexOf('@') + 1);
        }
        return id;
    }
}
