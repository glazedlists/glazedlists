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
 * The reason a connection is closed when the server is shutdown.
 */
class CTPServerShutdownException extends Exception {

    /**
     * Creates a new ServerShutdownException.
     */
    public CTPServerShutdownException() {
        super("Server shutting down");
    }
}
