/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.util.concurrent;

/**
 * Limits threads passing through the gate to a specified rate. Note that
 * this lame implementation doesn't do anything special to try and guarantee
 * first-in first-out order, so threads could starve.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class RateGate {
    private final long period;
    private long last = 0;

    public RateGate(long period) {
        this.period = period;
    }
    public synchronized void passThrough() {
        // if we waited, we only increment the clock by the minimum amount
        boolean waited = false;
        while(true) {
            // done waiting?
            long wait;
            long now = System.currentTimeMillis();
            if(now - last >= period) {
                last = waited ? last + period : now;
                return;
            } else {
                wait = last + period - now;
            }

            // wait some more
            try {
                waited = true;
                wait(wait);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
