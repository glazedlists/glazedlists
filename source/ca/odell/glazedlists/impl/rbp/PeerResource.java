/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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

    /** the peer that owns all connections */
    private Peer peer;
    
    /** the resource being managed */
    private Resource resource = null;
    /** the update ID of the resource */
    private int resourceUpdateId = 0;
    
    /** the address that this resource is being published as */
    private ResourceUri resourceUri;
    
    /** the publisher of this resource */
    private PeerConnection publisher = null;
    /** subscribers interested in this resource */
    private List subscribers = new ArrayList();
    
    /** the session ID is a simple validation */
    private int sessionId = -1;

    /** listens to changes in the resource */
    private PrivateResourceListener resourceListener = new PrivateResourceListener();
    
    /** provides information about the status of this resource */
    private PrivateResourceStatus resourceStatus = new PrivateResourceStatus();
    
    /**
     * Create a new PeerResource for an incoming or outgoing resource.
     */
    public PeerResource(Peer peer, Resource resource, ResourceUri resourceUri) {
        this.peer = peer;
        this.resource = resource;
        this.resourceUri = resourceUri;
        
        // create a random session ID as a check
        if(resourceUri.isLocal()) {
            this.sessionId = new Random(System.currentTimeMillis()).nextInt();
        }
        
        // subscribe to the resource
        resourceStatus.connect();
    }
    
    
    /**
     * Gets the address of this resource.
     */
    public ResourceUri getResourceUri() {
        return resourceUri;
    }
    
    /**
     * Handle the state of the specified connection changing.
     */
    public void connectionClosed(ResourceConnection connection, Exception reason) {
        if(publisher == connection.getConnection()) {
            publisher = null;
            resourceStatus.setConnected(false, reason);
            connection.getConnection().incomingSubscriptions.remove(resourceUri);
        } else {
            subscribers.remove(connection);
            connection.getConnection().outgoingPublications.remove(resourceUri);
        }
    }
    
    /**
     * Listens to changes in the resource, so they can be broadcast to subscribers.
     */
    private class PrivateResourceListener implements ResourceListener {
        /**
         * Handles a change in a resource contained by the specified delta. This method
         * will be called while holding the Resource's write lock.
         */
        public void resourceUpdated(Resource resource, Bufferlo delta) {
            resourceUpdateId++;
            peer.invokeLater(new UpdatedRunnable(delta, resourceUpdateId));
        }
        private class UpdatedRunnable implements Runnable {
            private Bufferlo delta = null;
            private int updateId = -1;
            public UpdatedRunnable(Bufferlo delta, int updateId) {
                this.delta = delta;
                this.updateId = updateId;
            }
            public void run() {
                // if nobody's listening, we're done
                if(subscribers.isEmpty()) return;
                
                // forward the event to listeners
                PeerBlock block = PeerBlock.update(resourceUri, sessionId, updateId, delta);
                
                // send the block to interested subscribers
                for(int s = 0; s < subscribers.size(); s++) {
                    ResourceConnection subscriber = (ResourceConnection)subscribers.get(s);
                    if(subscriber.getUpdateId() >= updateId) continue;
                    subscriber.getConnection().writeBlock(PeerResource.this, block);
                    subscriber.setUpdateId(updateId);
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
                // if this is remote, subscribe to this resource
                if(resourceUri.isRemote()) {
                    publisher = peer.getConnection(resourceUri.getHost(), resourceUri.getPort());
                
                    peer.subscribed.put(resourceUri, PeerResource.this);
                    publisher.incomingSubscriptions.put(resourceUri, new ResourceConnection(publisher, PeerResource.this));
                    PeerBlock subscribe = PeerBlock.subscribe(resourceUri);
                    publisher.writeBlock(PeerResource.this, subscribe);
        
                // if this is local, we're immediately connected
                } else if(resourceUri.isLocal()) {
                    resource.addResourceListener(resourceListener);
                
                    resourceStatus.setConnected(true, null);
                    if(peer.published.get(resourceUri) != null) throw new IllegalStateException();
                    peer.published.put(resourceUri, PeerResource.this);
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
                
                // if this is remote, unsubscribe
                if(resourceUri.isRemote()) {
                    peer.subscribed.remove(resourceUri);
                    
                    // clean up the publisher
                    if(publisher != null) {
                        publisher.writeBlock(PeerResource.this, PeerBlock.unsubscribe(resourceUri));
                        publisher.incomingSubscriptions.remove(resourceUri);
                        if(publisher.isIdle()) publisher.close();
                        publisher = null;
                    }
    
                // if this is local, unpublish everyone
                } else if(resourceUri.isLocal()) {
                    resource.removeResourceListener(resourceListener);
                    
                    if(peer.published.get(resourceUri) == null) throw new IllegalStateException();
                    peer.published.remove(resourceUri);
                    
                    // unpublish the subscribers
                    for(Iterator s = subscribers.iterator(); s.hasNext(); ) {
                        ResourceConnection subscriber = (ResourceConnection)s.next();
                        subscriber.getConnection().writeBlock(PeerResource.this, PeerBlock.unpublish(resourceUri));
                        subscriber.getConnection().outgoingPublications.remove(resourceUri);
                        if(subscriber.getConnection().isIdle()) subscriber.getConnection().close();
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
    void incomingBlock(ResourceConnection source, PeerBlock block) {
        if(block.isSubscribe()) remoteSubscribe(source, block);
        else if(block.isSubscribeConfirm()) remoteSubscribeConfirm(source, block);
        else if(block.isUpdate()) remoteUpdate(source, block);
        else if(block.isUnsubscribe()) remoteUnsubscribe(source, block);
        else if(block.isUnpublish()) remoteUnpublish(source, block);
        else throw new IllegalStateException();
    }
    private void remoteUpdate(ResourceConnection publisher, PeerBlock block) {
        // confirm the update is consistent with the session
        if(block.getSessionId() != sessionId) throw new IllegalStateException();

        // handle the update
        resource.getReadWriteLock().writeLock().lock();
        try {
            // confirm this update is consistent with the update ID
            if(block.getUpdateId() != (resourceUpdateId+1)) throw new IllegalStateException("Expected update id " + (resourceUpdateId+1) + " but found " + block.getUpdateId());
            // apply locally
            resource.update(block.getPayload());
            // update state and propagate
            resourceListener.resourceUpdated(resource, block.getPayload());
        } finally {
            resource.getReadWriteLock().writeLock().unlock();
        }
    }
    private void remoteSubscribe(ResourceConnection subscriber, PeerBlock block) {
        // we're accepting connections
        if(resourceStatus.isConnected()) {
            // save the update id and a snapshot
            int updateId = -1;
            Bufferlo snapshot = null;
            resource.getReadWriteLock().writeLock().lock();
            try {
                updateId = resourceUpdateId;
                snapshot = resource.toSnapshot();
            } finally {
                resource.getReadWriteLock().writeLock().unlock();
            }
            
            // create the subscription
            subscriber.setUpdateId(updateId);
            subscriber.getConnection().outgoingPublications.put(resourceUri, subscriber);
            subscribers.add(subscriber);
            
            // now send the snapshot to this subscriber
            PeerBlock subscribeConfirm = PeerBlock.subscribeConfirm(resourceUri, sessionId, updateId, snapshot);
            subscriber.getConnection().writeBlock(this, subscribeConfirm);

        // we're not accepting connections for now
        } else {
            PeerBlock unpublish = PeerBlock.unpublish(resourceUri);
            subscriber.getConnection().writeBlock(this, unpublish);
            if(subscriber.getConnection().isIdle()) subscriber.getConnection().close();
        }
    }
    private void remoteSubscribeConfirm(ResourceConnection publisher, PeerBlock block) {
        // handle the confirm
        resource.getReadWriteLock().writeLock().lock();
        try {
            // apply locally
            resource.fromSnapshot(block.getPayload());
            // update state and propagate
            resourceUpdateId = block.getUpdateId();
            //resourceListener.resourceUpdated(resource, block.getPayload());
        } finally {
            resource.getReadWriteLock().writeLock().unlock();
        }
        
        // save a session cookie to verify this is the same source
        sessionId = block.getSessionId();
        
        // finally we're connected
        resourceStatus.setConnected(true, null);
    }
    private void remoteUnsubscribe(ResourceConnection subscriber, PeerBlock block) {
        // remove the subscription
        subscribers.remove(subscriber);
        subscriber.getConnection().outgoingPublications.remove(resourceUri);
    }
    private void remoteUnpublish(ResourceConnection subscriber, PeerBlock block) {
        // immediately disconnected
        resourceStatus.setConnected(false, new Exception("Resource became unavailable"));
        
        // clean up the publisher
        if(publisher != null) {
            publisher.incomingSubscriptions.remove(resourceUri);
            if(publisher.isIdle()) publisher.close();
            publisher = null;
        }
    }
    
    /**
     * Gets this resource as a String for debugging.
     */
    public void print() {
        System.out.print(resourceUri);
        System.out.print(" from: ");
        System.out.print(publisher);
        System.out.print(" to: ");
        for(Iterator s = subscribers.iterator(); s.hasNext(); ) {
            ResourceConnection subscriber = (ResourceConnection)s.next();
            System.out.print(subscriber.getConnection().toString());
            if(s.hasNext()) System.out.print(", ");
        }
        System.out.println("");
    }
}
