/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;

/**
 * A peer manages publishing and subscribing to resources.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class Peer implements CTPHandlerFactory {
    
    /** constants used in the protocol */
    static final String RESOURCE_NAME = "Resource-Name";
    static final String DELTA = "Delta";
    static final String SNAPSHOT = "Snapshot";
    static final String SESSION_ID = "Session-Id";
    static final String UPDATE_ID = "Update-Id";
    static final String ACTION = "Action";
    static final String ACTION_SUBSCRIBE = "Subscribe";
    static final String ACTION_SUBSCRIBE_CONFIRM = "Subscribe-Confirm";
    static final String ACTION_UPDATE = "Update";
    static final String ACTION_UNSUBSCRIBE = "Unsubscribe";

    /** the resources being subscribed to */ 
    private Map subscribed = new TreeMap();
    
    /** the resources being published */
    private Map published = new TreeMap();
     
    /** the active connections to peers */
    private List connections = new ArrayList();
    
    /** the connection management */
    private CTPConnectionManager connectionManager;
    private int port;
    
    /**
     * Creates a new peer that binds to the specified port.
     */
    public Peer(int port) {
        this.port = port;
    }
    
    /**
     * Upon a connect, a CTPHandler is required to handle the data of this connection.
     * The returned CTPHandler will be delegated to handle the connection's data.
     */
    public CTPHandler constructHandler() {
        PeerConnection incoming = new PeerConnection(this);
        connections.add(incoming);
        return incoming;
    }

    /**
     * Starts the peer.
     */
    public void start() {
         connectionManager = new CTPConnectionManager(this, port);
         connectionManager.start();
    }
    
    /**
     * Stops the peer.
     */
    public void stop() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Subscribe to the specified resource.
     */
    public void subscribe(Resource resource, String resourceName, String host, int port) {
        PeerConnection connection = getConnection(host, port);
        PeerResource peerResource = new PeerResource(connection, resource, resourceName);
        subscribed.put(resourceName, peerResource);
    }
    
    /**
     * Publish the specified resource.
     */
    public void publish(Resource resource, String resourceName) {
        PeerResource peerResource = new PeerResource(resource, resourceName);
        published.put(resourceName, peerResource);
    }

    /**
     * Creates a block that contains the specified information.
     *
     * @return a List of ByteBuffers
     */
    static List encodeBlock(Map parameters) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates a Map of from the specified block or partial block.
     *
     * @param data a List of ByteBuffers
     */
     static Map decodeBlock(List data) {
        throw new UnsupportedOperationException();
     }
     
     /**
      * Gets the specified published resource.
      */
     PeerResource getPublishedResource(String name) {
         return (PeerResource)published.get(name);
     }
     
     /**
      * Gets the specified connection, or creates it if necessary.
      */
     private PeerConnection getConnection(String host, int port) {
         PeerConnection peerConnection = new PeerConnection(this);
         connectionManager.connect(peerConnection, host, port);
         connections.add(peerConnection);
         return peerConnection;
     }
}
