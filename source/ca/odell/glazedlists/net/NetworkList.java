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
import ca.odell.glazedlists.util.impl.*;
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
public class NetworkList extends TransformedList implements Resource {

    /** listeners to resource changes */
    private List resourceListeners = new ArrayList();
    
    /** how bytes are encoded and decoded */
    private ByteCoder byteCoder;
    
    /**
     * Create a NetworkList that connects to the specified source.
     */
    public NetworkList(EventList source, ByteCoder byteCoder) {
        super(source);
        this.byteCoder = byteCoder;
    }
    
    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }
    
    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // notify resource listeners
        try {
            ListEvent listChangesCopy = new ListEvent(listChanges);
            Bufferlo listChangesBytes = ListEventCoder.listEventToBytes(listChangesCopy, byteCoder);
            for(int r = 0; r < resourceListeners.size(); r++) {
                ResourceListener listener = (ResourceListener)resourceListeners.get(r);
                listener.resourceUpdated(this, listChangesBytes.duplicate());
            }
        } catch(IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
        
        // forward the event
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    public Bufferlo toSnapshot() {
        getReadWriteLock().writeLock().lock();
        try {
            return ListEventCoder.listToBytes(this, byteCoder);
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
            ListEventCoder.bytesToListEvent(data, this, byteCoder);
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
