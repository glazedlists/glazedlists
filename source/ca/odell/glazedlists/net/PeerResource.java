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
 * A resource that is being published on the network.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class PeerResource implements ResourceListener, ResourceStatus {
    
    /** the publisher of this resource */
    private PeerConnection publisher = null;
    
    /** the source of peer blocks */
    private PeerBlockFactory peerBlockFactory = null;
    
    /** the resource being managed */
    private Resource resource = null;
    
    /** subscribers interested in this resource */
    private List subscribers = new ArrayList();
    
    /** the name that this resource is being published as */
    private String resourceName;
    
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
        peerBlockFactory = new PeerBlockFactory(resourceName);
        peerBlockFactory.setSessionId(new Random(System.currentTimeMillis()).nextInt());
        
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
        
        // build a factory for data blocks
        peerBlockFactory = new PeerBlockFactory(resourceName);
        
        // subscribe to this resource
        PeerBlock block = peerBlockFactory.subscribe();
        publisher.addIncomingSubscription(this);
        publisher.writeBlock(this, block);
    }
    
    /**
     * Whether this resource is being updated from the network.
     */
    public boolean isLive() {
        return publisher != null;
    }
    
    /**
     * Unsubscribes to this resource.
     */
    public void unsubscribe() {
        // we can't unsubscribe until we have no subscribers
        if(publisher == null) throw new IllegalStateException();
        if(!subscribers.isEmpty()) throw new IllegalStateException();
        
        // unsubscribe from this resource
        PeerBlock block = peerBlockFactory.unsubscribe();
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
        updateId++;
        
        // if nobody's listening, we're done
        if(subscribers.isEmpty()) return;
        
        // forward the event to listeners
        PeerBlock block = peerBlockFactory.update(updateId, delta);
        
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
        // confirm the update is consistent
        if(block.getUpdateId() != (updateId + 1)) throw new IllegalStateException();
        if(block.getSessionId() != peerBlockFactory.getSessionId()) throw new IllegalStateException();

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
        PeerBlock subscribeConfirm = peerBlockFactory.subscribeConfirm(updateId, resource.toSnapshot());
        subscriber.writeBlock(this, subscribeConfirm);
        // unlock peer resource //////////////////////////////
    }
    
    /**
     * Confirms the subscription by the specified remote peer.
     */
    public void remoteSubscribeConfirm(PeerConnection publisher, PeerBlock block) {
        updateId = block.getUpdateId();
        peerBlockFactory.setSessionId(block.getSessionId());
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
