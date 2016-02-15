/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.analytics.math3.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Univariate aggregator for JSON tuples.
 * This is the runtime implementation interface
 * of {@link JsonUnivariateAggregate} defined aggregate.
 */
public interface JsonUnivariateAggregator {
    
    /**
     * Clear the aggregator to prepare for a new aggregation.
     * @param partitionKey Partition key.
     * @param n Number of tuples to be aggregated.
     */
    void clear(JsonElement partitionKey, int n);
    
    /**
     * Add a value to the aggregation. 
     * @param value Value to be added.
     */
    void increment(double value);
    
    /**
     * Place the result of the aggregation into the {@code result}
     * object. The key for the result must be
     * {@link JsonUnivariateAggregate#name()} for the corresponding
     * aggregate. The value of the aggregation may be a primitive value
     * such as a {@code double} or any valid JSON element.
     * 
     * @param partitionKey Partition key.
     * @param result JSON object holding the result.
     */
    void result(JsonElement partitionKey, JsonObject result);
}
