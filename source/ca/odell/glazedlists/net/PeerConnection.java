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
import java.text.ParseException;
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
    
    /** the state of this connection */
    private static final int AWAITING_CONNECT = 0;
    private static final int READY = 1;
    private static final int CLOSED = 2;
    private int state = AWAITING_CONNECT;
    
    /** whether this connection is ready yet */
    private boolean ready = false;
    
    /** whether this connection has been closed */
    private boolean closed = false;
    
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

        // clean up the incoming subscriptions
        for(Iterator r = incomingSubscriptions.values().iterator(); r.hasNext(); ) {
            PeerResource resource = (PeerResource)r.next();
            resource.publisherConnectionClosed(this);
        }
        incomingSubscriptions.clear();
        
        // clean up the outgoing publications
        for(Iterator r = outgoingPublications.values().iterator(); r.hasNext(); ) {
            PeerResource resource = (PeerResource)r.next();
            resource.subscriberConnectionClosed(this);
            r.remove();
        }
        outgoingPublications.clear();
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
                if(block.getAction().equals(Peer.ACTION_SUBSCRIBE)) remoteSubscribe(block);
                else if(block.getAction().equals(Peer.ACTION_SUBSCRIBE_CONFIRM)) remoteSubscribeConfirm(block);
                else if(block.getAction().equals(Peer.ACTION_UPDATE)) remoteUpdate(block);
                else if(block.getAction().equals(Peer.ACTION_UNSUBSCRIBE)) remoteUnsubscribe(block);
                else throw new IllegalStateException();
            }
        // if the data is corrupted, close the connection
        } catch(ParseException e) {
            source.close(e);
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
        if(incomingSubscription == null) throw new IllegalStateException("Connection not subscribed to " + resourceName);
        
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
     * Addes a subscription to the specified resource.
     */
    public void addIncomingSubscription(PeerResource resource) {
        incomingSubscriptions.put(resource.getResourceName(), resource);
    }
    
    /**
     * Remove the subscription to the specified resource.
     */
    public void removeIncomingSubscription(PeerResource resource) {
        Object removed = incomingSubscriptions.remove(resource.getResourceName());
        if(removed != resource) throw new IllegalStateException();
        if(incomingSubscriptions.isEmpty() && outgoingPublications.isEmpty()) {
            close();
        }
    }
    
    /**
     * Close this peer connection.
     */
    public void close() {
        if(state != READY) throw new IllegalStateException();
        if(!incomingSubscriptions.isEmpty()) throw new IllegalStateException();
        if(!outgoingPublications.isEmpty()) throw new IllegalStateException();
        if(connection == null) throw new IllegalStateException();
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
