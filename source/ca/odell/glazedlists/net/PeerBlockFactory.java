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
 * A factory for creating PeerBlocks.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class PeerBlockFactory {
    
    /** the resource this block is concerned with */
    private String resourceName = null;
    
    /** a check id */
    private int sessionId = -1;
    
    /**
     * Creates a new PeerBlockFactory that creates blocks for the specified local
     * resource.
     */
    public PeerBlockFactory(String resourceName) {
        this.resourceName = resourceName;
    }
    
    /**
     * Set the session ID to be used by this {@link PeerBlockFactory}.
     */
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
    * Get the session ID used by this {@link PeerBlockFactory}.
    */
    public int getSessionId() {
        return sessionId;
    }
    
    /**
     * Gets the name of the resource used by this {@link PeerBlockFactory}.
     */
    public String getResourceName() {
        return resourceName;
    }
    
    /**
     * Create a new subscribe block.
     */
    public PeerBlock subscribeConfirm(int updateId, List snapshot) {
        return new PeerBlock(resourceName, sessionId, Peer.ACTION_SUBSCRIBE_CONFIRM, updateId, snapshot);
    }
    
    /**
     * Create a new subscribe block.
     */
    public PeerBlock update(int updateId, List delta) {
        return new PeerBlock(resourceName, sessionId, Peer.ACTION_UPDATE, updateId, delta);
    }
    
    /**
     * Create a new subscribe block.
     */
    public PeerBlock subscribe() {
        return new PeerBlock(resourceName, -1, Peer.ACTION_SUBSCRIBE, -1, null);
    }

    /**
     * Create a new subscribe block.
     */
    public PeerBlock unsubscribe() {
        return new PeerBlock(resourceName, -1, Peer.ACTION_UNSUBSCRIBE, -1, null);
    }
}
