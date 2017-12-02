/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * A DefaultEventListModel adapts an EventList to the ListModel interface making it
 * appropriate for use with a {@link JList}. Each element of the list
 * corresponds to an element in the {@link ListModel}.
 *
 * <p>The DefaultEventListModel class is <strong>not thread-safe</strong>. Unless
 * otherwise noted, all methods are only safe to be called from the event
 * dispatch thread. To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)}
 * and wrap the source list (or some part of the source list's pipeline) using
 * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=14">Bug 14</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=146">Bug 146</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=177">Bug 177</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=228">Bug 228</a>
 * @see SwingUtilities#invokeAndWait(Runnable)
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author Holger Brands
 */
public class DefaultEventListModel<E> implements ListEventListener<E>, ListModel<E> {

    /** the source EventList */
    protected EventList<E> source;

    /** indicator to dispose source list */
    private boolean disposeSource;

    /** whom to notify of data changes */
    private final List<ListDataListener> listeners = new ArrayList<ListDataListener>();

    /** recycle the list data event to prevent unnecessary object creation */
    protected final MutableListDataEvent listDataEvent = new MutableListDataEvent(this);

    /**
     * Creates a new model that contains all objects located in the given
     * <code>source</code> and reacts to any changes in the given
     * <code>source</code>.
     *
     * @param source the EventList that provides the elements
     */
    public DefaultEventListModel(EventList<E> source) {
        this(source, false);
    }

    /**
     * Creates a new model that contains all objects located in the given
     * <code>source</code> and reacts to any changes in the given
     * <code>source</code>.
     *
     * @param source the EventList that provides the elements
     * @param disposeSource <code>true</code> if the source list should be disposed when disposing
     *            this model, <code>false</code> otherwise
     */
    public DefaultEventListModel(EventList<E> source, boolean disposeSource) {
        this.source = source;
        this.disposeSource = disposeSource;
        this.source.addListEventListener(this);
    }

    /**
     * For implementing the ListEventListener interface. This sends changes
     * to the table which can repaint the table cells. It's checked that all natural
     * calls to this method arrive on the Swing thread.
     *
     * <p>This always sends discrete changes for the complete size of the list.
     * It may be more efficient to implement a threshhold where a large list
     * of changes are grouped together as a single change. This is how the
     * ListTable accepts large change events.
     */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Events to " + this.getClass().getSimpleName() + " must arrive on the EDT - consider adding GlazedListsSwing.swingThreadProxyList(source) somewhere in your list pipeline");
        }

        // build an "optimized" ListDataEvent describing the precise range of rows in the first block
        listChanges.nextBlock();
        final int startIndex = listChanges.getBlockStartIndex();
        final int endIndex = listChanges.getBlockEndIndex();
        listDataEvent.setRange(startIndex, endIndex);

        final int changeType = listChanges.getType();
        switch (changeType) {
            case ListEvent.INSERT: listDataEvent.setType(ListDataEvent.INTERVAL_ADDED); break;
            case ListEvent.DELETE: listDataEvent.setType(ListDataEvent.INTERVAL_REMOVED); break;
            case ListEvent.UPDATE: listDataEvent.setType(ListDataEvent.CONTENTS_CHANGED); break;
        }

        // if another block exists, fallback to using a generic "data changed" ListDataEvent
        if (listChanges.nextBlock()) {
            listDataEvent.setRange(0, Integer.MAX_VALUE);
            listDataEvent.setType(ListDataEvent.CONTENTS_CHANGED);
        }

        fireListDataEvent(listDataEvent);
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    @Override
    public E getElementAt(int index) {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.get(index);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Gets the size of the list.
     */
    @Override
    public int getSize() {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.size();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Registers the specified ListDataListener to receive updates whenever
     * this list changes.
     *
     * <p>The specified ListDataListener must <strong>not</strong> save a
     * reference to the ListDataEvent beyond the end of the notification
     * method. This is because the ListDataEvent is re-used to increase
     * the performance of this implementation.
     */
    @Override
    public void addListDataListener(ListDataListener listDataListener) {
        listeners.add(listDataListener);
    }
    /**
     * Deregisters the specified ListDataListener from receiving updates
     * whenever this list changes.
     */
    @Override
    public void removeListDataListener(ListDataListener listDataListener) {
        listeners.remove(listDataListener);
    }

    /**
     * Notifies all ListDataListeners about one block of changes in the list.
     */
    protected void fireListDataEvent(ListDataEvent listDataEvent) {
        // notify all listeners about the event
        for(int i = 0, n = listeners.size(); i < n; i++) {
            ListDataListener listDataListener = listeners.get(i);
            switch (listDataEvent.getType()) {
                case ListDataEvent.CONTENTS_CHANGED: listDataListener.contentsChanged(listDataEvent); break;
                case ListDataEvent.INTERVAL_ADDED: listDataListener.intervalAdded(listDataEvent); break;
                case ListDataEvent.INTERVAL_REMOVED: listDataListener.intervalRemoved(listDataEvent); break;
            }
        }
    }

    /**
     * Releases the resources consumed by this {@link DefaultEventListModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link DefaultEventListModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link DefaultEventListModel}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link DefaultEventListModel} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an {@link DefaultEventListModel} after it has been disposed.
     * As such, this {@link DefaultEventListModel} should be detached from its
     * corresponding Component <strong>before</strong> it is disposed.
     */
    public void dispose() {
        source.removeListEventListener(this);
        if (disposeSource) {
            source.dispose();
        }
        // this encourages exceptions to be thrown if this model is incorrectly accessed again
        source = null;
    }
}