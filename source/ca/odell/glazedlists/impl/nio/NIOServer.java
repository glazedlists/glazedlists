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
 * Handles incoming NIO connections.
 */
public interface NIOServer {
    
    /**
     * Handle an accept-ready selection key.
     */
    public void handleAccept(SelectionKey selectionKey, Selector selector);

}
