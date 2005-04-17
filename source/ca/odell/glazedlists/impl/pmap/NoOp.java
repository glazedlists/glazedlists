/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * This doesn't do anything! It's useful for flushing pending events with invokeAndWait().
 */
class NoOp implements Runnable {
    
    /** singleton */
    private static NoOp instance = new NoOp();

    /**
     * Private constructor blocks users from not using the singleton.
     */
    private NoOp() {
        // nothing
    }
    
    /**
     * Get an instance of NoOp. This is used instead of a conventional constructor
     * because NoOp is a singleton.
     */
    public static NoOp instance() {
        return instance;
    }
    
    /**
     * Doesn't do anything!
     */
    public void run() {
        // nothing
    }
}
