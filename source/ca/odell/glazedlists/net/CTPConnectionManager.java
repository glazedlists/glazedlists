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
final class CTPConnectionManager implements Runnable {
    
    /** logging */
    private static Logger logger = Logger.getLogger(CTPConnectionManager.class.toString());
    
    /** default port to bind to */
    private static final int DEFAULT_PORT = 5309;

    /** factory for handlers of incoming connections */
    private CTPHandlerFactory handlerFactory;
    
    /** port to listen for incoming connections */
    private int listenPort = -1;
    
    /** used to multiplex I/O resources */
    private Selector selector = null;
    
    /** asynch queue of connections to establish */
    private List connectionsToEstablish = new ArrayList();
    
    /**
     * Creates a connection manager that handles incoming connections using the
     * specified connect handler. This binds to the default port.
     */
    public CTPConnectionManager(CTPHandlerFactory handlerFactory) {
        this(handlerFactory, DEFAULT_PORT);
    }
    
    /**
     * Creates a connection manager that handles incoming connections using the
     * specified connect handler. This binds to the specified port.
     */
    public CTPConnectionManager(CTPHandlerFactory handlerFactory, int listenPort) {
        this.handlerFactory = handlerFactory;
        this.listenPort = listenPort;
    }
    
    /**
     * Starts the CTPConnectionManager listening to incoming connections and
     * managing outgoing connections.
     */
    public void start() throws IOException {
        // open a channel and bind
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();
        InetSocketAddress listenAddress = new InetSocketAddress(listenPort);
        serverSocket.bind(listenAddress);
    
        // prepare for non-blocking, selectable IO
        selector = Selector.open();
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        // start handling connections
        new Thread(this, "GlazedLists net").start();

        logger.fine("Connection Manager ready, listening on " + listenAddress);
    }
        
    /**
     * Continuously selects a connection which needs servicing and services it.
     */
    public void run() {
        
        // continuously select a socket and action on it
        while(true) {
            // This may block for a long time. Upon returning, the
            // selected set contains keys of the ready channels
            try {
                selector.select();
            } catch(IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }

            // Iterate over the connections to establish
            List toConnect = new ArrayList();
            synchronized(this) {
                toConnect.addAll(connectionsToEstablish);
                connectionsToEstablish.clear();
            }
            for(Iterator i = toConnect.iterator(); i.hasNext(); ) {
                PendingConnect pendingConnect = (PendingConnect)i.next();
                pendingConnect.connect();
            }
            

            // Iterate over the selected keys
            for(Iterator i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                SelectionKey key = (SelectionKey)i.next();
                
                // Is a new connection coming in?
                if(key.isAcceptable()) {
                    handleAccept(key);
                }
                
                if(key.isConnectable()) {
                    CTPConnection connection = (CTPConnection)key.attachment();
                    connection.handleConnect();
                    if(!key.isValid()) continue;
                }

                if(key.isReadable()) {
                    CTPConnection connection = (CTPConnection)key.attachment();
                    connection.handleRead();
                    if(!key.isValid()) continue;
                }
                
                if(key.isWritable()) {
                    CTPConnection connection = (CTPConnection)key.attachment();
                    connection.handleWrite();
                    if(!key.isValid()) continue;
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
    private void handleAccept(SelectionKey key) {
        // construct the channels and selectors
        SocketChannel channel = null;
        SelectionKey channelKey = null;
        try {
            // peel the connection from the SocketChannel
            ServerSocketChannel server = (ServerSocketChannel)key.channel();
            channel = server.accept();

            // configure the channel for no-blocking and selection
            if(channel == null) return;
            channel.configureBlocking(false);
            channelKey = channel.register(selector, 0);
        } catch(IOException e) {
            // the accept failed, there's nothing to clean up
            return;
        }

        // construct handlers for this connection
        CTPHandler handler = handlerFactory.constructHandler();
        CTPConnection server = CTPConnection.server(channelKey, handler);
        channelKey.attach(server);
        server.handleConnect();
    }
    
    /**
     * Stops the CTPConnectionManager and closes all connections.
     */
    public void stop() {
         throw new UnsupportedOperationException();
    }
    
    /**
     * Connect to the specified host.
     */
    public void connect(String host, int port, CTPHandler handler) {
        synchronized(this) {
            connectionsToEstablish.add(new PendingConnect(host, port, handler));
        }
        selector.wakeup();
    }

    /**
     * Connect to the specified host.
     */
    public void connect(String host, CTPHandler handler) {
        connect(host, DEFAULT_PORT, handler);
    }

    
    /**
     * Listens for connections and echoes data back to them.
     */
    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("Usage: CTPConnectionManager <mode>");
            System.out.println("");
            System.out.println(" mode: server");
            System.out.println("       client");
            return;
        }

        try {
            if(args[0].equals("server")) {
                new CTPConnectionManager(new EmptyCTPConnectHandler()).start();
                
                
            } else if(args[0].equals("client")) {
                CTPConnectionManager connectionManager = new CTPConnectionManager(new EmptyCTPConnectHandler());
                connectionManager.start();
                connectionManager.connect("localhost", 5310, new EmptyCTPHandler());
                
            } else {
                throw new IllegalArgumentException(args[0]);
            }

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
         * Handles reception of the specified chunk of data. This chunk should be able
         * to be cleanly concatenated with the previous and following chunks without
         * problem by the reader.
         *
         * @param data A non-empty array of bytes.
         */
        public void receiveChunk(CTPConnection source, ByteBuffer data) {
            logger.info("Received data " + data);
        }
    
        /**
         * Handles the connection being closed by the remote client. This will also
         * be called if there is a connection error, which is the case when a server
         * sends data that cannot be interpretted by CTPServerProtocol.
         *
         * @param reason An exception if the connection was closed as the result of
         *      a failure. This may be null.
         */
        public void connectionClosed(CTPConnection source, Exception reason) {
            logger.info("connectionClosed( " + source + " , " + reason + " )");
        }
    }

    /**
     * A PendingConnect models a desired connection. It is a temporary object used
     * by the connection manager to be passed between threads.
     *
     * <p>A PendingConnect is created for each call to the connect() method, and
     * queued until it can be processed by the CTP thread.
     */
    class PendingConnect {
        
        private String host;
        private int port;
        private CTPHandler handler;
        
        /**
         * Create a new PendingConnect.
         */
        public PendingConnect(String host, int port, CTPHandler handler) {
            this.host = host;
            this.port = port;
            this.handler = handler;
        }
        
        /**
         * Establish the connection. This creates a CTPProtocol for the client and
         * registers it with the selector.
         */
        public void connect() {
            CTPConnection client = null;
            try {
                // prepare a channel to connect
                InetSocketAddress address = new InetSocketAddress(host, port);
                SocketChannel channel = SocketChannel.open();
        
                // configure the channel for no-blocking and selection
                channel.configureBlocking(false);
                SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_CONNECT);
        
                // prepare the handler for the connection
                client = CTPConnection.client(host, selectionKey, handler);
                selectionKey.attach(client);

                // connect (non-blocking)
                channel.connect(address);

            } catch(IOException e) {
                handler.connectionClosed(client, e);
            }
        }
    }
}
