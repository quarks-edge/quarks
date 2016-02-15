/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.topology;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import quarks.console.server.HttpServer;
import quarks.metrics.Metrics;
import quarks.providers.development.DevelopmentProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

public class DevelopmentMetricsSample {

    public static void main(String[] args) throws Exception {
        DevelopmentProvider dtp = new DevelopmentProvider();
        DevelopmentProvider dtp2 = new DevelopmentProvider();
        
        Topology t = dtp.newTopology("DevelopmentMetricsSample");
        Topology t2 = dtp2.newTopology("another one");
        
        Random r = new Random();
        Random r2 = new Random();
        TStream<Double> gaussian = t.poll(() -> r.nextGaussian(), 1, TimeUnit.SECONDS);
        
        TStream<Double> gaussian2 = t2.poll(() -> r2.nextGaussian(), 1, TimeUnit.SECONDS);

        // A filter
        gaussian = gaussian.filter(g -> g > 0.5);
        
        // Measure tuple arrival rate after filtering
        gaussian = Metrics.rateMeter(gaussian);

        // A transformation
        @SuppressWarnings("unused")
        TStream<String> gs = gaussian.map(g -> "G:" + g + ":");
        @SuppressWarnings("unused")
        TStream<String> gs2 = gaussian2.map(g -> "G:" + g + ":");
        
        dtp.submit(t);
        dtp2.submit(t2);
        
        System.out.println(dtp2.getServices().getService(HttpServer.class).getConsoleUrl());
        
        Thread.sleep(1000000);
    }
}
