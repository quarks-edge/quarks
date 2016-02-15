/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.runtime;

import java.io.Serializable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

/**
 * An abstract class for general connector connection management.
 * <p>
 * The basic (default) model is to maintain a connection across
 * inadvertent disconnects.  
 * <p>
 * A connected connector is returned
 * whenever {@link #client()} is called.  If the connection is lost,
 * actions are taken to reestablish it.  Overall tracking and
 * control of connection state is performed and
 * general logging is performed.
 * <p>
 * Sub-classes are able to further control when initial connection
 * occurs and generally expose ways to explicitly disconnect or connect.
 * <p>
 * Optionally, clients can request automatic disconnection after
 * a period of inactivity - see {@link #setIdleTimeout(long, TimeUnit)}.
 * Support for this feature requires connector implementations
 * to call {@link #notIdle()}.
 * <p>
 * TODO - dynamic control:
 * <ul>
 * <li>[abstract] ConnectorControl { getState(), connect(), disconnect(), ...}.</li>
 * <li>MqttConnectorControl extends ConnectorControl.  Adds subscribe(), unsubscribe().</li>
 * <li>Add Consumer<MqttConnectorControl> to MqttConfig. Runtime calls it supplying a connector control object.  By doing a disconnect()/connect(), the Supplier<MqttConfig> will be called to as part of reconnect -- hence using updated values if any.</li>
 * </ul>
 * <p>
 * Sub-classes are responsible for implementing a small number
 * of operations: {@link #doConnect(Object)}, {@link #doDisconnect(Object)},
 * {@link #doClose(Object)}, and {@link #id()}.  Sub-classes are also responsible for
 * calling {@link #connectionLost(Throwable)} and {@link #notIdle()}.
 * 
 * @param <T> type of the underlying managed connector (e.g., MqttClient)
 */
public abstract class Connector<T> implements AutoCloseable, Serializable {

    private static final long serialVersionUID = 1L;
    private static final long BASE_RETRY_WAIT_MSEC = 2*1000;
    private static final long MAX_RETRY_WAIT_MSEC = 60*1000;
    private final IdleManager idleManager;
    private State state = State.DISCONNECTED;
    private T client; // must be non-null when state==CONNECTED
    private Future<?> connectFuture;
    
    // TODO proper "operator-acquired" thread/scheduler w/thread factory, etc
    // hmm... today a single [Mqtt]Connector supports multiple publish operators
    // and a subscribe operator, hence the threads should come from... ?
    private final ExecutorService connectExecutor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService schedExecutor = Executors.newScheduledThreadPool(0);
    
    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        CLOSING,
        CLOSED
    }

    protected Connector() {
        idleManager = new IdleManager(this, schedExecutor);
    }

    public abstract Logger getLogger();
    
    /**
     * Must be called by the connector when an action is performed that
     * qualifies as a "not idle" condition.
     */
    public void notIdle() {
        idleManager.notIdle();
    }

    /**
     * Disconnect the connector after the specified period of inactivity.
     * @param idleTimeout
     * @param unit
     */
    public void setIdleTimeout(long idleTimeout, TimeUnit unit) {
        idleManager.setIdleTimeoutMsec(unit.toMillis(idleTimeout));
    }

    public void setIdleReconnectInterval(int intervalSec) {
        idleManager.setIdleReconnectInterval(intervalSec);
    }

    /**
     * Get a connected client connector.
     * @return the client connector
     * @throws IllegalStateException if state is inappropriate to return connected client connector 
     * @throws Exception if unable to connect
     */
    public T client() throws Exception {
        return connectInternal();
    }

    // Expose with client driven dynamic disconnect/reconnect
