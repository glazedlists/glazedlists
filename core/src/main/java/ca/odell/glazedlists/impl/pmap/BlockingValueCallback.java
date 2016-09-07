/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
import ca.odell.glazedlists.impl.io.Bufferlo;

/**
 * A ValueCallback that simply blocks until the value is ready.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class BlockingValueCallback implements ValueCallback {

    /** the value returned */
    private Bufferlo value = null;

    /**
     * Gets the value for the specified Chunk.
     */
    public static Bufferlo get(Chunk member) {
        // queue the get
        BlockingValueCallback callback = new BlockingValueCallback();
        member.fetchValue(callback);

        // wait till its ready
        synchronized(callback) {
            if(callback.value == null) {
                try {
                    callback.wait();
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // return the result
        return callback.value;
    }

    /**
     * Handles a value being completely loaded into memory and ready to read.
     */
    @Override
    public void valueLoaded(Chunk member, Bufferlo value) {
         synchronized(this) {
             this.value = value;
             notify();
         }
    }
}

