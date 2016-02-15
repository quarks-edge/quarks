/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.analytics.math3.json;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import quarks.function.BiFunction;
import quarks.function.ToDoubleFunction;
import quarks.topology.TStream;
import quarks.topology.TWindow;

/**
 * Apache Common Math analytics for streams with JSON tuples.
 *
 */
public class JsonAnalytics {
    
    /**
     * Aggregate against a single {@code Numeric} variable contained in an JSON object.
     * 
     * The returned stream contains a tuple for each execution performed against a window partition.
     * The tuple is a {@code JsonObject} containing:
     * <UL>
     * <LI> Partition key of type {@code K} as a property with key {@code resultPartitionProperty}. </LI>
     * <LI> Aggregation results as a {@code JsonObject} as a property with key {@code valueProperty}.
     * This results object contains the results of all aggregations defined by {@code aggregates} against
     * {@code double} property with key {@code valueProperty}.
     * <BR>
     * Each {@link JsonUnivariateAggregate} declares how it represents its aggregation in this result
     * object.
     * </LI>
     * </UL>
     * <P>
     * For example if the window contains these three tuples (pseudo JSON) for
     * partition 3:
     * <BR>
     * <code>{id=3,reading=2.0}, {id=3,reading=2.6}, {id=3,reading=1.8}</code>
     * <BR>
     * the resulting aggregation for the stream returned by:
     * <BR>
     * {@code aggregate(window, "id", "reading", Statistic.MIN, Statistic.MAX)}
     * <BR>
     * would contain this tuple with the maximum and minimum values in the {@code reading}
     * JSON object:
     * <BR>
     * <code>{id=3, reading={MIN=1.8, MAX=1.8}}</code>
     * </P>
     * @param <K> Partition type
     * 
     * @param window Window to aggregate over.
     * @param resultPartitionProperty Property to store the partition key in tuples on the returned stream.
     * @param valueProperty JSON property containing the value to aggregate.
     * @param aggregates Which aggregations to be performed.
     * @return Stream that will contain aggregations.
     */
    public static <K extends JsonElement> TStream<JsonObject> aggregate(
            TWindow<JsonObject, K> window,
            String resultPartitionProperty,
            String valueProperty,
            JsonUnivariateAggregate... aggregates) {
        return aggregate(window, resultPartitionProperty, valueProperty, j -> j.get(valueProperty).getAsDouble(), aggregates);

    }
    
    /**
     * Aggregate against a single {@code Numeric} variable contained in an JSON object.
     * 
     * The returned stream contains a tuple for each execution performed against a window partition.
     * The tuple is a {@code JsonObject} containing:
     * <UL>
     * <LI> Partition key of type {@code K} as a property with key {@code resultPartitionProperty}. </LI>
     * <LI> Aggregation results as a {@code JsonObject} as a property with key {@code resultProperty}.
     * This results object contains the results of all aggregations defined by {@code aggregates} against
     * value returned by {@code valueGetter}.
     * <BR>
     * Each {@link JsonUnivariateAggregate} declares how it represents its aggregation in this result
     * object.
     * </LI>
     * </UL>
     * 
     * @param <K> Partition type
     * @param window Window to aggregate over.
     * @param resultPartitionProperty Property to store the partition key in tuples on the returned stream.
     * @param resultProperty Property to store the aggregations in tuples on the returned stream.
     * @param valueGetter How to obtain the single variable from input tuples.
     * @param aggregates Which aggregations to be performed.
     * @return Stream that will contain aggregations.
     */
    public static <K extends JsonElement> TStream<JsonObject> aggregate(
            TWindow<JsonObject, K> window,
            String resultPartitionProperty,
            String resultProperty,
            ToDoubleFunction<JsonObject> valueGetter,
            JsonUnivariateAggregate... aggregates) {

        return window.aggregate(aggregateList(
                resultPartitionProperty,
                resultProperty,
                valueGetter,
                aggregates
                ));
    }
    
    /**
     * Create a Function that aggregates against a single {@code Numeric}
     * variable contained in an JSON object.
     * 
     * Calling {@code apply(List<JsonObject>)} on the returned {@code BiFunction}
     * returns a {@link JsonObject} containing:
     * <UL>
     * <LI> Partition key of type {@code K} as a property with key {@code resultPartitionProperty}. </LI>
     * <LI> Aggregation results as a {@code JsonObject} as a property with key {@code valueProperty}.
     * This results object contains the results of all aggregations defined by {@code aggregates}
     * against the value returned by {@code valueGetter}.
     * <BR>
     * Each {@link JsonUnivariateAggregate} declares how it represents its aggregation in this result
     * object.
     * </LI>
     * </UL>
     * <P>
     * For example if the list contains these three tuples (pseudo JSON) for
     * partition 3:
     * <BR>
     * <code>{id=3,reading=2.0}, {id=3,reading=2.6}, {id=3,reading=1.8}</code>
     * <BR>
     * the resulting aggregation for the JsonObject returned by:
     * <BR>
     * {@code aggregateList("id", "reading", Statistic.MIN, Statistic.MAX).apply(list, 3)}
     * <BR>
     * would be this tuple with the maximum and minimum values in the {@code reading}
     * JSON object:
     * <BR>
     * <code>{id=3, reading={MIN=1.8, MAX=1.8}}</code>
     * </P>
     * @param <K> Partition type
     * 
     * @param resultPartitionProperty Property to store the partition key in tuples on the returned stream.
     * @param resultProperty Property to store the aggregations in the returned JsonObject.
     * @param valueGetter How to obtain the single variable from input tuples.
     * @param aggregates Which aggregations to be performed.
     * @return Function that performs the aggregations.
     */
    public static <K extends JsonElement> 
    BiFunction<List<JsonObject>, K, JsonObject> aggregateList(
            String resultPartitionProperty,
            String resultProperty,
            ToDoubleFunction<JsonObject> valueGetter,
            JsonUnivariateAggregate... aggregates) {

        BiFunction<List<JsonObject>, K, JsonObject> function = (tuples, partition) -> {
            
            final JsonUnivariateAggregator[] aggregators = new JsonUnivariateAggregator[aggregates.length];
            for (int i = 0; i < aggregates.length; i++) {
                aggregators[i] = aggregates[i].get();
            }     
            
            final JsonObject result = new JsonObject();
            result.add(resultPartitionProperty, partition);
            JsonObject aggregateResults = new JsonObject();
            result.add(resultProperty, aggregateResults);

            final int n = tuples.size();
            aggregateResults.addProperty(JsonUnivariateAggregate.N, n);
            
            if (n != 0) {

                for (JsonUnivariateAggregator agg : aggregators) {
                    agg.clear(partition, n);
                }
                for (JsonObject tuple : tuples) {
                    double v = valueGetter.applyAsDouble(tuple);
                    for (JsonUnivariateAggregator agg : aggregators) {
                        agg.increment(v);
                    }
                }
                for (JsonUnivariateAggregator agg : aggregators) {
                    agg.result(partition, aggregateResults);
                }
            }

            return result;
        };
        
        return function;
    }

}
