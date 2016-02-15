/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/

package quarks.connectors.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import quarks.function.BiFunction;

/**
 * Functions to process HTTP requests.
 *
 * @see HttpStreams
 */
public class HttpResponders {
    
    /**
     * Return the input tuple on OK.
     * Function that returns null (no output) if the HTTP response
     * is not OK (200), otherwise it returns the input tuple.
     * The HTTP entity in the response is consumed and discarded.
     * @param <T> Type of input tuple.
     * @return Function that returns the input tuple on OK.
     */
    public static <T> BiFunction<T,CloseableHttpResponse,T> inputOn200() {
        return inputOn(HttpStatus.SC_OK);
    }
    
    /**
     * Return the input tuple on specified codes.
     * Function that returns null (no output) if the HTTP response
     * is not one of {@code codes}, otherwise it returns the input tuple.
     * The HTTP entity in the response is consumed and discarded.
     * @param <T> Type of input tuple.
     * @param codes HTTP status codes to result in output
     * @return Function that returns the input tuple on matching codes.
     */
    public static <T> BiFunction<T, CloseableHttpResponse, T> inputOn(Integer... codes) {
        Set<Integer> uniqueCodes = new HashSet<>(Arrays.asList(codes));
        return (t, resp) -> {
            int sc = resp.getStatusLine().getStatusCode();
            try {
                EntityUtils.consume(resp.getEntity());
            } catch (Exception e) {
                ;
            }
            return uniqueCodes.contains(sc) ? t : null;
        };
    }
    
    /**
     * A HTTP response handler for {@code application/json}.
     * 
     * For each HTTP response a JSON object is produced that contains:
     * <UL>
     * <LI> {@code request} - the original input tuple that lead to the request  </LI>
     * <LI> {@code response} - JSON object containing information about the response</LI>
     * <UL>
     *    <LI> {@code status} - Status code for the response as an integer</LI>
     *    <LI> {@code entity} - JSON response entity if one exists </LI>
     * </UL>
     * </UL>
     * 
     * @return Function that will process the {@code application/json} responses.
     */
    public static BiFunction<JsonObject, CloseableHttpResponse, JsonObject> json() {
        return (request,resp) -> {
            JsonObject out = new JsonObject();
            out.add("request", request);
            JsonObject response = new JsonObject();
            out.add("response", response);
            response.addProperty("status", resp.getStatusLine().getStatusCode());
            JsonStreamParser reader;
            try {
                reader = new JsonStreamParser(
                        new InputStreamReader(resp.getEntity().getContent(), StandardCharsets.UTF_8));
                if (reader.hasNext())
                    response.add("entity", reader.next());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            return out;
            
        };
    }
}