//    /**
//     * Connect the client connector if necessary.
//     * Blocks until connected or it's no longer valid to request a connect().
//     * @throws IllegalStateException if a connect() is (no longer) appropriate. 
//     */
//    public void connect() throws Exception {
//        connectInternal();
//    }

    /**
     * Connect the client connector if necessary.
     * Blocks until connected or it's no longer valid to request a connect().
     * @return connected client connector
     * @throws IllegalStateException if a connect() is (no longer) appropriate. 
     */
    private T connectInternal() throws Exception {
        // N.B. have to deal with multiple concurrent connect requests
        // e.g., multiple publishers and a single subscriber for a connector.
        Future<?> f = null;
        synchronized(this) {
            if (state == State.CONNECTED)
                return client;
            else if (state == State.CONNECTING)
                f = connectFuture;
            else if (state == State.DISCONNECTED) {
                startAsyncConnect();
                f = connectFuture;
            }
            else
                throw wrongState(state, "connectInternal()");
        }
        awaitDone(f); // throws if task not successful
        return connectedClient();
    }
    
    private IllegalStateException wrongState(State s, String op) {
        String msg = String.format("%s %s wrong state %s", id(), op, s);
        getLogger().error(msg);
        return new IllegalStateException(msg);
    }
    
    /**
     * Get the connected client; throw if not connected. 
     * @return connected client
     * @throws IllegalStateException if not connected
     */
    private T connectedClient() {
        synchronized(this) {
            if (state != State.CONNECTED)
                throw wrongState(state, "connectedClient()");
            return client;
        }
    }
    
    /**
     * Schedule an async connect task.
     * <p>
     * Updates connectFuture if successfully initiated.
     * Updates state and client once successfully completed.
     * @throws IllegalStateException if inappropriate state for async connect request
     */
    private void startAsyncConnect() {
        synchronized(this) {
            if (state != State.DISCONNECTED)
                throw wrongState(state, "startAsyncConnect()");
            getLogger().trace("{} submitting async connect task", id());
            setStateUnsafe(State.CONNECTING);
            connectFuture = connectExecutor.submit(
                    () -> { connectTask(); return null; });
        }
    }
    
    /**
     * Wait till task is done.
     * @throws Exception if task wasn't successful
     */
    private void awaitDone(Future<?> f) throws Exception {
        try {
            getLogger().trace("{} awaiting done", id());
            f.get();
        } 
        catch (InterruptedException e) {
            // assume that if we're cancelled we want to cancel the future task
            getLogger().trace("{} awaitDone() interrupted, cancelling task", id());
            f.cancel(true);
            throw e;
        } 
        catch (CancellationException e) {
            String msg = String.format("%s awaitDone() task was cancelled", id());
            getLogger().trace(msg);
            throw new IllegalStateException(msg);
        } 
        catch (ExecutionException e) {
            String msg = String.format("%s awaitDone() task failed", id());
            getLogger().error(msg);
            throw new IllegalStateException(msg, e.getCause());
        }
    }
    
    /**
     * Do a blocking connect with retries.
     * <p>
     * Updates state and client when successful.
     * @throws IllegalStateException if state no longer appropriate for connect.
     * @throws Exception some other problem.
     */
    private void connectTask() throws Exception {
        int retryCnt = 0;
        while(true) {
            try {
                oneConnect(retryCnt++);
                return;
            }
            catch (IllegalStateException e) {
                throw e;  // already logged
            }
            catch (Exception e) {
                // already logged
                long msec = getConnectRetryDelayMsec(retryCnt);
                getLogger().info("{} connectTask() waiting {}msec to retry", id(), msec);
                Thread.sleep(msec);
            }
        }
    }
    
    /**
     * Do a blocking single connect attempt.
     * <p>
     * Updates state and client and idle task when successful.
     * @throws IllegalStateException if state no longer appropriate for connect.
     * @throws Exception if connect failed.
     */
    private void oneConnect(int tryCnt) throws Exception {
        synchronized(this) {
            if (state != State.CONNECTING)
                throw wrongState(state, "oneConnect()");
        }
        getLogger().trace("{} doing one connect", id());

        T result;
        try {
            result = doConnect(client);
        }
        catch (Exception e) {
            getLogger().error("{} doConnect() failed", id(), e);
            throw e;
        }
        
        getLogger().trace("{} connected", id());
        synchronized(this) {
            if (state != State.CONNECTING) {
                // need to disconnect/close result?
                throw wrongState(state, "oneConnect()");
            }
            setStateUnsafe(State.CONNECTED);
            client = result;
            idleManager.connected();
        }
    }
    
    /**
     * Get the next connect retry delay.
     * @param retryCnt the retry attempt number
     * @return the delay interval in msec
     */
    private long getConnectRetryDelayMsec(int retryCnt) {
        int factor = retryCnt <= 1 ? 1 : 2 << Math.min(retryCnt - 2, 8);
        return Math.min(BASE_RETRY_WAIT_MSEC * factor, MAX_RETRY_WAIT_MSEC);
    }

    // Expose with client driven dynamic disconnect/reconnect
