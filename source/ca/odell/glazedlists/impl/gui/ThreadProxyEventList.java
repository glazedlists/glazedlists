/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.gui;

// Java collections are used for underlying data storage
import java.util.*;
// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;

/**
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class ThreadProxyEventList extends TransformedList {
// theoretical problem: the viewer of this EventList is out-of-date
// when it makes a change?
    
    /** a local cache of the source list */
    private List localCache = new ArrayList();
    
    /** propagates events on the Swing thread */
    private UpdateRunner updateRunner = new UpdateRunner();
    
    /** the pending events, possibly empty, one, or multiple */
    private ListEvent listChanges = null;
        
    /**
     */
    public ThreadProxyEventList(EventList source) {
        super(source);
        
        // populate the initial cache value
        localCache.addAll(source);
        
        // handle my own events to update the internal state
        this.addListEventListener(updateRunner);
        
        // handle changes in the source event list
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public final void listChanged(ListEvent listChanges) {
        // ensure we have a Swing proxy for the update event
        this.listChanges = listChanges;

        // forward the event on the appropriate thread
        schedule(updateRunner);
    }
    
    /**
     * Schedule the specified runnable to be run on the proxied thread.
     */
    protected abstract void schedule(Runnable runnable);    

    /** {@inheritDoc} */
    public final int size() {
        return localCache.size();
    }
    
    /** {@inheritDoc} */
    public final Object get(int index) {
        return localCache.get(index);
    }

    /** {@inheritDoc} */
    protected final boolean isWritable() {
        return true;
    }
    
    
    /**
     * Updates the internal data over the Swing thread.
     *
     * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
     */
    private class UpdateRunner implements Runnable, ListEventListener {
    
        /**
         * When run, this combines all events thus far and forwards them.
         *
         * <p>If a reordering event is being forwarded, the reordering may be lost
         * if it arrives simultaneously with another event. This is somewhat of a
         * hack for the time being. Hopefully later we can refine this so that a
         * new event is created with these changes properly.
         */
        public void run() {
            getReadWriteLock().writeLock().lock();
            try {
                updates.beginEvent(true);
                boolean forwardedEvents = false;
                while(listChanges.hasNext()) {
                    if(listChanges.isReordering() && !forwardedEvents) {
                        updates.reorder(listChanges.getReorderMap());
                    } else {
                        while(listChanges.next()) {
                            updates.addChange(listChanges.getType(), listChanges.getIndex());
                        }
                    }
                    forwardedEvents = true;
                }
                updates.commitEvent();
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
        }
    
        /**
         * Update local state as a consequence of the change event.
         */
        public void listChanged(ListEvent listChanges) {
            // handle reordering events
            if(listChanges.isReordering()) {
                List newLocalCache = new ArrayList();
                int[] sourceReorderMap = listChanges.getReorderMap();
                for(int i = 0; i < sourceReorderMap.length; i++) {
                    newLocalCache.add(i, localCache.get(sourceReorderMap[i]));
                }
                localCache.clear();
                localCache.addAll(newLocalCache);
    
            // handle everything else
            } else {
                while(listChanges.next()) {
                    int sourceIndex = listChanges.getIndex();
                    int changeType = listChanges.getType();
    
                    if(changeType == ListEvent.DELETE) {
                        localCache.remove(sourceIndex);
                    } else if(changeType == ListEvent.INSERT) {
                        localCache.add(sourceIndex, source.get(sourceIndex));
                    } else if(changeType == ListEvent.UPDATE) {
                        localCache.set(sourceIndex, source.get(sourceIndex));
                    }
                }
            }
        }
    }
}
