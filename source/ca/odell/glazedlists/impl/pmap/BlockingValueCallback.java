/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
import ca.odell.glazedlists.impl.nio.*;
import ca.odell.glazedlists.impl.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * A ValueCallback that simply blocks until the value is ready.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
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
                    System.out.println("WAITING");
                    callback.wait();
                    System.out.println("DONE WAITING");
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
    public void valueLoaded(Chunk member, Bufferlo value) {
         synchronized(this) {
             this.value = value;
             notify();
         }
    }
}

