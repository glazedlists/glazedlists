/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;

/**
 * An {@link EventList} that only forwards its events on a proxy thread,
 * regardless of the thread of their origin.
 *
 * <p>While the proxy thread is not executing, any number of events may arrive.
 * This means that multiple changes may have occurred before the proxy can
 * notify its listeners. One problem created by this scenario is that some of
 * these changes may contradict one another. For example, an inserted item may
 * be later removed before either event is processed. To overcome this problem,
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
 * <p>At some point in the future, after one or more events have been enqueued,
 * the proxy thread executes and processes its queue:
 * <ol>
 *   <li>First it acquires a lock to prevent further concurrent changes
 *   <li>All enqueued changes are combined into a single change. Currently this
 *       implementation does a best effort on conflict resolution. It is
 *       limited in its handling of reordering events, as caused by changing a
 *       {@link SortedList} {@link Comparator}.
 *   <li>The single, combined event is fired.
 *   <li>The first listener is the {@link ThreadProxyEventList} itself. It listens
 *       to its own event because this event will be free of conflicts. It applies
 *       the changes to its own private copy of the data.
 *     <li>All other listeners are notified of the change.
 *     <li>The lock is released.
 * </ol>
 *
 * <p>The {@link ThreadProxyEventList} keeps a private copy of the elements of the
 * source {@link EventList}. This enables interested classes to read a consistent
 * (albeit potentially out of date) view of the data at all times.
 *
 * <p><strong><font color="#FF0000">Important:</font></strong> ThreadProxyEventList
 * relies heavily on its ability to pause changes to its source EventList
 * while it is updating its private copy of the source data. It does this by
 * acquiring the writeLock for the list pipeline. This implies that
 * <stong>ALL</stong> code which accesses the pipeline must be thread-safe
 * (or the acquisition of the writeLock will be meaningless). See
 * {@link EventList} for an example of thread safe code.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public abstract class ThreadProxyEventList<E> extends TransformedList<E, E> implements RandomAccess {

    /** a local cache of the source list */
    private List<E> localCache = new ArrayList<>();

    /** propagates events on the proxy thread */
    private UpdateRunner updateRunner = new UpdateRunner();

    /** propagates events immediately. The source events might be fired later */
    private final ListEventAssembler<E> cacheUpdates = new ListEventAssembler<>(this, ListEventAssembler.createListEventPublisher());

    /** whether the proxy thread has been scheduled */
    private volatile boolean scheduled = false;

    /**
     * Create a {@link ThreadProxyEventList} which delivers changes to the
     * given <code>source</code> on a particular {@link Thread}, called the
     * proxy {@link Thread} of a subclasses choosing. The {@link Thread} used
     * depends on the implementation of {@link #schedule(Runnable)}.
     *
     * @param source the {@link EventList} for which to proxy events
     */
    public ThreadProxyEventList(EventList<E> source) {
        super(source);

        // populate the initial cache value
        localCache.addAll(source);

        // handle my own events to update the internal state
        cacheUpdates.addListEventListener(updateRunner);

        // handle changes in the source event list
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public final void listChanged(ListEvent<E> listChanges) {
        // if we've haven't scheduled a commit, we need to begin a new event
        if(!scheduled) {
            updates.beginEvent(true);
            cacheUpdates.beginEvent(true);
        }

        // add the changes for this event to our queue
        updates.forwardEvent(listChanges);
        cacheUpdates.forwardEvent(listChanges);

        // commit the event on the appropriate thread
        if(!scheduled) {
            scheduled = true;
            schedule(updateRunner);
        }
    }

    /**
     * Schedule the specified runnable to be executed on the proxy thread.
     *
     * @param runnable a unit of work to be executed on the proxy thread
     */
    protected abstract void schedule(Runnable runnable);

    /** {@inheritDoc} */
    @Override
    public final int size() {
        return localCache.size();
    }

    /** {@inheritDoc} */
    @Override
    public final E get(int index) {
        return localCache.get(index);
    }

    /** {@inheritDoc} */
    @Override
    protected final boolean isWritable() {
        return true;
    }

    /**
     * Apply the {@link ListEvent} to the {@link List}.
     *
     * @param source the EventList whose changes are being proxied to another thread
     * @param listChanges the list of changes from the <code>source</code> to be applied
     * @param localCache a private snapshot of the <code>source</code> which
     *      is now out of sync with that source list and will be repaired
     * @return a new List to serve as the up-to-date local cache
     */
    private List<E> applyChangeToCache(EventList<E> source, ListEvent<E> listChanges, List<E> localCache) {
        List<E> result = new ArrayList<>(source.size());

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

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        super.dispose();
        cacheUpdates.removeListEventListener(updateRunner);
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
        @Override
        public void run() {
            getReadWriteLock().writeLock().lock();
            try {
                // We need to apply the changes to the local cache immediately,
                // before forwarding the event downstream to other listeners.
                // This is necessary so that intermediate states in this list
                // are visible to larger list changes (such as clearing tables,
                // see bug 447)
                cacheUpdates.commitEvent();
                updates.commitEvent();
            } finally {
                scheduled = false;
                getReadWriteLock().writeLock().unlock();
            }
        }

        /**
         * Update local state as a consequence of the change event.
         */
        @Override
        public void listChanged(ListEvent<E> listChanges) {
            localCache = applyChangeToCache(source, listChanges, localCache);
        }
    }
}