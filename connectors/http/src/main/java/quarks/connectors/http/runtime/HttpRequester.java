/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.http.runtime;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

import quarks.function.BiFunction;
import quarks.function.Function;
import quarks.function.Supplier;

/**
 * Function that processes HTTP requests at runtime.
 */
public class HttpRequester<T,R> implements Function<T,R>{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private final Supplier<CloseableHttpClient> clientCreator;
    private final Function<T,String> method;
    private final Function<T,String> url;
    private final BiFunction<T,CloseableHttpResponse,R> responseProcessor;
    
    private CloseableHttpClient client;
       
    public HttpRequester(
            Supplier<CloseableHttpClient> clientCreator,
            Function<T,String> method,
            Function<T,String> url,
            BiFunction<T,CloseableHttpResponse,R> responseProcessor) {
        this.clientCreator = clientCreator;
        this.method = method;
        this.url = url;
        this.responseProcessor = responseProcessor;
    }
    

    @Override
    public R apply(T t) {
        
        if (client == null)
            client = clientCreator.get();
        
        String m = method.apply(t);
        String uri = url.apply(t);
        HttpUriRequest request;
        switch (m) {
        case HttpGet.METHOD_NAME:          
            request = new HttpGet(uri);
            break;
        case HttpDelete.METHOD_NAME:          
            request = new HttpDelete(uri);
            break;

            default:
                throw new IllegalArgumentException();
        }
        
        try {
            try (CloseableHttpResponse response = client.execute(request)) {
                return responseProcessor.apply(t, response);
            }
             
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
