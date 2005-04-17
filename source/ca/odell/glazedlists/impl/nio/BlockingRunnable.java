/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.nio;

// NIO is used for CTP
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * A Runnable that unblocks the calling thread when it finishes executing.
 *
 * <p>If this Runnable throws any {@link RuntimeException}s, they can be accessed
 * via this API. They will not be propagated up.
 */
class BlockingRunnable implements Runnable {
    
    /** the target runnable */
    private Runnable target;
    
    /** any exception thrown during invocation */
    private RuntimeException problem = null;
    
    /**
     * Creates a BlockingRunnable that runs the specified target while the calling
     * thread waits.
     */
    public BlockingRunnable(Runnable target) {
        this.target = target;
    }
    
    /**
     * Runs the specified task.
     */
    public void run() {
        // run the target runnable
        try {
            target.run();
        } catch(RuntimeException e) {
            this.problem = e;
        }
         
        // wake up the waiting thread
        synchronized(this) {
            notify();
        }
    }
    
    /**
     * Get any exception that was thrown during invocation.
     */
    public RuntimeException getInvocationTargetException() {
        return problem;
    }
}
