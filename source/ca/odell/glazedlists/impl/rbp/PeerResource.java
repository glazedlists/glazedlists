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
import ca.odell.glazedlists.impl.io.Bufferlo;
import ca.odell.glazedlists.net.*;

/**
 * A resource that is being published on the network.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class PeerResource implements ResourceListener, ResourceStatus {
    
    /** the publisher of this resource */
    private PeerConnection publisher = null;
    
    /** the resource being managed */
    private Resource resource = null;
    
    /** subscribers interested in this resource */
    private List subscribers = new ArrayList();
    
    /** the name that this resource is being published as */
    private String resourceName;
    
    /** the session ID is a simple validation */
    private int sessionId = -1;
    
    /** the ID of the current update */
    private int updateId = 0;
    
    /**
     * Create a new PeerResource to manage the specified resource.
     */
    public PeerResource(Resource resource, String resourceName) {
        this.publisher = null;
        this.resource = resource;
        this.resourceName = resourceName;
        
        // build a factory for data blocks
        this.sessionId = new Random(System.currentTimeMillis()).nextInt();
        
        // listen for updates
        resource.addResourceListener(this);
    }
    
    /**
     * Create a new PeerResource to manage the specified resource.
     */
    public PeerResource(PeerConnection publisher, Resource resource, String resourceName) {
        this.publisher = publisher;
        this.resource = resource;
        this.resourceName = resourceName;
        
        // subscribe to this resource
        publisher.addIncomingSubscription(this);
        PeerBlock block = PeerBlock.subscribe(resourceName);
        publisher.writeBlock(this, block);
        
        // listen for updates
        resource.addResourceListener(this);
    }
    
    /**
     * Whether this resource is connected.
     */
    public boolean isConnected() {
        return publisher != null;
    }
    public void connect() {
        throw new UnsupportedOperationException();
    }
    public void disconnect() {
        throw new UnsupportedOperationException();
    }
    public void addResourceStatusListener(ResourceStatusListener listener) {
        throw new UnsupportedOperationException();
    }
    public void removeResourceStatusListener(ResourceStatusListener listener) {
        throw new UnsupportedOperationException();
    }
    
    
    
    /**
     * Unsubscribes to this resource.
     */
    public void unsubscribe() {
        // we can't unsubscribe until we have no subscribers
        if(publisher == null) throw new IllegalStateException();
        if(!subscribers.isEmpty()) throw new IllegalStateException();
        
        // unsubscribe from this resource
        PeerBlock block = PeerBlock.unsubscribe(resourceName);
        publisher.writeBlock(this, block);
        publisher.removeIncomingSubscription(this);
    }
    
    /**
     * Gets the name of this resource.
     */
    public String getResourceName() {
        return resourceName;
    }
    
    /**
     * Handles a change in a local resource contained by the specified delta.
     */
    public void resourceUpdated(Resource resource, Bufferlo delta) {
        // update the internal state
        if(publisher == null) updateId++;
        
        // if nobody's listening, we're done
        if(subscribers.isEmpty()) return;
        
        // forward the event to listeners
        PeerBlock block = PeerBlock.update(resourceName, sessionId, updateId, delta);
        
        // send the block to interested subscribers
        for(int s = 0; s < subscribers.size(); s++) {
            PeerConnection subscriber = (PeerConnection)subscribers.get(s);
            subscriber.writeBlock(this, block);
        }
    }
    
    /**
     * Handles a change in a remote resource.
     */
    public void remoteUpdate(PeerConnection publisher, PeerBlock block) {
        // update the internal state
        if(publisher != null) updateId++;

        // confirm the update is consistent
        if(block.getUpdateId() != updateId) throw new IllegalStateException("Expected update id " + updateId + " but found " + block.getUpdateId());
        if(block.getSessionId() != sessionId) throw new IllegalStateException();

        // handle the update
        resource.update(block.getPayload());
    }
    
    /**
     * Subscribe the specified remote peer to this resource.
     */
    public void remoteSubscribe(PeerConnection subscriber, PeerBlock block) {
        // lock peer resource //////////////////////////////
        
        // first create the subscription
        subscribers.add(subscriber);
        
        // now send the snapshot to this subscriber
        PeerBlock subscribeConfirm = PeerBlock.subscribeConfirm(resourceName, sessionId, updateId, resource.toSnapshot());
        subscriber.writeBlock(this, subscribeConfirm);
        // unlock peer resource //////////////////////////////
    }
    
    /**
     * Confirms the subscription by the specified remote peer.
     */
    public void remoteSubscribeConfirm(PeerConnection publisher, PeerBlock block) {
        updateId = block.getUpdateId();
        sessionId = block.getSessionId();
        resource.fromSnapshot(block.getPayload());
    }
    
    /**
     * Unsubscribe the specified remote peer from this resource.
     */
    public void remoteUnsubscribe(PeerConnection subscriber, PeerBlock block) {
        subscribers.remove(subscriber);
    }
    
    /**
     * Notifies this resource that the connection to its publisher has been
     * closed.
     */
    public void publisherConnectionClosed(PeerConnection connection) {
        if(publisher != connection) throw new IllegalStateException();
        publisher = null;
    }
    
    /**
     * Notifies this resource that the connection to a subscriber has been closed.
     */
    public void subscriberConnectionClosed(PeerConnection connection) {
        boolean removed = subscribers.remove(connection);
        if(!removed) throw new IllegalStateException();
    }

    /**
     * Gets this resource as a String for debugging.
     */
    public void print() {
        System.out.print(resourceName);
        System.out.print(" from: ");
        System.out.print(publisher);
        System.out.print(" to: ");
        for(Iterator s = subscribers.iterator(); s.hasNext(); ) {
            PeerConnection subscriber = (PeerConnection)s.next();
            System.out.print(subscriber.toString());
            if(s.hasNext()) System.out.print(", ");
        }
        System.out.println("");
    }
}
