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
 * The CTPServerProtocol is a serverside implementation of Chunked Transfer
 * Protocol. This protocol is a subset of HTTP/1.1 with special interest to
 * chunked encoding.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class CTPServerProtocol {
    /**
     * Sends the response header to the client.
     *
     * @param code an HTTP response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2
     */
    public void sendResponse(int code, Map headers) throws CTPException {
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

/**
 * A callback interface for classes that implement a CTP Server. The
 * CTPServerProtocol shall handle the lowest network level of the protocol
 * and handlers are used to interpret the data.
 */
interface CTPServerHandler {
    /**
     * Handles an HTTP request for the specified URI. The request must be responded
     * to using the specified CTPServerProtocol.
     *
     * @param uri the address requested by the client, in the format of a file
     *      address. See HTTP/1.1 RFC, 5.1.2
     * @param headers a Map of HTTP request headers. See HTTP/1.1 RFC, 5.3
     */ 
    public void receiveGet(CTPServerProtocol source, String uri, Map headers);
    /**
     * Handles the connection being closed by the remote client. This will also
     * be called if there is a connection error, which is the case when a client
     * sends data that cannot be interpretted by CTPServerProtocol.
     *
     * @param reason An exception if the connection was closed as the result of
     *      a failure. This may be null.
     */
    public void connectionClosed(CTPServerProtocol source, Exception reason);
}

/**
 * The CTPClientProtocol is a clientside implementation of Chunked Transfer
 * Protocol. This protocol is a subset of HTTP/1.1 with special interest to
 * chunked encoding.
 */
class CTPClientProtocol {
    public void sendGet(String uri, Map headers) throws CTPException {
    }
    public void close() {
    }
}

/**
 * A callback interface for classes that implement a CTP Server. The
 * CTPClientProtocol shall handle the lowest network level of the protocol
 * and handlers are used to interpret the data.
 */
interface CTPClientHandler {
    public void receiveResponse(CTPClientProtocol source, int code, Map headers);
    public void receiveChunk(CTPClientProtocol source, byte[] data);
    public void connectionClosed(CTPClientProtocol source, Exception reason);
}

class CTPException extends Exception {
}
