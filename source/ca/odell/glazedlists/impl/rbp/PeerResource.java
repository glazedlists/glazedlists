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
// logging
import java.util.logging.*;

/**
 * A resource that is being published on the network.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class PeerResource {

    /** logging */
    private static Logger logger = Logger.getLogger(PeerResource.class.toString());
    
    /** the peer that owns all connections */
    private Peer peer;
    
    /** the resource being managed */
    private Resource resource = null;
    /** the name that this resource is being published as */
    private String resourceName;
    
    /** the publisher of this resource */
    private PeerConnection publisher = null;
    /** subscribers interested in this resource */
    private List subscribers = new ArrayList();
    
    /** the session ID is a simple validation */
    private int sessionId = -1;
    /** the ID of the current update */
    private int updateId = 0;
    
    /** the host and port of the publisher */
    private String publisherHost = null;
    private int publisherPort = -1;
    
    /** listens to changes in the resource */
    private PrivateResourceListener resourceListener = new PrivateResourceListener();
    
    /** provides information about the status of this resource */
    private PrivateResourceStatus resourceStatus = new PrivateResourceStatus();
    
    /**
     * Create a new PeerResource for an outgoing resource.
     */
    public PeerResource(Peer peer, Resource resource, String resourceName) {
        this.peer = peer;
        this.publisher = null;
        this.resource = resource;
        this.resourceName = resourceName;
        
        // create a random session ID as a check
        this.sessionId = new Random(System.currentTimeMillis()).nextInt();
        
        // listen for updates
        resource.addResourceListener(resourceListener);
        
        // a server is initially connected
        resourceStatus.setConnected(true, null);
    }
    
    /**
     * Create a new PeerResource for an incoming resource.
     */
    public PeerResource(Peer peer, Resource resource, String resourceName, String publisherHost, int publisherPort) {
        this.peer = peer;
        this.resource = resource;
        this.resourceName = resourceName;
        this.publisherHost = publisherHost;
        this.publisherPort = publisherPort;

        // listen for updates
        resource.addResourceListener(resourceListener);
        
        // subscribe to the resource
        resourceStatus.connect();
    }
    
    
    /**
     * Gets the name of this resource.
     */
    public String getResourceName() {
        return resourceName;
    }
    
    /**
     * Handle the state of the specified connection changing.
     */
    public void connectionClosed(PeerConnection connection, Exception reason) {
        if(publisher == connection) {
            publisher = null;
            resourceStatus.setConnected(false, reason);
            connection.incomingSubscriptions.remove(resourceName);
        } else {
            subscribers.remove(connection);
            connection.outgoingPublications.remove(resourceName);
        }
    }
    
    /**
     * Listens to changes in the resource, so they can be broadcast to subscribers.
     */
    private class PrivateResourceListener implements ResourceListener {
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
                subscriber.writeBlock(PeerResource.this, block);
            }
        }
    }
    public ResourceListener resourceListener() {
        return resourceListener;
    }

    /**
     * Provides information about the status of this resource.
     */
    private class PrivateResourceStatus implements ResourceStatus {
        
        /** listeners interested in status changes */
        private List statusListeners = new ArrayList();
        
        /** connected if the current subscription has been confirmed */
        private boolean connected = false;
    
        /** {@inheritDoc} */
        public boolean isConnected() {
            return connected;
        }
        
        /** {@inheritDoc} */
        public void connect() {
            if(publisher != null) return;
            
            // connect to the publisher
            publisher = peer.getConnection(publisherHost, publisherPort);
            
            // subscribe to this resource
            publisher.incomingSubscriptions.put(resourceName, PeerResource.this);
            PeerBlock block = PeerBlock.subscribe(resourceName);
            publisher.writeBlock(PeerResource.this, block);
        }
        
        /** {@inheritDoc} */
        public void disconnect() {
            // if we're not connected
            if(publisher == null) return;
                
            // we can't unsubscribe until we have no subscribers
            if(!subscribers.isEmpty()) throw new IllegalStateException();
            
            // mark this as done
            PeerConnection oldPublisher = publisher;
            publisher = null;
            resourceStatus.setConnected(false, null);
            
            // unsubscribe from this resource
            oldPublisher.writeBlock(PeerResource.this, PeerBlock.unsubscribe(resourceName));
            oldPublisher.incomingSubscriptions.remove(resourceName);
            if(oldPublisher.isIdle()) oldPublisher.close();
        }
        
        /** {@inheritDoc} */
        public void addResourceStatusListener(ResourceStatusListener listener) {
            statusListeners.add(listener);
        }

        /** {@inheritDoc} */
        public void removeResourceStatusListener(ResourceStatusListener listener) {
            statusListeners.remove(listener);
        }
        
        /**
         * Update the status of this PeerResource and notify everyone who's interested.
         */
        private void setConnected(boolean connected, Exception reason) {
            this.connected = connected;
            for(Iterator i = statusListeners.iterator(); i.hasNext(); ) {
                ResourceStatusListener statusListener = (ResourceStatusListener)i.next();
                if(connected) statusListener.resourceConnected(this);
                else statusListener.resourceDisconnected(this, reason);
            }
        }
    
    }
    public ResourceStatus status() {
        return resourceStatus;
    }
    

    /**
     * Handles a block of incoming data.
     */
    void incomingBlock(PeerConnection source, PeerBlock block) {
        if(block.isSubscribe()) remoteSubscribe(source, block);
        else if(block.isSubscribeConfirm()) remoteSubscribeConfirm(source, block);
        else if(block.isUpdate()) remoteUpdate(source, block);
        else if(block.isUnsubscribe()) remoteUnsubscribe(source, block);
        else throw new IllegalStateException();
    }
    private void remoteUpdate(PeerConnection publisher, PeerBlock block) {
        // update the internal state
        if(publisher != null) updateId++;

        // confirm the update is consistent
        if(block.getUpdateId() != updateId) throw new IllegalStateException("Expected update id " + updateId + " but found " + block.getUpdateId());
        if(block.getSessionId() != sessionId) throw new IllegalStateException();

        // handle the update
        resource.update(block.getPayload());
    }
    private void remoteSubscribe(PeerConnection subscriber, PeerBlock block) {
        // lock peer resource //////////////////////////////
        
        // first create the subscription
        subscriber.outgoingPublications.put(resourceName, this);
        subscribers.add(subscriber);
        
        // now send the snapshot to this subscriber
        PeerBlock subscribeConfirm = PeerBlock.subscribeConfirm(resourceName, sessionId, updateId, resource.toSnapshot());
        subscriber.writeBlock(this, subscribeConfirm);
        // unlock peer resource //////////////////////////////
    }
    private void remoteSubscribeConfirm(PeerConnection publisher, PeerBlock block) {
        publisher.incomingSubscriptions.put(resourceName, this);
        
        updateId = block.getUpdateId();
        sessionId = block.getSessionId();
        resourceStatus.setConnected(true, null);
        resource.fromSnapshot(block.getPayload());
    }
    private void remoteUnsubscribe(PeerConnection subscriber, PeerBlock block) {
        subscribers.remove(subscriber);
        subscriber.outgoingPublications.remove(resourceName);
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
