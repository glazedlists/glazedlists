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
import ca.odell.glazedlists.util.impl.*;

/**
 * A binary message between peers.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class PeerBlock {
    
    /** the resource this block is concerned with */
    private String resourceName = null;
    
    /** a check id */
    private int sessionId = -1;
    
    /** the action of this block */
    private String action = null;

    /** the sequence ID of this block */
    private int updateId = -1;
    
    /** the binary data of this block */
    private List payload = null;
    
    /**
     * Create a new PeerBlock.
     */
    public PeerBlock(String resourceName, int sessionId, String action, int updateId, List payload) {
        this.resourceName = resourceName;
        this.sessionId = sessionId;
        this.action = action;
        this.updateId = updateId;
        this.payload = payload;
    }
    
    /**
     * Get the PeerBlock from the specified bytes.
     *
     * @return the first PeerBlock from the specified bytes, it it exists. The
     *      result may be null indicating an incomplete PeerBlock. There may be
     *      multiple PeerBlocks available in the specified list of bytes.
     */
    public static PeerBlock fromBytes(Bufferlo bytes) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the bytes for this block.
     */
    public Bufferlo getBytes() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Gets the action of this block.
     */
    public String getAction() {
        return action;
    }
    
    /**
     * Gets the binary data of this block.
     */
    public List getPayload() {
        return payload;
    }
    
    /**
     * Gets the update ID of this block.
     */
    public int getUpdateId() {
        return updateId;
    }

    /**
     * Gets the session ID of this block.
     */
    public int getSessionId() {
        return sessionId;
    }
    
    /**
     * Gets the resource that this block is for.
     */
    public String getResourceName() {
        return resourceName;
    }
}
