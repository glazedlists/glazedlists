/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.listselectionmodel;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
import com.odellengineeringltd.glazedlists.util.*;
// for listening to list selection events
import javax.swing.*;
import javax.swing.event.*;
// for lists of listeners
import java.util.*;

/**
 * A SelectionModelEventList is a class that performs two simulaneous
 * services. It is a ListSelectionModel to provide selection tracking for a
 * JTable. It is also a EventList that contains the table's selection.
 *
 * <p>As elements are selected or deselected, the List aspect of the
 * SelectionModelEventList changes.
 *
 * <p>The SelectionModelEventList responds to two classes of changes from a
 * JTable. The JTable's structure can change and the JTable's selection can
 * change. In either case the SelectionModelEventList is responsible for
 * updating its view of the selection and sending SelectionEvents and
 * ListChangeEvents to listeners.
 *
 * <p>This class is <strong>not thread-safe</strong>. Users of this class
 * should access it using the Event Dispatch thread only.
 *
 * <p>Internally this maintains a flag list to keep track of which elements
 * are selected and not and where they are in the source list. It uses
 * a sparse list that corresponds directly to the source list. The sparse
 * list contains null for all unselected elements and Boolean.TRUE for
 * elements which are selected. This is used to map indexes between the
 * selection subset and the full source list.
 *
 * <p>Alongside <code>MULTIPLE_INTERVAL_SELECTION</code>, this selection model
 * supports an additional selection mode.
 * <code>MULTIPLE_INTERVAL_SELECTION_DEFENSIVE</code> is a new selection mode.
 * It is idential to <code>MULTIPLE_INTERVAL_SELECTION</code> in every way but
 * one. When a row is inserted immediately before a selected row in the
 * <code>MULTIPLE_INTERVAL_SELECTION</code> mode, it becomes selected. But in
 * the <code>MULTIPLE_INTERVAL_SELECTION_DEFENSIVE</code> mode, it does not
 * become selected. To set this mode, use 
 * <code>setSelectionMode(SelectionModelEventList.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE)</code>.
 * on the ListSelectionModel.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=15">Bug 15</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SelectionModelEventList {
    
    /** the new selection mode behaves similar to MULTIPLE_INTERVAL_SELECTION */
    public static final int MULTIPLE_INTERVAL_SELECTION_DEFENSIVE = 103;
    
    /** the list selection model handles selection tables */
    private EventListSelectionModel selectionModel;

    /** the event list provides an event list view of the selection */
    private SelectionEventList eventList;
    
    /** the flag list contains Boolean.TRUE for selected items and null for others */
    private SparseList flagList = new SparseList();
    
    /** the source event list knows the table dimensions */
    private EventList source;
    
    /** list change updates */
    private ListChangeSequence updates = null;
    
    /**
     * Creates a new Mutation list that uses the specified source list.
     */
    public SelectionModelEventList(EventList source) {
        this.source = source;
        prepareFlagList();

        this.selectionModel = new EventListSelectionModel();
        this.eventList = new SelectionEventList(source);
    }
    
    /**
     * When the FlagList is prepared, it populates it with information from
     * the source list and the initial selection model.
     */
    private void prepareFlagList() {
        for(int i = 0; i < source.size(); i++) {
            flagList.add(null);
        }
    }
    
    /**
     * Gets the event list that always contains the current selection.
     */
    public EventList getEventList() {
        return eventList;
    }

    /**
     * Gets the selection model that provides selection management for a
     * table.
     */
    public ListSelectionModel getListSelectionModel() {
        return selectionModel;
    }

    /**
     * This is the EventList component of the SelectionModelEventList. It is
     * responsible for listening to changes in the JTable's size and modifying
     * the internal list model to match.
     */
    class SelectionEventList extends MutationList {
        
        /**
         * Creates a new SelectionEventList that listens to changes from the
         * source event list.
         */
        public SelectionEventList(EventList source) {
            super(source);
            source.addListChangeListener(this);
            SelectionModelEventList.this.updates = super.updates;
        }
    
        /**
         * Returns the element at the specified position in this list.
         *
         * This gets the object with the specified index from the source list.
         *
         * Before every get, we need to validate the row because there may be an
         * update waiting in the event queue. For example, it is possible that
         * the selection has changed. Such a change may have been sent as notification,
         * but after this request in the event queue. In the case where a row is no longer
         * available, null is returned. The value returned is insignificant in this case
         * because the Event queue will very shortly be repainting (or removing) the row
         * anyway. This strategy is also used in ListTable where changes are frequently
         * queued to be processed.
         */
        public Object get(int index) {
            int sourceIndex = flagList.getIndex(index);
    
            // ensure that this value still exists before retrieval
            if(sourceIndex < source.size()) {
                return source.get(sourceIndex);
            } else {
                //new Exception("Returning null for removed selection " + row).printStackTrace();
                return null;
            }
        }

        /**
         * Returns the number of elements in this list.
         *
         * This is the number of elements currently selected.
         */
        public int size() {
            return flagList.getCompressedList().size();
        }

        /**
         * Notifies this SelectionList about changes to its underlying list store.
         *
         * This changes the flag list. Changes to the source list may cause simultaneous
         * changes to the corresponding selection list.
         */
        public void notifyListChanges(ListChangeEvent listChanges) {
            synchronized(getRootList()) {
            
                // prepare for notifying ListSelectionListeners
                int minSelectionIndexBefore = selectionModel.getMinSelectionIndex();
                int maxSelectionIndexBefore = selectionModel.getMaxSelectionIndex();
                
                // prepare a sequence of changes
                updates.beginAtomicChange();
                
                // for all changes simply update the flag list
                while(listChanges.next()) {
                    int index = listChanges.getIndex();
                    int changeType = listChanges.getType();
                    
                    // learn about what it was
                    boolean previouslySelected = (flagList.size() > index) && (flagList.get(index) != null);
                    int previousSelectionIndex = -1;
                    if(previouslySelected) previousSelectionIndex = flagList.getCompressedIndex(index);
                    
                    // when an element is deleted, blow it away
                    if(changeType == ListChangeBlock.DELETE) {
                        flagList.remove(index);
        
                        // fire a change to the selection list if a selected object is changed
                        if(previouslySelected) {
                            updates.appendChange(previousSelectionIndex, ListChangeBlock.DELETE);
                        }
                        
                    // when an element is inserted, it is selected if its index was selected
                    } else if(changeType == ListChangeBlock.INSERT) {
                        
                        // when selected, decide based on selection mode
                        if(previouslySelected) {

                            // select the inserted for single interval and multiple interval selection
                            if(selectionModel.selectionMode == selectionModel.SINGLE_INTERVAL_SELECTION
                            || selectionModel.selectionMode == selectionModel.MULTIPLE_INTERVAL_SELECTION) {
                                flagList.add(index, Boolean.TRUE);
                                updates.appendChange(previousSelectionIndex, ListChangeBlock.INSERT);

                            // do not select the inserted for single selection and defensive selection
                            } else {
                                flagList.add(index, null);
                            }
    
                        // when not selected, just add the space
                        } else {
                            flagList.add(index, null);
                        }
                        
                    // when an element is changed, assume selection stays the same
                    } else if(changeType == ListChangeBlock.UPDATE) {
        
                        // fire a change to the selection list if a selected object is changed
                        if(previouslySelected) {
                            updates.appendChange(previousSelectionIndex, ListChangeBlock.UPDATE);
                        }
                    }
                }
        
                // fire the changes to ListChangeListeners
                updates.commitAtomicChange();

                // fire the changes to ListSelectionListeners
                if(minSelectionIndexBefore != 0 && maxSelectionIndexBefore != 0) {
                    int minSelectionIndexAfter = selectionModel.getMinSelectionIndex();
                    int maxSelectionIndexAfter = selectionModel.getMaxSelectionIndex();
                    int changeStart = minSelectionIndexBefore;
                    int changeFinish = maxSelectionIndexBefore;
                    if(minSelectionIndexAfter != -1 && minSelectionIndexAfter < changeStart) changeStart = minSelectionIndexAfter;
                    if(maxSelectionIndexAfter != -1 && maxSelectionIndexAfter > changeFinish) changeFinish = maxSelectionIndexAfter;
                    selectionModel.fireSelectionChanged(changeStart, changeFinish);
                }
            }
        }
    }
    
    /**
     * Gets this as a string for debugging purposes only.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < flagList.size(); i++) {
            if(i != 0) result.append(" ");
            if(flagList.get(i) == null) result.append("-");
            else result.append("+");
        }
        return result.toString();
    }
    
    /**
     * This model provides a service for the JTable. It listens to changes in
     * the JTable's selection and keeps track of what is selected. It is also
     * responsible for notifying ListSelectionListeners of any changes.
     */
    class EventListSelectionModel implements ListSelectionModel {

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
         * Notify listeners that the selection has changed.
         *
         * <p>This notifies all listeners with the same immutable
         * ListSelectionEvent.
         */
        protected void fireSelectionChanged(int changeStart, int changeFinish) {
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
                throw new ArrayIndexOutOfBoundsException("Invalid range for selection: " + changeIndex0 + "-" + changeIndex1 + ", list size is " + flagList.size());
            }
            // verify that the second range is legitimate
            if(invertIndex0 >= flagList.size() || invertIndex1 >= flagList.size()
            || ((invertIndex0 == -1 || invertIndex1 == -1) && invertIndex0 != invertIndex1)) {
                throw new ArrayIndexOutOfBoundsException("Invalid range for invert selection: " + invertIndex0 + "-" + invertIndex1 + ", list size is " + flagList.size());
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
            updates.beginAtomicChange();

            // walk through the list making changes
            for(int i = minUnionIndex; i <= maxUnionIndex; i++) {
                boolean selectedBefore = (flagList.get(i) != null);
                boolean inChangeRange = (i >= minChangeIndex && i <= maxChangeIndex);
                boolean selectedAfter = (inChangeRange == select);
                // when there's a change
                if(selectedBefore != selectedAfter) {
                    // update change range
                    if(i < minChangedIndex) minChangedIndex = i;
                    if(i > maxChangedIndex) maxChangedIndex = i;
                    
                    // if it is being deselected
                    if(selectedBefore) {
                        int selectionIndex = flagList.getCompressedIndex(i);
                        flagList.set(i, null);
                        updates.appendChange(selectionIndex, ListChangeBlock.DELETE);
                    // if it is being selected
                    } else {
                        flagList.set(i, Boolean.TRUE);
                        int selectionIndex = flagList.getCompressedIndex(i);
                        updates.appendChange(selectionIndex, ListChangeBlock.INSERT);
                    }
                }
            }
            
            // notify event lists first
            updates.commitAtomicChange();
            
            // notify list selection listeners second
            if(minChangedIndex <= maxChangedIndex) fireSelectionChanged(minChangedIndex, maxChangedIndex);
        }

        /**
         * Change the selection to be between index0 and index1 inclusive.
         *
         * First this calculates the smallest range where changes occur. This
         * includes the union of the selection range before and the selection
         * range specified. It then walks through the change and sets each
         * index as selected or not based on whether the index is in the 
         * new range. Finally it fires events to both the listening lists and
         * selection listeners about what changes happened.
         *
         * If the selection does not change, this will not fire any events.
         */
        public void setSelectionInterval(int index0, int index1) {
            // update anchor and lead
            anchorSelectionIndex = index0;
            leadSelectionIndex = index1;
            
            // set the selection to the range and nothing else
            if(selectionMode == SINGLE_SELECTION) {
                setSubRangeOfRange(true, index0, index0, getMinSelectionIndex(), getMaxSelectionIndex());
            } else {
                setSubRangeOfRange(true, index0, index1, getMinSelectionIndex(), getMaxSelectionIndex());
            }
        }
        
        /**
         * Change the selection to be the set union of the current selection  and the indices between index0 and index1 inclusive
         */
        public void addSelectionInterval(int index0, int index1) {
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
        }
        /**
         * Change the selection to be the set difference of the current selection  and the indices between index0 and index1 inclusive.
         */
        public void removeSelectionInterval(int index0, int index1) {
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
        }

        /**
         * Returns the first selected index or -1 if the selection is empty.
         */
        public int getMinSelectionIndex() {
            if(flagList.getCompressedList().size() == 0) return -1;
            return flagList.getIndex(0);
        }
        /**
         * Returns the last selected index or -1 if the selection is empty.
         */
        public int getMaxSelectionIndex() {
            if(flagList.getCompressedList().size() == 0) return -1;
            return flagList.getIndex(flagList.getCompressedList().size() - 1);
        }
        /**
         * Returns true if the specified index is selected.
         */
        public boolean isSelectedIndex(int index) {
            // bail if index is too high
            if(index < 0 || index >= flagList.size()) {
                throw new ArrayIndexOutOfBoundsException("Cannot get selection index " + index + ", list size " + flagList.size());
            }
            
            // a value is selected if it is not null in the flag list
            return (flagList.get(index) != null);
        }

        /**
         * Return the first index argument from the most recent call to setSelectionInterval(), addSelectionInterval() or removeSelectionInterval().
         */
        public int getAnchorSelectionIndex() {
            return anchorSelectionIndex;
        }
        /**
         * Set the anchor selection index.
         */
        public void setAnchorSelectionIndex(int anchorSelectionIndex) {
            // update anchor
            this.anchorSelectionIndex = anchorSelectionIndex;

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
        }
        /**
         * Return the second index argument from the most recent call to setSelectionInterval(), addSelectionInterval() or removeSelectionInterval().
         */
        public int getLeadSelectionIndex() {
            return leadSelectionIndex;
        }
        /**
         * Set the lead selection index.
         */
        public void setLeadSelectionIndex(int leadSelectionIndex) {
            // update lead
            int originalLeadIndex = this.leadSelectionIndex;
            this.leadSelectionIndex = leadSelectionIndex;

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
        }

        /**
         * Change the selection to the empty set.
         */
        public void clearSelection() {
            setSubRangeOfRange(false, getMinSelectionIndex(), getMaxSelectionIndex(), -1, -1);
        }
        /**
         * Returns true if no indices are selected.
         */
        public boolean isSelectionEmpty() {
            return (flagList.getCompressedList().size() == 0);
        }

        /**
         * Insert length indices beginning before/after index.
         */
        public void insertIndexInterval(int index, int length, boolean before) {
            // these changes are handled by the ListChangeListener
        }
        /**
         * Remove the indices in the interval index0,index1 (inclusive) from  the selection model. 
         */
        public void removeIndexInterval(int index0, int index1) {
            // these changes are handled by the ListChangeListener
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
            this.selectionMode = selectionMode;
            
            // ensure the selection is no more than a single element
            if(selectionMode == SINGLE_SELECTION) {
                setSubRangeOfRange(true, getMinSelectionIndex(), getMinSelectionIndex(), getMinSelectionIndex(), getMaxSelectionIndex());

            // ensure the selection is no more than a single interval
            } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {
                setSubRangeOfRange(true, getMinSelectionIndex(), getMaxSelectionIndex(), getMinSelectionIndex(), getMaxSelectionIndex());
            }
        }
        /**
         * Returns the current selection mode.
         */
        public int getSelectionMode() {
            return selectionMode;
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
    }
}
