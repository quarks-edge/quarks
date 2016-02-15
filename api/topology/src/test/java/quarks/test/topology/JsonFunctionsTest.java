/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.topology;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import quarks.function.Function;
import quarks.topology.json.JsonFunctions;

public class JsonFunctionsTest {
    
    private JsonObject newTestObject() {
        // Just a mix of things so we have reasonable confidence
        // the JsonFunctions are working.
        JsonObject jo = new JsonObject();
        jo.addProperty("boolean", true);
        jo.addProperty("character", 'c');
        jo.addProperty("short", (short)7);
        jo.addProperty("int", 23);
        jo.addProperty("long", 99L);
        jo.addProperty("float", 3.0f);
        jo.addProperty("double", 7.128d);
        jo.addProperty("string", "a string value");
        JsonArray ja = new JsonArray();
        ja.add(new JsonPrimitive(123));
        ja.add(new JsonPrimitive(456));
        jo.add("array", ja);
        JsonObject jo2 = new JsonObject();
        jo2.addProperty("int", 789);
        jo.add("object", jo2);
        return jo;
    }
    
    @Test
    public void testStrings() {
        JsonObject jo1 = newTestObject();
        Function<JsonObject,String> asString = JsonFunctions.asString();
        Function<String,JsonObject> fromString = JsonFunctions.fromString();
        
        String s1 = asString.apply(jo1);
        JsonObject jo2 = fromString.apply(s1);
        
        assertEquals(jo2, jo1);
    }
    
    @Test
    public void testBytes() {
        JsonObject jo1 = newTestObject();
        Function<JsonObject,byte[]> asBytes = JsonFunctions.asBytes();
        Function<byte[],JsonObject> fromBytes = JsonFunctions.fromBytes();
        
        byte[] b1 = asBytes.apply(jo1);
        JsonObject jo2 = fromBytes.apply(b1);
        
        assertEquals(jo2, jo1);
    }
}
