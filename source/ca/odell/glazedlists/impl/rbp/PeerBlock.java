/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.rbp;

// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;
import ca.odell.glazedlists.impl.io.Bufferlo;
import java.text.ParseException;

/**
 * A binary message between peers.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class PeerBlock {
    
    /** constants used in the protocol */
    private static final String RESOURCE_URI = "Resource-Uri";
    private static final String SESSION_ID = "Session-Id";
    private static final String UPDATE_ID = "Update-Id";
    private static final String ACTION = "Action";
    private static final String ACTION_SUBSCRIBE = "Subscribe";
    private static final String ACTION_SUBSCRIBE_CONFIRM = "Subscribe-Confirm";
    private static final String ACTION_UPDATE = "Update";
    private static final String ACTION_UNSUBSCRIBE = "Unsubscribe";
    private static final String ACTION_UNPUBLISH = "Unpublish";

    /** the resource this block is concerned with */
    private ResourceUri resourceUri = null;
    
    /** a check id */
    private int sessionId = -1;
    
    /** the action of this block */
    private String action = null;

    /** the sequence ID of this block */
    private int updateId = -1;
    
    /** the binary data of this block */
    private Bufferlo payload = null;
    
    /**
     * Create a new PeerBlock.
     */
    private PeerBlock(ResourceUri resourceUri, int sessionId, String action, int updateId, Bufferlo payload) {
        this.resourceUri = resourceUri;
        this.sessionId = sessionId;
        this.action = action;
        this.updateId = updateId;
        this.payload = payload;
    }
    
    /**
     * Create a new subscribe block.
     */
    public static PeerBlock subscribeConfirm(ResourceUri resourceUri, int sessionId, int updateId, Bufferlo snapshot) {
        return new PeerBlock(resourceUri, sessionId, PeerBlock.ACTION_SUBSCRIBE_CONFIRM, updateId, snapshot);
    }
    
    /**
     * Create a new subscribe block.
     */
    public static PeerBlock update(ResourceUri resourceUri, int sessionId, int updateId, Bufferlo delta) {
        return new PeerBlock(resourceUri, sessionId, PeerBlock.ACTION_UPDATE, updateId, delta);
    }
    
    /**
     * Create a new subscribe block.
     */
    public static PeerBlock subscribe(ResourceUri resourceUri) {
        return new PeerBlock(resourceUri, -1, PeerBlock.ACTION_SUBSCRIBE, -1, null);
    }

    /**
     * Create a new subscribe block.
     */
    public static PeerBlock unsubscribe(ResourceUri resourceUri) {
        return new PeerBlock(resourceUri, -1, PeerBlock.ACTION_UNSUBSCRIBE, -1, null);
    }

    /**
     * Create a new subscribe block.
     */
    public static PeerBlock unpublish(ResourceUri resourceUri) {
        return new PeerBlock(resourceUri, -1, PeerBlock.ACTION_UNPUBLISH, -1, null);
    }
    
    /**
     * Whether this is a subscribe-confirm block.
     */
    public boolean isSubscribeConfirm() {
        return ACTION_SUBSCRIBE_CONFIRM.equals(action);
    }
    
    /**
     * Whether this is an update block.
     */
    public boolean isUpdate() {
        return ACTION_UPDATE.equals(action);
    }
    
    /**
     * Whether this is a subscribe block.
     */
    public boolean isSubscribe() {
        return ACTION_SUBSCRIBE.equals(action);
    }
    
    /**
     * Whether this is an unsubscribe block.
     */
    public boolean isUnsubscribe() {
        return ACTION_UNSUBSCRIBE.equals(action);
    }

    /**
     * Whether this is an unpublish block.
     */
    public boolean isUnpublish() {
        return ACTION_UNPUBLISH.equals(action);
    }
    
    /**
     * Get the PeerBlock from the specified bytes.
     *
     * @return the first PeerBlock from the specified bytes, it it exists. The
     *      result may be null indicating an incomplete PeerBlock. There may be
     *      multiple PeerBlocks available in the specified list of bytes.
     */
    public static PeerBlock fromBytes(Bufferlo bytes, String localHost, int localPort) throws ParseException {
        // if a full block is not loaded
        int blockEndIndex = bytes.indexOf("\\r\\n");
        if(blockEndIndex == -1) return null;
        
        // read the bytes of the first block
        String blockSizeInDecimal = bytes.readUntil("\\r\\n", false);
        int blockSizeWithHeaders = Integer.parseInt(blockSizeInDecimal);
        
        // if the full block is not loaded, give up
        int bytesRequired = blockEndIndex + 2 + blockSizeWithHeaders + 2;
        if(bytes.length() < bytesRequired) {
            return null;
        }
        
        // consume the size
        bytes.consume("[0-9]*\\r\\n");
        
        // load the headers
        int lengthBeforeHeaders = bytes.length();
        Map headers = new TreeMap();
        while(true) {
            if(bytes.indexOf("\\r\\n") == 0) break;
            String key = bytes.readUntil("\\:( )*");
            String value = bytes.readUntil("\\r\\n");
            headers.put(key, value);
        }
        bytes.consume("\\r\\n");
        int lengthAfterHeaders = bytes.length();
        int headersLength = lengthBeforeHeaders - lengthAfterHeaders;
        
        // load the data
        int payloadLength = blockSizeWithHeaders - headersLength;
        Bufferlo payload = bytes.consume(payloadLength);
        bytes.consume("\\r\\n");
        
        // parse the headers
        String resourceUriString = (String)headers.get(RESOURCE_URI);
        ResourceUri resourceUri = ResourceUri.localOrRemote(resourceUriString, localHost, localPort);
        String sessionIdString = (String)headers.get(SESSION_ID);
        String action = (String)headers.get(ACTION);
        String updateIdString = (String)headers.get(UPDATE_ID);
        int sessionId = (sessionIdString != null) ? Integer.parseInt(sessionIdString) : -1;
        int updateId = (updateIdString != null) ? Integer.parseInt(updateIdString) : -1;
        
        // return the result
        return new PeerBlock(resourceUri, sessionId, action, updateId, payload);
    }

    /**
     * Get the bytes for this block.
     */
    public Bufferlo toBytes(String localHost, int localPort) {
        // the writer with no size info
        Bufferlo writer = new Bufferlo();
        
        // populate the map of headers
        Map headers = new TreeMap();
        if(resourceUri != null) headers.put(RESOURCE_URI, resourceUri.toString(localHost, localPort));
        if(sessionId != -1) headers.put(SESSION_ID, new Integer(sessionId));
        if(action != null) headers.put(ACTION, action);
        if(updateId != -1) headers.put(UPDATE_ID, new Integer(updateId));
        
        // write the header values
        for(Iterator i = headers.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry mapEntry = (Map.Entry)i.next();
            writer.write(mapEntry.getKey().toString());
            writer.write(": ");
            writer.write(mapEntry.getValue().toString());
            writer.write("\r\n");
        }
        writer.write("\r\n");

        // write the payload
        if(payload != null) {
            writer.append(payload.duplicate());
        }
        
        // wrap the size
        Bufferlo writerWithSize = new Bufferlo();
        writerWithSize.write("" + writer.length());
        writerWithSize.write("\r\n");
        writerWithSize.append(writer);
        writerWithSize.write("\r\n");
        
        // all done
        return writerWithSize;
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
    public Bufferlo getPayload() {
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
    public ResourceUri getResourceUri() {
        return resourceUri;
    }
}
