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
 * The CTPClientProtocol is a clientside implementation of Chunked Transfer
 * Protocol. The clientside implementation must generate outgoing POST requests
 * and accept the server's coded response but is otherwise identical to the server.
 */
final class CTPClientProtocol extends CTPProtocol {

    /**
     * Creates a server that connects to the specified address.
     */
    public CTPClientProtocol(String host) throws CTPException {
        
    }

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

