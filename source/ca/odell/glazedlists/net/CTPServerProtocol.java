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
class CTPProtocol {
    
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

/**
 * The CTPServerProtocol is a serverside implementation of Chunked Transfer
 * Protocol. The serverside implementation must handle incoming POST requests
 * by responding with a coded response and is otherwise identical to the client. 
 */
class CTPServerProtocol extends CTPProtocol {

    /**
     * Sends the response header to the client.
     *
     * @param code an HTTP response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2. This can
     *      be null to indicate no headers.
     */
    public void sendResponse(int code, Map headers) throws CTPException {
    }
}

/**
 * The CTPClientProtocol is a clientside implementation of Chunked Transfer
 * Protocol. The clientside implementation must generate outgoing POST requests
 * and accept the server's coded response but is otherwise identical to the server.
 */
class CTPClientProtocol extends CTPProtocol {

    /**
     * Sends the response header to the client.
     *
     * @param code an HTTP response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2. This can
     *      be null to indicate no headers.
     */
    public void sendRequest(String uri, Map headers) throws CTPException {
    }
}

/**
 * A callback interface for classes that implement a CTP Client or Server.
 * The CTPClientProtocol and CTPServerProtocol shall handle the lowest network
 * level of the protocol and handlers are used to interpret the data.
 */
interface CTPHandler {
    
    /** standard HTTP response headers, see HTTP/1.1 RFC, 6.1.1 */
    public static final int OK = 200;
    public static final int NOT_FOUND = 404;

    /**
     * Handles an HTTP request or response from the specified connection.
     *
     * @param code the HTTP  response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1.
     *      This will be null if this is an HTTP request.
     * @param uri the address requested by the client, in the format of a file
     *      address. See HTTP/1.1 RFC, 5.1.2. This will be null if this is an
     *      HTTP response.
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2.
     */
    public void receiveConnect(CTPProtocol source, Integer code, String uri, Map headers);

    /**
     * Handles reception of the specified chunk of data. This chunk should be able
     * to be cleanly concatenated with the previous and following chunks without
     * problem by the reader.
     *
     * @param data A non-empty array of bytes.
     */
    public void receiveChunk(CTPProtocol source, byte[] data);

    /**
     * Handles the connection being closed by the remote client. This will also
     * be called if there is a connection error, which is the case when a server
     * sends data that cannot be interpretted by CTPServerProtocol.
     *
     * @param reason An exception if the connection was closed as the result of
     *      a failure. This may be null.
     */
    public void connectionClosed(CTPProtocol source, Exception reason);
}

/**
 * A CTPException is thrown when there is an error connecting to the
 * network or parsing a response.
 */
class CTPException extends Exception {
    public CTPException(String message) {
        super(message);
    }
    public CTPException(Exception cause) {
        super(cause);
    }
}
