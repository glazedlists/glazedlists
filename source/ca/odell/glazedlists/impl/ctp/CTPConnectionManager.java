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

    /** port to listen for incoming connections */
    private int listenPort = -1;
    
    /** asynch queue of tasks to execute */
    private List pendingRunnables = new ArrayList();
    
    /** the only thread that shall access the network resources of this manager */
    private Thread networkThread = null;
    
    /** the selector to awaken when necessary */
    Selector selector;
    
    /** factory for handlers of incoming connections */
    CTPHandlerFactory handlerFactory;
    
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
    public synchronized boolean start() throws IOException{
        // verify we haven't already started
        if(networkThread != null) throw new IllegalStateException();
        
        // open a channel and bind
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();
        InetSocketAddress listenAddress = new InetSocketAddress(listenPort);
        serverSocket.bind(listenAddress);
        
        // bind success
        logger.info("Connection Manager ready, listening on " + listenAddress);

        // prepare for non-blocking, selectable IO
        selector = Selector.open();
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

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
        CTPSelectorHandler selectorHandler = new CTPSelectorHandler();

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
        
        // do final clean up of state
        synchronized(this) {
            listenPort = -1;
            pendingRunnables.clear();
            selector = null;
            networkThread = null;
            handlerFactory = null;
            keepRunning = false;
        }
    }
    
    /**
     * Tests whether this connection manager has started.
     */
    public synchronized boolean isRunning() {
        return (networkThread != null);
    }

    /**
     * Tests whether the current thread is the network thread.
     */
    public synchronized boolean isNetworkThread() {
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
        // if the server has not yet been started
        if(!isRunning()) throw new IllegalStateException(); 

        // invoke immediately if possible
        if(isNetworkThread()) {
            runnable.run(selector, this);

        // run on the network thread while waiting on the current thread
        } else {
            CTPBlockingRunnable blockingRunnable = new CTPBlockingRunnable(runnable);
            synchronized(blockingRunnable) {
                // start the event
                synchronized(this) {
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
     * Runs the specified task the next time the CTPConnectionManager thread has
     * a chance.
     */
    public void invokeLater(Runnable runnable) {
        invokeLater(new CTPSimpleRunnable(runnable));
    }
    void invokeLater(CTPRunnable runnable) {
        synchronized(this) {
            // if the server has not yet been started
            if(!isRunning()) throw new IllegalStateException(); 
            
            pendingRunnables.add(runnable);
            wakeUp();
        }
    }
    
    /**
     * Stops the CTPConnectionManager and closes all connections.
     */
    public void stop() {
        invokeAndWait(new CTPShutdown());
    }
    
    /**
     * Connect to the specified host.
     */
    public void connect(CTPHandler handler, String host, int port) {
        invokeLater(new CTPConnectionToEstablish(handler, host, port));
    }
    public void connect(CTPHandler handler, String host) {
        connect(handler, host, DEFAULT_PORT);
    }
}
