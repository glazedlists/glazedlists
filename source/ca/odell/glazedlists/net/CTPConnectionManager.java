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
 * The CTPConnectionManager provides managed access to multiple CTP connections
 * for both incoming and outgoing data.
 *
 * <p>Each instance of this class owns a single thread which is used to perform
 * all read and write operations on all connections. A pool of other threads are
 * used to notify the handlers of the data and status of a connection.
 */
final class CTPConnectionManager {
    
    /** logging */
    private static Logger logger = Logger.getLogger(CTPConnectionManager.class.toString());

    /** factory for handlers of incoming connections */
    private CTPHandlerFactory handlerFactory;
    
    /** port to listen for incoming connections */
    private int listenPort = 5309;
    
    /** used to multiplex I/O resources */
    Selector selector = null;
    
    /**
     * Creates a connection manager that handles incoming connections using the
     * specified connect handler.
     */
    public CTPConnectionManager(CTPHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }
    
    /**
     * Starts the CTPConnectionManager listening to incoming connections and
     * managing outgoing connections.
     */
    public void start() throws IOException {

        // Allocate an unbound server socket channel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // Get the associated ServerSocket to bind it with
        ServerSocket serverSocket = serverChannel.socket();
        // Create a new Selector for use below
        selector = Selector.open();
        
        // Set the port the server channel will listen to
        InetSocketAddress listenAddress = new InetSocketAddress(listenPort);
        logger.fine("Binding to " + listenAddress);
        serverSocket.bind(listenAddress);
        
        // Set nonblocking mode for the listening Socket
        serverChannel.configureBlocking(false);
        
        // Register the Server SocketChannel with the Selector
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        while(true) {
            // This may block for a long time. Upon returning, the
            // selected set contains keys of the ready channels
            int n = selector.select();
            
            // nothing to do
            if(n == 0) continue;
            
            // Iterate over the selected keys
            for(Iterator i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                SelectionKey key = (SelectionKey)i.next();
                
                // Is a new connection coming in?
                if(key.isAcceptable()) {
                    handleAccept(key);
                }
                
                // Is there data to be read on this channel?
                if(key.isReadable()) {
                    CTPProtocol protocol = (CTPProtocol)key.attachment();
                    protocol.handleRead();
                }
                
                // Remove key from selected set; it's been handled
                i.remove();
            }
        }
    }
    
    /**
     * Handle an incoming connection.
     *
     * <p>This creates a CTPServerProtocol to handle the connection.
     */
    private void handleAccept(SelectionKey key) throws IOException {
        // peel the connection from the SocketChannel
        ServerSocketChannel server = (ServerSocketChannel)key.channel();
        SocketChannel channel = server.accept();
        if(channel == null) return;
        
        // configure the channel for no-blocking and selection
        channel.configureBlocking(false);
        SelectionKey channelKey = channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        
        // construct handlers for this connection
        CTPHandler handler = handlerFactory.constructHandler();
        CTPServerProtocol serverProtocol = new CTPServerProtocol(channelKey, handler);
        channelKey.attach(serverProtocol);

        // document our success
        logger.fine("Accepted connection from " + channel.socket().getRemoteSocketAddress());
    }
    
    /**
     * Stops the CTPConnectionManager and closes all connections.
     */
    public void stop() {
         
    }
    
    /**
     * Connect to the specified host.
     */
    public CTPClientProtocol connect(String host, CTPHandler handler) {
        return null;
    }
    
    /**
     * Listens for connections and echoes data back to them.
     */
    public static void main(String[] args) {
        try {
            new CTPConnectionManager(new EmptyCTPConnectHandler()).start();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a connection
     */
    static class EmptyCTPConnectHandler implements CTPHandlerFactory {
        
        /**
         * Upon a connect, a CTPHandler is required to handle the data of this connection.
         * The returned CTPHandler will be delegated to handle the connection's data.
         */
        public CTPHandler constructHandler() {
            return new EmptyCTPHandler();
        }
    }


    /**
     * A callback interface for classes that implement a CTP Client or Server.
     * The CTPClientProtocol and CTPServerProtocol shall handle the lowest network
     * level of the protocol and handlers are used to interpret the data.
     */
    static class EmptyCTPHandler implements CTPHandler {
        
        /**
         * Handles an HTTP response from the specified connection.
         *
         * @param code the HTTP  response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1.
         *      This will be null if this is an HTTP request.
         * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2.
         */
        public void receiveResponse(CTPProtocol source, Integer code, Map headers) {
            throw new IllegalStateException("write code here");
        }
    
        /**
         * Handles an HTTP request from the specified connection.
         *
         * @param uri the address requested by the client, in the format of a file
         *      address. See HTTP/1.1 RFC, 5.1.2. This will be null if this is an
         *      HTTP response.
         * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2.
         */
        public void receiveRequest(CTPProtocol source, String uri, Map headers) {
            throw new IllegalStateException("write code here");
        }
        
        /**
         * Handles reception of the specified chunk of data. This chunk should be able
         * to be cleanly concatenated with the previous and following chunks without
         * problem by the reader.
         *
         * @param data A non-empty array of bytes.
         */
        public void receiveChunk(CTPProtocol source, byte[] data) {
            throw new IllegalStateException("write code here");
        }
    
        /**
         * Handles the connection being closed by the remote client. This will also
         * be called if there is a connection error, which is the case when a server
         * sends data that cannot be interpretted by CTPServerProtocol.
         *
         * @param reason An exception if the connection was closed as the result of
         *      a failure. This may be null.
         */
        public void connectionClosed(CTPProtocol source, Exception reason) {
            logger.info("connectionClosed( " + source + " , " + reason + " )");
        }
    }
}


/**
 * The CTPHandlerFactory provides a factory to handle incoming connections.
 */
interface CTPHandlerFactory {
    
    /**
     * Upon a connect, a CTPHandler is required to handle the data of this connection.
     * The returned CTPHandler will be delegated to handle the connection's data.
     */
    public CTPHandler constructHandler();
}


