/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015  
*/
package quarks.runtime.etiao.mbeans;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import quarks.execution.mbeans.JobMXBean;
import quarks.runtime.etiao.EtiaoJob;
import quarks.runtime.etiao.graph.model.GraphType;

/**
 * Implementation of a JMX control interface for a job.
 */
public class EtiaoJobBean implements JobMXBean {
    private final EtiaoJob job;
    public EtiaoJobBean(EtiaoJob job) {
        this.job = job;
    }

    @Override
    public String getId() {
        return job.getId();
    }

    @Override
    public String getName() {
        return job.getName();
    }

    @Override
    public State getCurrentState() {
        return State.fromString(job.getCurrentState().name());
    }

    @Override
    public State getNextState() {
        return State.fromString(job.getNextState().name());
    }

    @Override
    public String graphSnapshot() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(new GraphType(job.graph()));
    }
}
