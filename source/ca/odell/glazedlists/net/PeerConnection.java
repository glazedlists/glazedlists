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
 * Models a connection to a local peer.
 *
 * <p>A connection is multiplexed and serves multiple resources. It contains a map
 * of resources being published and resources being subscribed to.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class PeerConnection implements CTPHandler {
    
    /** the peer that owns all connections */
    private Peer peer;
    
    /** the lower level connection to this peer */
    private CTPConnection connection = null;
    
    /** whether this connection is ready yet */
    private boolean ready = false;
    
    /** the incoming bytes pending a full block */
    private Bufferlo currentBlock = new Bufferlo();
    
    /** the outgoing bytes pending a connection */
    private Bufferlo pendingConnect = new Bufferlo();

    /** locally subscribed resources by resource name */
    private Map incomingSubscriptions = new TreeMap();
    
    /** locally published resources by resource name */
    private Map outgoingPublications = new TreeMap();
    
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
        this.ready = true;
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
        throw new UnsupportedOperationException("we have to clean up resources");
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
        PeerBlock block = null;
        while((block = PeerBlock.fromBytes(currentBlock)) != null) {
            if(block.getAction().equals(Peer.ACTION_SUBSCRIBE)) remoteSubscribe(block);
            else if(block.getAction().equals(Peer.ACTION_SUBSCRIBE_CONFIRM)) remoteSubscribeConfirm(block);
            else if(block.getAction().equals(Peer.ACTION_UPDATE)) remoteUpdate(block);
            else if(block.getAction().equals(Peer.ACTION_UNSUBSCRIBE)) remoteUnsubscribe(block);
            else throw new IllegalStateException();
        }
    }
    
    /**
     * Handles an request to subscribe to something we're publishing.
     */
    private void remoteSubscribe(PeerBlock block) {
        // make sure we're not already publishing this
        String resourceName = block.getResourceName();
        if(outgoingPublications.get(resourceName) != null) throw new IllegalStateException("Connection is already subscribed to " + resourceName);
        
        // lookup the resource in the published list
        PeerResource localResource = peer.getPublishedResource(resourceName);
        if(localResource == null) throw new IllegalStateException();
        
        // do the subscribe
        localResource.remoteSubscribe(this, block);
        outgoingPublications.put(resourceName, localResource);
    }

    /**
     * Handles an incoming subscription confirmation for something we've requested.
     */
    private void remoteSubscribeConfirm(PeerBlock block) {
        // make sure we're not already subscribed
        String resourceName = block.getResourceName();
        PeerResource incomingSubscription = (PeerResource)incomingSubscriptions.get(resourceName);
        if(incomingSubscription != null) throw new IllegalStateException("Connection already subscribed to " + resourceName);
        
        // handle the confirmation
        incomingSubscription.remoteSubscribeConfirm(this, block);
        incomingSubscriptions.put(resourceName, incomingSubscription);
    }

    /**
     * Handles an incoming update to something we're subscribing to.
     */
    private void remoteUpdate(PeerBlock block) {
        // make sure we're subscribed
        String resourceName = block.getResourceName();
        PeerResource incomingSubscription = (PeerResource)incomingSubscriptions.get(resourceName);
        if(incomingSubscription == null) throw new IllegalStateException("Connection not subscribed to " + resourceName);

        // handle the update
        incomingSubscription.remoteUpdate(this, block);
    }

    /**
     * Handles an incoming request unsubscribe from something we're publishing.
     */
    private void remoteUnsubscribe(PeerBlock block) {
        // make sure we're subscribed
        String resourceName = block.getResourceName();
        PeerResource localResource = (PeerResource)outgoingPublications.get(resourceName);
        if(localResource == null) throw new IllegalStateException("Connection not subscribed to " + resourceName);
        
        // handle the unsubscribe
        localResource.remoteUnsubscribe(this, block);
        outgoingPublications.remove(resourceName);
    }
    
    /**
     * Writes the specified block to this peer.
     */
    public void writeBlock(PeerResource resource, PeerBlock block) {
        if(!ready) {
            pendingConnect.append(block.getBytes());
        } else if(connection == null) {
            throw new IllegalStateException();
        } else {
            connection.sendChunk(block.getBytes());
        }
    }
}
