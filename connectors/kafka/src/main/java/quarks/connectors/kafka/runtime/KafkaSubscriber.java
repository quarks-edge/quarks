/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.kafka.runtime;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;

import kafka.message.MessageAndMetadata;
import quarks.connectors.kafka.KafkaConsumer;
import quarks.function.Consumer;
import quarks.function.Function;

/**
 * A consumer of Kafka key/value records that generates tuples of type {@code T}
 *
 * @param <T> tuple type
 */
public class KafkaSubscriber<T> implements Consumer<Consumer<T>>, AutoCloseable {
    private static final long serialVersionUID = 1L;
    private static final Logger trace = KafkaConsumerConnector.getTrace();
    private String id;
    private final KafkaConsumerConnector connector;
    private Function<ByteConsumerRecord, T> byteToTupleFn;
    private Function<StringConsumerRecord, T> stringToTupleFn;
    private final String[] topics;
    private Consumer<T> eventSubmitter;
   
    @SuppressWarnings("unchecked")
    public KafkaSubscriber(KafkaConsumerConnector connector, Function<? extends KafkaConsumer.ConsumerRecord<?,?>, T> toTupleFn, boolean isStringFn, String... topics) {
        this.connector = connector;
        if (isStringFn)
            stringToTupleFn = (Function<StringConsumerRecord, T>) toTupleFn;
        else
            byteToTupleFn = (Function<ByteConsumerRecord, T>) toTupleFn;
        this.topics = topics;
        connector.addSubscriber(this, this.topics);
    }
    
    // The explicit topicPartition style of subscription is part of the
    // Kafka's new KafkaConsumer API and is unbaked as of 8.2.2
//    @SuppressWarnings("unchecked")
//    public KafkaSubscriber(KafkaConsumerConnector connector, Function<? extends KafkaConsumer.ConsumerRecord<?,?>,T> toTupleFn, boolean isStringFn, KafkaConsumer.TopicPartition... topicPartitions) {
//        this.connector = connector;
//        if (isStringFn)
//            stringToTupleFn = (Function<KafkaSubscriber<T>.StringConsumerRecord, T>) toTupleFn;
//        else
//            byteToTupleFn = (Function<KafkaSubscriber<T>.ByteConsumerRecord, T>) toTupleFn;
//        this.topics = null;
//        TopicPartition[] tps = new TopicPartition[topicPartitions.length];
//        int i = 0;
//        for (KafkaConsumer.TopicPartition tp : topicPartitions)
//            tps[i++] = new TopicPartition(tp.topic(), tp.partition());
//        this.topicPartitions = tps;
//        connector.addSubscriber(this, this.topicPartitions);
//    }

    @Override
    public void accept(Consumer<T> eventSubmitter) {
        try {
            this.eventSubmitter = eventSubmitter;
            connector.start(this);
        }
        catch (Throwable t) {
            trace.error("{} initialization failure", id(), t);
        }
    }
    
    // unbaked 8.2.2 KafkaConsumer
//    void accept(ConsumerRecord<byte[],byte[]> rec) {
//        if (rec.error()) {
//            trace.error("{} error retrieving record.", id(), rec.error());
//            return;
//        }
//        try {
//            T tuple;
//            if (stringToTupleFn != null)
//                tuple = stringToTupleFn.apply(new StringConsumerRecord(rec));
//            else
//                tuple = byteToTupleFn.apply(new ByteConsumerRecord(rec));
//            eventSubmitter.accept(tuple);
//        }
//        catch (Exception e) {
//            trace.error("{} failure processing record from {}", id(), rec.topicAndPartition(), e);
//        }
//    }
    
    void accept(MessageAndMetadata<byte[],byte[]> rec) {
        try {
            trace.trace("{} received rec for topic:{} partition:{} offset:{}",
                        id(), rec.topic(), rec.partition(), rec.offset());
            T tuple;
            if (stringToTupleFn != null)
                tuple = stringToTupleFn.apply(new StringConsumerRecord(rec));
            else
                tuple = byteToTupleFn.apply(new ByteConsumerRecord(rec));
            eventSubmitter.accept(tuple);
        }
        catch (Exception e) {
            String tp = String.format("[%s,%d]", rec.topic(), rec.partition());
            trace.error("{} failure processing record from {}", id(), tp, e);
        }
    }
    
    private String id() {
        if (id == null) {
            // include our short object Id
            id = connector.id() + " SUB " + toString().substring(toString().indexOf('@') + 1);
        }
        return id;
    }

    @Override
    public void close() throws Exception {
        connector.close(this);
    }
    
    private static abstract class ConsumerRecordBase<K,V>
                        implements KafkaConsumer.ConsumerRecord<K,V> {
        protected final MessageAndMetadata<byte[], byte[]> rec;
        
        ConsumerRecordBase(MessageAndMetadata<byte[], byte[]> rec) {
            this.rec = rec;
        }
        
        public abstract K key();
        public abstract V value();

        @Override
        public String topic() { return rec.topic(); };
        @Override
        public int partition() { return rec.partition(); }
        @Override
        public long offset() { return rec.offset(); }
    }
    
    private static class ByteConsumerRecord extends ConsumerRecordBase<byte[],byte[]>
                                implements KafkaConsumer.ByteConsumerRecord {
        
        ByteConsumerRecord(MessageAndMetadata<byte[], byte[]> rec) {
            super(rec);
        }

        @Override
        public byte[] key() { return rec.key(); }
        @Override
        public byte[] value() { return rec.message(); }
    }
    
    private static class StringConsumerRecord extends ConsumerRecordBase<String,String>
                                implements KafkaConsumer.StringConsumerRecord {
        
        StringConsumerRecord(MessageAndMetadata<byte[], byte[]> rec) {
            super(rec);
        }

        @Override
        public String key() {
            byte[] key = rec.key();
            if (key == null)
                return null;
            return new String(key, StandardCharsets.UTF_8);
        }
        @Override
        public String value() {
            return new String(rec.message(), StandardCharsets.UTF_8);
        }
    }
    
}
