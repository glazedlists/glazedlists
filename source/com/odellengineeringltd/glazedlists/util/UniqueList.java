/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * A UniqueList is a list that guarantees the uniqueness of its elements.
 *
 * <p>The goal of the UniqueList is to provide a simple and fast implementation
 * of a unique list view for a given list.
 *
 * <p>As such, this list is explicitly sorted via the provided Comparator or by
 * requiring all elements in the list to implement <code>Comparable</code>. This
 * allows the provision of uniquness without the need for exhaustive searches.
 * Also, this avoids having to define heuristics for unique entry ordering
 * (i.e. First Found, Last Found, First Occurrence, etc) which would add
 * a significant and unecessary level of complexity.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class UniqueList extends MutationList implements ListChangeListener, EventList {

    /** the comparator used to determine equality */
    private Comparator comparator;

    /** the sparse list tracks which elements are duplicates */
    private SparseList duplicatesList = new SparseList();

    /**
     * Creates a new UniqueList that determines uniqueness using the specified
     * comparator.
     *
     * @param source The EventList to use to populate the UniqueList
     * @param comparator The comparator to use for sorting
     */
    public UniqueList(EventList source, Comparator comparator) {
        super(new SortedList(source, comparator));
        SortedList sortedSource = (SortedList)super.source;
        this.comparator = sortedSource.getComparator();

        populateDuplicatesList();
        sortedSource.addListChangeListener(this);
    }

    /**
     * Creates a new UniqueList that determines uniqueness by relying on
     * all elements in the list implementing <code>Comparable</code>.
     *
     * @param source The EventList to use to populate the UniqueList
     */
    public UniqueList(EventList source) {
        this(source, new ComparableComparator());
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return The number of elements in the list.
     */
    public int size() {
        return duplicatesList.getCompressedList().size();
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index The index of the element to retrieve
     *
     * @return The element at the given index
     */
    public Object get(int index) {
        synchronized(getRootList()) {
            int sourceIndex = duplicatesList.getIndex(index);
            return source.get(sourceIndex);
        }
    }

    /**
     * When the list is changed the change may create new duplicates or remove
     * duplicates.  The list is then altered to restore uniqeness and the events
     * describing the alteration of the unique view are forwarded on.
     *
     * @param listChanges The group of list changes to process
     */
    public void notifyListChanges(ListChangeEvent listChanges) {

        updates.beginAtomicChange();

        while(listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            // Process the event
            if(changeType == ListChangeBlock.INSERT) {
                processInsertEvent(changeIndex);
            } else if(changeType == ListChangeBlock.DELETE) {
                processDeleteEvent(changeIndex);
            } else if(changeType == ListChangeBlock.UPDATE) {
                processUpdateEvent(changeIndex);
            }
        }

        updates.commitAtomicChange();
    }

    /**
     * Called to handle all INSERT events
     *
     * @param changeIndex The index which the UPDATE event affects
     */
    private void processInsertEvent(int changeIndex) {
        if(valueIsDuplicate(changeIndex)) {
            // The element is a duplicate so just add a nullity
            duplicatesList.add(changeIndex, null);
        } else {
            // The value is NOT a duplicate so add it to the unique view
            duplicatesList.add(changeIndex, Boolean.TRUE);
            // Guarantee this is unique with respect to its follower
            int compressedIndex = duplicatesList.getCompressedIndex(changeIndex);
            if(compressedIndex < size() - 1 && 0 == comparator.compare(get(compressedIndex), get(compressedIndex + 1))) {
                // Duplicate was created in the unique view, correct this
                int duplicateIndex = duplicatesList.getIndex(compressedIndex + 1);
                duplicatesList.set(duplicateIndex, null);
                appendChange(compressedIndex, ListChangeBlock.UPDATE, true);
            } else {
                // Element has no duplicate follower
                appendChange(compressedIndex, ListChangeBlock.INSERT, true);
            }
        }
    }

    /**
     * Called to handle all DELETE events
     *
     * <p>The delete event is currently <strong>broken!</strong> It fails
     * to forward update events due to lack of intersection support in
     * ListChangeSequence.
     *
     * @param changeIndex The index which the UPDATE event affects
     */
    private void processDeleteEvent(int changeIndex) {
        if(duplicatesList.get(changeIndex) == null) {
            // The element is a duplicate, the remove doesn't alter the unique view
            duplicatesList.remove(changeIndex);
        } else {
            // The element is in the unique view
            int compressedIndex = duplicatesList.getCompressedIndex(changeIndex);
            if(changeIndex < duplicatesList.size() - 1 && duplicatesList.get(changeIndex + 1) == null) {
                // The next element is null and thus is a duplicate
                // Add it to the unique view and remove the current element.
                duplicatesList.set(changeIndex + 1, Boolean.TRUE);
                duplicatesList.remove(changeIndex);

                /** HAVING THE FOLLOWING LINE UNCOMMENTED BREAKS clear()  */
                //appendChange(compressedIndex, ListChangeBlock.UPDATE, true);
            } else {
                // The element has no duplicates
                duplicatesList.remove(changeIndex);
                appendChange(compressedIndex, ListChangeBlock.DELETE, true);
            }
        }
    }

    /**
     * Called to handle all UPDATE events
     *
     * @param changeIndex The index which the UPDATE event affects
     */
    private void processUpdateEvent(int changeIndex) {
        if(duplicatesList.get(changeIndex) == null) {
            // Handle all cases where the change does not affect an element of the unique view
            updateDuplicate(changeIndex);
        } else {
            // Handle all cases where the change does affect an element of the unique view
            updateNonDuplicate(changeIndex);
        }
    }

    /**
     * Handles the UPDATE case where the element being updated was
     * previously a duplicate.
     *
     * <p>It affects the unique view with either an INSERT if the value is
     * unique, an UPDATE if the value creates a duplicate in the unique view or
     * no change if it is still a duplicate.
     *
     * @param changeIndex The index which the UPDATE event affects
     */
    private void updateDuplicate(int changeIndex) {
        if(!valueIsDuplicate(changeIndex)) {
            // The nullity at this index is no longer valid
            duplicatesList.set(changeIndex, Boolean.TRUE);
            int compressedIndex = duplicatesList.getCompressedIndex(changeIndex);
            // Guarantee this is unique with respect to its follower
            if(compressedIndex < size() - 1 && 0 == comparator.compare(get(compressedIndex), get(compressedIndex + 1))) {
                // Duplicate was created, make the first instance unique
                int duplicateIndex = duplicatesList.getIndex(compressedIndex + 1);
                duplicatesList.set(duplicateIndex, null);
                appendChange(compressedIndex, ListChangeBlock.UPDATE, true);
            } else {
                // The value is new and unique
                appendChange(compressedIndex, ListChangeBlock.INSERT, true);
            }
        } else {
            // Otherwise, it is still a duplicate and must be NULL, no event forwarded
        }
    }

    /**
     * Handles the UPDATE case where the element being updated was
     * previously in the unique view.
     *
     * <p>It affects the unique view with either an DELETE if the value is now
     * a duplicate, an UPDATE if the value creates a duplicate in the unique
     * view, or an INSERT if the value is unique.
     *
     * @param changeIndex The index which the UPDATE event affects
     */
    private void updateNonDuplicate(int changeIndex) {
        int compressedIndex = duplicatesList.getCompressedIndex(changeIndex);
        if(valueIsDuplicate(changeIndex)) {
            // The value at this index should be NULL as it is the same as previous
            // values in the list
            if(changeIndex < duplicatesList.size() - 1 && duplicatesList.get(changeIndex + 1) == null) {
                // The next element was previously a duplicate but should now
                // be in the unique view.
                duplicatesList.set(changeIndex + 1, Boolean.TRUE);
                duplicatesList.set(changeIndex, null);
                appendChange(compressedIndex, ListChangeBlock.UPDATE, true);
            } else {
                // The element was unique and is now a duplicate
                duplicatesList.set(changeIndex, null);
                appendChange(compressedIndex, ListChangeBlock.DELETE, true);
            }
        } else {
            // this is still unique, but we must handle the follower
            if(changeIndex < duplicatesList.size() - 1) {

                // the follower was a duplicate
                if(duplicatesList.get(changeIndex + 1) == null) {
                    // The next value was a duplicate before the UPDATE
                    if(!valueIsDuplicate(changeIndex + 1)) {
                        // The next value should be in the unique view
                        duplicatesList.set(changeIndex + 1, Boolean.TRUE);
                        appendChange(compressedIndex, ListChangeBlock.INSERT, true);
                        appendChange(compressedIndex + 1, ListChangeBlock.UPDATE, true);
                    } else {
                        // The next value is the same as this value
                        appendChange(compressedIndex, ListChangeBlock.UPDATE, true);
                    }
                // the follower was not a duplicate
                } else {
                    // The next value is in the unique view
                    if(valueIsDuplicate(changeIndex + 1)) {
                        // But the next value is the same, creating a duplicate
                        duplicatesList.set(changeIndex + 1, null);
                        appendChange(compressedIndex + 1, ListChangeBlock.UPDATE, true);
                        appendChange(compressedIndex, ListChangeBlock.DELETE, true);
                    } else {
                        // The next value is different so just update this value
                        appendChange(compressedIndex, ListChangeBlock.UPDATE, true);
                    }
                }
            // the follower does not exist
            } else {
                // The value is at the end of the list and thus has no duplicates
                appendChange(compressedIndex, ListChangeBlock.UPDATE, true);
            }
        }
    }

    /**
     * Appends a change to the ListChangeSequence.
     *
     * <p>This is to handle the case where more verbosity could add value to
     * lists listening to changes on the unique list.
     *
     * @param index The index of the change
     * @param type The type of this change
     * @param mandatory Whether or not to propagate this change to all listeners
     */
    private void appendChange(int index, int type, boolean mandatory) {
        if(mandatory) {
            updates.appendChange(index, type);
        } else {
            // Does nothing currently
            // This is a hook for overlaying the Bag ADT over top of the UniqueList
        }
    }

    /**
     * Populates the duplicates list by walking through the elements of the
     * source list and examining adjacent entries for equality.
     */
    private void populateDuplicatesList() {
        synchronized(getRootList()) {
            if(!duplicatesList.isEmpty()) throw new IllegalStateException();

            for(int i = 0; i < source.size(); i++) {
                if(!valueIsDuplicate(i)) {
                    duplicatesList.add(i, Boolean.TRUE);
                } else {
                    duplicatesList.add(i, null);
                }
            }
        }
    }

    /**
     * Tests if the specified value is a duplicate. It is a duplicate if and
     * only if it equals its immediate predecessor in the list.
     *
     * @param sourceIndex The index to check for duplicity
     *
     * @return true iff the preceeding value is deemed equal to the value at
     *         index by the comparator for this list.
     */
    private boolean valueIsDuplicate(int sourceIndex) {
        if(sourceIndex == 0) return false;
        if(sourceIndex >= source.size()) return false;
        return (0 == comparator.compare(source.get(sourceIndex - 1), source.get(sourceIndex)));
    }
}
