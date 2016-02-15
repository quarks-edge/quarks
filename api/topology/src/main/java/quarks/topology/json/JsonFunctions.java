/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.topology.json;

import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import quarks.function.Function;

/**
 * Utilities for use of JSON and Json Objects in a streaming topology.
 */
public class JsonFunctions {

    /**
     * Get the JSON for a JsonObject.
     * 
     * TODO consider adding an override where the caller can specify
     * the number of significant digits to include in the string representation
     * of floating point types.
     * 
     * @return the JSON
     */
    public static Function<JsonObject,String> asString() {
        return jo -> jo.toString();
    }

    /**
     * Create a new JsonObject from JSON
     * @return the JsonObject
     */
    public static Function<String,JsonObject> fromString() {
        JsonParser jp = new JsonParser();
        return json -> jp.parse(json).getAsJsonObject();
    }

    /**
     * Get the UTF-8 bytes representation of the JSON for a JsonObject.
     * @return the byte[]
     */
    public static Function<JsonObject,byte[]> asBytes() {
        return jo -> jo.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Create a new JsonObject from the UTF8 bytes representation of JSON
     * @return the JsonObject
     */
    public static Function<byte[],JsonObject> fromBytes() {
        JsonParser jp = new JsonParser();
        return jsonbytes -> jp.parse(new String(jsonbytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }

}
