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
 *     RECEIVED_CLOSE
 *       --[cleanup]-->
 *         CLOSED_PERMANENTLY
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
    protected static final int STATE_RECEIVED_CLOSE = 6;
    protected static final int STATE_CLOSED_PERMANENTLY = 7;
    
    /** the current state of this protocol */
    protected int state = STATE_AWAITING_CONNECT;
    
    /** the key to this protocol's channel */
    protected SelectionKey selectionKey = null;
    
    /** the channel where communication occurs */
    protected SocketChannel socketChannel = null;
    
    /** parse the input channel */
    protected ByteChannelReader parser;

    /** write the output channel */
    protected ByteChannelWriter writer;

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
        this.parser = new ByteChannelReader(socketChannel);
        this.writer = new ByteChannelWriter(socketChannel);
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
        close(null);
    }
    
    /**
     * Close the connection to the client. 
     *
     * <p>The closing behaviour is dictated by the current state of the connection
     * and the reason for closing.
     * <li>If the reason is an IOException, the socket is closed immediately
     * <li>Otherwise a "goodbye" message is sent to notify the other party of the close
     * <li>If the state is READY, the goodbye is a single 0-byte chunk
     * <li>If the state is AWAITING_REQUEST, the goodbye is a request error
     * <li>If the state is RECEIVED_CLOSE, no goodbye message is sent
     */
    protected void close(Exception reason) {
        try {
            // close is not a result of a connection error, so say goodbye
            if(reason == null || !(reason instanceof IOException)) {
            
                // if we haven't yet responded, respond now
                if(state == STATE_AWAITING_REQUEST) {
                    state = STATE_CONSTRUCTING_RESPONSE;
                    Map errorResponse = new TreeMap();
                    errorResponse.put("Content-Length", "0");
                    ((CTPServerProtocol)this).sendResponse(CTPHandler.ERROR, errorResponse);
                } else {
                    throw new IllegalStateException();
                }
            // when a connection error occurs, bail without a good-bye message
            } else if(reason instanceof IOException) {
                throw new UnsupportedOperationException();
                
            }
        } catch(CTPException e) {
            // the clean close failed, continue anyway
        }
        
        
        // close the socket
        try {
            logger.fine("Closed connection to " + this);
            socketChannel.close();
            state = STATE_CLOSED_PERMANENTLY;
            handler.connectionClosed(this, reason);
        } catch(IOException e) {
            // if this close failed, there's nothing we can do
        }
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
}
