/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016
*/
package quarks.samples.apps;

import static quarks.analytics.math3.stat.Statistic.MAX;
import static quarks.analytics.math3.stat.Statistic.MEAN;
import static quarks.analytics.math3.stat.Statistic.MIN;
import static quarks.analytics.math3.stat.Statistic.STDDEV;

import java.util.List;

import org.apache.commons.math3.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import quarks.analytics.math3.json.JsonAnalytics;
import quarks.analytics.math3.stat.Statistic;
import quarks.function.BiFunction;
import quarks.function.Function;
import quarks.topology.TStream;

/**
 * Utilties to ease working working with sensor "samples" by wrapping them
 * in JsonObjects.
 * <p>
 * The Json Tuple sensor "samples" have a standard collection of properties.
 */
public class JsonTuples {
        
    /*
     * Common attributes in the JsonObject
     */
    public static final String KEY_ID = "id";
    public static final String KEY_TS = "msec";
    public static final String KEY_READING = "reading";
    public static final String KEY_AGG_BEGIN_TS = "agg.begin.msec";
    public static final String KEY_AGG_COUNT = "agg.count";
    
    /**
     * Create a JsonObject wrapping a raw {@code Pair<Long msec,T reading>>} sample.
     * @param sample the raw sample
     * @param id the sensor's Id
     * @return the wrapped sample
     */
    public static <T> JsonObject wrap(Pair<Long,T> sample, String id) {
        JsonObject jo = new JsonObject();
        jo.addProperty(KEY_ID, id);
        jo.addProperty(KEY_TS, sample.getFirst());
        T value = sample.getSecond();
        if (value instanceof Number)
            jo.addProperty(KEY_READING, (Number)sample.getSecond());
        else if (value instanceof String)
            jo.addProperty(KEY_READING, (String)sample.getSecond());
        else if (value instanceof Boolean)
            jo.addProperty(KEY_READING, (Boolean)sample.getSecond());
//        else if (value instanceof array) {
//            // TODO cvt to JsonArray
//        }
//        else if (value instanceof Object) {
//            // TODO cvt to JsonObject
//        }
        else {
            Class<?> clazz = value != null ? value.getClass() : Object.class;
            throw new IllegalArgumentException("Unhandled value type: "+ clazz);
        }
        return jo;
    }
    
    /**
     * Create a stream of JsonObject wrapping a stream of 
     * raw {@code Pair<Long msec,T reading>>} samples.
     * 
     * @param stream the raw input stream
     * @param id the sensor's Id
     * @return the wrapped stream
     */
    public static <T> TStream<JsonObject> wrap(TStream<Pair<Long,T>> stream, String id) {
        return stream.map(pair -> wrap(pair, id));
    }
    
    /**
     * The partition key function for wrapped sensor samples.
     * <p>
     * The {@code KEY_ID} property is returned for the key.
     * @return the function
     */
    public static Function<JsonObject,String> keyFn() {
        return sample -> sample.get(KEY_ID).getAsString();
    }
    
    
    /**
     * Get a statistic value from a sample.
     * <p>
     * Same as {@code getStatistic(jo, JsonTuples.KEY_READING, stat)}.
     * 
     * @param jo the sample
     * @param stat the Statistic of interest
     * @return the JsonElement for the Statistic
     * @throws RuntimeException of the stat isn't present
     */
    public static JsonElement getStatistic(JsonObject jo, Statistic stat) {
        return getStatistic(jo, JsonTuples.KEY_READING, stat);
    }
    
    /**
     * Get a statistic value from a sample.
     * <p>
     * Convenience for working with samples containing a property
     * whose value is one or more {@link Statistic}
     * as created by 
     * {@link JsonAnalytics#aggregate(quarks.topology.TWindow, String, String, quarks.analytics.math3.json.JsonUnivariateAggregate...) JsonAnalytics.aggregate()}
     * 
     * @param jo the sample
     * @param valueKey the name of the property containing the JsonObject of Statistics
     * @param stat the Statistic of interest
     * @return the JsonElement for the Statistic
     * @throws RuntimeException of the stat isn't present
     */
    public static JsonElement getStatistic(JsonObject jo, String valueKey, Statistic stat) {
        JsonObject statsjo = jo.get(valueKey).getAsJsonObject();
        return statsjo.get(stat.name());
    }

