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
public final class CTPConnectionManager implements Runnable {
    
    /** logging */
    private static Logger logger = Logger.getLogger(CTPConnectionManager.class.toString());
    
    /** default port to bind to */
    private static final int DEFAULT_PORT = 5309;

    /** factory for handlers of incoming connections */
    private CTPHandlerFactory handlerFactory;
    
    /** port to listen for incoming connections */
    private int listenPort = -1;
    
    /** asynch queue of tasks to execute */
    private List pendingRunnables = new ArrayList();
    
    /** the selector to awaken when necessary */
    private Selector selector;
    
    /** the only thread that shall access the network resources of this manager */
    private Thread networkThread = null;
    
    /** whether the connection manager shall shut down */
    boolean keepRunning = false;
    
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
     *
     * @return true if the server successfully binds to the listen port.
     */
    public boolean start() {
        // verify we haven't already started
        if(networkThread != null) throw new IllegalStateException();
        
        // open a channel and bind
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverChannel.socket();
            InetSocketAddress listenAddress = new InetSocketAddress(listenPort);
            serverSocket.bind(listenAddress);
        
            // prepare for non-blocking, selectable IO
            selector = Selector.open();
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            // bind success
            logger.info("Connection Manager ready, listening on " + listenAddress);
        } catch(IOException e) {
            logger.warning("Connection Manager failed to start, " + e.getMessage());
            return false;
        }

        // start handling connections
        keepRunning = true;
        networkThread = new Thread(this, "GlazedLists net");
        networkThread.start();
        
        // success
        return true;
    }
        
    /**
     * Continuously selects a connection which needs servicing and services it.
     */
    public void run() {
        // the list of runnables to run this iteration
        List toExecute = new ArrayList();
            
        // always run the selector handler
        CTPSelectorHandler selectorHandler = new CTPSelectorHandler(handlerFactory);

        // continuously select a socket and action on it
        while(keepRunning) {

            // get the list of runnables to run
            synchronized(this) {
                toExecute.addAll(pendingRunnables);
                toExecute.add(selectorHandler);
                pendingRunnables.clear();
            }
            
            // run the runnables
            for(Iterator i = toExecute.iterator(); keepRunning && i.hasNext(); ) {
                CTPRunnable runnable = (CTPRunnable)i.next();
                i.remove();
                runnable.run(selector, this);
            }
        }
        
        logger.info("Server stopped");
    }
    
    /**
     * Tests whether the current thread is the network thread.
     */
    public boolean isNetworkThread() {
        return Thread.currentThread() == networkThread;
    }
    
    /**
     * Wake up the CTP thread so that it may process pending events.
     */
    private void wakeUp() {
        selector.wakeup();
    }
    
    /**
     * Runs the specified task on the CTPConnectionManager thread.
     */
    void invokeAndWait(CTPRunnable runnable) {
        // invoke immediately if possible
        if(isNetworkThread()) {
            runnable.run(selector, this);

        // run on the network thread while waiting on the current thread
        } else {
            CTPBlockingRunnable blockingRunnable = new CTPBlockingRunnable(runnable);
            synchronized(blockingRunnable) {
                // start the event
                synchronized(pendingRunnables) {
                    pendingRunnables.add(blockingRunnable);
                }
                wakeUp();
                
                // wait for it to be completed
                try {
                    blockingRunnable.wait();
                } catch(InterruptedException e) {
                    throw new RuntimeException("Wait interrupted " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Stops the CTPConnectionManager and closes all connections.
     */
    public synchronized void stop() {
        pendingRunnables.add(new CTPShutdown());
        wakeUp();
    }
    
    /**
     * Connect to the specified host.
     */
    public synchronized void connect(String host, int port, CTPHandler handler) {
        pendingRunnables.add(new CTPConnectionToEstablish(host, port, handler));
        wakeUp();
    }
    public synchronized void connect(String host, CTPHandler handler) {
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

        if(args[0].equals("server")) {
            new CTPConnectionManager(new EmptyCTPConnectHandler()).start();
            
        } else if(args[0].equals("client")) {
            CTPConnectionManager connectionManager = new CTPConnectionManager(new EmptyCTPConnectHandler());
            connectionManager.start();
            connectionManager.connect("localhost", 5310, new EmptyCTPHandler());
            
        } else {
            throw new IllegalArgumentException(args[0]);
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
            System.out.println("Received data " + data);
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
            System.out.println("connectionClosed( " + source + " , " + reason + " )");
        }

        /**
         * Handles the connection being ready for chunks to be sent.
         */
        public void connectionReady(CTPConnection source) {
            System.out.println("connectionReady( " + source + " )");
        }
    }
}
