/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// NIO
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;

/**
 * The CTPClientProtocol is a clientside implementation of Chunked Transfer
 * Protocol. The clientside implementation must generate outgoing POST requests
 * and accept the server's coded response but is otherwise identical to the server.
 */
final class CTPClientProtocol extends CTPProtocol {

    /**
     * Creates a new CTPServerProtocol.
     *
     * @param host the target connection.
     * @param selectionKey the connection managed by this higher-level protocol.
     */
    CTPClientProtocol(String host, SelectionKey selectionKey, CTPHandler handler) {
        super(selectionKey, handler);
        this.host = host;
        
        // wait for the request
        state = STATE_CONSTRUCTING_REQUEST;
        
        // we are currently interested in reading
        selectionKey.interestOps(SelectionKey.OP_READ);
    }
}