//    /**
//     * Disconnect the client connector.
//     * <p>
//     * Subsequently, a connect() may performed to reconnect.
//     * @throws IllegalStateException inappropriate state to request disconnect
//     */
//    public void disconnect() {
//        disconnect(false);
//    }
    
    /**
     * Disconnect the client connector.
     * <p>
     * Subsequently, a connect() may performed to reconnect.
     * @param wasIdle true if being disconnected due to an idle connection
     * @throws IllegalStateException inappropriate state to request disconnect
     */
    void disconnect(boolean wasIdle) {
        synchronized(this) {
            if (!(state == State.CONNECTED || state == State.CONNECTING))
                throw wrongState(state, "disconnect("+wasIdle+")");
            try {
                getLogger().trace("Connection {} disconnecting wasIdle:{}", id(), wasIdle);
                setStateUnsafe(State.DISCONNECTING);
                cancelConnectTaskUnsafe();
                doDisconnect(client);
            }
            catch (Exception e) {
                getLogger().error("{} disconnnect() failed", id(), e);
            }
            finally {
                setStateUnsafe(State.DISCONNECTED);
                idleManager.disconnected(wasIdle);
            }
        }
    }
    
    /**
     * Permanently close the client connector.
     * <p>
     * Expect any subsequent operations to fail.
     * @throws Exception shouldn't happen
     */
    @Override
    public void close() throws Exception {
        synchronized(this) {
            if (state == State.CLOSED) {
                getLogger().trace("{} close() state already {}", id(), state);
                return;
            }
            try {
                getLogger().info("Connection {} closing", id());
                setStateUnsafe(State.CLOSING);
                idleManager.close();
                cancelConnectTaskUnsafe();
                if (client != null)
                    doClose(client);
            }
            catch (Exception e) {
                getLogger().error("{} close() failed", id(), e);
            }
            finally {
                setStateUnsafe(State.CLOSED);
                connectFuture = null;
                client = null;
                connectExecutor.shutdownNow();
                schedExecutor.shutdownNow();
            }
        }
    }

    /**
     * Cancel a pending connect task if any.
     */
    private void cancelConnectTaskUnsafe() {
        if (connectFuture != null && !connectFuture.isDone()) {
            getLogger().trace("{} cancelConnect()", id());
            connectFuture.cancel(true);
        }
    }

    /**
     * To be called by the connector when it detects a connection lost condition.
     * <p>
     * An asynchronous reconnect is initiated if appropriate.
     * @param t the cause.  may be null.
     */
    protected void connectionLost(Throwable t) {
        synchronized(this) {
            getLogger().info("Connection {} connectionLost()", id(), t);
            if (state == State.CONNECTED) {
                setStateUnsafe(State.DISCONNECTED);
                // don't allow unwind
                try {
                    startAsyncConnect();
                }
                catch (Exception e) {
                    getLogger().error("{} startAsyncConnect() failed", id(), e);
                }
            }
            else {
                getLogger().trace("{} connectionLost() state already {}", id(), state);
            }
        }
    }
    
    private void setStateUnsafe(State state) {
        State prev = this.state;
        this.state = state;
        getLogger().info("{} state {} (was {})", id(), state, prev);
    } 
    
    State getState() {
        synchronized(this) {
            return state;
        }
    }

    /**
     * A one-shot request to connect the client to its server.
     * @param client the connector's client object.
     * @throws Exception if unable to connect
     */
    protected abstract T doConnect(T client) throws Exception;

    /**
     * A one-shot request to disconnect the client.
     * @param client the connector's client object.
     * @throws Exception if unable to disconnect
     */
    protected abstract void doDisconnect(T client) throws Exception;
    
    /**
     * A one-shot request to permanently close the client.
     * @param client the connector's client object.
     * @throws Exception if unable to close
     */
    protected abstract void doClose(T client) throws Exception;
    
    /** 
     * Get a connector id to use in log and exception msgs 
     */
    protected abstract String id();

}
