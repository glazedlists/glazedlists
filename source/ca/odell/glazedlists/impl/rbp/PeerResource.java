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
        
        // subscribe to the resource
        resourceStatus.connect();
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
            peer.invokeLater(new UpdatedRunnable(delta));
        }
        private class UpdatedRunnable implements Runnable {
            private Bufferlo delta = null;
            public UpdatedRunnable(Bufferlo delta) {
                this.delta = delta;
            }
            public void run() {
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
        public synchronized boolean isConnected() {
            return connected;
        }
        
        /** {@inheritDoc} */
        public void connect() {
            peer.invokeLater(new ConnectRunnable());
        }
        private class ConnectRunnable implements Runnable {
            public void run() {
                // listen for updates
                resource.addResourceListener(resourceListener);
                
                // if this is remote, subscribe to this resource
                if(publisherHost != null) {
                    publisher = peer.getConnection(publisherHost, publisherPort);
                
                    peer.subscribed.put(resourceName, PeerResource.this);
                    publisher.incomingSubscriptions.put(resourceName, PeerResource.this);
                    PeerBlock block = PeerBlock.subscribe(resourceName);
                    publisher.writeBlock(PeerResource.this, block);
        
                // if this is local, we're immediately connected
                } else {
                    resourceStatus.setConnected(true, null);
                    if(peer.published.get(resourceName) != null) throw new IllegalStateException();
                    peer.published.put(resourceName, PeerResource.this);
                }
            }
        }
    
        /** {@inheritDoc} */
        public void disconnect() {
            peer.invokeLater(new DisconnectRunnable());
        }
        private class DisconnectRunnable implements Runnable {
            public void run() {
                // we're immediately disconnected
                resourceStatus.setConnected(false, null);
                
                // listen for updates
                resource.removeResourceListener(resourceListener);
                
                // if this is remote
                if(publisherHost != null) {
                    peer.subscribed.remove(resourceName);
                    
                    // clean up the publisher
                    if(publisher != null) {
                        publisher.writeBlock(PeerResource.this, PeerBlock.unsubscribe(resourceName));
                        publisher.incomingSubscriptions.remove(resourceName);
                        if(publisher.isIdle()) publisher.close();
                        publisher = null;
                    }
    
                // if this is local
                } else {
                    if(peer.published.get(resourceName) == null) throw new IllegalStateException();
                    peer.published.remove(resourceName);
                    
                    // unpublish the subscribers
                    for(Iterator s = subscribers.iterator(); s.hasNext(); ) {
                        PeerConnection subscriber = (PeerConnection)s.next();
                        subscriber.writeBlock(PeerResource.this, PeerBlock.unpublish(resourceName));
                        subscriber.outgoingPublications.remove(resourceName);
                        if(subscriber.isIdle()) subscriber.close();
                        s.remove();
                    }
                }
            }
        }
        
        /** {@inheritDoc} */
        public synchronized void addResourceStatusListener(ResourceStatusListener listener) {
            statusListeners.add(listener);
        }

        /** {@inheritDoc} */
        public synchronized void removeResourceStatusListener(ResourceStatusListener listener) {
            statusListeners.remove(listener);
        }
        
        /**
         * Update the status of this PeerResource and notify everyone who's interested.
         */
        private void setConnected(boolean connected, Exception reason) {
            List listenersToNotify = new ArrayList();
            synchronized(PrivateResourceStatus.this) {
                this.connected = connected;
                listenersToNotify.addAll(statusListeners);
            }
            for(Iterator i = listenersToNotify.iterator(); i.hasNext(); ) {
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
        else if(block.isUnpublish()) remoteUnpublish(source, block);
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
        // we're accepting connections
        if(resourceStatus.isConnected()) {
            // first create the subscription
            subscriber.outgoingPublications.put(resourceName, this);
            subscribers.add(subscriber);
            
            // now send the snapshot to this subscriber
            PeerBlock subscribeConfirm = PeerBlock.subscribeConfirm(resourceName, sessionId, updateId, resource.toSnapshot());
            subscriber.writeBlock(this, subscribeConfirm);

        // we're not accepting connections for now
        } else {
            PeerBlock unpublish = PeerBlock.unpublish(resourceName);
            subscriber.writeBlock(this, unpublish);
            if(subscriber.isIdle()) subscriber.close();
        }
    }
    private void remoteSubscribeConfirm(PeerConnection publisher, PeerBlock block) {
        // update publisher
        publisher.incomingSubscriptions.put(resourceName, this);
        
        // update state
        updateId = block.getUpdateId();
        sessionId = block.getSessionId();
        resource.fromSnapshot(block.getPayload());
        
        // finally we're connected
        resourceStatus.setConnected(true, null);
    }
    private void remoteUnsubscribe(PeerConnection subscriber, PeerBlock block) {
        // remove the subscription
        subscribers.remove(subscriber);
        subscriber.outgoingPublications.remove(resourceName);
    }
    private void remoteUnpublish(PeerConnection subscriber, PeerBlock block) {
        // immediately disconnected
        resourceStatus.setConnected(false, new Exception("Resource became unavailable"));
        
        // clean up the publisher
        if(publisher != null) {
            publisher.incomingSubscriptions.remove(resourceName);
            if(publisher.isIdle()) publisher.close();
            publisher = null;
        }
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
