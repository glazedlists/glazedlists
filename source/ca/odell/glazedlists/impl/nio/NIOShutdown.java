/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
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
 * A task that gracefully shuts down the NIO daemon.
 */
class NIOShutdown implements Runnable {
    
    /** logging */
    private static Logger logger = Logger.getLogger(NIOShutdown.class.toString());

    /** the I/O event queue daemon */
    private NIODaemon nioDaemon = null;

    /**
     * Create a new NIOShutdown that shuts down a server using the specified
     * NIODaemon.
     */
    public NIOShutdown(NIODaemon nioDaemon) {
        this.nioDaemon = nioDaemon;
    }
    
    /**
     * Runs the specified task.
     *
     * @param selector the selector being shared by all connections.
     * @return true unless the server shall shutdown due to a shutdown request or
     *      an unrecoverable failure.
     */
    public void run() {
        logger.info("Cleaning up listening socket and closing " + (nioDaemon.getSelector().keys().size()-1) + " connections");

        // kill all connections
        for(Iterator k = nioDaemon.getSelector().keys().iterator(); k.hasNext(); ) {
            SelectionKey key = (SelectionKey)k.next();

            // close an invalid connection
            if(!key.isValid()) {
                NIOHandler nioHandler = (NIOHandler)key.attachment();
                nioHandler.close(new IOException("Connection closed"));

            // close the server socket
            } else if((key.interestOps() & SelectionKey.OP_ACCEPT) != 0) {
                try {
                    ServerSocketChannel server = (ServerSocketChannel)key.channel();
                    server.close();
                    key.cancel();
                } catch(IOException e) {
                    logger.warning("Error closing server socket, " + e.getMessage());
                }
                
            // close a connection socket
            } else {
                NIOHandler nioHandler = (NIOHandler)key.attachment();
                nioHandler.close(new ServerShutdownException());
            }
        }
    }
}
