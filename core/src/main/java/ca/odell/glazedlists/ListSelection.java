/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.adt.Barcode;
import ca.odell.glazedlists.impl.adt.BarcodeIterator;
import ca.odell.glazedlists.matchers.Matcher;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class to provide index-based selection features. This class maintains two
 * lists derived from a single {@link EventList}:
 * <ul>
 *   <li>{@link #getSelected() Selected} - an {@link EventList} that contains only the selected values.</li>
 *   <li>{@link #getDeselected() Deselected} - an {@link EventList} that contains only the deselected values.</li>
 * </ul>
 *
 * <p>This design is intended to allow the sharing of selection logic between
 * all supported GUI toolkits as well as being available for use in non-GUI
 * applications and for index-based filtering.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class ListSelection<E> implements ListEventListener<E> {

    /**
     * A selection mode where at most one element may be selected at one time.
     * For convenience, this value equals {@link javax.swing.ListSelectionModel#SINGLE_SELECTION}.
     */
    public static final int SINGLE_SELECTION = 0;

    /**
     * A selection mode where at most one range of elements may be selected at one time.
     * For convenience, this value equals {@link javax.swing.ListSelectionModel#SINGLE_INTERVAL_SELECTION}.
     */
    public static final int SINGLE_INTERVAL_SELECTION = 1;

    /**
     * A selection mode where any element may be selected and elements added
     * adjacent to selected elements are selected. For convenience, this value
     * equals {@link javax.swing.ListSelectionModel#MULTIPLE_INTERVAL_SELECTION}.
     */
    public static final int MULTIPLE_INTERVAL_SELECTION = 2;

    /**
     * A selection mode where any element may be selected and freshly added elements
     * are always deselected. No equivalent policy exists in
     * {@link javax.swing.ListSelectionModel}
     */
    public static final int MULTIPLE_INTERVAL_SELECTION_DEFENSIVE = 103;

    /** the current selection colour */
    private static final Object SELECTED = Barcode.BLACK;

    /** the current deselection colour */
    private static final Object DESELECTED = Barcode.WHITE;

    /** the source list */
    private final EventList<E> source;

    /** the selected view */
    private SelectedList<E> selectedList;

    /** the deselected view */
    private DeselectedList<E> deselectedList;

    /** the toggling selected view */
    private SelectionToggleList<E> selectedToggleList;

    /** the toggling deselected view */
    private DeselectionToggleList<E> deselectedToggleList;

    /** the selection state */
    private Barcode barcode = new Barcode();

    /** the lead selection index */
    private int leadSelectionIndex = -1;

    /** the anchor selection index */
    private int anchorSelectionIndex = -1;

    /** the selection mode defines characteristics of the selection */
    private int selectionMode = MULTIPLE_INTERVAL_SELECTION_DEFENSIVE;

    /** the Matchers, if any, which decide whether a source element can be selected or not */
    private final Collection<Matcher<E>> validSelectionMatchers = new ArrayList<>();

    /**
     * the registered SelectionListeners; also used as a mutex to ensure we
     * build each of the following only once:
     *
     * <ul>
     *   <li>{@link #selectedList}</li>
     *   <li>{@link #deselectedList}</li>
     *   <li>{@link #selectedToggleList}</li>
     *   <li>{@link #deselectedToggleList}</li>
     * </ul>
     */
    private final CopyOnWriteArrayList<Listener> selectionListeners = new CopyOnWriteArrayList<>();

    /**
     * Creates a new ListSelection that listens to changes on the given source.
     * When using this constructor, all elements are deselected by default.
     */
    public ListSelection(EventList<E> source) {
        this.source = source;
        barcode.add(0, DESELECTED, source.size());
        source.addListEventListener(this);
    }

    /**
     * Creates a new ListSelection that listens to changes on the given source
     * and initializes selection with the given array of indices.
     */
    public ListSelection(EventList<E> source, int[] initialSelection) {
        this(source);
        select(initialSelection);
    }

    /**
     * Handle changes to the source list by adjusting our selection state and
     * the contents of the selected and deselected lists.
     */
    @Override
    public void listChanged(ListEvent<E> listChanges) {

        // keep track of what used to be selected
        final int minSelectionIndexBefore = getMinSelectionIndex();
        final int maxSelectionIndexBefore = getMaxSelectionIndex();

        boolean selectionChanged = false;

        // handle reordering events
        if(listChanges.isReordering()) {
            // prepare for the reordering event
            beginSelected();
            int[] sourceReorderMap = listChanges.getReorderMap();
            int[] selectReorderMap = new int[barcode.colourSize(SELECTED)];
            int[] deselectReorderMap = new int[barcode.colourSize(DESELECTED)];

            // adjust the flaglist & construct a reorder map to propagate
            Barcode previousBarcode = barcode;
            barcode = new Barcode();
            for(int c = 0; c < sourceReorderMap.length; c++) {
                Object flag = previousBarcode.get(sourceReorderMap[c]);
                boolean wasSelected = (flag != DESELECTED);
                barcode.add(c, flag, 1);
                if(wasSelected) {
                    int previousIndex = previousBarcode.getColourIndex(sourceReorderMap[c], SELECTED);
                    int currentIndex = barcode.getColourIndex(c, SELECTED);
                    selectReorderMap[currentIndex] = previousIndex;
                } else {
                    int previousIndex = previousBarcode.getColourIndex(sourceReorderMap[c], DESELECTED);
                    int currentIndex = barcode.getColourIndex(c, DESELECTED);
                    deselectReorderMap[currentIndex] = previousIndex;
                }
            }

            // adjust other internal state
            anchorSelectionIndex = -1;
            leadSelectionIndex = -1;

            // fire the reorder on the selected list
            addSelectedReorder(selectReorderMap);
            commitSelected();

            // fire the reorder on the deselected list
            beginDeselected();
            addDeselectedReorder(deselectReorderMap);
            commitDeselected();

            // all reordering events are assumed to have changed the selection
            selectionChanged = true;

        // handle non-reordering events
        } else {
            // prepare a sequence of changes
            beginAll();
            // for all changes update the barcode
            while(listChanges.next()) {
                int index = listChanges.getIndex();
                int changeType = listChanges.getType();

                // learn about what it was
                int previousSelectionIndex = barcode.getColourIndex(index, SELECTED);
                boolean previouslySelected = previousSelectionIndex != -1;

                // when an element is deleted, blow it away
                if(changeType == ListEvent.DELETE) {

                    // if the change occurred anywhere before the maxSelectionIndexBefore
                    // then it effects the current selection and we must fire a ListSelectionEvent
                    if (index <= maxSelectionIndexBefore)
                        selectionChanged = true;

                    // delete selected values
                    if(previouslySelected) {
                        barcode.remove(index, 1);
                        addSelectedDelete(previousSelectionIndex, listChanges.getOldValue());

                    // delete deselected values
                    } else {
                        int deselectedIndex = barcode.getColourIndex(index, DESELECTED);
                        addDeselectedDelete(deselectedIndex, listChanges.getOldValue());
                        barcode.remove(index, 1);
                    }

                // when an element is inserted, it is selected if its index was selected
                } else if(changeType == ListEvent.INSERT) {

                    // if the change occurred anywhere before the maxSelectionIndexBefore
                    // then it effects the current selection and we must fire a ListSelectionEvent
                    if (index <= maxSelectionIndexBefore)
                        selectionChanged = true;

                    // when selected, decide based on selection mode
                    if(previouslySelected) {

                        // select the inserted for single interval and multiple interval selection
                        if(selectionMode == SINGLE_INTERVAL_SELECTION
                        || selectionMode == MULTIPLE_INTERVAL_SELECTION) {
                            barcode.add(index, SELECTED, 1);
                            addSelectedInsert(previousSelectionIndex, listChanges.getNewValue());

                        // do not select the inserted for single selection and defensive selection
                        } else {
                            barcode.add(index, DESELECTED, 1);
                            int deselectedIndex = barcode.getColourIndex(index, DESELECTED);
                            addDeselectedInsert(deselectedIndex, listChanges.getNewValue());
                        }

                    // add a deselected value
                    } else {
                        barcode.add(index, DESELECTED, 1);
                        int deselectedIndex = barcode.getColourIndex(index, DESELECTED);
                        addDeselectedInsert(deselectedIndex, listChanges.getNewValue());
                    }

                // when an element is changed, assume selection stays the same
                } else if(changeType == ListEvent.UPDATE) {
                    // update a selected value
                    if(previouslySelected) {
                        addSelectedUpdate(previousSelectionIndex, listChanges.getOldValue(), listChanges.getNewValue());

                    // update a deselected value
                    } else {
                        int deselectedIndex = barcode.getColourIndex(index, DESELECTED);
                        addDeselectedUpdate(deselectedIndex, listChanges.getOldValue(), listChanges.getNewValue());
                    }
                }

                // adjust other internal state
                anchorSelectionIndex = adjustIndex(anchorSelectionIndex, changeType, index);
                leadSelectionIndex = adjustIndex(leadSelectionIndex, changeType, index);
            }
            commitAll();
        }

        // notify listeners of the selection change
        if(minSelectionIndexBefore != -1 && maxSelectionIndexBefore != -1 && selectionChanged) {
            final int minSelectionIndexAfter = getMinSelectionIndex();
            final int maxSelectionIndexAfter = getMaxSelectionIndex();

            int changeStart = minSelectionIndexBefore;
            int changeFinish = maxSelectionIndexBefore;

            if(minSelectionIndexAfter != -1 && minSelectionIndexAfter < changeStart) changeStart = minSelectionIndexAfter;
            if(maxSelectionIndexAfter != -1 && maxSelectionIndexAfter > changeFinish) changeFinish = maxSelectionIndexAfter;

            fireSelectionChanged(changeStart, changeFinish);
        }
    }

    /**
     * Add a matcher which decides when source elements are valid for selection.
     *
     * @param validSelectionMatcher returns <tt>true</tt> if a source elements
     *      can be selected; <tt>false</tt> otherwise
     */
    public void addValidSelectionMatcher(Matcher<E> validSelectionMatcher) {
        validSelectionMatchers.add(validSelectionMatcher);

        // walk through the existing selections deselecting anything that is no longer selectable
        for (int i = getMinSelectionIndex(), n = getMaxSelectionIndex(); i <= n; i++)
            if (isSelected(i) && !isSelectable(i))
                deselect(i);
    }

    /**
     * Remove a matcher which decides when source elements are valid for selection.
     *
     * @param validSelectionMatcher returns <tt>true</tt> if a source elements
     *      can be selected; <tt>false</tt> otherwise
     */
    public void removeValidSelectionMatcher(Matcher<E> validSelectionMatcher) {
        validSelectionMatchers.remove(validSelectionMatcher);
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
     * Gets an {@link EventList} that contains only selected values and modifies
     * the source list on mutation.
     *
     * Adding and removing items from this list performs the same operation on
     * the source list.
     */
    public EventList<E> getSelected() {
        synchronized (selectionListeners) {
            if(selectedList == null){
                selectedList = new SelectedList<>(source);
                source.getPublisher().setRelatedListener(selectedList, this);
            }
        }
        return selectedList;
    }

    /**
     * Gets an {@link EventList} that contains only selected values and modifies
     * the selection state on mutation.
     *
     * <p>Adding an item to this list selects it and removing an item deselects it.
     * If an item not in the source list is added an
     * {@link IllegalArgumentException} is thrown. This list does not support
     * the {@link List#set set} method.
     */
    public EventList<E> getTogglingSelected() {
        synchronized (selectionListeners) {
            if(selectedToggleList == null) {
                selectedToggleList = new SelectionToggleList<>(source);
                source.getPublisher().setRelatedListener(selectedToggleList, this);
            }
        }
        return selectedToggleList;
    }

    /**
     * Gets an {@link EventList} that contains only deselected values add
     * modifies the source list on mutation.
     *
     * Adding and removing items from this list performs the same operation on
     * the source list.
     */
    public EventList<E> getDeselected() {
        synchronized (selectionListeners) {
            if(deselectedList == null) {
                deselectedList = new DeselectedList<>(source);
                source.getPublisher().setRelatedListener(deselectedList, this);
            }
        }
        return deselectedList;
    }

    /**
     * Gets an {@link EventList} that contains only deselected values and
     * modifies the selection state on mutation.
     *
     * <p>Adding an item to this list deselects it and removing an item selects it.
     * If an item not in the source list is added an
     * {@link IllegalArgumentException} is thrown. This list does not support
     * the {@link List#set set} method.
     */
    public EventList<E> getTogglingDeselected() {
        synchronized (selectionListeners) {
            if(deselectedToggleList == null) {
                deselectedToggleList = new DeselectionToggleList<>(source);
                source.getPublisher().setRelatedListener(deselectedToggleList, this);
            }
        }
        return deselectedToggleList;
    }

    /**
     * Get the {@link EventList} that selection is being managed for.
     */
    public EventList<E> getSource() {
        return source;
    }

    /**
     * Inverts the current selection.
     */
    public void invertSelection() {
        // Clear the anchor and lead
        anchorSelectionIndex = -1;
        leadSelectionIndex = -1;

        // Update the selected list to reflect the selection inversion
        beginAll();
        for(BarcodeIterator i = barcode.iterator(); i.hasNext(); ) {
            Object color = i.next();
            E value = source.get(i.getIndex());
            int originalIndex = i.getColourIndex(color);

            if(color == SELECTED) {
                i.set(DESELECTED);
                int newIndex = i.getColourIndex(DESELECTED);
                addDeselectEvent(originalIndex, newIndex, value);

            } else {
                i.set(SELECTED);
                int newIndex = i.getColourIndex(SELECTED);
                addSelectEvent(newIndex, originalIndex, value);
            }
        }
        commitAll();

        // notify selection listeners that selection has been inverted
        fireSelectionChanged(0, source.size() - 1);
    }

    /**
     * Returns whether or not the item with the given source index
     * is selected.
     */
    public boolean isSelected(int sourceIndex) {
        if(sourceIndex < 0 || sourceIndex >= source.size()) {
            return false;
        }
        return barcode.getColourIndex(sourceIndex, SELECTED) != -1;
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

        // record the original anchor and lead if they are about to change
        final int oldAnchor = anchorSelectionIndex == start ? -1 : anchorSelectionIndex;
        final int oldLead = leadSelectionIndex == end ? -1 : leadSelectionIndex;

        // update anchor and lead
        anchorSelectionIndex = start;
        leadSelectionIndex = end;

        // alter selection accordingly
        setSubRangeOfRange(false, start, end, -1, -1, oldLead, oldAnchor);
    }

    /**
     * Deselects all of the elements in the given array of indices.  The
     * array must contain indices in sorted, ascending order.
     */
    public void deselect(int[] indices) {
        // keep track of the range of values that were affected
        int firstAffectedIndex = -1;
        int lastAffectedIndex = -1;

        // iterate through the barcode updating the selected list as you go
        beginAll();
        int currentIndex = 0;
        for(BarcodeIterator i = barcode.iterator(); i.hasNext() && currentIndex != indices.length;) {
            Object value = i.next();
            if(i.getIndex() == indices[currentIndex]) {
                // selection changed
                if(value == SELECTED) {
                    if(firstAffectedIndex == -1) firstAffectedIndex = i.getIndex();
                    lastAffectedIndex = i.getIndex();
                    addDeselectEvent(i);
                }
                currentIndex++;
            }
        }
        commitAll();

        // notify listeners of selection change
        if(firstAffectedIndex > -1) fireSelectionChanged(firstAffectedIndex, lastAffectedIndex);
    }

    /**
     * Deselect all elements.
     */
    public void deselectAll() {
        setAllColor(DESELECTED);
    }

    /**
     * @param color either selected or deselected.
     */
    private void setAllColor(Object color) {
        // if there is nothing selected, we're done
        Object oppositeColor = (color == SELECTED) ? DESELECTED : SELECTED;
        if(barcode.colourSize(oppositeColor) == 0) return;

        // keep track of the range of values that were affected
        int firstAffectedIndex = -1;
        int lastAffectedIndex = -1;

        // prepare the change events
        beginAll();
        for(BarcodeIterator i = barcode.iterator(); i.hasNextColour(oppositeColor);) {
            i.nextColour(oppositeColor);
            int index = i.getIndex();
            E value = source.get(index);
            if(color == SELECTED) {
                addDeselectedDelete(0, value);
                addSelectedInsert(index, value);
            } else {
                addSelectedDelete(0, value);
                addDeselectedInsert(index, value);
            }

            if(firstAffectedIndex == -1) firstAffectedIndex = index;
            lastAffectedIndex = index;
        }

        // reset barcode state
        barcode.clear();
        barcode.add(0, color, source.size());

        // fire events
        commitAll();
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

        // record the original anchor and lead if they are about to change
        final int oldAnchor = anchorSelectionIndex == start ? -1 : anchorSelectionIndex;
        final int oldLead = leadSelectionIndex == end ? -1 : leadSelectionIndex;

        // update anchor and lead
        anchorSelectionIndex = start;
        leadSelectionIndex = end;

        // alter selection accordingly
        setSubRangeOfRange(true, start, end, -1, -1, oldLead, oldAnchor);
    }

    /**
     * Selects all of the elements in the given array of indices.  The
     * array must contain indices in sorted, ascending order.
     */
    public void select(int[] indices) {

        // keep track of the range of values that were affected
        int firstAffectedIndex = -1;
        int lastAffectedIndex = -1;

        // iterate through the barcode updating the selected list as you go
        beginAll();
        int currentIndex = 0;
        for(BarcodeIterator i = barcode.iterator(); i.hasNext() && currentIndex != indices.length;) {
            Object value = i.next();
            if(i.getIndex() == indices[currentIndex]) {
                // selection changed
                if(value != SELECTED) {
                    if(firstAffectedIndex == -1) firstAffectedIndex = i.getIndex();
                    lastAffectedIndex = i.getIndex();
                    addSelectEvent(i);
                }
                currentIndex++;
            }
        }
        commitAll();

        // notify listeners of selection change
        if(firstAffectedIndex > -1) fireSelectionChanged(firstAffectedIndex, lastAffectedIndex);
    }

    /**
     * Select the specified element, if it exists.
     *
     * @return the index of the newly selected element, or -1 if no
     *     element was found.
     */
    public int select(E value) {
        int index = source.indexOf(value);
        if(index != -1) select(index);
        return index;
    }

    /**
     * Select all of the specified values.
     *
     * @return <code>true</code> if the selection changed as a result of the call.
     */
    public boolean select(Collection<E> values) {
        // This implementation leaves a lot to be desired. It's inefficient and
        // awkward. If possible, we should clean up the entire SelectionList so
        // we don't need to worry as much about the deselection list.

        // 1. Convert our Collection of values into a SortedSet of indices
        SortedSet<Integer> indicesToSelect = new TreeSet<>();
        for(Iterator<E> v = values.iterator(); v.hasNext(); ) {
            E value = v.next();
            int index = source.indexOf(value);
            if(index == -1) continue;
            indicesToSelect.add(new Integer(index));
        }
        if(indicesToSelect.isEmpty()) return false;

        // 2. convert the sorted set of Integers into an int[]
        int[] indicesToSelectAsInts = new int[indicesToSelect.size()];
        int arrayIndex = 0;
        for(Iterator<Integer> i = indicesToSelect.iterator(); i.hasNext(); ) {
            Integer selectIndex = i.next();
            indicesToSelectAsInts[arrayIndex] = selectIndex.intValue();
            arrayIndex++;
        }

        // 3. Delegate to the other method, and return true if the selection grew
        int selectionSizeBefore = getSelected().size();
        select(indicesToSelectAsInts);
        int selectionSizeAfter = getSelected().size();
        return selectionSizeAfter > selectionSizeBefore;
    }

    /**
     * Selects all elements.
     */
    public void selectAll() {
        setAllColor(SELECTED);
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

        // record the original anchor and lead if they are about to change
        final int oldAnchor = anchorSelectionIndex == start ? -1 : anchorSelectionIndex;
        final int oldLead = leadSelectionIndex == end ? -1 : leadSelectionIndex;

        // update anchor and lead
        anchorSelectionIndex = start;
        leadSelectionIndex = end;

        // alter selection accordingly
        setSubRangeOfRange(true, start, end, getMinSelectionIndex(), getMaxSelectionIndex(), oldLead, oldAnchor);
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

        // keep track of the range of values that were affected
        int firstAffectedIndex = -1;
        int lastAffectedIndex = -1;

        // iterate through the barcode updating the selected list as you go
        beginAll();
        int currentIndex = 0;
        for(BarcodeIterator i = barcode.iterator(); i.hasNext();) {
            Object value = i.next();
            // this element should be selected
            if(i.getIndex() == indices[currentIndex]) {
                // selection changed
                if(value != SELECTED) {
                    if(firstAffectedIndex == -1) firstAffectedIndex = i.getIndex();
                    lastAffectedIndex = i.getIndex();
                    addSelectEvent(i);
                }

                // look at the next value
                if(currentIndex < indices.length - 1) currentIndex++;

            // element was selected and isn't within the new selection
            } else if(value == SELECTED) {
                if(firstAffectedIndex == -1) firstAffectedIndex = i.getIndex();
                lastAffectedIndex = i.getIndex();
                addDeselectEvent(i);
            }
        }
        commitAll();

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
        // record the original anchor and lead if they are about to change
        final int oldAnchor = this.anchorSelectionIndex == anchorSelectionIndex ? -1 : anchorSelectionIndex;

        // update anchor
        this.anchorSelectionIndex = anchorSelectionIndex;

        // a value of -1 clears selection
        if(anchorSelectionIndex == -1 || leadSelectionIndex == -1) {
            deselectAll();

        // only the anchor should be selected
        } else if(selectionMode == SINGLE_SELECTION) {
            setSubRangeOfRange(true, anchorSelectionIndex, anchorSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex(), -1, oldAnchor);

        // select the interval between anchor and lead
        } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {
            setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex(), -1, oldAnchor);

        // select the interval between anchor and lead without deselecting anything
        } else {
            setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, -1, -1, -1, oldAnchor);
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
        // record the original anchor and lead if they are about to change
        final int oldLead = this.leadSelectionIndex == leadSelectionIndex ? -1 : leadSelectionIndex;

        // update lead
        int originalLeadIndex = this.leadSelectionIndex;
        this.leadSelectionIndex = leadSelectionIndex;

        // a value of -1 clears selection
        if(leadSelectionIndex == -1 || anchorSelectionIndex == -1) {
            deselectAll();

        // select only the lead
        } else if(selectionMode == SINGLE_SELECTION) {
            setSubRangeOfRange(true, leadSelectionIndex, leadSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex(), oldLead, -1);

        // select the interval between anchor and lead
        } else if(selectionMode == SINGLE_INTERVAL_SELECTION) {
            setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, getMinSelectionIndex(), getMaxSelectionIndex(), oldLead, -1);

        // select the interval between anchor and lead deselecting as necessary
        } else {
            setSubRangeOfRange(true, anchorSelectionIndex, leadSelectionIndex, anchorSelectionIndex, originalLeadIndex, oldLead, -1);
        }
    }

    private void addSelectedReorder(int[] selectReorderMap) {
        if(selectedList != null)  selectedList.updates().reorder(selectReorderMap);
        if(selectedToggleList != null) selectedToggleList.updates().reorder(selectReorderMap);
    }
    private void addDeselectedReorder(int[] deselectReorderMap) {
        if(deselectedList != null) deselectedList.updates().reorder(deselectReorderMap);
        if(deselectedToggleList != null) deselectedToggleList.updates().reorder(deselectReorderMap);
    }

    private void addSelectEvent(BarcodeIterator i) {
        E value = source.get(i.getIndex());
        int deselectedIndex = i.getColourIndex(DESELECTED);
        int selectedIndex = i.set(SELECTED);
        addSelectEvent(selectedIndex, deselectedIndex, value);
    }
    private void addSelectEvent(int selectIndex, int deselectIndex, E value) {
        addDeselectedDelete(deselectIndex, value);
        addSelectedInsert(selectIndex, value);
    }
    private void addDeselectEvent(BarcodeIterator i) {
        E value = source.get(i.getIndex());
        int selectedIndex = i.getColourIndex(SELECTED);
        int deselectedIndex = i.set(DESELECTED);
        addDeselectEvent(selectedIndex, deselectedIndex, value);
    }
    private void addDeselectEvent(int selectIndex, int deselectIndex, E value) {
        addSelectedDelete(selectIndex, value);
        addDeselectedInsert(deselectIndex, value);
    }

    private void addSelectedInsert(int index, E newValue){
        if(selectedList != null) selectedList.updates().elementInserted(index, newValue);
        if(selectedToggleList != null) selectedToggleList.updates().elementInserted(index, newValue);
    }
    private void addSelectedUpdate(int index, E oldValue, E newValue){
        if(selectedList != null) selectedList.updates().elementUpdated(index, oldValue, newValue);
        if(selectedToggleList != null) selectedToggleList.updates().elementUpdated(index, oldValue, newValue);
    }
    private void addSelectedDelete(int index, E oldValue){
        if(selectedList != null) selectedList.updates().elementDeleted(index, oldValue);
        if(selectedToggleList != null) selectedToggleList.updates().elementDeleted(index, oldValue);
    }
    private void addDeselectedInsert(int index, E value){
        if(deselectedList != null) deselectedList.updates().elementInserted(index, value);
        if(deselectedToggleList != null) deselectedToggleList.updates().elementInserted(index, value);
    }
    private void addDeselectedDelete(int index, E oldValue){
        if(deselectedList != null) deselectedList.updates().elementDeleted(index, oldValue);
        if(deselectedToggleList != null) deselectedToggleList.updates().elementDeleted(index, oldValue);
    }
    private void addDeselectedUpdate(int index, E oldValue, E newValue) {
        if(deselectedList != null) deselectedList.updates().elementUpdated(index, oldValue, newValue);
        if(deselectedToggleList != null) deselectedToggleList.updates().elementUpdated(index, oldValue, newValue);
    }


    private void beginAll() {
        beginSelected();
        beginDeselected();
    }
    private void commitAll() {
        commitSelected();
        commitDeselected();
    }
    private void beginSelected() {
        if(selectedList != null) {
            selectedList.updates().beginEvent();
        }
        if(selectedToggleList != null) {
            selectedToggleList.updates().beginEvent();
        }
    }
    private void commitSelected() {
        if(selectedList != null) {
            selectedList.updates().commitEvent();
        }
        if(selectedToggleList != null) {
            selectedToggleList.updates().commitEvent();
        }
    }
    private void beginDeselected() {
        if(deselectedList != null) deselectedList.updates().beginEvent();
        if(deselectedToggleList != null) deselectedToggleList.updates().beginEvent();
    }
    private void commitDeselected() {
        if(deselectedList != null) deselectedList.updates().commitEvent();
        if(deselectedToggleList != null) deselectedToggleList.updates().commitEvent();
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
        if(barcode.colourSize(SELECTED) == 0) return -1;
        return barcode.getIndex(0, SELECTED);
    }

    /**
     * Returns the last selected index or -1 if nothing is selected.
     */
    public int getMaxSelectionIndex() {
        if(barcode.colourSize(SELECTED) == 0) return -1;
        return barcode.getIndex(barcode.colourSize(SELECTED) - 1, SELECTED);
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
    private void setSubRangeOfRange(boolean select, int changeIndex0, int changeIndex1, int invertIndex0, int invertIndex1, int oldLead, int oldAnchor) {
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
        beginAll();

        // walk through the effected range updating selection
        for(int i = minUnionIndex; i <= maxUnionIndex; i++) {
            int selectionIndex = barcode.getColourIndex(i, SELECTED);
            boolean selectedBefore = (selectionIndex != -1);
            boolean inChangeRange = (i >= minChangeIndex && i <= maxChangeIndex);
            boolean selectedAfter = (inChangeRange == select) && isSelectable(i);

            // when there's a change
            if(selectedBefore != selectedAfter) {
                E value = source.get(i);

                // update change range
                if(i < minChangedIndex) minChangedIndex = i;
                if(i > maxChangedIndex) maxChangedIndex = i;

                // it is being deselected
                if(selectedBefore) {
                    barcode.set(i, DESELECTED, 1);
                    addDeselectEvent(selectionIndex, i - selectionIndex, value);

                // it is being selected
                } else {
                    barcode.set(i, SELECTED, 1);
                    int newSelectionIndex = barcode.getColourIndex(i, SELECTED);
                    addSelectEvent(newSelectionIndex, i - newSelectionIndex, value);
                }
            }
        }
        commitAll();

        // consider the original lead/anchor indexes, if any, when firing the "selection changed" event
        // (this forces a redraw of the old lead/anchor rows when the lead/anchor changes)
        if (oldLead != -1) {
            minChangedIndex = Math.min(minChangedIndex, oldLead);
            maxChangedIndex = Math.max(maxChangedIndex, oldLead);
        }

        if (oldAnchor != -1) {
            minChangedIndex = Math.min(minChangedIndex, oldAnchor);
            maxChangedIndex = Math.max(maxChangedIndex, oldAnchor);
        }

        // notify selection listeners
        if(minChangedIndex <= maxChangedIndex) fireSelectionChanged(minChangedIndex, maxChangedIndex);
    }

    /**
     * Checks the {@link #validSelectionMatchers} to determine if the value at
     * the given <code>index</code> is allowed to be selected.
     *
     * @param index the index to check
     * @return <tt>true</tt> if the index can be selected; <tt>false</tt>
     *      otherwise
     */
    private boolean isSelectable(int index) {
        // no matchers implies the index is selectable
        if (validSelectionMatchers.isEmpty())
            return true;

        // otherwise fetch the row object and validate it against all matchers
        final E rowObject = source.get(index);
        for (Iterator<Matcher<E>> iterator = validSelectionMatchers.iterator(); iterator.hasNext();)
            if (!iterator.next().matches(rowObject))
                return false;

        return true;
    }

    /**
     * Register a {@link ca.odell.glazedlists.ListSelection.Listener Listener}
     * that will be notified when selection is changed.
     */
    public void addSelectionListener(Listener selectionListener) {
    	if (selectionListener != null) {
    		selectionListeners.add(selectionListener);
    	}
    }

    /**
     * Remove a {@link ca.odell.glazedlists.ListSelection.Listener Listener}
     * so that it will no longer be notified when selection changes.
     */
    public void removeSelectionListener(Listener selectionListener) {
    	if (selectionListener != null) {
    		selectionListeners.remove(selectionListener);
    	}
    }

    /**
     * Fire changes in selection to all registered listeners.
     */
    private void fireSelectionChanged(int start, int end) {
        // notify all
        for(Iterator<Listener> i = selectionListeners.iterator(); i.hasNext(); ) {
            i.next().selectionChanged(start, end);
        }
    }

    /**
     * Disposes of this ListSelection freeing up it's resources for
     * garbage collection.  It is an error to use a ListSelection after
     * dispose() has been called.
     */
    public void dispose() {
        source.removeListEventListener(this);
        selectionListeners.clear();

        // detach the publisher dependencies
        if(selectedList != null) source.getPublisher().clearRelatedListener(selectedList, this);
        if(deselectedList != null) source.getPublisher().clearRelatedListener(deselectedList, this);
        if(selectedToggleList != null) source.getPublisher().clearRelatedListener(selectedToggleList, this);
        if(deselectedToggleList != null) source.getPublisher().clearRelatedListener(deselectedToggleList, this);
    }

    /**
     * A generic interface to respond to changes in selection that doesn't
     * require including a particular GUI toolkit.
     */
    @FunctionalInterface
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
     * The {@link EventList} that contains only values that are currently
     * selected.
     */
    private class SelectedList<E> extends TransformedList<E, E> {

        /**
         * Creates an {@link EventList} that provides a view of the
         * selected items in a ListSelection.
         */
        SelectedList(EventList<E> source) {
            super(source);
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return barcode.colourSize(SELECTED);
        }

        /** {@inheritDoc} */
        @Override
        protected int getSourceIndex(int mutationIndex) {
            return barcode.getIndex(mutationIndex, SELECTED);
        }

        /** {@inheritDoc} */
        @Override
        public void listChanged(ListEvent<E> listChanges) {
            // Do nothing as all state changes are handled in ListSelection.listChanged()
        }

        /**
         * This allows access to the EventAssembler for this list.
         */
        public ListEventAssembler<E> updates() {
            return updates;
        }

        /** {@inheritDoc} */
        @Override
        protected boolean isWritable() {
            return true;
        }

        /**
         * A no-op dispose method to prevent the user from shooting themselves
         * in the foot. To dispose a {@link ListSelection}, call
         * {@link ListSelection#dispose()} on that class directly.
         */
        @Override
        public void dispose() {
            // Do Nothing
        }
    }

    /**
     * A SelectedList that mutates the selection instead of the underlying list.
     */
    private class SelectionToggleList<E> extends SelectedList<E>{

        SelectionToggleList(EventList<E> source) {
            super(source);
        }

        /** @throws UnsupportedOperationException unconditionally */
        @Override
        public E set(int index, E item){
            throw new UnsupportedOperationException("Toggling lists don't support setting items");
        }

        /**
         * Select the specified value in the source list, regardless of its
         * index. If the given item is found in the source list, it is selected.
         *
         * @throws IllegalArgumentException if the element isn't found
         */
        @Override
        public void add(int index, E item) {
            index = source.indexOf(item);
            if(index != -1) {
                select(index);
            } else {
                throw new IllegalArgumentException("Added item " + item + " must be in source list");
            }
        }

        /**
         * Deselect the specified index.
         */
        @Override
        public E remove(int index){
            if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot remove at " + index + " on list of size " + size());
            int sourceIndex = getSourceIndex(index);
            deselect(sourceIndex);
            return source.get(sourceIndex);
        }
    }

    /**
     * The {@link EventList} that contains only values that are not currently
     * selected.
     */
    private class DeselectedList<E> extends TransformedList<E, E> {

        /**
         * Creates an {@link EventList} that provides a view of the
         * deselected items in a ListSelection.
         */
        DeselectedList(EventList<E> source) {
            super(source);
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return barcode.colourSize(DESELECTED);
        }

        /** {@inheritDoc} */
        @Override
        protected int getSourceIndex(int mutationIndex) {
            return barcode.getIndex(mutationIndex, DESELECTED);
        }

        /** {@inheritDoc} */
        @Override
        public void listChanged(ListEvent<E> listChanges) {
            // Do nothing as all state changes are handled in ListSelection.listChanged()
        }

        /**
         * This allows access to the EventAssembler for this list.
         */
        public ListEventAssembler<E> updates() {
            return updates;
        }

        /** {@inheritDoc} */
        @Override
        protected boolean isWritable() {
            return true;
        }

        /**
         * A no-op dispose method to prevent the user from shooting themselves
         * in the foot. To dispose a {@link ListSelection}, call
         * {@link ListSelection#dispose()} on that class directly.
         */
        @Override
        public void dispose() {
            // Do Nothing
        }
    }

    /**
     * A DeselectedList that mutates the selection instead of the underlying list.
     */
    private class DeselectionToggleList<E> extends DeselectedList<E>{

        DeselectionToggleList(EventList<E> source) {
            super(source);
        }

        /** @throws UnsupportedOperationException unconditionally */
        @Override
        public E set(int index, E item){
            throw new UnsupportedOperationException("Toggling lists don't support setting items");
        }

        /**
         * Deselect the specified value.
         *
         * @throws IllegalArgumentException if the element isn't found
         */
        @Override
        public void add(int index, E item) {
            index = source.indexOf(item);
            if(index != -1) {
                deselect(index);
            } else {
                throw new IllegalArgumentException("Added item " + item + " must be in source list");
            }
        }

        /**
         * Select the specified index.
         */
        @Override
        public E remove(int index){
            if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot remove at " + index + " on list of size " + size());
            int sourceIndex = getSourceIndex(index);
            select(sourceIndex);
            return source.get(sourceIndex);
        }
    }
}