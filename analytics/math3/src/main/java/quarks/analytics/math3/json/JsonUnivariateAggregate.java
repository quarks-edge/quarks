/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.analytics.math3.json;

import quarks.function.Supplier;

/**
 * Univariate aggregate for a JSON tuple.
 * This is the declaration of the aggregate that
 * application use when declaring a topology.
 * <P>
 * Implementations are typically enums such
 * as {@link quarks.analytics.math3.stat.Statistic Statistic}.
 * </P>
 * <P>
 * Each call to {@code get()} must return a new
 * {@link JsonUnivariateAggregator aggregator}
 * that implements the required aggregate.
 * </P>
 * 
 * @see JsonAnalytics
 */
public interface JsonUnivariateAggregate extends Supplier<JsonUnivariateAggregator>{
    
    /**
     * JSON key used for representation of the number
     * of tuples that were aggregated. Value is {@value}.
     */
    public static final String N = "N";
    
    /**
     * Name of the aggregate.
     * The returned value is used as the JSON key containing
     * the result of the aggregation.
     * @return Name of the aggregate.
     */
    public String name();
}
