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
import ca.odell.glazedlists.util.impl.ByteChannelReader;
import ca.odell.glazedlists.util.impl.ByteChannelWriter;
// logging
import java.util.logging.*;
import java.text.ParseException;

/**
 * The CTPServerProtocol is a serverside implementation of Chunked Transfer
 * Protocol. The serverside implementation must handle incoming POST requests
 * by responding with a coded response and is otherwise identical to the client. 
 */
final class CTPServerProtocol extends CTPProtocol {

    /**
     * Creates a new CTPServerProtocol.
     *
     * @param selectionKey the connection managed by this higher-level protocol.
     */
    public CTPServerProtocol(SelectionKey selectionKey, CTPHandler handler) {
        super(selectionKey, handler);
        
        // wait for the request
        state = STATE_AWAITING_REQUEST;
        
        // we are currently interested in reading
        selectionKey.interestOps(SelectionKey.OP_READ);
    }
}
