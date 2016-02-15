/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import quarks.execution.Job;

/**
 * Utilities for connector samples.
 */
public class Util {

    /**
     * Generate a simple timestamp with the form {@code HH:mm:ss.SSS}
     * @return the timestamp
     */
    public static String simpleTS() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }

    
    /**
     * Wait for the job to reach the specified state.
     * <p>
     * A placeholder till GraphJob directly supports awaitState()?
     * @param job
     * @param state
     * @param timeout specify -1 to wait forever (until interrupted)
     * @param unit may be null if timeout is -1
     * @return true if the state was reached, false otherwise: the time limit
     * was reached of the thread was interrupted.
     */
    public static boolean awaitState(Job job, Job.State state, long timeout, TimeUnit unit) {
        long endWait = -1;
        if (timeout != -1) {
            endWait = System.currentTimeMillis()
                        + unit.toMillis(timeout);
        }
        while (true) {
            Job.State curState = job.getCurrentState();
            if (curState == state)
                return true;
            if (endWait != -1) {
                long now = System.currentTimeMillis();
                if (now >= endWait)
                    return false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

}
