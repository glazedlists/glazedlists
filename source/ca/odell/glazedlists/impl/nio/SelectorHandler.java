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
 * The SelectorHandler walks through the list of ready keys and handles them.
 */
class SelectorHandler implements Runnable {
     
    /** logging */
    private static Logger logger = Logger.getLogger(SelectorHandler.class.toString());

    /** the I/O event queue daemon */
    private NIODaemon nioDaemon = null;
    
    /**
     * Create a new SelectorHandler for the specified NIO Daemon.
     */
    public SelectorHandler(NIODaemon nioDaemon) {
        this.nioDaemon = nioDaemon;
    }
    
    /**
     * Handle each selection key.
     */
    public void run() {
        // This may block for a long time. Upon returning, the
        // selected set contains keys of the ready channels
        try {
            nioDaemon.getSelector().select();
        } catch(IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        
        // handle what's ready
        handleSelectedKeys();
    }
    
    /**
     * Handles all keys which are ready to be processed.
     */
    void handleSelectedKeys() {
        // Iterate over the selected keys
        for(Iterator i = nioDaemon.getSelector().selectedKeys().iterator(); i.hasNext(); ) {
            SelectionKey key = (SelectionKey)i.next();
            i.remove();
            
            // Is a new connection coming in?
            if(key.isValid() && key.isAcceptable()) {
                nioDaemon.getServerHandler().handleAccept(key, nioDaemon.getSelector());
            }
            
            // an outgoing connection has been established
            if(key.isValid() && key.isConnectable()) {
                NIOHandler nioHandler = (NIOHandler)key.attachment();
                nioHandler.handleConnect();
            }

            // incoming data can be read
            if(key.isValid() && key.isReadable()) {
                NIOHandler nioHandler = (NIOHandler)key.attachment();
                nioHandler.handleRead();
            }
            
            // outgoing data can be written
            if(key.isValid() && key.isWritable()) {
                NIOHandler nioHandler = (NIOHandler)key.attachment();
                nioHandler.handleWrite();
            }

            // clean up broken connections
            if(!key.isValid()) {
                NIOHandler nioHandler = (NIOHandler)key.attachment();
                nioHandler.close(new IOException("Connection closed"));
            }
        }
    }
}
