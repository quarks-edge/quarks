/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.test.connectors.pubsub;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import quarks.connectors.pubsub.PublishSubscribe;
import quarks.connectors.pubsub.service.ProviderPubSub;
import quarks.connectors.pubsub.service.PublishSubscribeService;
import quarks.execution.Job;
import quarks.execution.Job.Action;
import quarks.providers.direct.DirectProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;
import quarks.topology.tester.Condition;
import quarks.topology.tester.Tester;

public class PubSubTest {

    /**
     * Test without a pub-sub service so no
     * cross job connections will be made.
     * @throws Exception
     */
    @Test
    public void testNoService() throws Exception {
        DirectProvider dp = new DirectProvider();

        TStream<String> publishedStream = createPublisher(dp, "t1", String.class, "A", "B", "C");
        Tester testPub = publishedStream.topology().getTester();
        Condition<Long> tcPub = testPub.tupleCount(publishedStream, 3);

        TStream<String> subscribedStream = createSubscriber(dp, "t1", String.class);
        Tester testSub = subscribedStream.topology().getTester();
        Condition<Long> tcSub = testSub.tupleCount(subscribedStream, 0); // Expect none

        Job js = dp.submit(subscribedStream.topology()).get();
        Job jp = dp.submit(publishedStream.topology()).get();

        Thread.sleep(1500);

        assertTrue(tcPub.valid());
        assertTrue(tcSub.valid());

        js.stateChange(Action.CLOSE);
        jp.stateChange(Action.CLOSE);
    }
    
    private <T> TStream<T> createPublisher(DirectProvider dp, String topic, Class<? super T> streamType, @SuppressWarnings("unchecked") T...values) {
        Topology publisher = dp.newTopology("Pub");
        TStream<T> stream = publisher.of(values);
        PublishSubscribe.publish(stream, topic, streamType);
        return stream;
    }
    
    private <T> TStream<T> createSubscriber(DirectProvider dp, String topic, Class<T> streamType) {
        Topology subscriber = dp.newTopology("Sub");
        return PublishSubscribe.subscribe(subscriber, topic, streamType);     
    }

    @Test
    public void testProviderServiceSingleSubscriber() throws Exception {
        DirectProvider dp = new DirectProvider();

        dp.getServices().addService(PublishSubscribeService.class, new ProviderPubSub());

        TStream<String> publishedStream = createPublisher(dp, "t1", String.class, "A", "B", "C");
        Tester testPub = publishedStream.topology().getTester();
        Condition<List<String>> tcPub = testPub.streamContents(publishedStream, "A", "B", "C");

        TStream<String> subscribedStream = createSubscriber(dp, "t1", String.class);
        Tester testSub = subscribedStream.topology().getTester();
        Condition<List<String>> tcSub = testSub.streamContents(subscribedStream, "A", "B", "C"); // Expect all tuples

        Job js = dp.submit(subscribedStream.topology()).get();
        Job jp = dp.submit(publishedStream.topology()).get();
        
        for (int i = 0; i < 30 && !tcSub.valid(); i++)
            Thread.sleep(50);

        assertTrue(tcPub.valid());
        assertTrue(tcSub.valid());

        js.stateChange(Action.CLOSE);
        jp.stateChange(Action.CLOSE);
    }
    
    @Test
    public void testProviderServiceMultipleSubscriber() throws Exception {
        DirectProvider dp = new DirectProvider();

        dp.getServices().addService(PublishSubscribeService.class, new ProviderPubSub());

        TStream<String> publishedStream = createPublisher(dp, "t1", String.class, "A", "B", "C");
        Tester testPub = publishedStream.topology().getTester();
        Condition<List<String>> tcPub = testPub.streamContents(publishedStream, "A", "B", "C");
        
        TStream<String> subscribedStream1 = createSubscriber(dp, "t1", String.class);
        Tester testSub1 = subscribedStream1.topology().getTester();
        Condition<List<String>> tcSub1 = testSub1.streamContents(subscribedStream1, "A", "B", "C");
        
        TStream<String> subscribedStream2 = createSubscriber(dp, "t1", String.class);
        Tester testSub2 = subscribedStream2.topology().getTester();
        Condition<List<String>> tcSub2 = testSub2.streamContents(subscribedStream2, "A", "B", "C");
        
        TStream<String> subscribedStream3 = createSubscriber(dp, "t1", String.class);
        Tester testSub3 = subscribedStream3.topology().getTester();
        Condition<List<String>> tcSub3 = testSub3.streamContents(subscribedStream3, "A", "B", "C");

        
        Job js1 = dp.submit(subscribedStream1.topology()).get();
        Job js2 = dp.submit(subscribedStream2.topology()).get();
        Job js3 = dp.submit(subscribedStream3.topology()).get();
        
        Job jp = dp.submit(publishedStream.topology()).get();
        
        for (int i = 0; i < 30 && !tcSub1.valid() && !tcSub2.valid() && !tcSub3.valid(); i++)
            Thread.sleep(50);

        assertTrue(tcPub.valid());
        assertTrue(tcSub1.valid());
        assertTrue(tcSub2.valid());
        assertTrue(tcSub3.valid());

        js1.stateChange(Action.CLOSE);
        js2.stateChange(Action.CLOSE);
        js3.stateChange(Action.CLOSE);
        jp.stateChange(Action.CLOSE);
    }
    
    @Test
    public void testProviderServiceMultiplePublisher() throws Exception {
        DirectProvider dp = new DirectProvider();

        dp.getServices().addService(PublishSubscribeService.class, new ProviderPubSub());

        TStream<Integer> publishedStream1 = createPublisher(dp, "i1", Integer.class, 1,2,3,82);
        Tester testPub1 = publishedStream1.topology().getTester();
        Condition<List<Integer>> tcPub1 = testPub1.streamContents(publishedStream1, 1,2,3,82);
        
        TStream<Integer> publishedStream2 = createPublisher(dp, "i1", Integer.class, 5,432,34,99);
        Tester testPub2 = publishedStream2.topology().getTester();
        Condition<List<Integer>> tcPub2 = testPub2.streamContents(publishedStream2, 5,432,34,99);
 
        TStream<Integer> publishedStream3 = createPublisher(dp, "i1", Integer.class, 35,456,888,263,578);
        Tester testPub3 = publishedStream3.topology().getTester();
        Condition<List<Integer>> tcPub3 = testPub3.streamContents(publishedStream3, 35,456,888,263,578);
 

        TStream<Integer> subscribedStream = createSubscriber(dp, "i1", Integer.class);
        Tester testSub = subscribedStream.topology().getTester();
        Condition<List<Integer>> tcSub = testSub.contentsUnordered(subscribedStream,
                1,2,3,82,5,432,34,99,35,456,888,263,578); // Expect all tuples

        Job js = dp.submit(subscribedStream.topology()).get();
        Job jp1 = dp.submit(publishedStream1.topology()).get();
        Job jp2 = dp.submit(publishedStream2.topology()).get();
        Job jp3 = dp.submit(publishedStream3.topology()).get();
        
        for (int i = 0; i < 30 && !tcSub.valid(); i++)
            Thread.sleep(50);

        assertTrue(tcPub1.valid());
        assertTrue(tcPub2.valid());
        assertTrue(tcPub3.valid());
        assertTrue(tcSub.valid());

        js.stateChange(Action.CLOSE);
        jp1.stateChange(Action.CLOSE);
        jp2.stateChange(Action.CLOSE);
        jp3.stateChange(Action.CLOSE);
    }
}
