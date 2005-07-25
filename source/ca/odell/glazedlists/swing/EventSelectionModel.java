/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
// for listening to list selection events
import javax.swing.*;
import javax.swing.event.*;
// for lists of listeners
import java.util.*;

/**
 * An {@link EventSelectionModel} is a class that performs two simulaneous
 * services. It is a {@link ListSelectionModel} to provide selection tracking for a
 * {@link JTable}. It is also a {@link EventList} that contains the table's selection.
 *
 * <p>As elements are selected or deselected, the {@link EventList} aspect of this
 * {@link EventSelectionModel} changes. Changes to that {@link List} will change the
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
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class EventSelectionModel implements ListSelectionModel {

    /** the event lists that provide an event list view of the selection */
    private ListSelection listSelection;

    /** the proxy moves events to the Swing Event Dispatch thread */
    private TransformedList swingSource;

    /** whether the user can modify the selection */
    private boolean enabled = true;

    /** listeners to notify when the selection changes */
    private List listeners = new ArrayList();

    /** whether there are a series of changes on the way */
    private boolean valueIsAdjusting = false;
    private int fullChangeStart = -1;
    private int fullChangeFinish = -1;

    /**
     * Creates a new selection model that also presents a list of the selection.
     *
     * The {@link EventSelectionModel} listens to this {@link EventList} in order
     * to adjust selection when the {@link EventList} is modified. For example,
     * when an element is added to the {@link EventList}, this may offset the
     * selection of the following elements.
     *
     * @param source the {@link EventList} whose selection will be managed. This should
     *      be the same {@link EventList} passed to the constructor of your
     *      {@link EventTableModel} or {@link EventListModel}.
     */
    public EventSelectionModel(EventList source) {
        swingSource = GlazedListsSwing.swingThreadProxyList(source);

        // build a list for reading the selection
        this.listSelection = new ListSelection(swingSource);
        listSelection.addSelectionListener(new SwingSelectionListener());
    }

    /**
     * Gets the event list that always contains the current selection.
     *
     * @deprecated As of 2005/02/18, the naming of this method became
     *             ambiguous.  Please use {@link #getSelected()}.
     */
    public EventList getEventList() {
        return getSelected();
    }

    /**
     * Gets an {@link EventList} that always contains the current selection.
     */
    public EventList getSelected() {
        return listSelection.getSelected();
    }

    /**
     * Gets an {@link EventList} that always contains the items from the source
     * that are currently deselected.
     */
    public EventList getDeselected() {
        return listSelection.getDeselected();
    }

    /**
     * Gets the selection model that provides selection management for a table.
     *
     * @deprecated As of 2004/11/26, the {@link EventSelectionModel} implements
     *      {@link ListSelectionModel} directly.
     */
    public ListSelectionModel getListSelectionModel() {
        return this;
    }

    /**
     * Set the EventSelectionModel as editable or not. This means that the user cannot
     * manipulate the selection by clicking. The selection can still be changed as
     * the source list changes.
     *
     * <p>Note that this will also disable the selection from being modified
     * <strong>programatically</strong>. Therefore you must use setEnabled(true) to
     * modify the selection in code.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets whether the EventSelectionModel is editable or not.
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
        ListSelectionEvent event = new ListSelectionEvent(this, changeStart, changeFinish, valueIsAdjusting);
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            ListSelectionListener listener = (ListSelectionListener)i.next();
            listener.valueChanged(event);
        }
    }

    /**
     * Inverts the current selection.
     */
    public void invertSelection() {
        listSelection.invertSelection();
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
        listSelection.setSelection(index0, index1);
    }

    /**
     * Change the selection to be the set union of the current selection  and the indices between index0 and index1 inclusive
     */
    public void addSelectionInterval(int index0, int index1) {
        if(!enabled) return;
        listSelection.select(index0, index1);
    }
    /**
     * Change the selection to be the set difference of the current selection  and the indices between index0 and index1 inclusive.
     */
    public void removeSelectionInterval(int index0, int index1) {
        if(!enabled) return;
        if(index0 == 0 && index1 == 0 && swingSource.isEmpty()) return; // hack for Java 5 compatibility
        listSelection.deselect(index0, index1);
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
        listSelection.setAnchorSelectionIndex(anchorSelectionIndex);
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
        listSelection.setLeadSelectionIndex(leadSelectionIndex);
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
        listSelection.deselectAll();
    }
    /**
     * Returns true if no indices are selected.
     */
    public boolean isSelectionEmpty() {
        return (listSelection.getSelected().size() == 0);
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
                fireSelectionChanged(fullChangeStart, fullChangeFinish);
                fullChangeStart = -1;
                fullChangeFinish = -1;
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
        listSelection.setSelectionMode(selectionMode);
    }

    /**
     * Returns the current selection mode.
     */
    public int getSelectionMode() {
        return listSelection.getSelectionMode();
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
     * Releases the resources consumed by this {@link EventSelectionModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventSelectionModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventSelectionModel}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link EventSelectionModel} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link EventSelectionModel} after it has been disposed.
     */
    public void dispose() {
        listSelection.dispose();
    }
}