/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.rbp;

// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;
import java.text.ParseException;
import ca.odell.glazedlists.impl.io.Bufferlo;
// BRP sits atop Chunk Transfer Protocol
import ca.odell.glazedlists.impl.ctp.*;
// logging
import java.util.logging.*;


/**
 * Models a connection to a local peer.
 *
 * <p>A connection is multiplexed and serves multiple resources. It contains a map
 * of resources being published and resources being subscribed to.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class PeerConnection implements CTPHandler {
    
    /** logging */
    private static Logger logger = Logger.getLogger(PeerConnection.class.toString());

    /** the peer that owns all connections */
    private Peer peer;
    
    /** the lower level connection to this peer */
    private CTPConnection connection = null;

    /** the state of this connection */
    private static final int AWAITING_CONNECT = 0;
    private static final int READY = 1;
    private static final int CLOSED = 2;
    private int state = AWAITING_CONNECT;

    /** the incoming bytes pending a full block */
    private Bufferlo currentBlock = new Bufferlo();

    /** the outgoing bytes pending a connection */
    private Bufferlo pendingConnect = new Bufferlo();

    /** locally subscribed resources by resource name */
    Map incomingSubscriptions = new TreeMap();

    /** locally published resources by resource name */
    Map outgoingPublications = new TreeMap();

    /**
     * Creates a new PeerConnection for the specified peer.
     */
    public PeerConnection(Peer peer) {
        this.peer = peer;
    }

    /**
     * Handles the connection being ready for chunks to be sent.
     */
    public void connectionReady(CTPConnection connection) {
        this.connection = connection;
        this.state = READY;
        if(pendingConnect.length() > 0) {
            connection.sendChunk(pendingConnect);
        }
    }

    /**
     * Handles the connection being closed by the remote client. This will also
     * be called if there is a connection error, which is the case when a remote
     * host sends data that cannot be interpretted by CTPConnection.
     *
     * @param reason An exception if the connection was closed as the result of
     *      a failure. This may be null.
     */
    public void connectionClosed(CTPConnection source, Exception reason) {
        this.connection = null;
        this.state = CLOSED;
        peer.removeConnection(this);
        
        // notify resources of the close
        List resourcesToNotify = new ArrayList();
        resourcesToNotify.addAll(incomingSubscriptions.values());
        resourcesToNotify.addAll(outgoingPublications.values());
        for(Iterator r = resourcesToNotify.iterator(); r.hasNext(); ) {
            PeerResource resource = (PeerResource)r.next();
            resource.connectionClosed(this, reason);
        }
    }

    /**
     * Handles reception of the specified chunk of data. This chunk should be able
     * to be cleanly concatenated with the previous and following chunks without
     * problem by the reader.
     *
     * @param data A non-empty ByteBuffer containing the bytes for this chunk. The
     *      relevant bytes start at data.position() and end at data.limit(). This
     *      buffer is only valid for the duration of this method call.
     */
    public void receiveChunk(CTPConnection source, Bufferlo data) {
        // get all the data in the working block
        currentBlock.append(data);
        
        // handle all blocks
        try {
            PeerBlock block = null;
            while((block = PeerBlock.fromBytes(currentBlock)) != null) {
                String resourceName = block.getResourceName();
                
                // get the resource for this connection
                PeerResource resource = null;
                if(block.isSubscribe()) {
                    resource = peer.getPublishedResource(resourceName);
                } else if(block.isUnsubscribe()) {
                    resource = (PeerResource)outgoingPublications.get(resourceName);
                } else if(block.isSubscribeConfirm() || block.isUpdate()) {
                    resource = (PeerResource)incomingSubscriptions.get(resourceName);
                } else {
                    throw new UnsupportedOperationException();
                }
                
                // handle an unknown resource name
                if(resource == null) {
                    logger.warning("Unknown resource: \"" + resourceName + "\"");
                    close();
                    return;
                }
                
                // handle the block
                resource.incomingBlock(this, block);
            }
        // if the data is corrupted, close the connection
        } catch(ParseException e) {
            source.close(e);
        }
    }
    
    /**
     * Test whether this connection is being used by incoming subscriptions or
     * outgoing publications.
     */
    boolean isIdle() {
        return (incomingSubscriptions.isEmpty() && outgoingPublications.isEmpty());
    }
    
    /**
     * Tests whether this connection is closed.
     */
    boolean isClosed() {
        return (state == CLOSED);
    }

    /**
     * Close this peer connection.
     */
    public void close() {
        if(state != READY) throw new UnsupportedOperationException();
        if(connection == null) throw new UnsupportedOperationException();
        connection.close();
        peer.removeConnection(this);
    }
    
    /**
     * Writes the specified block to this peer.
     */
    public void writeBlock(PeerResource resource, PeerBlock block) {
        if(state == AWAITING_CONNECT) {
            pendingConnect.append(block.getBytes());
        } else if(state == READY) {
            connection.sendChunk(block.getBytes());
        } else if(state == CLOSED) {
            throw new IllegalStateException();
        } else {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Gets this connection as a String.
     */
    public String toString() {
        if(state == AWAITING_CONNECT) return "pending";
        else if(state == READY) return connection.toString();
        else if(state == CLOSED) return "closed";
        else throw new IllegalStateException();
    }
    
    /**
     * Prints the current state of this connection.
     */
    void print() {
        StringBuffer result = new StringBuffer();
        System.out.print(this);
        System.out.print(": ");
        System.out.print("Incoming {");
        for(Iterator s = incomingSubscriptions.keySet().iterator(); s.hasNext(); ) {
            String resourceName = (String)s.next();
            System.out.print(resourceName);
            if(s.hasNext()) System.out.print(", ");
        }
        System.out.print("}, ");
        System.out.print("Outgoing {");
        for(Iterator s = outgoingPublications.keySet().iterator(); s.hasNext(); ) {
            String resourceName = (String)s.next();
            System.out.print(resourceName);
            if(s.hasNext()) System.out.print(", ");
        }
        System.out.println("}");
    }
}
