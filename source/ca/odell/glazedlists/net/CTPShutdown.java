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
 * A task that gracefully shuts down the CTP Connection manager.
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
        logger.info("Cleaning up listening socket and closing " + (selector.keys().size()-1) + " connections");

        // kill all connections
        for(Iterator k = selector.keys().iterator(); k.hasNext(); ) {
            SelectionKey key = (SelectionKey)k.next();

            // close the server socket
            if((key.interestOps() & SelectionKey.OP_ACCEPT) != 0) {
                try {
                    ServerSocketChannel server = (ServerSocketChannel)key.channel();
                    server.close();
                    key.cancel();
                } catch(IOException e) {
                    logger.warning("Error closing server socket, " + e.getMessage());
                }
                
            // close a connection socket
            } else {
                CTPConnection connection = (CTPConnection)key.attachment();
                connection.close(new CTPServerShutdownException());
            }
        }
        
        // stop the server
        manager.invokeLater(new CTPStop());
    }
}
