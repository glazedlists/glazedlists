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
import ca.odell.glazedlists.impl.io.Bufferlo;
import java.text.ParseException;

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
    private Bufferlo payload = null;
    
    /**
     * Create a new PeerBlock.
     */
    public PeerBlock(String resourceName, int sessionId, String action, int updateId, Bufferlo payload) {
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
    public static PeerBlock fromBytes(Bufferlo bytes) throws ParseException {
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
        String resourceName = (String)headers.get(Peer.RESOURCE_NAME);
        String sessionIdString = (String)headers.get(Peer.SESSION_ID);
        String action = (String)headers.get(Peer.ACTION);
        String updateIdString = (String)headers.get(Peer.UPDATE_ID);
        int sessionId = (sessionIdString != null) ? Integer.parseInt(sessionIdString) : -1;
        int updateId = (updateIdString != null) ? Integer.parseInt(updateIdString) : -1;
        
        // return the result
        return new PeerBlock(resourceName, sessionId, action, updateId, payload);
    }

    /**
     * Get the bytes for this block.
     */
    public Bufferlo getBytes() {
        // the writer with no size info
        Bufferlo writer = new Bufferlo();
        
        // populate the map of headers
        Map headers = new TreeMap();
        if(resourceName != null) headers.put(Peer.RESOURCE_NAME, resourceName);
        if(sessionId != -1) headers.put(Peer.SESSION_ID, new Integer(sessionId));
        if(action != null) headers.put(Peer.ACTION, action);
        if(updateId != -1) headers.put(Peer.UPDATE_ID, new Integer(updateId));
        
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
            writer.append(payload);
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
    public String getResourceName() {
        return resourceName;
    }
}
