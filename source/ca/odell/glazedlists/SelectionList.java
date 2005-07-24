/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// core glazed lists
import ca.odell.glazedlists.event.*;
// the common user interface classes
import ca.odell.glazedlists.gui.*;
// access to the volatile implementation classes
import ca.odell.glazedlists.impl.adt.*;
// to store event info for forwarding on the deselected EventList
import java.util.*;

/**
 * An {@link EventList} to provide index-based selection features.  This
 * {@link EventList} does not perform a transformation on the source, but
 * instead provides two additional {@link EventList}s:
 * <ul>
 *   <li>{@link #getSelected() Selected} - an {@link EventList} that contains only the selected values.</li>
 *   <li>{@link #getDeselected() Deselected} - an {@link EventList} that contains only the deselected values.</li>
 * </ul>
 *
 * <p>This design is intended to allow the sharing of selection logic between
 * both of our supported GUI toolkits as well as being available for use in
 * non-GUI applications and for index-based filtering.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class SelectionList extends TransformedList {

    /** A selection mode where at most one element may be selected at one time. */
    public static final int SINGLE_SELECTION = 100;

    /** A selection mode where at most one range of elements may be selected at one time. */
    public static final int SINGLE_INTERVAL_SELECTION = 101;

    /** A selection mode where any element may be selected and elements added adjacent to selected elements are selected. */
    public static final int MULTIPLE_INTERVAL_SELECTION = 102;

    /** A selection mode where any element may be selected and freshly added elements are always deselected. */
    public static final int MULTIPLE_INTERVAL_SELECTION_DEFENSIVE = 103;

    /** the selected view */
    private SelectedList selectedList = null;

    /** the deselected view */
    private DeselectedList deselectedList = null;

    /** the selection state */
    private Barcode barcode = new Barcode();

    /** the lead selection index */
    private int leadSelectionIndex = -1;

    /** the anchor selection index */
    private int anchorSelectionIndex = -1;

    /** the selection mode defines characteristics of the selection */
    private int selectionMode = MULTIPLE_INTERVAL_SELECTION_DEFENSIVE;

    /** the current selection colour */
    private Object selected = Barcode.BLACK;

    /** the current deselection colour */
    private Object deselected = Barcode.WHITE;

    /** the registered SelectionListeners */
    private List selectionListeners = new ArrayList(1);

    /**
     * Creates a new SelectionList that listens to changes on the given source.
     * When using this constructor, all elements are deselected by default.
     */
    public SelectionList(EventList source) {
        super(source);
        barcode.add(0, deselected, source.size());
        source.addListEventListener(this);
        deselectedList = new DeselectedList(source);
        selectedList = new SelectedList(source);
    }

    /**
     * Creates a new SelectionList that listens to changes on the given source
     * and initializes selection with the given array of indices.
     */
    public SelectionList(EventList source, int[] initialSelection) {
        this(source);
        select(initialSelection);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {

        // keep track of what used to be selected
        int minSelectionIndexBefore = getMinSelectionIndex();
        int maxSelectionIndexBefore = getMaxSelectionIndex();

        // handle reordering events
        if(listChanges.isReordering()) {
            // prepare for the reordering event
            selectedList.updates().beginEvent();

            int[] sourceReorderMap = listChanges.getReorderMap();
            int[] selectReorderMap = new int[barcode.colourSize(selected)];
            int[] deselectReorderMap = new int[barcode.colourSize(deselected)];

            // adjust the flaglist & construct a reorder map to propagate
            Barcode previousBarcode = barcode;
            barcode = new Barcode();
            for(int c = 0; c < sourceReorderMap.length; c++) {
                Object flag = previousBarcode.get(sourceReorderMap[c]);
                boolean wasSelected = (flag != deselected);
                barcode.add(c, flag, 1);
                if(wasSelected) {
                    int previousIndex = previousBarcode.getColourIndex(sourceReorderMap[c], selected);
                    int currentIndex = barcode.getColourIndex(c, selected);
                    selectReorderMap[currentIndex] = previousIndex;
                } else {
                    int previousIndex = previousBarcode.getColourIndex(sourceReorderMap[c], deselected);
                    int currentIndex = barcode.getColourIndex(c, deselected);
                    deselectReorderMap[currentIndex] = previousIndex;
                }
            }

            // adjust other internal state
            anchorSelectionIndex = -1;
            leadSelectionIndex = -1;

            // fire the reorder on the selected list
            selectedList.updates().reorder(selectReorderMap);
            selectedList.updates().commitEvent();

            // fire the reorder on the deselected list
            deselectedList.updates().beginEvent();
            deselectedList.updates().reorder(deselectReorderMap);
            deselectedList.updates().commitEvent();

        // handle non-reordering events
        } else {
            // Keep track of deselected changes as you go
            ArrayList savedChanges = new ArrayList();

            // prepare a sequence of changes
            selectedList.updates().beginEvent();

            // for all changes update the barcode
            while(listChanges.next()) {
                int index = listChanges.getIndex();
                int changeType = listChanges.getType();

                // learn about what it was
                int previousSelectionIndex = barcode.getColourIndex(index, selected);
                boolean previouslySelected = previousSelectionIndex != -1;

                // when an element is deleted, blow it away
                if(changeType == ListEvent.DELETE) {

                    // delete selected values
                    if(previouslySelected) {
                        barcode.remove(index, 1);
                        selectedList.updates().addDelete(previousSelectionIndex);

                    // delete deselected values
                    } else {
                        int deselectedIndex = barcode.getColourIndex(index, deselected);
                        savedChanges.add(new DeselectedChange(ListEvent.DELETE, deselectedIndex));
                        barcode.remove(index, 1);
                    }


                // when an element is inserted, it is selected if its index was selected
                } else if(changeType == ListEvent.INSERT) {

                    // when selected, decide based on selection mode
                    if(previouslySelected) {

                        // select the inserted for single interval and multiple interval selection
                        if(selectionMode == SINGLE_INTERVAL_SELECTION
                        || selectionMode == MULTIPLE_INTERVAL_SELECTION) {
                            barcode.add(index, selected, 1);
                            selectedList.updates().addInsert(previousSelectionIndex);

                        // do not select the inserted for single selection and defensive selection
                        } else {
                            barcode.add(index, deselected, 1);
                            int deselectedIndex = barcode.getColourIndex(index, deselected);
                            savedChanges.add(new DeselectedChange(ListEvent.INSERT, deselectedIndex));
                        }

                    // add a deselected value
                    } else {
                        barcode.add(index, deselected, 1);
                        int deselectedIndex = barcode.getColourIndex(index, deselected);
                        savedChanges.add(new DeselectedChange(ListEvent.INSERT, deselectedIndex));
                    }

                // when an element is changed, assume selection stays the same
                } else if(changeType == ListEvent.UPDATE) {
                    // update a selected value
                    if(previouslySelected) {
                        selectedList.updates().addUpdate(previousSelectionIndex);

                    // update a deselected value
                    } else {
                        int deselectedIndex = barcode.getColourIndex(index, deselected);
                        savedChanges.add(new DeselectedChange(ListEvent.UPDATE, deselectedIndex));
                    }
                }

                // adjust other internal state
                anchorSelectionIndex = adjustIndex(anchorSelectionIndex, changeType, index);
                leadSelectionIndex = adjustIndex(leadSelectionIndex, changeType, index);
            }
            // fire changes on the selected list
            selectedList.updates().commitEvent();

            // Forward all of the collected changes made to the deselected list
            deselectedList.updates().beginEvent();
            for(int i = 0;i < savedChanges.size();i++) {
                DeselectedChange change = (DeselectedChange)savedChanges.get(i);
                change.fireChange();
            }
            savedChanges.clear();
            deselectedList.updates().commitEvent();
        }

        // forward events on the primary EventList
        listChanges.reset();
        updates.forwardEvent(listChanges);

        // notify listeners of selection change
        if(minSelectionIndexBefore != -1 && maxSelectionIndexBefore != -1) {
            int minSelectionIndexAfter = getMinSelectionIndex();
            int maxSelectionIndexAfter = getMaxSelectionIndex();
            int changeStart = minSelectionIndexBefore;
            int changeFinish = maxSelectionIndexBefore;
            if(minSelectionIndexAfter != -1 && minSelectionIndexAfter < changeStart) changeStart = minSelectionIndexAfter;
            if(maxSelectionIndexAfter != -1 && maxSelectionIndexAfter > changeFinish) changeFinish = maxSelectionIndexAfter;
            fireSelectionChanged(changeStart, changeFinish);
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
     * Provides access to an {@link EventList} that contains only selected values.
     */
    public EventList getSelected() {
        return selectedList;
    }

    /**
     * Provides access to an {@link EventList} that contains only deselected values.
     */
    public EventList getDeselected() {
        return deselectedList;
    }

    /**
     * Inverts the current selection.
     */
    public void invertSelection() {
        // Switch what colour is considered to be selected
        if(selected == Barcode.BLACK) {
            selected = Barcode.WHITE;
            deselected = Barcode.BLACK;
        } else {
            selected = Barcode.BLACK;
            deselected = Barcode.WHITE;
        }

        // Clear the anchor and lead
        anchorSelectionIndex = -1;
        leadSelectionIndex = -1;

        // Update the selected list to reflect the selection inversion
        selectedList.updates().beginEvent();
        selectedList.updates().addDelete(0, barcode.colourSize(deselected) - 1);
        selectedList.updates().addInsert(0, barcode.colourSize(selected) - 1);
        selectedList.updates().commitEvent();

        // Update the deselected list to reflect the selection inversion
        deselectedList.updates().beginEvent();
        deselectedList.updates().addDelete(0, barcode.colourSize(selected) - 1);
        deselectedList.updates().addInsert(0, barcode.colourSize(deselected) - 1);
        deselectedList.updates().commitEvent();

        // notify selection listeners that selection has been inverted
        fireSelectionChanged(0, size() - 1);
    }

    /**
     * Returns whether or not the item with the given source index
     * is selected.
     */
    public boolean isSelected(int sourceIndex) {
        if(sourceIndex < 0 || sourceIndex >= source.size()) {
            return false;
        }
        return barcode.getColourIndex(sourceIndex, selected) != -1;
    }

    /**
     * Deselects the element at the given index.
     */
    public void deselect(int index) {
        deselect(index, index);
    }

    /**
     * Deselects all of the elements within the given range.
     */
    public void deselect(int start, int end) {
        // fast fail if the range is invalid
        if(start == -1 || end == -1) {
            return;

        // use single value deselection in single selection mode
        } else if(selectionMode == SINGLE_SELECTION) {
            int selectedIndex = getMaxSelectionIndex();
            // only change selection if you have to
            if(selectedIndex >= start && selectedIndex <= end) {
                deselectAll();
            }
            return;

        // adjust the range to prevent the creation of multiple intervals
        } else if(selectionMode == SINGLE_INTERVAL_SELECTION && start > getMinSelectionIndex()) {
            end = Math.max(end, getMaxSelectionIndex());
        }
        // update anchor and lead
        anchorSelectionIndex = start;
        leadSelectionIndex = end;

        // alter selection accordingly
        setSubRangeOfRange(false, start, end, -1, -1);
    }

    /**
     * Deselects all of the elements in the given array of indices.  The
     * array must contain indices in sorted, ascending order.
     */
    public void deselect(int[] indices) {
        // have to keep track of what deselected values were added
        List deselections = new ArrayList();

        // keep track of the range of values that were affected
        int firstAffectedIndex = -1;
        int lastAffectedIndex = -1;

        // iterate through the barcode updating the selected list as you go
        selectedList.updates().beginEvent();
        int currentIndex = 0;
        for(BarcodeIterator i = barcode.iterator();i.hasNext() && currentIndex != indices.length; ) {
            Object value = i.next();
            if(i.getIndex() == indices[currentIndex]) {
                // selection changed
                if(value == selected) {
                    if(firstAffectedIndex == -1) firstAffectedIndex = i.getIndex();
                    lastAffectedIndex = i.getIndex();
                    int selectedIndex = i.getColourIndex(selected);
                    deselections.add(new DeselectedChange(ListEvent.INSERT, i.set(deselected)));
                    selectedList.updates().addDelete(selectedIndex);
                }
                currentIndex++;
            }
        }
        selectedList.updates().commitEvent();

        // update the deselected list
        deselectedList.updates().beginEvent();
        for(int i = 0; i < deselections.size(); i++) {
            DeselectedChange change = (DeselectedChange)deselections.get(i);
            change.fireChange();
        }
        deselectedList.updates().commitEvent();

        // notify listeners of selection change
        if(firstAffectedIndex > -1) fireSelectionChanged(firstAffectedIndex, lastAffectedIndex);
    }

    /**
     * Deselects all elements.
     */
    public void deselectAll() {
        // keep track of how many selected elements there were
        int selectionChangeSize = barcode.colourSize(selected);

        // fast fail if there is no change to make
        if(selectionChangeSize == 0) return;

        // keep track of the range of values that were affected
        int firstAffectedIndex = -1;
        int lastAffectedIndex = -1;

        // update the deselected list while processing the change
        deselectedList.updates().beginEvent();
        for(BarcodeIterator i = barcode.iterator(); i.hasNextColour(selected); ) {
            i.nextColour(selected);
            if(firstAffectedIndex == -1) firstAffectedIndex = i.getIndex();
            lastAffectedIndex = i.getIndex();
            deselectedList.updates().addInsert(i.getIndex());
        }
        // bulk update the barcode to be entirely deselected
        barcode.clear();
        barcode.add(0, deselected, source.size());
        deselectedList.updates().commitEvent();

        // update the selected list
        selectedList.updates().beginEvent();
        selectedList.updates().addDelete(0, selectionChangeSize - 1);
        selectedList.updates().commitEvent();

        // notify listeners of selection change
        fireSelectionChanged(firstAffectedIndex, lastAffectedIndex);
    }

    /**
     * Selects the element at the given index.
     */
    public void select(int index) {
        select(index, index);
    }

    /**
     * Selects all of the elements within the given range.
     */
    public void select(int start, int end) {
        // fast fail if the range is an no-op
        if(start == -1 || end == -1) {
            return;

        // use single value deselection in single selection mode
        } else if(selectionMode == SINGLE_SELECTION) {
            setSelection(start);
            return;

        // adjust the range to prevent the creation of multiple intervals
        } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {
            // test if the current and new selection overlap
            boolean overlap = false;
            int minSelectedIndex = getMinSelectionIndex();
            int maxSelectedIndex = getMaxSelectionIndex();
            if(minSelectedIndex - 1 <= start && start <= maxSelectedIndex + 1) overlap = true;
            if(minSelectedIndex - 1 <= end && end <= maxSelectedIndex + 1) overlap = true;

            if(!overlap) {
                setSelection(start, end);
                return;
            }
        }
        // update anchor and lead
        anchorSelectionIndex = start;
        leadSelectionIndex = end;

        // alter selection accordingly
        setSubRangeOfRange(true, start, end, -1, -1);
    }

    /**
     * Selects all of the elements in the given array of indices.  The
     * array must contain indices in sorted, ascending order.
     */
    public void select(int[] indices) {
        // have to keep track of what deselected values were added
        List selections = new ArrayList();

        // keep track of the range of values that were affected
        int firstAffectedIndex = -1;
        int lastAffectedIndex = -1;

        // iterate through the barcode updating the selected list as you go
        selectedList.updates().beginEvent();
        int currentIndex = 0;
        for(BarcodeIterator i = barcode.iterator();i.hasNext() && currentIndex != indices.length; ) {
            Object value = i.next();
            if(i.getIndex() == indices[currentIndex]) {
                // selection changed
                if(value != selected) {
                if(firstAffectedIndex == -1) firstAffectedIndex = i.getIndex();
                lastAffectedIndex = i.getIndex();
                    int deselectedIndex = i.getColourIndex(deselected);
                    selections.add(new DeselectedChange(ListEvent.DELETE, deselectedIndex));
                    selectedList.updates().addInsert(i.set(selected));
                }
                currentIndex++;
            }
        }
        selectedList.updates().commitEvent();

        // update the deselected list
        deselectedList.updates().beginEvent();
        for(int i = 0; i < selections.size(); i++) {
            DeselectedChange change = (DeselectedChange)selections.get(i);
            change.fireChange();
        }
        deselectedList.updates().commitEvent();

        // notify listeners of selection change
        if(firstAffectedIndex > -1) fireSelectionChanged(firstAffectedIndex, lastAffectedIndex);
    }

    /**
     * Selects all elements.
     */
    public void selectAll() {
        // keep track of how many deselected elements there were
        int deselectionChangeSize = barcode.colourSize(deselected);

        // fast fail if there is nothing to change
        if(deselectionChangeSize == 0) return;

        // keep track of the range of values that were affected
        int firstAffectedIndex = -1;
        int lastAffectedIndex = -1;

        // update the selected list while processing the change
        selectedList.updates().beginEvent();
        for(BarcodeIterator i = barcode.iterator(); i.hasNextColour(deselected); ) {
            i.nextColour(deselected);
            if(firstAffectedIndex == -1) firstAffectedIndex = i.getIndex();
            lastAffectedIndex = i.getIndex();
            selectedList.updates().addInsert(i.getIndex());
        }
        // bulk update the barcode to be entirely selected
        barcode.clear();
        barcode.add(0, selected, source.size());
        selectedList.updates().commitEvent();

        // update the deselected list
        deselectedList.updates().beginEvent();
        deselectedList.updates().addDelete(0, deselectionChangeSize - 1);
        deselectedList.updates().commitEvent();

        // notify listeners of selection change
        fireSelectionChanged(firstAffectedIndex, lastAffectedIndex);
    }

    /**
     * Sets the selection to be only the element at the given index.  If the
     * given index is -1, the selection will be cleared.
     */
    public void setSelection(int index) {
        setSelection(index, index);
    }

    /**
     * Sets the selection to be only elements within the given range.  If the
     * endpoints of the range are -1, the selection will be cleared.
     */
    public void setSelection(int start, int end) {
        // a range including -1 implies a deselectAll()
        if(start == -1 || end == -1) {
            deselectAll();
            return;

        } else if(selectionMode == SINGLE_SELECTION) {
            end = start;
        }

        // update anchor and lead
        anchorSelectionIndex = start;
        leadSelectionIndex = end;

        // alter selection accordingly
        setSubRangeOfRange(true, start, end, getMinSelectionIndex(), getMaxSelectionIndex());
    }

    /**
     * Sets the selection to be only the element in the given array of indices.
     * Unlike {@link #setSelection(int)} and {@link #setSelection(int,int)},
     * providing a value of -1 is an error.  The array must contain indices in
     * sorted, ascending order.
     */
    public void setSelection(int[] indices) {
        // fast fail is the selection is empty
        if(indices.length == 0) {
            deselectAll();
            return;
        }

        // have to keep track of what deselected values were added and removed
        List changes = new ArrayList();

        // keep track of the range of values that were affected
        int firstAffectedIndex = -1;
        int lastAffectedIndex = -1;

        // iterate through the barcode updating the selected list as you go
        selectedList.updates().beginEvent();
        int currentIndex = 0;
        for(BarcodeIterator i = barcode.iterator();i.hasNext(); ) {
            Object value = i.next();
            // this element should be selected
            if(i.getIndex() == indices[currentIndex]) {
                // selection changed
                if(value != selected) {
                    if(firstAffectedIndex == -1) firstAffectedIndex = i.getIndex();
                    lastAffectedIndex = i.getIndex();
                    int deselectedIndex = i.getColourIndex(deselected);
                    changes.add(new DeselectedChange(ListEvent.DELETE, deselectedIndex));
                    selectedList.updates().addInsert(i.set(selected));
                }

                // look at the next value
                if(currentIndex < indices.length - 1) currentIndex++;

            // element was selected and isn't within the new selection
            } else if(value == selected) {
                if(firstAffectedIndex == -1) firstAffectedIndex = i.getIndex();
                lastAffectedIndex = i.getIndex();
                int selectedIndex = i.getColourIndex(selected);
                selectedList.updates().addDelete(selectedIndex);
                changes.add(new DeselectedChange(ListEvent.INSERT, i.set(deselected)));
            }
        }
        selectedList.updates().commitEvent();

        // update the deselected list
        deselectedList.updates().beginEvent();
        for(int i = 0; i < changes.size(); i++) {
            DeselectedChange change = (DeselectedChange)changes.get(i);
            change.fireChange();
        }
        deselectedList.updates().commitEvent();

        // notify listeners of selection change
        if(firstAffectedIndex > -1) fireSelectionChanged(firstAffectedIndex, lastAffectedIndex);
    }

    /**
     * Return the anchor of the current selection.
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

        // a value of -1 clears selection
        if(anchorSelectionIndex == -1 || leadSelectionIndex == -1) {
            deselectAll();

        // only the anchor should be selected
        } else if(selectionMode == SINGLE_SELECTION) {
            setSubRangeOfRange(true, anchorSelectionIndex, anchorSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex());
            //setSelection(anchorSelectionIndex);

        // select the interval between anchor and lead
        } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {
            setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex());
            //setSelection(anchorSelectionIndex, leadSelectionIndex);

        // select the interval between anchor and lead without deselecting anything
        } else {
            setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, -1, -1);
            //select(anchorSelectionIndex, leadSelectionIndex);
        }
    }

    /**
     * Return the lead of the current selection.
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

        // a value of -1 clears selection
        if(leadSelectionIndex == -1 || anchorSelectionIndex == -1) {
            deselectAll();

        // select only the lead
        } else if(selectionMode == SINGLE_SELECTION) {
            setSubRangeOfRange(true, leadSelectionIndex, leadSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex());
            //setSelection(leadSelectionIndex);

        // select the interval between anchor and lead
        } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {
            setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex());
            //setSelection(anchorSelectionIndex, leadSelectionIndex);

        // select the interval between anchor and lead deselecting as necessary
        } else {
            setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, anchorSelectionIndex, originalLeadIndex);
        }
    }

    /**
     * Set the selection mode.
     */
    public void setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        setSelection(getMinSelectionIndex(), getMaxSelectionIndex());
    }

    /**
     * Returns the current selection mode.
     */
    public int getSelectionMode() {
        return selectionMode;
    }

    /**
     * Returns the first selected index or -1 if nothing is selected.
     */
    public int getMinSelectionIndex() {
        if(barcode.colourSize(selected) == 0) return -1;
        return barcode.getIndex(0, selected);
    }

    /**
     * Returns the last selected index or -1 if nothing is selected.
     */
    public int getMaxSelectionIndex() {
        if(barcode.colourSize(selected) == 0) return -1;
        return barcode.getIndex(barcode.colourSize(selected) - 1, selected);
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
        if(changeIndex0 >= source.size() || changeIndex1 >= source.size()
        || ((changeIndex0 == -1 || changeIndex1 == -1) && changeIndex0 != changeIndex1)) {
            throw new IndexOutOfBoundsException("Invalid range for selection: " + changeIndex0 + "-" + changeIndex1 + ", list size is " + source.size());
        }
        // verify that the second range is legitimate
        if(invertIndex0 >= source.size() || invertIndex1 >= source.size()
        || ((invertIndex0 == -1 || invertIndex1 == -1) && invertIndex0 != invertIndex1)) {
            throw new IndexOutOfBoundsException("Invalid range for invert selection: " + invertIndex0 + "-" + invertIndex1 + ", list size is " + source.size());
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

        int minChangedIndex = maxUnionIndex + 1;
        int maxChangedIndex = minUnionIndex - 1;

        // prepare a sequence of changes on the selection list
        selectedList.updates().beginEvent();

        // Keep track of deselected changes as you go
        ArrayList savedChanges = new ArrayList();

        // walk through the affect range updating selection
        for(int i = minUnionIndex; i <= maxUnionIndex; i++) {
            int selectionIndex = barcode.getColourIndex(i, selected);
            boolean selectedBefore = (selectionIndex != -1);
            boolean inChangeRange = (i >= minChangeIndex && i <= maxChangeIndex);
            boolean selectedAfter = (inChangeRange == select);

            // when there's a change
            if(selectedBefore != selectedAfter) {

                // update change range
                if(i < minChangedIndex) minChangedIndex = i;
                if(i > maxChangedIndex) maxChangedIndex = i;

                // it is being deselected
                if(selectedBefore) {
                    barcode.set(i, deselected, 1);
                    selectedList.updates().addDelete(selectionIndex);
                    savedChanges.add(new DeselectedChange(ListEvent.INSERT, i - selectionIndex));

                // it is being selected
                } else {
                    barcode.set(i, selected, 1);
                    int newSelectionIndex  = barcode.getColourIndex(i, selected);
                    selectedList.updates().addInsert(newSelectionIndex);
                    savedChanges.add(new DeselectedChange(ListEvent.DELETE, i - newSelectionIndex));
                }
            }
        }

        // notify listeners of changes to the selection list
        selectedList.updates().commitEvent();

        // Forward all of the collected changes made to the deselected list
        deselectedList.updates().beginEvent();
        for(int i = 0;i < savedChanges.size();i++) {
            DeselectedChange change = (DeselectedChange)savedChanges.get(i);
            change.fireChange();
        }
        savedChanges.clear();
        deselectedList.updates().commitEvent();

        // notify selection listeners
        if(minChangedIndex <= maxChangedIndex) fireSelectionChanged(minChangedIndex, maxChangedIndex);
    }

    /**
     * Register a {@link ca.odell.glazedlists.gui.SelectionListener SelectionListener}
     * that will be notified when selection is changed.
     */
    public void addSelectionListener(Listener selectionListener) {
        selectionListeners.add(selectionListener);
    }

    /**
     * Remove a {@link ca.odell.glazedlists.gui.SelectionListener SelectionListener}
     * so that it will no longer be notified when selection changes.
     */
    public void removeSelectionListener(Listener selectionListener) {
        selectionListeners.remove(selectionListener);
    }

    /**
     * Fire changes in selection to all registered listeners.
     */
    private void fireSelectionChanged(int start, int end) {
        // notify all
        for(Iterator i = selectionListeners.iterator(); i.hasNext(); ) {
            Listener listener = (Listener)i.next();
            listener.selectionChanged(start, end);
        }
    }

    /**
     * Disposes of this SelectionList freeing up it's resources for
     * garbage collection.  It is an error to use a SelectionList after
     * dispose() has been called.
     */
    public void dispose() {
        selectionListeners.clear();
        super.dispose();
    }

    /**
     * A generic interface to respond to changes in selection that doesn't
     * require including a particular GUI toolkit.
     */
    public interface Listener {

        /**
         * Notifies this SelectionListener of a change in selection.
         *
         * @param changeStart The first zero-relative index affected by a change in selection.
         * @param changeEnd   The last zero-relative index affected by a change in selection.
         */
        public void selectionChanged(int changeStart, int changeEnd);

    }

    /**
     * This is a bit of a hack, but it gets around the cannot start a new Event
     * while an Event is in progress expection from the ListEventAssembler
     */
    private final class DeselectedChange {

        /** the type of event corresponding to the types defined in ListEvent*/
        private int type = 0;

        /** the index the change occurred at */
        private int index = 0;

        /**
         * Creates a new DeselectedChange which bears a striking resemblance to
         * ListEvent.
         */
        public DeselectedChange(int type, int index) {
            this.type = type;
            this.index = index;
        }

        /**
         * Fires the change event on the Deselected list.
         */
        public void fireChange() {
            deselectedList.updates().addChange(type, index);
        }
    }

    /**
     * The {@link EventList} that contains only values that are not currently
     * selected.
     */
    private final class SelectedList extends TransformedList {

        /**
         * Creates an {@link EventList} that provides a view of the
         * deselected items in a SelectionList.
         */
        SelectedList(EventList source) {
            super(source);
        }

        /** {@inheritDoc} */
        public int size() {
            return barcode.colourSize(selected);
        }

        /** {@inheritDoc} */
        protected int getSourceIndex(int mutationIndex) {
            return barcode.getIndex(mutationIndex, selected);
        }

        /** {@inheritDoc} */
        public void listChanged(ListEvent listChanges) {
            // Do nothing as all state changes are handled in SelectionList.listChanged()
        }

        /**
         * This allows access to the EventAssembler for this list.
         */
        public ListEventAssembler updates() {
            return updates;
        }

        /**
         * A no-op dispose method to prevent the user from shooting themselves
         * in the foot.
         */
        public void dispose() {
            // Do Nothing
        }
    }

    /**
     * The {@link EventList} that contains only values that are not currently
     * selected.
     */
    private final class DeselectedList extends TransformedList {

        /**
         * Creates an {@link EventList} that provides a view of the
         * deselected items in a SelectionList.
         */
        DeselectedList(EventList source) {
            super(source);
        }

        /** {@inheritDoc} */
        public int size() {
            return barcode.colourSize(deselected);
        }

        /** {@inheritDoc} */
        protected int getSourceIndex(int mutationIndex) {
            return barcode.getIndex(mutationIndex, deselected);
        }

        /** {@inheritDoc} */
        public void listChanged(ListEvent listChanges) {
            // Do nothing as all state changes are handled in SelectionList.listChanged()
        }

        /**
         * This allows access to the EventAssembler for this list.
         */
        public ListEventAssembler updates() {
            return updates;
        }

        /**
         * A no-op dispose method to prevent the user from shooting themselves
         * in the foot.
         */
        public void dispose() {
            // Do Nothing
        }
    }
}