/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.io.*;
import ca.odell.glazedlists.event.*;
// access to the volatile implementation pacakge
import ca.odell.glazedlists.impl.rbp.*;
import ca.odell.glazedlists.impl.io.*;
// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;

/**
 * A resource is a dynamic Object that can publish its changes as a series of deltas.
 * It is also possible to construct a resource using a shapshot.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class NetworkList extends TransformedList {

    /** listeners to resource changes */
    private List resourceListeners = new ArrayList();
    
    /** listeners to status changes */
    private List statusListeners = new ArrayList();
    
    /** how bytes are encoded and decoded */
    private ByteCoder byteCoder;
    
    /** who manages this resource's connection */
    private ResourceStatus resourceStatus = null;
    
    /** whether this NetworkList is writable via its own API */
    private boolean writable = false;
    
    /** implementations of ResourceStatusListener and Resource */
    private PrivateInterfaces privateInterfaces = new PrivateInterfaces();
    
    /**
     * Create a NetworkList that connects to the specified source.
     */
    NetworkList(EventList source, ByteCoder byteCoder) {
        super(source);
        this.byteCoder = byteCoder;
        source.addListEventListener(this);
    }
    
    /**
     * Sets the ResourceStatus to delegate connection information requests to.
     */
    void setResourceStatus(ResourceStatus resourceStatus) {
        if(this.resourceStatus != null) throw new IllegalStateException();
        this.resourceStatus = resourceStatus;
        resourceStatus.addResourceStatusListener(privateInterfaces);
    }
    
    /** 
     * Set the NetworkList as writable.
     */
    void setWritable(boolean writable) {
        this.writable = writable;
    }
    /** {@inheritDoc} */
    public boolean isWritable() {
        return writable;
    }
    
    /**
     * Gets the {@link Resource} that is the peer of this NetworkList.
     */
    Resource getResource() {
        return privateInterfaces;
    }
    
    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // notify resource listeners
        try {
            ListEvent listChangesCopy = new ListEvent(listChanges);
            Bufferlo listChangesBytes = ListEventToBytes.toBytes(listChangesCopy, byteCoder);
            for(int r = 0; r < resourceListeners.size(); r++) {
                ResourceListener listener = (ResourceListener)resourceListeners.get(r);
                listener.resourceUpdated(privateInterfaces, listChangesBytes.duplicate());
            }
        } catch(IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
        
        // forward the event
        updates.forwardEvent(listChanges);
    }
    
    /**
     * Returns true if this resource is actively being updated by the network.
     */
    public boolean isConnected() {
         return resourceStatus.isConnected();
    }
    
    /**
     * Forces this resource to attempt to connect. The results from the attempt
     * will not be visible immediately.
     */
    public void connect() {
        resourceStatus.connect();
    }
    
    /**
     * Forces this resource to attempt to disconnect. This will prevent the resource
     * from consuming network resources.
     */
    public void disconnect() {
        resourceStatus.disconnect();
    }
    
    /**
     * Implementations of all private interfaces.
     */
    private class PrivateInterfaces implements Resource, ResourceStatusListener {
    
        /**
         * Called each time a resource becomes connected.
         */
        public void resourceConnected(ResourceStatus resource) {
            for(Iterator i = statusListeners.iterator(); i.hasNext(); ) {
                NetworkListStatusListener listener = (NetworkListStatusListener)i.next();
                listener.connected(NetworkList.this);
            }
        }
        
        /**
         * Called each time a resource's disconnected status changes. This method may
         * be called for each attempt it makes to reconnect to the network.
         */
        public void resourceDisconnected(ResourceStatus resource, Exception cause) {
            for(Iterator i = statusListeners.iterator(); i.hasNext(); ) {
                NetworkListStatusListener listener = (NetworkListStatusListener)i.next();
                listener.disconnected(NetworkList.this, cause);
            }
        }

        /** {@inheritDoc} */
        public Bufferlo toSnapshot() {
            getReadWriteLock().writeLock().lock();
            try {
                return ListEventToBytes.toBytes(NetworkList.this, byteCoder);
            } catch(IOException e) {
                throw new IllegalStateException(e.getMessage());
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
        }
    
        /** {@inheritDoc} */
        public void fromSnapshot(Bufferlo snapshot) {
            applyCodedEvent(snapshot);
        }
        
        /** {@inheritDoc} */
        private void applyCodedEvent(Bufferlo data) {
            getReadWriteLock().writeLock().lock();
            try {
                ListEventToBytes.toListEvent(data, source, byteCoder);
            } catch(IOException e) {
                throw new IllegalStateException(e.getMessage());
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
        }
        
        /** {@inheritDoc} */
        public void update(Bufferlo delta) {
            applyCodedEvent(delta);
        }
        
        /** {@inheritDoc} */
        public void addResourceListener(ResourceListener listener) {
            resourceListeners.add(listener);
        }
        
        /** {@inheritDoc} */
        public void removeResourceListener(ResourceListener listener) {
            for(int r = 0; r < resourceListeners.size(); r++) {
                if(resourceListeners.get(r) == listener) {
                    resourceListeners.remove(r);
                    return;
                }
            }
        }
    }
        
    /**
     * Registers the specified listener to receive events about the status of this
     * resource.
     */
    public void addStatusListener(NetworkListStatusListener listener) {
        statusListeners.add(listener);
    }
    
    /**
     * Deregisters the specified listener from receiving events about the status of
     * this resource.
     */
    public void removeStatusListener(NetworkListStatusListener listener) {
        statusListeners.remove(listener);
    }
}
