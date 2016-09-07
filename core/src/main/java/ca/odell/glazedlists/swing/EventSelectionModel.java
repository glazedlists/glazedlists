/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.matchers.Matcher;

import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

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
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=39">Bug 39</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=61">Bug 61</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=76">Bug 76</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=108">Bug 108</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=110">Bug 110</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=112">Bug 112</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=222">Bug 222</a>
 *
 * @deprecated Use {@link DefaultEventSelectionModel} instead. This class will be removed in the GL
 *             2.0 release. The wrapping of the source list with an EDT safe list has been
 *             determined to be undesirable (it is better for the user to provide their own EDT
 *             safe list).
 *
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
@Deprecated
public final class EventSelectionModel<E> implements AdvancedListSelectionModel<E> {

	/** The DefaultEventSelectionModel this delegates to. */
	private DefaultEventSelectionModel<E> delegateSelectionModel;

    /** the proxy moves events to the Swing Event Dispatch thread */
    protected TransformedList<E,E> swingThreadSource;

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
    public EventSelectionModel(EventList<E> source) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventSelectionModel
        source.getReadWriteLock().readLock().lock();
        try {
            swingThreadSource = GlazedListsSwing.swingThreadProxyList(source);
            delegateSelectionModel = new DefaultEventSelectionModel<E>(swingThreadSource);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
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
	@Override
    public void addListSelectionListener(ListSelectionListener listener) {
		delegateSelectionModel.addListSelectionListener(listener);
	}

    /**
     * Change the selection to be the set union of the current selection  and the indices between index0 and index1 inclusive
     */
	@Override
    public void addSelectionInterval(int index0, int index1) {
		delegateSelectionModel.addSelectionInterval(index0, index1);
	}

    /**
     * Add a matcher which decides when source elements are valid for selection.
     *
     * @param validSelectionMatcher returns <tt>true</tt> if a source element
     *      can be selected; <tt>false</tt> otherwise
     */
	@Override
    public void addValidSelectionMatcher(Matcher<E> validSelectionMatcher) {
		delegateSelectionModel.addValidSelectionMatcher(validSelectionMatcher);
	}

    /**
     * Change the selection to the empty set.
     */
	@Override
    public void clearSelection() {
		delegateSelectionModel.clearSelection();
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
	@Override
    public void dispose() {
		delegateSelectionModel.dispose();
		swingThreadSource.dispose();
	}

    /**
     * Return the first index argument from the most recent call to setSelectionInterval(), addSelectionInterval() or removeSelectionInterval().
     */
	@Override
    public int getAnchorSelectionIndex() {
		return delegateSelectionModel.getAnchorSelectionIndex();
	}

    /**
     * Gets an {@link EventList} that contains only deselected values and
     * modifies the source list on mutation.
     *
     * Adding and removing items from this list performs the same operation on
     * the source list.
     */
	@Override
    public EventList<E> getDeselected() {
		return delegateSelectionModel.getDeselected();
	}

    /**
     * Returns whether the EventSelectionModel is editable or not.
     */
	@Override
    public boolean getEnabled() {
		return delegateSelectionModel.getEnabled();
	}

    /**
     * Gets the event list that always contains the current selection.
     *
     * @deprecated As of 2005/02/18, the naming of this method became
     *             ambiguous.  Please use {@link #getSelected()}.
     */
	@Deprecated
    public EventList<E> getEventList() {
		return delegateSelectionModel.getSelected();
	}

    /**
     * Return the second index argument from the most recent call to setSelectionInterval(), addSelectionInterval() or removeSelectionInterval().
     */
	@Override
    public int getLeadSelectionIndex() {
		return delegateSelectionModel.getLeadSelectionIndex();
	}

    /**
     * Gets the selection model that provides selection management for a table.
     *
     * @deprecated As of 2004/11/26, the {@link EventSelectionModel} implements
     *      {@link ListSelectionModel} directly.
     */
	@Deprecated
    public ListSelectionModel getListSelectionModel() {
		return delegateSelectionModel;
	}

    /**
     * Gets the index of the last selected element.
     */
	@Override
    public int getMaxSelectionIndex() {
		return delegateSelectionModel.getMaxSelectionIndex();
	}

    /**
     * Gets the index of the first selected element.
     */
	@Override
    public int getMinSelectionIndex() {
		return delegateSelectionModel.getMinSelectionIndex();
	}

    /**
     * Gets an {@link EventList} that contains only selected
     * values and modifies the source list on mutation.
     *
     * Adding and removing items from this list performs the same operation on
     * the source list.
     */
	@Override
    public EventList<E> getSelected() {
		return delegateSelectionModel.getSelected();
	}

    /**
     * Returns the current selection mode.
     */
	@Override
    public int getSelectionMode() {
		return delegateSelectionModel.getSelectionMode();
	}

    /**
     * Gets an {@link EventList} that contains only deselected values and
     * modifies the selection state on mutation.
     *
     * Adding an item to this list deselects it and removing an item selects it.
     * If an item not in the source list is added an
     * {@link IllegalArgumentException} is thrown
     */
	@Override
    public EventList<E> getTogglingDeselected() {
		return delegateSelectionModel.getTogglingDeselected();
	}

    /**
     * Gets an {@link EventList} that contains only selected
     * values and modifies the selection state on mutation.
     *
     * Adding an item to this list selects it and removing an item deselects it.
     * If an item not in the source list is added an
     * {@link IllegalArgumentException} is thrown.
     */
	@Override
    public EventList<E> getTogglingSelected() {
		return delegateSelectionModel.getTogglingSelected();
	}

    /**
     * Returns true if the value is undergoing a series of changes.
     */
	@Override
    public boolean getValueIsAdjusting() {
		return delegateSelectionModel.getValueIsAdjusting();
	}

    /**
     * Insert length indices beginning before/after index.
     */
	@Override
    public void insertIndexInterval(int index, int length, boolean before) {
		delegateSelectionModel.insertIndexInterval(index, length, before);
	}

    /**
     * Inverts the current selection.
     */
	@Override
    public void invertSelection() {
		delegateSelectionModel.invertSelection();
	}

    /**
     * Returns true if the specified index is selected. If the specified
     * index has not been seen before, this will return false. This is
     * in the case where the table painting and selection have fallen
     * out of sync. Usually in this case there is an update event waiting
     * in the event queue that notifies this model of the change
     * in table size.
     */
	@Override
    public boolean isSelectedIndex(int index) {
		return delegateSelectionModel.isSelectedIndex(index);
	}

    /**
     * Returns true if no indices are selected.
     */
	@Override
    public boolean isSelectionEmpty() {
		return delegateSelectionModel.isSelectionEmpty();
	}

    /**
     * Remove the indices in the interval index0,index1 (inclusive) from  the selection model.
     */
	@Override
    public void removeIndexInterval(int index0, int index1) {
		delegateSelectionModel.removeIndexInterval(index0, index1);
	}

    /**
     * Remove a listener from the list that's notified each time a change to the selection occurs.
     */
	@Override
    public void removeListSelectionListener(ListSelectionListener listener) {
		delegateSelectionModel.removeListSelectionListener(listener);
	}

    /**
     * Change the selection to be the set difference of the current selection  and the indices between index0 and index1 inclusive.
     */
	@Override
    public void removeSelectionInterval(int index0, int index1) {
		delegateSelectionModel.removeSelectionInterval(index0, index1);
	}

    /**
     * Remove a matcher which decides when source elements are valid for selection.
     *
     * @param validSelectionMatcher returns <tt>true</tt> if a source element
     *      can be selected; <tt>false</tt> otherwise
     */
	@Override
    public void removeValidSelectionMatcher(Matcher<E> validSelectionMatcher) {
		delegateSelectionModel
				.removeValidSelectionMatcher(validSelectionMatcher);
	}

    /**
     * Set the anchor selection index.
     */
	@Override
    public void setAnchorSelectionIndex(int anchorSelectionIndex) {
		delegateSelectionModel.setAnchorSelectionIndex(anchorSelectionIndex);
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
	@Override
    public void setEnabled(boolean enabled) {
		delegateSelectionModel.setEnabled(enabled);
	}

    /**
     * Set the lead selection index.
     */
	@Override
    public void setLeadSelectionIndex(int leadSelectionIndex) {
		delegateSelectionModel.setLeadSelectionIndex(leadSelectionIndex);
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
	@Override
    public void setSelectionInterval(int index0, int index1) {
		delegateSelectionModel.setSelectionInterval(index0, index1);
	}

    /**
     * Set the selection mode.
     */
	@Override
    public void setSelectionMode(int selectionMode) {
		delegateSelectionModel.setSelectionMode(selectionMode);
	}

    /**
     * This property is true if upcoming changes to the value  of the model should be considered a single event.
     */
	@Override
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
		delegateSelectionModel.setValueIsAdjusting(valueIsAdjusting);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return delegateSelectionModel.toString();
	}
}