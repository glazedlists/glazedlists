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
 * A task that immediately stops the CTP Connection manager.
 */
class CTPStop implements CTPRunnable {
    
    /** logging */
    private static Logger logger = Logger.getLogger(CTPStop.class.toString());
    
    /**
     * Stops the server.
     *
     * @param selector the selector being shared by all connections.
     * @return true unless the server shall shutdown due to a shutdown request or
     *      an unrecoverable failure.
     */
    public void run(Selector selector, CTPConnectionManager manager) {
        // warn if unsatisfied keys remain
        if(selector.keys().size() != 0) {
            logger.warning("Sever stopping with " + selector.keys().size() + " active connections");
        } else {
            logger.info("Sever stopping with " + selector.keys().size() + " active connections");
        }

        // break out of the server dispatch loop
        manager.keepRunning = false;
    }
}
