/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.ctp;

// NIO is used for CTP
import ca.odell.glazedlists.impl.nio.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * A task that starts the CTP Connection manager.
 */
class CTPStartUp implements Runnable {
    
    /** logging */
    private static Logger logger = Logger.getLogger(CTPStartUp.class.toString());

    /** the I/O event queue daemon */
    private CTPConnectionManager connectionManager = null;
    
    /** port to listen for incoming connections */
    private int listenPort = -1;
    
    /**
     * Create a new CTPStartUp that starts a server using the specified
     * NIODaemon.
     */
    public CTPStartUp(CTPConnectionManager connectionManager, int listenPort) {
        this.connectionManager = connectionManager;
        this.listenPort = listenPort;
    }
    
    /**
     * Runs the specified task.
     *
     * @param selector the selector being shared by all connections.
     * @return true unless the server shall shutdown due to a shutdown request or
     *      an unrecoverable failure.
     */
    public void run() {
        try {
            // open a channel and bind
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverChannel.socket();
            serverSocket.setReuseAddress(false); // fix for Apple JVM bug 3922515
            InetSocketAddress listenAddress = new InetSocketAddress(listenPort);
            serverSocket.bind(listenAddress);
            
            // prepare for non-blocking, selectable IO
            serverChannel.configureBlocking(false);
            serverChannel.register(connectionManager.getNIODaemon().getSelector(), SelectionKey.OP_ACCEPT);
            connectionManager.getNIODaemon().setServerHandler(connectionManager);
    
            // bind success
            logger.info("Connection Manager ready, listening on " + listenAddress);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
