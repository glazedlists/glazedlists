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
 * A callback interface for classes that implement a CTP Client or Server.
 * The CTPClientProtocol and CTPServerProtocol shall handle the lowest network
 * level of the protocol and handlers are used to interpret the data.
 */
interface CTPHandler {
    
    /** standard HTTP response headers, see HTTP/1.1 RFC, 6.1.1 */
    public static final int OK = 200;
    public static final int NOT_FOUND = 404;

    /**
     * Handles an HTTP response from the specified connection.
     *
     * @param code the HTTP  response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1.
     *      This will be null if this is an HTTP request.
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2.
     */
    public void receiveResponse(CTPProtocol source, Integer code, Map headers);

    /**
     * Handles an HTTP request from the specified connection.
     *
     * @param uri the address requested by the client, in the format of a file
     *      address. See HTTP/1.1 RFC, 5.1.2. This will be null if this is an
     *      HTTP response.
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2.
     */
    public void receiveRequest(CTPProtocol source, String uri, Map headers);
    
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
