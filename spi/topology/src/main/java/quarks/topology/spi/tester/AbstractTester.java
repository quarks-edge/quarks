/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.topology.spi.tester;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

import quarks.execution.Job;
import quarks.execution.Job.State;
import quarks.execution.Submitter;
import quarks.topology.Topology;
import quarks.topology.tester.Condition;
import quarks.topology.tester.Tester;

public abstract class AbstractTester implements Tester { 
    
    private Job job;

    @Override
    public boolean complete(Submitter<Topology, ? extends Job> submitter, JsonObject config, Condition<?> endCondition,
            long timeout, TimeUnit unit) throws Exception {

        long tmoMsec = Math.max(unit.toMillis(timeout), 1000);
        long maxTime = System.currentTimeMillis() + tmoMsec;

        Future<?> future = submitter.submit(topology(), config);
        // wait at most tmoMsec for the submit to create the job
        job = (Job) future.get(tmoMsec, TimeUnit.MILLISECONDS);

        // wait for the first of: endCondition, jobComplete, tmo
        while (!endCondition.valid()
                && getJob().getCurrentState() != State.CLOSED
                && System.currentTimeMillis() < maxTime) {
            Thread.sleep(100);
        }
        
        if (getJob().getCurrentState() != State.CLOSED)
            getJob().stateChange(Job.Action.CLOSE);

        return endCondition.valid();
    }
    
    @Override
    public Job getJob() {
        return job;
    }
    
    @Override
    public Condition<Boolean> and(final Condition<?>... conditions) {
        return new Condition<Boolean>() {

            @Override
            public boolean valid() {
                for (Condition<?> condition : conditions)
                    if (!condition.valid())
                        return false;
                return true;
            }

            @Override
            public Boolean getResult() {
                return valid();
            }
        };
    }
}
