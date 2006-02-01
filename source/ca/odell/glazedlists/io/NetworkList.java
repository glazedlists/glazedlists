/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// access to the volatile implementation pacakge
import ca.odell.glazedlists.impl.rbp.*;
import ca.odell.glazedlists.impl.io.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// NIO is used for BRP
import java.util.*;
import java.io.*;

/**
 * An {@link EventList} that is either published to the network or subscribed from
 * the network. Since list elements must be transmitted over the network, each
 * {@link NetworkList} requires a {@link ByteCoder} to convert {@link Object}s to
 * and from bytes.
 *
 * <p>To instantiate a {@link NetworkList}, use the
 * {@link ListPeer#subscribe(String,int,String,ByteCoder) subscribe()}
 * and {@link ListPeer#publish(EventList,String,ByteCoder) publish()} methods
 * of a started {@link ListPeer}.
 *
 * <p>{@link NetworkList}s may be taken offline and brought back online with the
 * {@link #connect()} and {@link #disconnect()} methods. This allows an application
 * to use a {@link NetworkList} in spite of an unreliable network connection.
 *
 * <p>As a consequence of imperfect networks, {@link NetworkList}s may sometimes go
 * offline on their own. Some causes of this include the server program shutting
 * down or crashing, the local network connection becoming unavailable, or a
 * problem with the physical link, such as an unplugged cable.
 *
 * <p>{@link NetworkList}s use a subset of HTTP/1.1 for transport, including
 * chunked encoding. This protocol brings its own set of advantages:
 *    <li>HTTP is a standard well-understood protocol
 *    <li>Clients may be served even if they are behind NAT or Firewalls
 *    <li>The connection could be proxied by a standard HTTP proxy server, if necessary
 *    <li>In theory, the served port could be shared with another HTTP daemon such as Tomcat
 * 
 * <p>And HTTP brings some disadvantages also:
 *    <li>A persistent connection must be held, even if updates are infrequent
 *    <li>It cannot be differentiated from web traffic for analysis
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> The protocol used by
 * this version of {@link NetworkList} will be incompatible with future versions.
 * Eventually the protocol will be finalized but the current protocol is a work
 * in progress.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>Requires {@link ReadWriteLock} for every access, even for single-threaded use</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>O(N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
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
     * Create a {@link NetworkList} that brings the specified source online.
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
            ListEvent listChangesCopy = listChanges.copy();
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
     * Returns true if this resource is on the network. For published lists, this
     * requires that the list is being served. For subscribed lists, this requires
     * that a connection to the server has been established.
     */
    public boolean isConnected() {
         return resourceStatus.isConnected();
    }
    
    /**
     * Attempts to bring this {@link NetworkList} online. When the connection attempt
     * is successful (or when it fails), all {@link ResourceStatusListener}s will be
     * notified.
     */
    public void connect() {
        resourceStatus.connect();
    }
    
    /**
     * Attempts to take this {@link NetworkList} offline. When the {@link NetworkList}
     * is fully disconnected, all {@link ResourceStatusListener}s will be notified.
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
                updates.beginEvent(true);
                ListEventToBytes.toListEvent(data, source, byteCoder);
                updates.commitEvent();
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
        
        /** {@inheritDoc} */
        public ReadWriteLock getReadWriteLock() {
             return NetworkList.this.getReadWriteLock();
        }
        public String toString() {
            return NetworkList.this.toString();
        }
    }
        
    /**
     * Registers the specified listener to receive events about the status of this
     * {@link NetworkList}.
     */
    public void addStatusListener(NetworkListStatusListener listener) {
        statusListeners.add(listener);
    }
    
    /**
     * Deregisters the specified listener from receiving events about the status of
     * this {@link NetworkList}.
     */
    public void removeStatusListener(NetworkListStatusListener listener) {
        statusListeners.remove(listener);
    }
    
    /** {@inheritDoc} */
    public void dispose() {
        resourceStatus.removeResourceStatusListener(privateInterfaces);
        disconnect();
        super.dispose();
    }
}
