/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// for maps of headers
import java.util.*;

/**
 * The CTPProtocol is base class for building a client or server implementation
 * of Chunked Transfer Protocol. This protocol is a subset of HTTP/1.1 with special
 * interest to chunked encoding.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
abstract class CTPProtocol {
    
    /**
     * Creates a new CTPProtocol.
     */
    protected CTPProtocol() {
        
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
}

