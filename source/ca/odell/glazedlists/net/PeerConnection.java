/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

import java.util.*;
// NIO
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;

/**
 * Manages a connection to a peer. The peer connection is multiplexed and can be
 * have the following purposes:
 * <li>Publish local updates to the subscriber peer
 * <li>Subscribe to remote updates from the publisher peer
 */
class PeerConnection implements CTPHandler {
    
    /** although updates have unique USP uris, they share the same HTTP URI */ 
    private static final String HTTP_URI = "/";
    
    /** our connection to the peer */
    private CTPProtocol peer;

    /** the source lists this peer is publishing that we are subscribing to */
    private List peerSources = new ArrayList();

    /** the update sources this peer is interested in receiving updates from */
    private List listenersToPeer = new ArrayList();

    /**
     * Creates a new peer connection to the specified client.
     *
     * @todo replace this constructor with one that takes a socket and constructs
     *      the CTPProtocol
     */
    public PeerConnection(CTPProtocol peer) {
        this.peer = peer;
    }
    
    /**
     * CTPHandler method handles an HTTP request or response from the specified
     * connection.
     */
    /*public void receiveConnect(CTPProtocol source, Integer code, String uri, Map headers) {
        // this is a request for a local source
        if(uri != null) {
            // throw out this connection if possible
            CTPServerProtocol serverProtocol = (CTPServerProtocol)source;
            if(!uri.equals(HTTP_URI)) {
                System.out.println("Request from " + source + " denied, uri: " + uri);
            } else {
                System.out.println("Request from " + source + " accepted, uri: " + uri);
            }

        // this is a response for a remote source
        } else if(code != null) {
            // close this connection if it has failed
            CTPClientProtocol clientProtocol = (CTPClientProtocol)source;
            if(code.intValue() != OK) {
                System.out.println("Response from " + source + " failed, code: " + code);
            } else {
                System.out.println("Response from " + source + " accepted, code: " + code);
            }
        }
    }*/
    
    /**
     * Handles an HTTP response from the specified connection.
     *
     * @param code the HTTP  response code such as 200 (OK). See HTTP/1.1 RFC, 6.1.1.
     *      This will be null if this is an HTTP request.
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2.
     */
    public void receiveResponse(CTPProtocol source, Integer code, Map headers) {
    }

    /**
     * Handles an HTTP request from the specified connection.
     *
     * @param uri the address requested by the client, in the format of a file
     *      address. See HTTP/1.1 RFC, 5.1.2. This will be null if this is an
     *      HTTP response.
     * @param headers a Map of HTTP response headers. See HTTP/1.1 RFC, 6.2.
     */
    public void receiveRequest(CTPProtocol source, String uri, Map headers) {
    }

    /**
     * Handles reception of the specified chunk of data.
     */
    public void receiveChunk(CTPProtocol source, ByteBuffer data) {
        System.out.println("Received chunk from " + source + " of size " + data.remaining());
        
        // handle the chunk based on its type
        /*if(chunkType.equals("USP-SUBSCRIBE")) {
            System.out.println("Recognized subscribe chunk type");
        } else {
            System.out.println("Unrecognized chunk type: \"" + chunkType + "\"");
        }*/
    }

    /**
     * Handles the connection being closed by the remote client.
     */
    public void connectionClosed(CTPProtocol source, Exception reason) {
        System.out.println("Connection to " + source + " closed, reason: " + reason.getMessage());
    }
    
    /**
     * Creates a chunk for subscribing to the specified list.
     */
    private byte[] createSubscribeChunk(String sourceName) {
         StringBuffer subscribeChunk = new StringBuffer();
         subscribeChunk.append("USP-SUBSCRIBE\n");
         subscribeChunk.append("source: ").append(sourceName).append("\n");
         return stringToBytes(subscribeChunk.toString());
    }
    
    /**
     * Converts the specified text into an array of bytes. This uses naive encoding
     * of casting characters to bytes. This shall not be used for character sets
     * that are not exclusively US ASCII.
     */
    private byte[] stringToBytes(String text) {
        byte[] result = new byte[text.length()];
        for(int b = 0; b < result.length; b++) {
            result[b] = (byte)text.charAt(b);
        }
        return result;
    }
}
