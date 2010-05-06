/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.matchers.Matcher;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * An {@link DefaultEventSelectionModel} is a class that performs two simulaneous
 * services. It is a {@link ListSelectionModel} to provide selection tracking for a
 * {@link JTable}. It is also a {@link EventList} that contains the table's selection.
 *
 * <p>As elements are selected or deselected, the {@link EventList} aspect of this
 * {@link DefaultEventSelectionModel} changes. Changes to that {@link List} will change the
 * source {@link EventList}. To modify only the selection, use the
 * {@link ListSelectionModel}'s methods.
 *
 * <p>Alongside <code>MULTIPLE_INTERVAL_SELECTION</code>, this {@link ListSelectionModel}
 * supports an additional selection mode.
 * <code>MULTIPLE_INTERVAL_SELECTION_DEFENSIVE</code> is a new selection mode.
 * It is identical to <code>MULTIPLE_INTERVAL_SELECTION</code> in every way but
 * one. When a row is inserted immediately before a selected row in the
 * <code>MULTIPLE_INTERVAL_SELECTION</code> mode, it becomes selected. But in
 * the <code>MULTIPLE_INTERVAL_SELECTION_DEFENSIVE</code> mode, it does not
 * become selected. To set this mode, use
 * {@link #setSelectionMode(int) setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE)}.
 *
 * <p>{@link DefaultEventSelectionModel} is <strong>not thread-safe</strong>. Unless otherwise
 * noted, all methods are only safe to be called from the event dispatch thread.
 * To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)} and
 * wrap the source list (or some part of the source list's pipeline) using
 * GlazedListsSwing#swingThreadProxyList(EventList).</p>
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=39">Bug 39</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=61">Bug 61</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=76">Bug 76</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=108">Bug 108</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=110">Bug 110</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=112">Bug 112</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=222">Bug 222</a> *
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class DefaultEventSelectionModel<E> implements AdvancedListSelectionModel<E> {

    /** the event lists that provide an event list view of the selection */
    private ListSelection<E> listSelection;

    /** the source event list. */
    private EventList<E> source;

    /** indicator to dispose source list */
    private boolean disposeSource;

    /** whether the user can modify the selection */
    private boolean enabled = true;

    /** listeners to notify when the selection changes */
    private final List<ListSelectionListener> listeners = new ArrayList<ListSelectionListener>();

    /** whether there are a series of changes on the way */
    private boolean valueIsAdjusting = false;
    private int fullChangeStart = -1;
    private int fullChangeFinish = -1;

    /**
     * Creates a new selection model that also presents a list of the selection.
     *
     * The {@link DefaultEventSelectionModel} listens to this {@link EventList} in order
     * to adjust selection when the {@link EventList} is modified. For example,
     * when an element is added to the {@link EventList}, this may offset the
     * selection of the following elements.
     *
     * @param source the {@link EventList} whose selection will be managed. This should
     *      be the same {@link EventList} passed to the constructor of your
     *      {@link DefaultEventTableModel} or {@link DefaultEventListModel}.
     */
    public DefaultEventSelectionModel(EventList<E> source) {
        this(source, false);
    }

    /**
     * Creates a new selection model that also presents a list of the selection. The
     * {@link DefaultEventSelectionModel} listens to this {@link EventList} in order to adjust
     * selection when the {@link EventList} is modified. For example, when an element is added to
     * the {@link EventList}, this may offset the selection of the following elements.
     *
     * @param source the {@link EventList} whose selection will be managed. This should be the
     *            same {@link EventList} passed to the constructor of your
     *            {@link DefaultEventTableModel} or {@link DefaultEventListModel}.
     * @param diposeSource <code>true</code> if the source list should be disposed when disposing
     *            this model, <code>false</code> otherwise
     */
    protected DefaultEventSelectionModel(EventList<E> source, boolean disposeSource) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventSelectionModel
        source.getReadWriteLock().readLock().lock();
        try {
            this.source = source;

            // build a list for reading the selection
            this.listSelection = new ListSelection<E>(source);
            this.listSelection.addSelectionListener(new SwingSelectionListener());
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
        this.disposeSource = disposeSource;
    }


    /**
     * {@inheritDoc}
     */
    public EventList<E> getSelected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return listSelection.getSelected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public EventList<E> getTogglingSelected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return listSelection.getTogglingSelected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public EventList<E> getDeselected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return listSelection.getDeselected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public EventList<E> getTogglingDeselected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return listSelection.getTogglingDeselected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getEnabled() {
        return enabled;
    }

    /**
     * Listens to selection changes on the {@link ListSelection} and fires
     * {@link ListSelectionEvent}s to registered listeners.
     */
    private class SwingSelectionListener implements ListSelection.Listener {
        /** {@inheritDoc} */
        public void selectionChanged(int changeStart, int changeEnd) {
            fireSelectionChanged(changeStart, changeEnd);
        }
    }

    /**
     * Notify listeners that the selection has changed.
     *
     * <p>This notifies all listeners with the same immutable
     * ListSelectionEvent.
     */
    private void fireSelectionChanged(int changeStart, int changeFinish) {
        // if this is a change in a series, save the bounds of this change
        if(valueIsAdjusting) {
            if(fullChangeStart == -1 || changeStart < fullChangeStart) fullChangeStart = changeStart;
            if(fullChangeFinish == -1 || changeFinish > fullChangeFinish) fullChangeFinish = changeFinish;
        }

        // fire the change
        final ListSelectionEvent event = new ListSelectionEvent(this, changeStart, changeFinish, valueIsAdjusting);
        for (int i = 0, n = listeners.size(); i < n; i++)
            listeners.get(i).valueChanged(event);
    }

    /**
     * {@inheritDoc}
     */
    public void invertSelection() {
        source.getReadWriteLock().writeLock().lock();
        try {
            listSelection.invertSelection();
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Change the selection to be between index0 and index1 inclusive.
     *
     * <p>First this calculates the smallest range where changes occur. This
     * includes the union of the selection range before and the selection
     * range specified. It then walks through the change and sets each
     * index as selected or not based on whether the index is in the
     * new range. Finally it fires events to both the listening lists and
     * selection listeners about what changes happened.
     *
     * <p>If the selection does not change, this will not fire any events.
     */
    public void setSelectionInterval(int index0, int index1) {
        if(!enabled) return;
        source.getReadWriteLock().writeLock().lock();
        try {
            listSelection.setSelection(index0, index1);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Change the selection to be the set union of the current selection  and the indices between index0 and index1 inclusive
     */
    public void addSelectionInterval(int index0, int index1) {
        if(!enabled) return;
        source.getReadWriteLock().writeLock().lock();
        try {
            listSelection.select(index0, index1);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }
    /**
     * Change the selection to be the set difference of the current selection  and the indices between index0 and index1 inclusive.
     */
    public void removeSelectionInterval(int index0, int index1) {
        if(!enabled) return;
        if(index0 == 0 && index1 == 0 && source.isEmpty()) return; // hack for Java 5 compatibility
        source.getReadWriteLock().writeLock().lock();
        try {
            listSelection.deselect(index0, index1);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Returns true if the specified index is selected. If the specified
     * index has not been seen before, this will return false. This is
     * in the case where the table painting and selection have fallen
     * out of sync. Usually in this case there is an update event waiting
     * in the event queue that notifies this model of the change
     * in table size.
     */
    public boolean isSelectedIndex(int index) {
        return (listSelection.isSelected(index));
    }

    /**
     * Return the first index argument from the most recent call to setSelectionInterval(), addSelectionInterval() or removeSelectionInterval().
     */
    public int getAnchorSelectionIndex() {
        return listSelection.getAnchorSelectionIndex();
    }
    /**
     * Set the anchor selection index.
     */
    public void setAnchorSelectionIndex(int anchorSelectionIndex) {
        if(!enabled) return;
        source.getReadWriteLock().writeLock().lock();
        try {
            listSelection.setAnchorSelectionIndex(anchorSelectionIndex);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }
    /**
     * Return the second index argument from the most recent call to setSelectionInterval(), addSelectionInterval() or removeSelectionInterval().
     */
    public int getLeadSelectionIndex() {
        return listSelection.getLeadSelectionIndex();
    }
    /**
     * Set the lead selection index.
     */
    public void setLeadSelectionIndex(int leadSelectionIndex) {
        if(!enabled) return;
        source.getReadWriteLock().writeLock().lock();
        try {
            listSelection.setLeadSelectionIndex(leadSelectionIndex);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Gets the index of the first selected element.
     */
    public int getMinSelectionIndex() {
        return listSelection.getMinSelectionIndex();
    }

    /**
     * Gets the index of the last selected element.
     */
    public int getMaxSelectionIndex() {
        return listSelection.getMaxSelectionIndex();
    }

    /**
     * Change the selection to the empty set.
     */
    public void clearSelection() {
        if(!enabled) return;
        source.getReadWriteLock().writeLock().lock();
        try {
            listSelection.deselectAll();
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }
    /**
     * Returns true if no indices are selected.
     */
    public boolean isSelectionEmpty() {
        source.getReadWriteLock().readLock().lock();
        try {
            return listSelection.getSelected().isEmpty();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Insert length indices beginning before/after index.
     */
    public void insertIndexInterval(int index, int length, boolean before) {
        // these changes are handled by the ListSelection
    }
    /**
     * Remove the indices in the interval index0,index1 (inclusive) from  the selection model.
     */
    public void removeIndexInterval(int index0, int index1) {
        // these changes are handled by the ListSelection
    }

    /**
     * This property is true if upcoming changes to the value  of the model should be considered a single event.
     */
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
        this.valueIsAdjusting = valueIsAdjusting;

        // fire one extra change containing all changes in this set
        if(!valueIsAdjusting) {
            if(fullChangeStart != -1 && fullChangeFinish != -1) {
                source.getReadWriteLock().writeLock().lock();
                try {
                    fireSelectionChanged(fullChangeStart, fullChangeFinish);
                    fullChangeStart = -1;
                    fullChangeFinish = -1;
                } finally {
                    source.getReadWriteLock().writeLock().unlock();
                }
            }
        }
    }

    /**
     * Returns true if the value is undergoing a series of changes.
     */
    public boolean getValueIsAdjusting() {
        return valueIsAdjusting;
    }

    /**
     * Set the selection mode.
     */
    public void setSelectionMode(int selectionMode) {
        source.getReadWriteLock().writeLock().lock();
        try {
            listSelection.setSelectionMode(selectionMode);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Returns the current selection mode.
     */
    public int getSelectionMode() {
        return listSelection.getSelectionMode();
    }

    /**
     * {@inheritDoc}
     */
    public void addValidSelectionMatcher(Matcher<E> validSelectionMatcher) {
        listSelection.addValidSelectionMatcher(validSelectionMatcher);
    }

    /**
     * {@inheritDoc}
     */
    public void removeValidSelectionMatcher(Matcher<E> validSelectionMatcher) {
        listSelection.removeValidSelectionMatcher(validSelectionMatcher);
    }

    /**
     * Add a listener to the list that's notified each time a change to
     * the selection occurs.
     *
     * Note that the change events fired by this class may include rows
     * that have been removed from the table. For this reason it is
     * advised not to <code>for()</code> through the changed range without
     * also verifying that each row is still in the table.
     */
    public void addListSelectionListener(ListSelectionListener listener) {
        listeners.add(listener);
    }
    /**
     * Remove a listener from the list that's notified each time a change to the selection occurs.
     */
    public void removeListSelectionListener(ListSelectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        listSelection.dispose();
        if (disposeSource) source.dispose();
    }
}