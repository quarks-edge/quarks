/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.http;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import quarks.function.Supplier;

/**
 * Creation of HTTP Clients.
 * 
 * This methods are called at runtime to create
 * HTTP clients for {@link HttpStreams}. They are
 * passed into methods such as
 * @link {@link HttpStreams#requests(quarks.topology.TStream, Supplier, quarks.function.Function, quarks.function.Function, quarks.function.BiFunction)}
 * as functions, for example:
 * <UL style="list-style-type:none">
 * <LI>{@code () -> HttpClients::noAuthentication } // using a method reference</LI>
 *  <LI>{@code () -> HttpClients.basic("user", "password") } // using a lambda expression</LI>
 * </UL>
 *
 * @see HttpStreams
 */
public class HttpClients {
    
    /**
     * Create HTTP client with no authentication.
     * @return HTTP client with basic authentication.
     * 
     * @see HttpStreams
     */
    public static CloseableHttpClient noAuthentication() {
        return HttpClientBuilder.create().build();
    }
    
    /**
     * Create a basic authentication HTTP client with a fixed user and password.
     * @param user User for authentication
     * @param password Password for authentication
     * @return HTTP client with basic authentication.
     * 
     * @see HttpStreams
     */
    public static CloseableHttpClient basic(String user, String password) {
        return basic(()->user, ()->password);
    }
    
    /**
     * Method to create a basic authentication HTTP client.
     * The functions {@code user} and {@code password} are called
     * when this method is invoked to obtain the user and password
     * and runtime.
     * 
     * @param user Function that provides user for authentication
     * @param password  Function that provides password for authentication
     * @return HTTP client with basic authentication.
     * 
     * @see HttpStreams
     */
    public static CloseableHttpClient basic(Supplier<String> user, Supplier<String> password) {

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(user.get(), password.get()));
 
        return HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();
    }
}
