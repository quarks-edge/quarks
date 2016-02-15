/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.runtime;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

import quarks.connectors.runtime.Connector.State;

/**
 * Manager to auto-disconnect a connector when idle and
 * subsequently auto-reconnect it.
 */
public class IdleManager {
    private final Connector<?> connector;
    private final AtomicLong idleTimeoutMsec = new AtomicLong();
    private final ScheduledExecutorService schedExecutor;
    private long idleReconnectIntervalMsec;
    private final AtomicLong lastActionMsec = new AtomicLong();
    private Future<?> idleFuture;
    private Future<?> idleReconnectFuture;
    
    /**
     * Create a new idle manager for the connector.
     * <p>
     * By default idle (disconnect) timeouts and subsequent auto-reconnect
     * is disabled. 
     * @param connector
     * @param schedExecitor
     */
    IdleManager(Connector<?> connector, ScheduledExecutorService schedExecitor) {
        this.connector = connector;
        this.schedExecutor = schedExecitor;
    }
    
    protected Logger getLogger() {
        return connector.getLogger();
    }
    
    /**
     * A "not idle" event has occurred.
     * <p>
     * If idle timeouts have been enabled, this must be called by the
     * connector when an event occur that qualifies as a "not idle" condition.
     */
    public void notIdle() {
        if (idleTimeoutMsec.get() > 0)
            lastActionMsec.set(System.currentTimeMillis());
    }

    /**
     * Disconnect the connector after the specified period of inactivity.
     * @param idleTimeoutMsec 0 to disable idle timeouts
     */
    public void setIdleTimeoutMsec(long idleTimeoutMsec) {
        getLogger().trace("{} setIdleTimeout({}msec)", id(), idleTimeoutMsec);
        this.idleTimeoutMsec.set(idleTimeoutMsec);
    }

    /**
     * Reconnect the connector after disconnect due to idleness.
     * @param intervalSec delay following disconnect until reconnect. 0 to disable.
     */
    public void setIdleReconnectInterval(int intervalSec) {
        getLogger().trace("{} setIdleReconnectInterval({}sec)", id(), intervalSec);
        idleReconnectIntervalMsec = intervalSec * 1000;
    }
    
    /**
     * To be called when the connector is being permanently closed.
     */
    public void close() {
        synchronized(this) {
            if (idleFuture != null)
                idleFuture.cancel(true);
            if (idleReconnectFuture != null)
                idleReconnectFuture.cancel(true);
        }
    }
    
    /**
     * To be called when the connector has become connected.
     */
    public void connected() {
        synchronized(this) {
            if (idleReconnectFuture != null)
                idleReconnectFuture.cancel(false);
            scheduleIdleTask(idleTimeoutMsec.get(), false);
        }
    }

    /**
     * To be called when the connector has become disconnected.
     * @param wasIdle true if the disconnect was due to an idle condition.
     */
    public void disconnected(boolean wasIdle) {
        synchronized(this) {
            if (idleFuture != null)
                idleFuture.cancel(false);
            if (wasIdle)
                scheduleIdleReconnectTask(idleReconnectIntervalMsec);
        }
    }
    
    private void scheduleIdleTask(long delayMsec, boolean isResched) {
        synchronized(this) {
            if (idleFuture != null)
                idleFuture.cancel(true);
            if (delayMsec > 0) {
                if (isResched)
                    getLogger().trace("{} scheduleIdleTask({}msec)", id(), delayMsec);
                else
                    getLogger().info("{} scheduleIdleTask({}msec)", id(), delayMsec);
                idleFuture = schedExecutor.schedule(
                        () -> idleTimeoutTask(), delayMsec, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    private void idleTimeoutTask() {
        boolean doDisconnect = false;
        try {
            synchronized(this) {
                long tmo = idleTimeoutMsec.get();
                if (tmo == 0)
                    return;
                State s = connector.getState();
                if (s != State.CONNECTED) {
                    getLogger().info("{} idleTimeoutTask() no longer connected ({})", id(), s);
                    return;
                }
                long last = lastActionMsec.get();
                long now = System.currentTimeMillis();
                if (now > last + tmo) {
                    getLogger().info("{} idleTimeoutTask() disconnecting", id());
                    doDisconnect = true;
                }
                else {
                    long adj = now - last;
                    if (adj >= tmo)
                        adj = 0;
                    long delayMsec = tmo - adj;
                    getLogger().trace("{} scheduleIdleTask({}msec)", id(), delayMsec);
                    scheduleIdleTask(delayMsec, true);
                }
            }
            if (doDisconnect)
                connector.disconnect(true);
        }
        catch (RuntimeException e) {
            getLogger().trace("{} idleTimeoutTask() disconnect failed", id(), e);
        }
    }
    
    private void scheduleIdleReconnectTask(long delayMsec) {
        synchronized(this) {
            getLogger().info("{} scheduleIdleReconnectTask({}msec)", id(), delayMsec);
            if (idleReconnectFuture != null)
                idleReconnectFuture.cancel(true);
            if (delayMsec > 0) {
                idleReconnectFuture = schedExecutor.schedule(
                        () -> idleReconnectTask(),
                        delayMsec, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    private void idleReconnectTask() {
        try {
            getLogger().info("{} idleReconnectTask() reconnecting", id());
            connector.client(); // induce reconnect
        }
        catch (Exception e) {
            getLogger().error("{} idleReconnectTask() failed", id(), e);
        }
    }

    private String id() {
        return connector.id();
    }
}
