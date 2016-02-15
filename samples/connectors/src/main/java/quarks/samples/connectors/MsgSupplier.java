/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors;

import quarks.function.Supplier;

/**
 * A Supplier<String> for creating sample messages to publish.
 */
public class MsgSupplier implements Supplier<String> {
    private static final long serialVersionUID = 1L;
    private final int maxCnt;
    private int cnt;
    private boolean done;
    
    public MsgSupplier(int maxCnt) {
        this.maxCnt = maxCnt;
    }

    @Override
    public synchronized String get() {
        ++cnt;
        if (maxCnt >= 0 && cnt >= maxCnt) {
            if (!done) {
                done = true;
                System.out.println("poll: no more messages to generate.");
            }
            return null;
        }
        String msg = String.format("Message-%d from %s", cnt, Util.simpleTS());
        System.out.println("poll generated msg to publish: " + msg);
        return msg;
    }
}
