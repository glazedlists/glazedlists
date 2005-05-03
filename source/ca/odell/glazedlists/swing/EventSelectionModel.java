/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.util.concurrent.*;
// volatile implementation support
import ca.odell.glazedlists.impl.adt.*;
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
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p>Alongside <code>MULTIPLE_INTERVAL_SELECTION</code>, this {@link ListSelectionModel}
 * supports an additional selection mode.
 * <code>MULTIPLE_INTERVAL_SELECTION_DEFENSIVE</code> is a new selection mode.
 * It is identical to <code>MULTIPLE_INTERVAL_SELECTION</code> in every way but
 * one. When a row is inserted immediately before a selected row in the
 * <code>MULTIPLE_INTERVAL_SELECTION</code> mode, it becomes selected. But in
 * the <code>MULTIPLE_INTERVAL_SELECTION_DEFENSIVE</code> mode, it does not
 * become selected. To set this mode, use
 * {@link #setSelectionMode(int) setSelectionMode(EventSelectionModel.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE)}.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class EventSelectionModel implements ListSelectionModel {
    // Internally this maintains a flag list to keep track of which elements
    // are selected and not and where they are in the source list. It uses
    // a barcode that corresponds directly to the source list. The barcode
    // contains white elements for all unselected elements and black elements
    // for elements which are selected. This is used to map indexes between the
    // selection subset and the full source list.
    //
    // The SelectionModelEventList responds to two classes of changes from a
    // JTable. The JTable's structure can change and the JTable's selection can
    // change. In either case the SelectionModelEventList is responsible for
    // updating its view of the selection and sending SelectionEvents and
    // ListEvents to listeners.
    //
    // This model provides a service for the JTable. It listens to changes in
    // the JTable's selection and keeps track of what is selected. It is also
    // responsible for notifying ListSelectionListeners of any changes.
    //
    // <p>All read and write access to the EventSelectionModel are protected
    // with the list's read/write lock. This guarantees that user classes such
    // as JTable or JList can access the EventListSelectionModel even if the
    // source list is being modified by a separate thread.
    //
    /** the new selection mode behaves similar to MULTIPLE_INTERVAL_SELECTION */
    public static final int MULTIPLE_INTERVAL_SELECTION_DEFENSIVE = 103;

    /** the event lists that provide an event list view of the selection */
    private SelectionEventList selectionList;

    /** the flag list contains Barcode.BLACK for selected items and Barcode.WHITE for others */
    private Barcode flagList = new Barcode();

    /** to allow for selection inversion without changing the barcode */
    private Object selected = Barcode.BLACK;
    private Object deselected = Barcode.WHITE;

    /** the proxy moves events to the Swing Event Dispatch thread */
    private TransformedList swingSource;

    /** list change updates */
    private ListEventAssembler updates = null;

    /** whether the user can modify the selection */
    private boolean enabled = true;

    /** listeners to notify when the selection changes */
    private List listeners = new ArrayList();

    /** whether there are a series of changes on the way */
    private boolean valueIsAdjusting = false;
    private int fullChangeStart = -1;
    private int fullChangeFinish = -1;

    /** the lead and anchor selection index are the first and last in a range */
    private int anchorSelectionIndex = -1;
    private int leadSelectionIndex = -1;

    /** the selection mode defines characteristics of the selection */
    private int selectionMode = MULTIPLE_INTERVAL_SELECTION_DEFENSIVE;

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

        // build the initial state
        flagList.add(0, deselected, swingSource.size());

        // build a list for reading the selection
        this.selectionList = new SelectionEventList(swingSource);
    }

    /**
     * Gets the event list that always contains the current selection.
     *
     * @deprecated As of 2005/02/18, the naming of this method became
     *             ambiguous.  Please use {@link #getSelected()}.
     */
    public EventList getEventList() {
        return selectionList;
    }

    /**
     * Gets an {@link EventList} that always contains the current selection.
     */
    public EventList getSelected() {
        return selectionList;
    }

    /**
     * Gets an {@link EventList} that always contains the items from the source
     * that are NOT currently selected.
     */
    public EventList getDeselected() {
        throw new UnsupportedOperationException("This feature is not yet implemented.");
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
     * Inverts the current selection.
     */
    public void invertSelection() {
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            // Switch what will be considered selected by this model
            if(selected == Barcode.BLACK) {
                selected = Barcode.WHITE;
                deselected = Barcode.BLACK;
            } else {
                selected = Barcode.BLACK;
                deselected = Barcode.WHITE;
            }

            // Forward the change on the selection driven EventList
            updates.beginEvent();
            updates.addDelete(0, flagList.colourSize(deselected));
            updates.addInsert(0, flagList.colourSize(selected));
            updates.commitEvent();

            // Clear the anchor and lead
            anchorSelectionIndex = -1;
            leadSelectionIndex = -1;

            // Forward a change to registered selection listeners
            fireSelectionChanged(0, flagList.size());
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
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
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            this.enabled = enabled;
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
    }

    /**
     * Gets whether the EventSelectionModel is editable or not.
     */
    public boolean getEnabled() {
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            return enabled;
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
    }

    /**
     * This is the EventList component of the EventSelectionModel. It is
     * responsible for listening to changes in the JTable's size and modifying
     * the internal list model to match.
     */
    private class SelectionEventList extends TransformedList {

        /**
         * Creates a new SelectionEventList that listens to changes from the
         * source event list.
         */
        public SelectionEventList(TransformedList swingSource) {
            super(swingSource);

            // use an Internal Lock to avoid locking the source list during a selection change
            readWriteLock = new InternalReadWriteLock(swingSource.getReadWriteLock(), new J2SE12ReadWriteLock());

            // prepare listeners
            swingSource.addListEventListener(this);
            EventSelectionModel.this.updates = super.updates;
        }

        /**
         * Returns the element at the specified position in this list.
         *
         * <p>This gets the object with the specified index from the source list.
         *
         * <p>Before every get, we need to validate the row because there may be an
         * update waiting in the event queue. For example, it is possible that
         * the selection has changed. Such a change may have been sent as notification,
         * but after this request in the event queue. In the case where a row is no longer
         * available, null is returned. The value returned is insignificant in this case
         * because the Event queue will very shortly be repainting (or removing) the row
         * anyway. This strategy is also used in ListTable where changes are frequently
         * queued to be processed.
         */
        public Object get(int index) {
            int sourceIndex = flagList.getIndex(index, selected);

            // ensure that this value still exists before retrieval
            if(sourceIndex < swingSource.size()) {
                return swingSource.get(sourceIndex);
            } else {
                //new Exception("Returning null for removed selection " + row).printStackTrace();
                return null;
            }
        }

        /**
         * Returns the number of elements in this list.
         *
         * @return the number of elements currently selected.
         */
        public int size() {
            return flagList.colourSize(selected);
        }

        /**
         * Gets the index into the source list for the object with the specified
         * index in this list.
         */
        protected int getSourceIndex(int mutationIndex) {
            return flagList.getIndex(mutationIndex, selected);
        }

        /**
         * Tests if this mutation shall accept calls to <code>add()</code>,
         * <code>remove()</code>, <code>set()</code> etc.
         */
        protected boolean isWritable() {
            return true;
        }

        /**
         * Notifies this SelectionList about changes to its underlying list store.
         *
         * <p>This changes the flag list. Changes to the source list may cause simultaneous
         * changes to the corresponding selection list.
         */
        public void listChanged(ListEvent listChanges) {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
            try {
                // prepare for notifying ListSelectionListeners
                int minSelectionIndexBefore = getMinSelectionIndex();
                int maxSelectionIndexBefore = getMaxSelectionIndex();

                // prepare a sequence of changes
                updates.beginEvent();

                // handle reordering events
                if(listChanges.isReordering()) {
                    int[] sourceReorderMap = listChanges.getReorderMap();
                    int[] selectReorderMap = new int[flagList.colourSize(selected)];

                    // adjust the flaglist & construct a reorder map to propagate
                    Barcode previousFlagList = flagList;
                    flagList = new Barcode();
                    for(int c = 0; c < sourceReorderMap.length; c++) {
                        Object flag = previousFlagList.get(sourceReorderMap[c]);
                        boolean wasSelected = (flag != deselected);
                        flagList.add(c, flag, 1);
                        if(wasSelected) {
                            int previousIndex = previousFlagList.getColourIndex(sourceReorderMap[c], selected);
                            int currentIndex = flagList.getColourIndex(c, selected);
                            selectReorderMap[currentIndex] = previousIndex;
                        }
                    }

                    // adjust other internal state
                    anchorSelectionIndex = -1;
                    leadSelectionIndex = -1;

                    // fire the reorder
                    updates.reorder(selectReorderMap);

                // handle non-reordering events
                } else {

                    // for all changes simply update the flag list
                    while(listChanges.next()) {
                        int index = listChanges.getIndex();
                        int changeType = listChanges.getType();

                        // learn about what it was
                        int previousSelectionIndex = flagList.getColourIndex(index, selected);
                        boolean previouslySelected = previousSelectionIndex != -1;

                        // when an element is deleted, blow it away
                        if(changeType == ListEvent.DELETE) {
                            flagList.remove(index, 1);

                            // fire a change to the selection list if a selected object is changed
                            if(previouslySelected) {
                                updates.addDelete(previousSelectionIndex);
                            }

                        // when an element is inserted, it is selected if its index was selected
                        } else if(changeType == ListEvent.INSERT) {

                            // when selected, decide based on selection mode
                            if(previouslySelected) {

                                // select the inserted for single interval and multiple interval selection
                                if(selectionMode == SINGLE_INTERVAL_SELECTION
                                || selectionMode == MULTIPLE_INTERVAL_SELECTION) {
                                    flagList.add(index, selected, 1);
                                    updates.addInsert(previousSelectionIndex);

                                // do not select the inserted for single selection and defensive selection
                                } else {
                                    flagList.add(index, deselected, 1);
                                }

                            // when not selected, just add the space
                            } else {
                                flagList.add(index, deselected, 1);
                            }

                        // when an element is changed, assume selection stays the same
                        } else if(changeType == ListEvent.UPDATE) {

                            // fire a change to the selection list if a selected object is changed
                            if(previouslySelected) {
                                updates.addUpdate(previousSelectionIndex);
                            }
                        }

                        // adjust other internal state
                        anchorSelectionIndex = adjustIndex(anchorSelectionIndex, changeType, index);
                        leadSelectionIndex = adjustIndex(leadSelectionIndex, changeType, index);
                    }
                }

                // fire the changes to ListEventListeners
                updates.commitEvent();

                // fire the changes to ListSelectionListeners
                if(minSelectionIndexBefore != -1 && maxSelectionIndexBefore != -1) {
                    int minSelectionIndexAfter = getMinSelectionIndex();
                    int maxSelectionIndexAfter = getMaxSelectionIndex();
                    int changeStart = minSelectionIndexBefore;
                    int changeFinish = maxSelectionIndexBefore;
                    if(minSelectionIndexAfter != -1 && minSelectionIndexAfter < changeStart) changeStart = minSelectionIndexAfter;
                    if(maxSelectionIndexAfter != -1 && maxSelectionIndexAfter > changeFinish) changeFinish = maxSelectionIndexAfter;
                    valueIsAdjusting = false;
                    fireSelectionChanged(changeStart, changeFinish);
                }
            } finally {
                ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
            }
        }

        /** {@inheritDoc} */
        public void dispose() {
            swingSource.removeListEventListener(this);
        }
    }

    /**
     * Adjusts the specified index to the specified change. This is used to adjust
     * the anchor and lead selection indices when list changes occur.
     */
    private int adjustIndex(int indexBefore, int changeType, int changeIndex) {
        if(indexBefore == -1) return -1;
        if(changeType == ListEvent.DELETE) {
            if(changeIndex < indexBefore) return indexBefore-1;
            else if(changeIndex == indexBefore) return -1;
            else return indexBefore;
        } else if(changeType == ListEvent.UPDATE) {
            return indexBefore;
        } else if(changeType == ListEvent.INSERT) {
            if(changeIndex <= indexBefore) return indexBefore+1;
            else return indexBefore;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Gets this as a string for debugging purposes only.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < flagList.size(); i++) {
            if(i != 0) result.append(" ");
            if(flagList.get(i) == deselected) result.append("-");
            else result.append("+");
        }
        return result.toString();
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
     * Walks through the union of the specified ranges. All values in the
     * first range are given the specified selection value. All values in
     * the second range but not in the first range are given the opposite
     * selection value. In effect, the first range and the intersection of
     * the two ranges are given the specified value. The parts that are
     * in the second range but not in the first range (ie. everything else)
     * is given the opposite selection value.
     *
     * @param select true to set values in the first range as selected and
     *      the other values in the second range as not selected. false to
     *      set values in the first range as not selected and the other
     *      values in the second range as selected.
     * @param changeIndex0 one end of the first range, inclusive.
     * @param changeIndex1 the opposite end of the first range, inclusive.
     *      This may be lower than changeIndex0.
     * @param invertIndex0 one end of the second range, inclusive. To
     *      specify an empty second range, specify -1 for this value.
     * @param invertIndex1 the opposite end of the second range, inclusive.
     *      This may be lower than invertIndex0. To specify an empty second
     *      range, specify -1 for this value.
     */
    private void setSubRangeOfRange(boolean select, int changeIndex0, int changeIndex1, int invertIndex0, int invertIndex1) {
        // verify that the first range is legitimate
        if(changeIndex0 >= flagList.size() || changeIndex1 >= flagList.size()
        || ((changeIndex0 == -1 || changeIndex1 == -1) && changeIndex0 != changeIndex1)) {
            throw new IndexOutOfBoundsException("Invalid range for selection: " + changeIndex0 + "-" + changeIndex1 + ", list size is " + flagList.size());
        }
        // verify that the second range is legitimate
        if(invertIndex0 >= flagList.size() || invertIndex1 >= flagList.size()
        || ((invertIndex0 == -1 || invertIndex1 == -1) && invertIndex0 != invertIndex1)) {
            throw new IndexOutOfBoundsException("Invalid range for invert selection: " + invertIndex0 + "-" + invertIndex1 + ", list size is " + flagList.size());
        }

        // when the first range is empty
        if(changeIndex0 == -1 && changeIndex1 == -1) {
            // if the second range is empty, we're done
            if(invertIndex0 == -1 && invertIndex1 == -1) return;
            // otherwise set the first range to the second range and invert the goal
            changeIndex0 = invertIndex0;
            changeIndex1 = invertIndex1;
            select = !select;
        }
        // when the second range is empty
        if(invertIndex0 == -1 && invertIndex1 == -1) {
            // make this a subset of the first index which behaves the same as empty
            invertIndex0 = changeIndex0;
            invertIndex1 = changeIndex1;
        }

        // now get the change interval and invert interval
        int minChangeIndex = Math.min(changeIndex0, changeIndex1);
        int maxChangeIndex = Math.max(changeIndex0, changeIndex1);
        int minInvertIndex = Math.min(invertIndex0, invertIndex1);
        int maxInvertIndex = Math.max(invertIndex0, invertIndex1);

        // get the union of the two ranges
        int minUnionIndex = Math.min(minChangeIndex, minInvertIndex);
        int maxUnionIndex = Math.max(maxChangeIndex, maxInvertIndex);

        // keep track of the minimum and maximum change range
        int minChangedIndex = maxUnionIndex + 1;
        int maxChangedIndex = minUnionIndex - 1;

        // prepare a sequence of changes
        updates.beginEvent();

        // walk through the list making changes
        for(int i = minUnionIndex; i <= maxUnionIndex; i++) {
            int selectionIndex = flagList.getColourIndex(i, selected);
            boolean selectedBefore = (selectionIndex != -1);
            boolean inChangeRange = (i >= minChangeIndex && i <= maxChangeIndex);
            boolean selectedAfter = (inChangeRange == select);
            // when there's a change
            if(selectedBefore != selectedAfter) {
                // update change range
                if(i < minChangedIndex) minChangedIndex = i;
                if(i > maxChangedIndex) maxChangedIndex = i;

                // if it is being deselected
                if(selectedBefore) {
                    flagList.set(i, deselected, 1);
                    updates.addDelete(selectionIndex);
                // if it is being selected
                } else {
                    flagList.set(i, selected, 1);
                    updates.addInsert(flagList.getColourIndex(i, selected));
                }
            }
        }

        // notify event lists first
        updates.commitEvent();

        // notify list selection listeners second
        if(minChangedIndex <= maxChangedIndex) fireSelectionChanged(minChangedIndex, maxChangedIndex);
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
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            if(!enabled) return;

            // handle a clear
            if(index0 < 0 || index1 < 0) {
                clearSelection();
                return;
            }

            // update anchor and lead
            anchorSelectionIndex = index0;
            leadSelectionIndex = index1;

            // set the selection to the range and nothing else
            if(selectionMode == SINGLE_SELECTION) {
                setSubRangeOfRange(true, index0, index0, getMinSelectionIndex(), getMaxSelectionIndex());
            } else {
                setSubRangeOfRange(true, index0, index1, getMinSelectionIndex(), getMaxSelectionIndex());
            }
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
    }

    /**
     * Change the selection to be the set union of the current selection  and the indices between index0 and index1 inclusive
     */
    public void addSelectionInterval(int index0, int index1) {
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            if(!enabled) return;

            // handle a no-op
            if(index0 < 0 || index1 < 0) {
                return;
            }

            // update anchor and lead
            anchorSelectionIndex = index0;
            leadSelectionIndex = index1;

            // add this and deselect everything
            if(selectionMode == SINGLE_SELECTION) {
                setSubRangeOfRange(true, index0, index0, getMinSelectionIndex(), getMaxSelectionIndex());

            // add this interval and deselect every other interval
            } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {

                // test if the current and new selection overlap
                boolean overlap = false;
                int minSelectedIndex = getMinSelectionIndex();
                int maxSelectedIndex = getMaxSelectionIndex();
                if(minSelectedIndex - 1 <= index0 && index0 <= maxSelectedIndex + 1) overlap = true;
                if(minSelectedIndex - 1 <= index1 && index1 <= maxSelectedIndex + 1) overlap = true;

                // if they overlap, do not clear anything
                if(overlap) {
                    setSubRangeOfRange(true, index0, index1, -1, -1);
                // otherwise clear the previous selection
                } else {
                    setSubRangeOfRange(true, index0, index1, minSelectedIndex, maxSelectedIndex);
                }

            // select the specified interval without deselecting anything
            } else {
                setSubRangeOfRange(true, index0, index1, -1, -1);
            }
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
    }
    /**
     * Change the selection to be the set difference of the current selection  and the indices between index0 and index1 inclusive.
     */
    public void removeSelectionInterval(int index0, int index1) {
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            if(!enabled) return;

            // handle a no-op
            if(index0 < 0 || index1 < 0 || flagList.isEmpty()) {
                return;
            }

            // update anchor and lead
            anchorSelectionIndex = index0;
            leadSelectionIndex = index1;

            // deselect everything
            if(selectionMode == SINGLE_SELECTION) {
                setSubRangeOfRange(false, getMinSelectionIndex(), getMaxSelectionIndex(), -1, -1);
            // deselect from this to the end
            } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {
                setSubRangeOfRange(false, index0, getMaxSelectionIndex(), -1, -1);
            // deselect the specified interval without selecting anything
            } else {
                setSubRangeOfRange(false, index0, index1, -1, -1);
            }
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
    }

    /**
     * Returns the first selected index or -1 if the selection is empty.
     */
    public int getMinSelectionIndex() {
        selectionList.getReadWriteLock().readLock().lock();
        try {
            if(flagList.colourSize(selected) == 0) return -1;
            return flagList.getIndex(0, selected);
        } finally {
            selectionList.getReadWriteLock().readLock().unlock();
        }
    }
    /**
     * Returns the last selected index or -1 if the selection is empty.
     */
    public int getMaxSelectionIndex() {
        selectionList.getReadWriteLock().readLock().lock();
        try {
            if(flagList.colourSize(selected) == 0) return -1;
            return flagList.getIndex(flagList.colourSize(selected) - 1, selected);
        } finally {
            selectionList.getReadWriteLock().readLock().unlock();
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
        selectionList.getReadWriteLock().readLock().lock();
        try {
            // bail if index is too high
            if(index < 0 || index >= flagList.size()) {
                return false;
            }
            return (flagList.get(index) == selected);
        } finally {
            selectionList.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Return the first index argument from the most recent call to setSelectionInterval(), addSelectionInterval() or removeSelectionInterval().
     */
    public int getAnchorSelectionIndex() {
        selectionList.getReadWriteLock().readLock().lock();
        try {
            return anchorSelectionIndex;
        } finally {
            selectionList.getReadWriteLock().readLock().unlock();
        }
    }
    /**
     * Set the anchor selection index.
     */
    public void setAnchorSelectionIndex(int anchorSelectionIndex) {
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            if(!enabled) return;

            // update anchor
            this.anchorSelectionIndex = anchorSelectionIndex;

            // handle a clear
            if(leadSelectionIndex == -1 || anchorSelectionIndex == -1) {
                clearSelection();
                return;
            }

            // select the interval to be the anchor
            if(selectionMode == SINGLE_SELECTION) {
                setSubRangeOfRange(true, anchorSelectionIndex, anchorSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex());
            // select the interval between anchor and lead
            } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {
                setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex());
            // select the interval between anchor and lead without deselecting anything
            } else {
                setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, -1, -1);
            }
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
    }
    /**
     * Return the second index argument from the most recent call to setSelectionInterval(), addSelectionInterval() or removeSelectionInterval().
     */
    public int getLeadSelectionIndex() {
        selectionList.getReadWriteLock().readLock().lock();
        try {
            return leadSelectionIndex;
        } finally {
            selectionList.getReadWriteLock().readLock().unlock();
        }
    }
    /**
     * Set the lead selection index.
     */
    public void setLeadSelectionIndex(int leadSelectionIndex) {
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            if(!enabled) return;

            // update lead
            int originalLeadIndex = this.leadSelectionIndex;
            this.leadSelectionIndex = leadSelectionIndex;

            // handle a clear
            if(leadSelectionIndex == -1 || anchorSelectionIndex == -1) {
                clearSelection();
                return;
            }

            // select the interval to be the lead
            if(selectionMode == SINGLE_SELECTION) {
                setSubRangeOfRange(true, leadSelectionIndex, leadSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex());

            // select the interval between anchor and lead
            } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {
                setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex());

            // select the interval between anchor and lead without deselecting anything
            } else {
                setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, anchorSelectionIndex, originalLeadIndex);
            }
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
    }

    /**
     * Change the selection to the empty set.
     */
    public void clearSelection() {
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            if(!enabled) return;

            setSubRangeOfRange(false, getMinSelectionIndex(), getMaxSelectionIndex(), -1, -1);
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
    }
    /**
     * Returns true if no indices are selected.
     */
    public boolean isSelectionEmpty() {
        selectionList.getReadWriteLock().readLock().lock();
        try {
            return (flagList.colourSize(selected) == 0);
        } finally {
            selectionList.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Insert length indices beginning before/after index.
     */
    public void insertIndexInterval(int index, int length, boolean before) {
        // these changes are handled by the ListEventListener
    }
    /**
     * Remove the indices in the interval index0,index1 (inclusive) from  the selection model.
     */
    public void removeIndexInterval(int index0, int index1) {
        // these changes are handled by the ListEventListener
    }

    /**
     * This property is true if upcoming changes to the value  of the model should be considered a single event.
     */
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            this.valueIsAdjusting = valueIsAdjusting;

            // fire one extra change containing all changes in this set
            if(!valueIsAdjusting) {
                if(fullChangeStart != -1 && fullChangeFinish != -1) {
                    fireSelectionChanged(fullChangeStart, fullChangeFinish);
                    fullChangeStart = -1;
                    fullChangeFinish = -1;
                }
            }
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
    }

    /**
     * Returns true if the value is undergoing a series of changes.
     */
    public boolean getValueIsAdjusting() {
        selectionList.getReadWriteLock().readLock().lock();
        try {
            return valueIsAdjusting;
        } finally {
            selectionList.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Set the selection mode.
     */
    public void setSelectionMode(int selectionMode) {
        ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().lock();
        try {
            this.selectionMode = selectionMode;

            // ensure the selection is no more than a single element
            if(selectionMode == SINGLE_SELECTION) {
                setSubRangeOfRange(true, getMinSelectionIndex(), getMinSelectionIndex(), getMinSelectionIndex(), getMaxSelectionIndex());

            // ensure the selection is no more than a single interval
            } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {
                setSubRangeOfRange(true, getMinSelectionIndex(), getMaxSelectionIndex(), getMinSelectionIndex(), getMaxSelectionIndex());
            }
        } finally {
            ((InternalReadWriteLock)selectionList.getReadWriteLock()).internalLock().unlock();
        }
    }

    /**
     * Returns the current selection mode.
     */
    public int getSelectionMode() {
        selectionList.getReadWriteLock().readLock().lock();
        try {
            return selectionMode;
        } finally {
            selectionList.getReadWriteLock().readLock().unlock();
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
        selectionList.dispose();
    }
}