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
 * Runs a vanilla Runnable on the CTP thread.
 */
class CTPSimpleRunnable implements CTPRunnable {
    
    /** the runnable to run */
    private Runnable target = null;
    
    /**
     * Creates a new SimpleRunnable that runs the specified target.
     */
    public CTPSimpleRunnable(Runnable target) {
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
        target.run();
    }
}
