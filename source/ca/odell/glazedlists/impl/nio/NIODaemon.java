/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.nio;

// NIO is used for CTP
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * An event queue of I/O events and a thread to run them on.
 */
public final class NIODaemon implements Runnable {
    
    /** logging */
    private static Logger logger = Logger.getLogger(NIODaemon.class.toString());
    
    /** asynch queue of tasks to execute */
    private List pendingRunnables = new ArrayList();
    
    /** the only thread that shall access the network resources of this manager */
    private Thread ioThread = null;
    
    /** the selector to awaken when necessary */
    private Selector selector;
    
    /** whether the connection manager shall shut down */
    private boolean keepRunning = false;
    
    /** whom to handle incoming connections */
    private NIOServer server = null;
    
    /**
     * Starts the NIODaemon.
     */
    public synchronized void start() throws IOException {
        // verify we haven't already started
        if(ioThread != null) throw new IllegalStateException();
        
        // prepare for non-blocking, selectable IO
        selector = Selector.open();

        // start handling connections
        keepRunning = true;
        ioThread = new Thread(this, "GlazedLists nio");
        ioThread.start();
    }
        
    /**
     * Continuously selects a connection which needs servicing and services it.
     */
    public void run() {
        // the list of runnables to run this iteration
        List toExecute = new ArrayList();
            
        // always run the selector handler
        SelectAndHandle selectAndHandle = new SelectAndHandle(this);

        // continuously select a socket and action on it
        while(keepRunning) {

            // get the list of runnables to run
            synchronized(this) {
                toExecute.addAll(pendingRunnables);
                toExecute.add(selectAndHandle);
                pendingRunnables.clear();
            }
            
            // run the runnables
            for(Iterator i = toExecute.iterator(); keepRunning && i.hasNext(); ) {
                Runnable runnable = (Runnable)i.next();
                i.remove();
                try {
                    runnable.run();
                } catch(RuntimeException e) {
                    logger.log(Level.SEVERE, "Failure processing I/O, continuing", e);
                }
            }
        }
        
        // do final clean up of state
        synchronized(this) {
            pendingRunnables.clear();
            selector = null;
            ioThread = null;
            keepRunning = false;
        }
    }
    
    /**
     * Tests whether this connection manager has started.
     */
    public synchronized boolean isRunning() {
        return (ioThread != null);
    }

    /**
     * Tests whether the current thread is the network thread.
     */
    public synchronized boolean isNetworkThread() {
        return Thread.currentThread() == ioThread;
    }
    
    /**
     * Wake up the CTP thread so that it may process pending events.
     */
    private void wakeUp() {
        selector.wakeup();
    }
    
    /**
     * Runs the specified task on the NIODaemon thread.
     */
    public void invokeAndWait(Runnable runnable) {
        // if the server has not yet been started
        if(!isRunning()) throw new IllegalStateException(); 

        // invoke immediately if possible
        if(isNetworkThread()) {
            runnable.run();

        // run on the network thread while waiting on the current thread
        } else {
            BlockingRunnable blockingRunnable = new BlockingRunnable(runnable);
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
                
                // propagate any RuntimeExceptions
                RuntimeException problem = blockingRunnable.getInvocationTargetException();
                if(problem != null) throw problem;
            }
        }
    }
    
    /**
     * Runs the specified task the next time the NIODaemon thread has a chance.
     */
    public void invokeLater(Runnable runnable) {
        synchronized(this) {
            // if the server has not yet been started
            if(!isRunning()) throw new IllegalStateException(); 
            
            pendingRunnables.add(runnable);
            wakeUp();
        }
    }
    
    /**
     * Stops the NIODaemon.
     */
    public void stop() {
        // shutdown the server
        invokeAndWait(new Shutdown(this));
        
        // stop the server
        invokeAndWait(new Stop());
    }
    /**
     * Stops the server after it has been shut down.
     */
    private class Stop implements Runnable {
        public void run() {
            // warn if unsatisfied keys remain
            if(selector.keys().size() != 0) {
                logger.warning("Server stopping with " + selector.keys().size() + " active connections");
            } else {
                logger.info("Server stopping with " + selector.keys().size() + " active connections");
            }
    
            // break out of the server dispatch loop
            keepRunning = false;
        }
    }
    
    /**
     * Gets the selector that this NIODaemon manages.
     */
    public Selector getSelector() {
        return selector;
    }
    
    /**
     * Configure this NIODaemon to use the specified server handler for acceptable
     * selection keys.
     */
    public void setServer(NIOServer server) {
        this.server = server;
    }
    public NIOServer getServer() {
        return server;
    }
}
