package quarks.analytics.math3.stat;

import quarks.analytics.math3.json.JsonUnivariateAggregate;
import quarks.analytics.math3.json.JsonUnivariateAggregator;

/**
 * Univariate regression aggregates.
 *
 */
public enum Regression implements JsonUnivariateAggregate {
    
    /**
     * Calculate the slope of a single variable.
     * The slope is calculated using the first
     * order of a ordinary least squares
     * linear regression.
     * The list of values for the single
     * single variable are processed as Y-values
     * that are evenly spaced on the X-axis.
     * <BR>
     * This is useful as a simple determination
     * if the variable is increasing or decreasing.
     * <BR>
     * The slope value is represented as a {@code double}
     * with the key {@code SLOPE} in the aggregate result.
     * <BR>
     * If the window to be aggregated contains less than
     * two values then no regression is performed.
     */
    SLOPE() {
        @Override
        public JsonUnivariateAggregator get() {
            return new JsonOLS(this);
        }
    }
}
