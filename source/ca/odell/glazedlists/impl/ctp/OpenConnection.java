/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
 * A OpenConnection models a desired connection. It is a temporary object used
 * by the connection manager to be passed between threads.
 *
 * <p>A OpenConnection is created for each call to the connect() method, and
 * queued until it can be processed by the CTP thread.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class OpenConnection implements Runnable {
     
    /** the place to connect to */
    private CTPConnectionManager connectionManager;
    private String host;
    private int port;
    private CTPHandler handler;

    /**
     * Create a new CTPConnectionToEstablish.
     */
    public OpenConnection(CTPConnectionManager connectionManager, CTPHandler handler, String host, int port) {
        this.connectionManager = connectionManager;
        this.handler = handler;
        this.host = host;
        this.port = port;
    }
    
    /**
     * Establish the connection. This creates a CTPProtocol for the client and
     * registers it with the selector.
     */
    public void run() {
        CTPConnection client = null;
        try {
            // prepare a channel to connect
            InetSocketAddress address = new InetSocketAddress(host, port);
            SocketChannel channel = SocketChannel.open();
    
            // configure the channel for no-blocking and selection
            channel.configureBlocking(false);
            SelectionKey selectionKey = channel.register(connectionManager.getNIODaemon().getSelector(), SelectionKey.OP_CONNECT);
    
            // prepare the handler for the connection
            client = CTPConnection.client(host, selectionKey, handler, connectionManager);
            selectionKey.attach(client);

            // connect (non-blocking)
            channel.connect(address);

        } catch(IOException e) {
            handler.connectionClosed(client, e);
        }
    }
}