    /**
     * Create a function that computes the specified statistics on the list of
     * samples and returns a new sample containing the result.
     * <p>
     * The single tuple contains the specified statistics computed over
     * all of the {@code JsonTuple.KEY_READING} 
     * values from {@code List<JsonObject>}.
     * <p>
     * The resulting sample contains the properties:
     * <ul>
     * <li>JsonTuple.KEY_ID</li>
     * <li>JsonTuple.KEY_MSEC - msecTimestamp of the last sample in the window</li>
     * <li>JsonTuple.KEY_AGG_BEGIN_MSEC - msecTimestamp of the first sample in the window</li>
     * <li>JsonTuple.KEY_AGG_COUNT - number of samples in the window ({@code value=factor})</li>
     * <li>JsonTuple.KEY_READING - a JsonObject of the statistics
     *                      as defined by
     *                     {@link JsonAnalytics#aggregate(quarks.topology.TWindow, String, String, quarks.analytics.math3.json.JsonUnivariateAggregate...) JsonAnalytics.aggregate()}
     * </ul>
     * <p>
     * Sample use:
     * <pre>{@code
     * TStream<JsonObject> s = ...
     * // reduce s by a factor of 100 with stats MEAN and STDEV 
     * TStream<JsonObject> reduced = s.batch(100, statistics(Statistic.MEAN, Statistic.STDDEV));
     * }</pre>
     * 
     * @param statistics the statistics to calculate over the window
     * @return {@code TStream<JsonObject>} for the reduced {@code stream}
     */
    public static BiFunction<List<JsonObject>,String,JsonObject> statistics(Statistic... statistics) {
        BiFunction<List<JsonObject>,JsonElement,JsonObject> statsFn = 
                JsonAnalytics.aggregateList(KEY_ID, KEY_READING,
                    j -> j.get(KEY_READING).getAsDouble(), 
                    MIN, MAX, MEAN, STDDEV);

        return (samples, key) -> {
                    JsonObject jo = statsFn.apply(samples, samples.get(0).get(KEY_ID));
                    JsonTuples.addAggStdInfo(jo, samples);
                    return jo;
                };
    }

    /**
     * Create a function that creates a new sample containing the collection
     * of input sample readings.
     * <p>
     * The single sample collects the all of the {@code JsonTuple.KEY_READING} 
     * values from {@code List<JsonObject>} into a single {@code JsonArray}.
     * <p> 
     * The resulting sample contains the properties:
     * <ul>
     * <li>JsonTuple.KEY_ID</li>
     * <li>JsonTuple.KEY_MSEC - msecTimestamp of the last sample in the window</li>
     * <li>JsonTuple.KEY_AGG_BEGIN_MSEC - msecTimestamp of the first sample in the window</li>
     * <li>JsonTuple.KEY_AGG_COUNT - number of samples in the window ({@code value=factor})</li>
     * <li>JsonTuple.KEY_READING - a JsonArray of each JsonObject's KEY_READING reading</li>
     * </ul>
     * <p>
     * Sample use:
     * <pre>{@code
     * TStream<JsonObject> s = ...
     * // reduce s by a factor of 100 
     * TStream<JsonObject> reduced = batch(s, 100, collect());
     * }</pre>
     * @return the accumulate function
     * @see #batch(TStream, int, BiFunction)
     */
    public static BiFunction<List<JsonObject>,String,JsonObject> collect() {
        return (samples, key) -> {
            JsonObject jo = new JsonObject();
            JsonTuples.addAggStdInfo(jo, samples);
            JsonArray ja = new JsonArray();
            for (JsonObject j : samples) {
                ja.add(j.get(KEY_READING));
            }
            jo.add(KEY_READING, ja);
            return jo;
        };
    }

    private static void addAggStdInfo(JsonObject jo, List<JsonObject> samples) {
        // beginMsec, endMsec, nSamples
        long msec = samples.get(0).get(KEY_TS).getAsLong();
        long msec2 = samples.get(samples.size()-1).get(KEY_TS).getAsLong();
        int nSamples = samples.size();
        
        jo.addProperty(KEY_TS, msec2);
        jo.addProperty(KEY_AGG_BEGIN_TS, msec);
        jo.addProperty(KEY_AGG_COUNT, nSamples);
    }

    /**
     * Process a window of tuples in batches.  
     * TODO REMOVE ONCE TStream.batch(int) or such exists.
     * <p>
     * Accumulate a window of {@code size} tuples, 
     * invoke {@code batcher.apply()} on the batch, clear the window,
     * and repeat for the next batch.
     * <p>
     * Useful for applying analytics to reduce the number of samples
     * by a factor of {@code size} - e.g., 100:1.  Typical reductions
     * compute some characteristic statistic for the batch, e.g., a mean,
     * or simply collect the samples into a single sample to then
     * process by some other complex analytic such as an FFT.
     * <p>
     * Sample use:
     * <pre>{@code
     * TStream<JsonObject> s = ...
     * // reduce s by a factor of 100 
     * TStream<JsonObject> reduced = Reducers.batch(s, 100, 
     *          JsonTuples.statistics(Statistic.MEAN, Statistic.STDDEV));
     * }</pre>
     * 
     * @param stream the stream to reduce
     * @param size the batch size
     * @param batcher function to perform the aggregation
     * @return {@code TStream<JsonObject>} for the reduced {@code stream}
     */
    public static TStream<JsonObject> batch(TStream<JsonObject> stream, int size, BiFunction<List<JsonObject>,String,JsonObject> batcher) {
        // workaround for omission of batched windows
        int[] cnt = new int[] {0};
        return stream.last(size, keyFn())
                .aggregate(
                        (samples, key) -> {
                            if (++cnt[0] >= size) {
                                cnt[0] = 0;
                                return batcher.apply(samples, key);
                            }
                            else
                                return null;
                        })
                // workaround issue#133 where aggregate doesn't ignore null fn results
                .filter(sample -> sample != null);
    }

}
