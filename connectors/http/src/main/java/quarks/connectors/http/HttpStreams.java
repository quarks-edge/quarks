/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.JsonObject;

import quarks.connectors.http.runtime.HttpRequester;
import quarks.function.BiFunction;
import quarks.function.Function;
import quarks.function.Supplier;
import quarks.topology.TStream;


/**
 * HTTP streams.
 *
 */
public class HttpStreams {
    
    public static TStream<JsonObject> getJson(TStream<JsonObject> stream,
            Supplier<CloseableHttpClient> clientCreator,
            Function<JsonObject,String> uri) {
        
        return HttpStreams.<JsonObject,JsonObject>requests(stream, clientCreator,
                t -> HttpGet.METHOD_NAME, uri, HttpResponders.json());
    }
    
    /**
     * Make an HTTP request for each tuple on a stream.
     * <UL>
     * <LI>{@code clientCreator} is invoked once to create a new HTTP client
     * to make the requests.
     * </LI>
     *  <LI>
     * {@code method} is invoked for each tuple to define the method
     * to be used for the HTTP request driven by the tuple. A fixed method
     * can be declared using a function such as:
     * <UL style="list-style-type:none"><LI>{@code t -> HttpGet.METHOD_NAME}</LI></UL>
     *  </LI>
     *  <LI>
     * {@code uri} is invoked for each tuple to define the URI
     * to be used for the HTTP request driven by the tuple. A fixed method
     * can be declared using a function such as:
     * <UL style="list-style-type:none"><LI>{@code t -> "http://www.example.com"}</LI></UL>
     *  </LI>
     *  <LI>
     *  {@code response} is invoked after each request that did not throw an exception.
     *  It is passed the input tuple and the HTTP response. The function must completely
     *  consume the entity stream for the response. The return value is present on
     *  the stream returned by this method if it is non-null. A null return results
     *  in no tuple on the returned stream.
     *  
     *  </LI>
     *  </UL>
     *  
     * @param stream Stream to invoke HTTP requests.
     * @param clientCreator Function to create a HTTP client.
     * @param method Function to define the HTTP method.
     * @param uri Function to define the URI.
     * @param response Function to process the response.
     * @return Stream containing HTTP responses processed by the {@code response} function.
     * 
     * @see HttpClients
     * @see HttpResponders
     */
    public static <T,R> TStream<R> requests(TStream<T> stream,
            Supplier<CloseableHttpClient> clientCreator,
            Function<T,String> method,
            Function<T,String> uri,
            BiFunction<T,CloseableHttpResponse,R> response) {
        
        return stream.map(new HttpRequester<T,R>(clientCreator, method, uri, response));
    }
}

