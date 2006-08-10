/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.gui;

// Java collections are used for underlying data storage
import java.util.*;
// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;

/**
 * An {@link EventList} that only forwards its events on a proxy thread,
 * regardless of the thread of their origin.
 *
 * <p>While the proxy thread is not executing, any number of events may arrive.
 * This means that multiple changes may have occurred before the proxy can
 * notify its listeners. One problem with this limitation is that some of these
 * changes may contradict one another. For example, an inserted item may be
 * later removed before either event is processed. To overcome this limitation,
 * this class uses the change-contradiction resolving logic of
 * {@link ListEventAssembler}.
 *
 * <p>The flow of events is as follows:
 * <ol>
 *   <li>Any thread makes a change to the source list and calls
 *       {@link ThreadProxyEventList#listChanged(ListEvent)}.
 *   <li>The event is enqueued and the proxy thread is notified that it has a
 *       task to process in the future.
 * </ol>
 *
 * <p>Then some time later in the future after one or more events have been
 * enqueued, the proxy thread executes and processes its queue:
 * <ol>
 *   <li>First it acquires a lock so that no more concurrent changes can occur
 *   <li>All enqueued changes are combined into one change. Currently this
 *       implementation does a best effort on conflict resolution. It is
 *       limited in its handling of reordering events, as caused by changing a
 *       {@link SortedList} {@link Comparator}.
 *   <li>The completed set of events is fired.
 *   <li>The first listener is the {@link ThreadProxyEventList} itself. It listens
 *       to its own event because this event will be free of conflicts. It applies
 *       the changes to its own internal copy of the data.
 *     <li>All other listeners are notified of the change.
 *     <li>The lock is released.
 * </ol>
 *
 * <p>The {@link ThreadProxyEventList} keeps a private copy of the elements of the
 * source {@link EventList}. This enables interested classes to read a consistent
 * (albeit potentially out of date) view of the data at all times.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public abstract class ThreadProxyEventList<E> extends TransformedList<E, E> {
    
    /** a local cache of the source list */
    private List<E> localCache = new ArrayList<E>();
    
    /** propagates events on the proxy thread */
    private UpdateRunner updateRunner = new UpdateRunner();
    
    /** whether the proxy thread has been scheduled */
    private boolean scheduled = false;
    
    /**
     * Create a {@link ThreadProxyEventList} which delivers changes to the
     * given <code>source</code> on a particular {@link Thread}, called the
     * proxy {@link Thread} of a subclasses choosing. The {@link Thread} used
     * depends on the implementation of {@link #schedule(Runnable)}.
     */
    public ThreadProxyEventList(EventList<E> source) {
        super(source);
        
        // populate the initial cache value
        localCache.addAll(source);
        
        // handle my own events to update the internal state
        this.addListEventListener(updateRunner);
        
        // handle changes in the source event list
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public final void listChanged(ListEvent<E> listChanges) {
        // if we've haven't scheduled a commit, we need to begin a new event
        if(!scheduled) {
            updates.beginEvent(true);
        }
        
        // add the changes for this event to our queue
        updates.forwardEvent(listChanges);
        
        // commit the event on the appropriate thread
        if(!scheduled) {
            scheduled = true;
            schedule(updateRunner);
        }
    }
    
    /**
     * Schedule the specified runnable to be executed on the proxy thread.
     */
    protected abstract void schedule(Runnable runnable);    

    /** {@inheritDoc} */
    public final int size() {
        return localCache.size();
    }
    
    /** {@inheritDoc} */
    public final E get(int index) {
        return localCache.get(index);
    }

    /** {@inheritDoc} */
    protected final boolean isWritable() {
        return true;
    }

    /**
     * Apply the {@link ListEvent} to the {@link List}.
     */
    protected List applyChangeToCache(List<E> source, ListEvent<E> listChanges, List<E> localCache) {
        List<E> result = new ArrayList<E>(source.size());

        // cacheOffset is the running index delta between localCache and result
        int resultIndex = 0;
        int cacheOffset = 0;

        while(true) {

            // find the next change (or the end of the list)
            int changeIndex;
            int changeType;
            if(listChanges.next()) {
                changeIndex = listChanges.getIndex();
                changeType = listChanges.getType();
            } else {
                changeIndex = source.size();
                changeType = -1;
            }

            // perform all the updates before this change
            for(; resultIndex < changeIndex; resultIndex++) {
                result.add(resultIndex, localCache.get(resultIndex + cacheOffset));
            }

            // perform this change
            if(changeType == ListEvent.DELETE) {
                cacheOffset++;
            } else if(changeType == ListEvent.UPDATE) {
                result.add(resultIndex, source.get(changeIndex));
                resultIndex++;
            } else if(changeType == ListEvent.INSERT) {
                result.add(resultIndex, source.get(changeIndex));
                resultIndex++;
                cacheOffset--;
            } else if(changeType == -1) {
                break;
            }
        }

        return result;
    }
    
    /**
     * Updates the internal data using the proxy thread.
     *
     * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
     */
    private class UpdateRunner implements Runnable, ListEventListener<E> {
    
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
                updates.commitEvent();
                scheduled = false;
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
        }

        /**
         * Update local state as a consequence of the change event.
         */
        public void listChanged(ListEvent<E> listChanges) {
            localCache = applyChangeToCache(source, listChanges, localCache);
        }
    }
}