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
 * Handles incoming NIO connections.
 */
public interface NIOServer {
    
    /**
     * Handle an accept-ready selection key.
     */
    public void handleAccept(SelectionKey selectionKey, Selector selector);

}
