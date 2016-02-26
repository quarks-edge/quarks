/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.connectors.serial;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import quarks.function.BiConsumer;
import quarks.function.BiFunction;
import quarks.topology.TStream;
import quarks.topology.TopologyElement;

/**
 * Generic interface for a serial port.
 *
 */
public interface SerialDevice extends TopologyElement {
	
	/**
	 * Set the initialization function for this port.
	 * Can be used to send setup instructions to the
	 * device connected to this serial port.
	 * <BR>
	 * {@code initializer.accept(out, in)} is called once, passing the output
	 * and input streams for this port.
	 * 
	 * @param initializer Function to be called when the application runs.
	 */
	public void setInitializer(BiConsumer<OutputStream,InputStream> initializer);
		
	/**
	 * Poll the serial port.
	 * <BR>
	 * {@code driver.apply(out, in)} is called approximately every {@code period}
	 * seconds, passing the output and input streams for this port. The returned
	 * value is present on the returned stream if it is not null.
	 * 
	 * 
	 * @param driver Function to poll this serial port.
	 * @param period Polling period.
	 * @param unit Unit of {@code period}.
	 * @return Declaration of stream that will contain the results of calls to {@code driver}.
	 */
	public <T> TStream<T> poll(BiFunction<OutputStream,InputStream,T> driver, long period, TimeUnit unit);
}
