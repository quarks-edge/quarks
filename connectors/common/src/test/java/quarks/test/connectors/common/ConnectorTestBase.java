/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016 
*/
package quarks.test.connectors.common;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import quarks.test.providers.direct.DirectTestSetup;
import quarks.test.topology.TopologyAbstractTest;
import quarks.topology.TStream;
import quarks.topology.Topology;

public class ConnectorTestBase extends TopologyAbstractTest implements DirectTestSetup {
    
    public static List<String> createMsgs(MsgGenerator mgen, String topic) {
        List<String> msgs = new ArrayList<>();
        msgs.add(mgen.create(topic, "Hello"));
        msgs.add(mgen.create(topic, "Are you there?"));
        return msgs;
    }

    public static String simpleTS() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }
    
    public static class MsgId {
        private int seq;
        private String uniq;
        private String prefix;
        MsgId(String prefix) {
            this.prefix = prefix;
        }
        String next() {
            if (uniq==null) {
                uniq = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
            }
            return String.format("[%s.%d %s]", uniq, seq++, prefix);
        }
        String pattern() {
            return String.format(".*\\[%s\\.\\d+ %s\\].*", uniq, prefix);
        }
    }

    public static class MsgGenerator {
        private MsgId id;
        public MsgGenerator(String testName) {
            id = new MsgId(testName);
        }
        public String create(String topic, String baseContent) {
            return String.format("%s [for-topic=%s] %s", id.next(), topic, baseContent);
        }
        public String pattern() {
            return id.pattern();
        }
    }

    public static class Msg implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String topic;
        private final String msg;

        public Msg(String msg, String topic) {
            this.msg = msg;
            this.topic = topic;
        }
        public String getTopic()   { return topic; }
        public String getMessage() { return msg; }
        public String toString()   { return "[topic="+topic+", msg="+msg+"]"; }
    }
    
    public void completeAndValidate(String msg, Topology t,
            TStream<String> s, MsgGenerator mgen, int secTimeout, String... expected)
            throws Exception {
        completeAndValidate(true/*ordered*/, msg, t, s, mgen, secTimeout, expected);
    }
    
    public void completeAndValidate(boolean ordered, String msg, Topology t,
            TStream<String> s, MsgGenerator mgen, int secTimeout, String... expected)
            throws Exception {
        
        s = s.filter(tuple -> tuple.matches(mgen.pattern()));
        s.sink(tuple -> System.out.println(
                String.format("[%s][%s] rcvd: %s", t.getName(), simpleTS(), tuple)));

        super.completeAndValidate(ordered, msg, t, s, secTimeout, expected);
    }

}
