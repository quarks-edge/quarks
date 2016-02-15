/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.samples.topology;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import quarks.console.server.HttpServer;
import quarks.providers.development.DevelopmentProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

public class DevelopmentSample {
    
    public static void main(String[] args) throws Exception {
        DevelopmentProvider dtp = new DevelopmentProvider();
        
        Topology t = dtp.newTopology("DevelopmentSample");
        
        Random r = new Random();
        
        TStream<Double> d  = t.poll(() -> r.nextGaussian(), 100, TimeUnit.MILLISECONDS);
        
        d.sink(tuple -> System.out.print("."));
        
        dtp.submit(t);
        
        System.out.println(dtp.getServices().getService(HttpServer.class).getConsoleUrl());
    }
}
