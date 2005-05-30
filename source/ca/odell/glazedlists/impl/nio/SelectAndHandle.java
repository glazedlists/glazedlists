/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
 * The SelectAndHandle selects ready keys and handles them.
 */
class SelectAndHandle implements Runnable {
     
    /** logging */
    private static Logger logger = Logger.getLogger(SelectAndHandle.class.toString());

    /** the I/O event queue daemon */
    private NIODaemon nioDaemon = null;
    
    /**
     * Create a new SelectorHandler for the specified NIO Daemon.
     */
    public SelectAndHandle(NIODaemon nioDaemon) {
        this.nioDaemon = nioDaemon;
    }
    
    /**
     * Select and handle.
     */
    public void run() {
        select();
        handle();
    }
    
    /**
     * Selects keys which are ready to be processed.
     */
    void select() {
        // This may block for a long time. Upon returning, the
        // selected set contains keys of the ready channels
        try {
            nioDaemon.getSelector().select();
        } catch(IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    /**
     * Handles all keys which are ready to be processed.
     */
    void handle() {
        // Iterate over the selected keys
        for(Iterator i = nioDaemon.getSelector().selectedKeys().iterator(); i.hasNext(); ) {
            SelectionKey key = (SelectionKey)i.next();
            i.remove();
            
            // Is a new connection coming in?
            if(key.isValid() && key.isAcceptable()) {
                nioDaemon.getServer().handleAccept(key, nioDaemon.getSelector());
            }
            
            // an outgoing connection has been established
            if(key.isValid() && key.isConnectable()) {
                NIOAttachment attachment = (NIOAttachment)key.attachment();
                attachment.handleConnect();
            }

            // incoming data can be read
            if(key.isValid() && key.isReadable()) {
                NIOAttachment attachment = (NIOAttachment)key.attachment();
                attachment.handleRead();
            }
            
            // outgoing data can be written
            if(key.isValid() && key.isWritable()) {
                NIOAttachment attachment = (NIOAttachment)key.attachment();
                attachment.handleWrite();
            }

            // clean up broken connections
            if(!key.isValid()) {
                NIOAttachment attachment = (NIOAttachment)key.attachment();
                attachment.close(new IOException("Connection closed"));
            }
        }
    }
}
