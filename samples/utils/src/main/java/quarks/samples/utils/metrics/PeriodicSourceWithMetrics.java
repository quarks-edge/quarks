/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.utils.metrics;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricRegistry;

import quarks.metrics.Metrics;
import quarks.metrics.MetricsSetup;
import quarks.providers.direct.DirectProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

public class PeriodicSourceWithMetrics {
    public static void main(String[] args) throws InterruptedException {

        DirectProvider tp = new DirectProvider();

        Topology t = tp.newTopology("PeriodicSource");

        Random r = new Random();
        TStream<Double> gaussian = t.poll(() -> r.nextGaussian(), 1, TimeUnit.SECONDS);

        // Testing Peek
        gaussian = gaussian.peek(g -> System.out.println("R:" + g));

        // Measure the tuple count for the gaussian TStream
        gaussian = Metrics.counter(gaussian);
        
        // A filter
        gaussian = gaussian.filter(g -> g > 0.5);

        // Measure tuple arrival rate after filtering
        gaussian = Metrics.rateMeter(gaussian);

        // A transformation
        TStream<String> gs = gaussian.map(g -> "G:" + g + ":");
        gs.print();

        // Initialize the metrics service
        MetricRegistry metrics = new MetricRegistry();
        
        // Start metrics JMX reporter
        MetricsSetup.withRegistry(tp.getServices(), metrics).startJMXReporter(
                PeriodicSourceWithMetrics.class.getName());

        // Submit the topology
        tp.submit(t);
    }
}
