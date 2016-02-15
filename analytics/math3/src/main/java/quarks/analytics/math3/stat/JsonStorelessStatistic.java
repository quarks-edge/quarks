/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.analytics.math3.stat;

import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import quarks.analytics.math3.json.JsonUnivariateAggregator;

/**
 * JSON univariate aggregator implementation wrapping a {@code StorelessUnivariateStatistic}.
 */
public class JsonStorelessStatistic implements JsonUnivariateAggregator {
        
    private final Statistic stat;
    private final StorelessUnivariateStatistic statImpl;
    
    public JsonStorelessStatistic(Statistic stat, StorelessUnivariateStatistic statImpl) {
        this.stat = stat;
        this.statImpl = statImpl;
    }

    @Override
    public void clear(JsonElement partition, int n) {
        statImpl.clear();
    }

    @Override
    public void increment(double v) {
        statImpl.increment(v);
    }

    @Override
    public void result(JsonElement partition, JsonObject result) {        
        double rv = statImpl.getResult();
        
        if (Double.isFinite(rv))
            result.addProperty(stat.name(), rv);
    }

}
