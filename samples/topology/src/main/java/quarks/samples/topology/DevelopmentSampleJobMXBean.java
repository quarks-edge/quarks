/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.samples.topology;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import quarks.console.server.HttpServer;
import quarks.providers.development.DevelopmentProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

public class DevelopmentSampleJobMXBean {
    public static void main(String[] args) throws Exception {
        DevelopmentProvider dtp = new DevelopmentProvider();
        
        Topology t = dtp.newTopology("DevelopmentSampleJobMXBean");
        
        Random r = new Random();
        
        TStream<Double> d  = t.poll(() -> r.nextGaussian(), 100, TimeUnit.MILLISECONDS);
        
        d.sink(tuple -> System.out.print("."));
        
        dtp.submit(t);
        
        System.out.println(dtp.getServices().getService(HttpServer.class).getConsoleUrl());
        
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        StringBuffer sbuf = new StringBuffer();
        sbuf.append(DevelopmentProvider.JMX_DOMAIN);
        sbuf.append(":interface=");
        sbuf.append(ObjectName.quote("quarks.graph.execution.mbeans.JobMXBean"));
        sbuf.append(",type=");
        sbuf.append(ObjectName.quote("job"));
        sbuf.append(",*");
        
        System.out.println("Looking for MBeans of type job: " + sbuf.toString());
        
        ObjectName jobObjName = new ObjectName(sbuf.toString());
        Set<ObjectInstance> jobInstances = mBeanServer.queryMBeans(jobObjName, null);
        Iterator<ObjectInstance> jobIterator = jobInstances.iterator();

        while (jobIterator.hasNext()) {
        	ObjectInstance jobInstance = jobIterator.next();
        	ObjectName objectName = jobInstance.getObjectName();

        	String jobId = (String) mBeanServer.getAttribute(objectName, "Id");
        	String jobName = (String) mBeanServer.getAttribute(objectName, "Name");
        	String jobCurState = (String) mBeanServer.getAttribute(objectName, "CurrentState");
        	String jobNextState = (String) mBeanServer.getAttribute(objectName, "NextState");
        	
        	System.out.println("Found a job with JobId: " + jobId + " Name: " + jobName + " CurrentState: " + jobCurState + " NextState: " + jobNextState);
        }
    }
}
