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
 * The CTPProtocol is base class for building a client or server implementation
 * of Chunked Transfer Protocol. This protocol is a subset of HTTP/1.1 with special
 * interest to chunked encoding.
 *
 * client:
 * AWAITING_CONNECT
 *   --[connect]-->
 *     CONSTRUCTING_REQUEST
 *     --[send request]-->
 *       AWAITING_RESPONSE
 *         --[received response]-->
 *           READY
 * server:
 * AWAITING_CONNECT
 *   --[connect]-->
 *     AWAITING_REQUEST
 *       --[received request]-->
 *         CONSTRUCTING_RESPONSE
 *           --[sent response]-->
 *             READY
 *
 * requested shutdown:
 * READY
 *   --[close()]-->
 *     --[sent close chunk]-->
 *       --[cleanup]-->
 *         CLOSED_PERMANENTLY
 *
 * received shutdown:
 * READY
 *   --[received close chunk]-->
 *     --[cleanup]-->
 *       CLOSED_PERMANENTLY
 *
 * IO error:
 * READY
 *   --[IO error]-->
 *     --[cleanup]-->
 *       CLOSED_PERMANENTLY
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
abstract class CTPProtocol {

    /** logging */
    private static Logger logger = Logger.getLogger(CTPProtocol.class.toString());
    
    /** HTTP constants */
    protected static final String HTTP_VERSION = "HTTP/1.1";
    protected static final String HTTP_POST = "POST";
    protected static final byte[] HTTP_REASON_PHRASE_OK = "OK".getBytes();
    protected static final byte[] HTTP_REASON_PHRASE_NOT_FOUND = "Not Found".getBytes();
    protected static final byte[] HTTP_SPACE = " ".getBytes();
    protected static final byte[] HTTP_CRLF = "\r\n".getBytes();
    protected static final byte[] HTTP_HEADER_SEPARATOR = ": ".getBytes();
    protected static final String CHARSET = "US-ASCII";
    
    /** track the current state of this protocol */
    protected static final int STATE_AWAITING_CONNECT = 0;
    protected static final int STATE_CONSTRUCTING_REQUEST = 1;
    protected static final int STATE_AWAITING_REQUEST = 2;
    protected static final int STATE_CONSTRUCTING_RESPONSE = 3;
    protected static final int STATE_AWAITING_RESPONSE = 4;
    protected static final int STATE_READY = 5;
    protected static final int STATE_CLOSED_PERMANENTLY = 6;
    
    /** the current state of this protocol */
    protected int state = STATE_AWAITING_CONNECT;
    
    /** the key to this protocol's channel */
    protected SelectionKey selectionKey = null;
    
    /** the channel where communication occurs */
    protected SocketChannel socketChannel = null;
    
    /** the handler to delegate data interpretation to */
    protected CTPHandler handler;

    /**
     * Creates a new CTPProtocol.
     *
     * @param selectionKey the connection managed by this higher-level protocol.
     */
    protected CTPProtocol(SelectionKey selectionKey, CTPHandler handler) {
        if(selectionKey == null) throw new IllegalArgumentException();
        
        this.selectionKey = selectionKey;
        this.handler = handler;
        this.socketChannel = (SocketChannel)selectionKey.channel();
    }

    /**
     * Sends the specified chunk of data immediately. This chunk should be able to
     * be cleanly concatenated with the previous and following chunks without
     * problem by the reader.
     *
     * @param data A non-empty array of bytes.
     */
    public void sendChunk(byte[] data) throws CTPException {
    }

    /**
     * Closes the connection to the client. As specified by the HTTP/1.1 RFC,
     * this sends a single empty chunk and closes the TCP/IP connection.
     */
    public void close() {
    }

    /**
     * Handles the incoming bytes.
     *
     * @param data a ByteBuffer that must not be written to or accessed after
     *      this method has returned.
     */
    abstract void handleRead() throws IOException;
    
    /**
     * Gets this protocol as a String for debugging.
     */
    public String toString() {
        return socketChannel.socket().getRemoteSocketAddress().toString();
    }
    
    /**
     * Handles the connection being closed.
     */
    protected void handleClose(Exception reason) throws IOException {
        state = STATE_CLOSED_PERMANENTLY;
        logger.fine("Closed connection to " + this);
        socketChannel.close();
        handler.connectionClosed(this, reason);
    }
}
