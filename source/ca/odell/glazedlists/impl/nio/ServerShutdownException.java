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
 * The reason a connection is closed when the server is shutdown.
 */
class ServerShutdownException extends Exception {

    /**
     * Creates a new ServerShutdownException.
     */
    public ServerShutdownException() {
        super("Server shutting down");
    }
}
