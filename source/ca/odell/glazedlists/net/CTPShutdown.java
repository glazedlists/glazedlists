/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// NIO is used for CTP
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * A task that simply stops the CTP Connection manager.
 */
class CTPShutdown implements CTPRunnable {
    
    /** logging */
    private static Logger logger = Logger.getLogger(CTPShutdown.class.toString());

    /**
     * Runs the specified task.
     *
     * @param selector the selector being shared by all connections.
     * @return true unless the server shall shutdown due to a shutdown request or
     *      an unrecoverable failure.
     */
    public void run(Selector selector, CTPConnectionManager manager) {
        logger.warning("selector not cleaned up!");
        logger.warning("pending tasks not completed!");
        manager.keepRunning = false;
    }
}
