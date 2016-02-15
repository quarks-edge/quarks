/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2016 
*/
package quarks.analytics.math3.stat;

import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import quarks.analytics.math3.json.JsonAnalytics;
import quarks.analytics.math3.json.JsonUnivariateAggregate;
import quarks.analytics.math3.json.JsonUnivariateAggregator;

/**
 * Statistic implementations.
 * 
 * Univariate statistic aggregate calculations against a value
 * extracted from a {@code JsonObject}.
 * 
 * @see JsonAnalytics
 */
public enum Statistic implements JsonUnivariateAggregate {
    
    /**
     * Calculate the arithmetic mean.
     * The mean value is represented as a {@code double}
     * with the key {@code MEAN} in the aggregate result.
     */
    MEAN(new Mean()),
    /**
     * Calculate the minimum.
     * The minimum value is represented as a {@code double}
     * with the key {@code MIN} in the aggregate result.
     */
    MIN(new Min()),
    /**
     * Calculate the maximum.
     * The maximum value is represented as a {@code double}
     * with the key {@code MAXIMUM} in the aggregate result.
     */
    MAX(new Max()),
    /**
     * Calculate the sum.
     * The sum is represented as a {@code double}
     * with the key {@code SUM} in the aggregate result.
     */
    SUM(new Sum()),
    /**
     * Calculate the standard deviation.
     */
    STDDEV(new StandardDeviation());

    private final StorelessUnivariateStatistic statImpl;

    private Statistic(StorelessUnivariateStatistic statImpl) {
        this.statImpl = statImpl;
        statImpl.clear();
    }

    /**
     * Return a new instance of this statistic implementation.
     * @return A new instance of this statistic implementation.
     */
    @Override
    public JsonUnivariateAggregator get() {
        return new JsonStorelessStatistic(this, statImpl.copy());
    }
}