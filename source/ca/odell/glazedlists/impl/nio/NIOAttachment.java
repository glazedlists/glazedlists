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
import java.io.*;
// logging
import java.util.logging.*;

/**
 * Handles all sorts of incoming NIO events. An object implementing this interface
 * must be set as the attachment for all SelectionKeys used by the NIODaemon. 
 */
public interface NIOAttachment {
    
    /**
     * Handle a connect-ready key.
     */
    public void handleConnect();

    /**
     * Handle a read-ready key.
     */
    public void handleRead();

    /**
     * Handle a write-ready key.
     */
    public void handleWrite();

    /**
     * Handle a close-ready key.
     */
    public void close(Exception reason);

}
