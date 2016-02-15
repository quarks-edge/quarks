/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.oplet.core.mbeans;

import java.util.concurrent.TimeUnit;

/**
 * Control interface for a periodic oplet.
 * 
 * @see quarks.oplet.core.PeriodicSource
 *
 */
public interface PeriodicMXBean {
    
    /**
     * Get the period.
     * @return period
     */
    public long getPeriod();
    
    /**
     * Get the time unit for {@link #getPeriod()}.
     * @return time unit
     */
    public TimeUnit getUnit();
    
    /**
     * Set the period.
     */
    public void setPeriod(long period);
}
