/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.ctp;

// NIO is used for CTP
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * A CTPRunnable that unblocks the calling thread when it finishes executing.
 */
class CTPBlockingRunnable implements CTPRunnable {
    
    /** the target runnable */
    private CTPRunnable target;
    
    /**
     * Creates a CTPBlockingRunnable that runs the specified target while the calling
     * thread waits.
     */
    public CTPBlockingRunnable(CTPRunnable target) {
        this.target = target;
    }
    
    /**
     * Runs the specified task.
     *
     * @param selector the selector being shared by all connections.
     * @return true unless the server shall shutdown due to a shutdown request or
     *      an unrecoverable failure.
     */
    public void run(Selector selector, CTPConnectionManager manager) {
         target.run(selector, manager);
         
         // we're done, wake up the waiting thread
         synchronized(this) {
             notify();
         }
    }
}
