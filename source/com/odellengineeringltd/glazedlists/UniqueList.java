/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists;

// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;
// for event list utilities, iterators and comparators
import com.odellengineeringltd.glazedlists.util.*;
// volatile implementation support
import com.odellengineeringltd.glazedlists.util.impl.*;
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
 * <p><strong>Note:</strong> When values are indistinguishable to the given
 * <code>Comparator</code> (or by using <code>Comparable</code> elements),
 * this list does not forward mandatory change events if those values are
 * added, removed, or updated in a way that they remain indistinguishable.
 * For example, if you have 5 String elements that are all equal to "B",
 * removing, or adding one does not result in a change event being forwarded.
 * This should be taken into consideration whenever making use of the
 * UniqueList.  It has been implemented this way because forwarding events when
 * an underlying Object is changed in the UniqueList (with no change in
 * uniqueness) would have non-deterministic results.
 *
 * <p>Though it might appear to be the case, these change events are also not
 * supported by "non-mandatory change events".  Non-mandatory change events
 * represent changes to the number of duplicates contained in the list.
 * Support for this type of change event has not yet been added to UniqueList
 * and will be unsupported by all main list types in GlazedLists.  The primary
 * use for these events is referenced in
 * <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=36">issue 36</a>.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class UniqueList extends MutationList implements ListEventListener, EventList {

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
        sortedSource.addListEventListener(this);
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
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     *
     * @return The number of elements in the list.
     */
    public int size() {
        return duplicatesList.getCompressedList().size();
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     *
     * @param index The index of the element to retrieve
     *
     * @return The element at the given index
     */
    public Object get(int index) {
        int sourceIndex = duplicatesList.getIndex(index);
        return source.get(sourceIndex);
    }

    /**
     * When the list is changed the change may create new duplicates or remove
     * duplicates.  The list is then altered to restore uniqeness and the events
     * describing the alteration of the unique view are forwarded on.
     *
     * @param listChanges The group of list changes to process
     */
    public void listChanged(ListEvent listChanges) {
        List nonUniqueInserts = new ArrayList();

        updates.beginEvent();

        while(listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();
            int changeResult = -1;

            // Process the event
            if(changeType == ListEvent.INSERT) {
                changeResult = processInsertEvent(changeIndex);
            } else if(changeType == ListEvent.DELETE) {
                processDeleteEvent(changeIndex);
            } else if(changeType == ListEvent.UPDATE) {
                changeResult = processUpdateEvent(changeIndex);
            }

            if(changeResult != -1) {
                nonUniqueInserts.add(new Integer(changeResult));
            }
        }

        // Clean up after potentially non unique insertions
        int uniqueInsertSize = nonUniqueInserts.size();
        for(int i = 0; i < uniqueInsertSize; i++) {
            int insertIndex = ((Integer)nonUniqueInserts.get(i)).intValue();
            guaranteeUniqueness(insertIndex);
        }

        updates.commitEvent();
    }

    /**
     * Called to handle all INSERT events
     *
     * @param changeIndex The index which the UPDATE event affects
     */
    private int processInsertEvent(int changeIndex) {
        if(valueIsDuplicate(changeIndex)) {
            // The element is a duplicate so add a nullity and forward a
            // non-mandatory change event since the number of duplicates changed.
            duplicatesList.add(changeIndex, null);
            int compressedIndex = duplicatesList.getCompressedIndex(changeIndex, true);
            addChange(ListEvent.UPDATE, compressedIndex, false);
            return -1;
        } else {
            // The value might not be a duplicate so add it to the unique view
            duplicatesList.add(changeIndex, Boolean.TRUE);
            return changeIndex;
        }
    }

    /**
     * Called to handle all DELETE events
     *
     * @param changeIndex The index which the UPDATE event affects
     */
    private void processDeleteEvent(int changeIndex) {
        // The element is a duplicate, the remove doesn't alter the unique view
        if(duplicatesList.get(changeIndex) == null) {
            // Remove and forward a non-mandatory change event since the number
            // of duplicates changed
            int compressedIndex = duplicatesList.getCompressedIndex(changeIndex, true);
            duplicatesList.remove(changeIndex);
            addChange(ListEvent.UPDATE, compressedIndex, false);
        // The element is in the unique view
        } else {
            int compressedIndex = duplicatesList.getCompressedIndex(changeIndex);
            if(changeIndex < duplicatesList.size() - 1 && duplicatesList.get(changeIndex + 1) == null) {
                // The next element is null and thus is a duplicate
                // Add it to the unique view and remove the current element.
                duplicatesList.set(changeIndex + 1, Boolean.TRUE);
                duplicatesList.remove(changeIndex);

                if(source.size() > compressedIndex) {
                    addChange(ListEvent.UPDATE, compressedIndex, false);
                }
            } else {
                // The element has no duplicates
                duplicatesList.remove(changeIndex);
                addChange(ListEvent.DELETE, compressedIndex, true);
            }
        }
    }

    /**
     * Called to handle all UPDATE events
     *
     * @param changeIndex The index which the UPDATE event affects
     */
    private int processUpdateEvent(int changeIndex) {
        if(duplicatesList.get(changeIndex) == null) {
            // Handle all cases where the change does not affect an element of the unique view
            return updateDuplicate(changeIndex);
        } else {
            // Handle all cases where the change does affect an element of the unique view
            return updateNonDuplicate(changeIndex);
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
    private int updateDuplicate(int changeIndex) {
        if(!valueIsDuplicate(changeIndex)) {
            // The nullity at this index is no longer valid
            duplicatesList.set(changeIndex, Boolean.TRUE);
            return changeIndex;
        } else {
            // Otherwise, it is still a duplicate and must be NULL, no event forwarded
            return -1;
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
    private int updateNonDuplicate(int changeIndex) {
        int compressedIndex = duplicatesList.getCompressedIndex(changeIndex);
        if(valueIsDuplicate(changeIndex)) {
            // The value at this index should be NULL as it is the same as previous
            // values in the list
            if(changeIndex < duplicatesList.size() - 1 && duplicatesList.get(changeIndex + 1) == null) {
                // The next element was previously a duplicate but should now
                // be in the unique view.
                duplicatesList.set(changeIndex + 1, Boolean.TRUE);
                duplicatesList.set(changeIndex, null);
                // Need to send non-mandatory updates for this and previous index
                // since a duplicate was added.  valueIsDuplicate() guarantees
                // that a previous value exists.
                addChange(ListEvent.UPDATE, compressedIndex, false);
                addChange(ListEvent.UPDATE, compressedIndex - 1, false);

            } else {
                // The element was unique and is now a duplicate
                duplicatesList.set(changeIndex, null);
                addChange(ListEvent.DELETE, compressedIndex, true);
            }
        } else {
            // this is still unique, but we must handle the follower
            if(changeIndex < duplicatesList.size() - 1) {

                // The next value was a duplicate before the UPDATE
                if(duplicatesList.get(changeIndex + 1) == null) {
                    // Set the next value to be non-null, forward an
                    // UPDATE event and return

                    // This could be OPTIMIZED:
                    // Example : D D D
                    // - causes 2 unnecessary non-mandatory UPDATEs when
                    // D -> D when 0 should be forwarded.
                    duplicatesList.set(changeIndex + 1, Boolean.TRUE);
                    addChange(ListEvent.UPDATE, compressedIndex, false);
                    return changeIndex;
                // The next value is in the unique view
                } else {
                    // Forward a remove for the current element and return.

                    // This could be OPTIMIZED:
                    // Example : B D D
                    // - This will cause a DELETE then an INSERT of the same
                    // value if B -> B
                    addChange(ListEvent.DELETE, compressedIndex, true);
                    return changeIndex;
                }
            // the follower does not exist
            } else {
                // The value is at the end of the list and thus has no duplicates
                addChange(ListEvent.UPDATE, compressedIndex, true);
            }
        }
        return -1;
    }

    /**
     * Guarantee that a value is unique with respect to its follower.
     *
     * @param changeIndex the index to inspect for duplicates.
     */
    private void guaranteeUniqueness(int changeIndex) {
        int compressedIndex = duplicatesList.getCompressedIndex(changeIndex, true);

        // Duplicate was created in the unique view, correct this
        if(compressedIndex < size() - 1 && 0 == comparator.compare(get(compressedIndex), get(compressedIndex + 1))) {
            int duplicateIndex = duplicatesList.getIndex(compressedIndex + 1);
            duplicatesList.set(duplicateIndex, null);
            addChange(ListEvent.UPDATE, compressedIndex, false);
        // Element has no duplicate follower
        } else {
            addChange(ListEvent.INSERT, compressedIndex, true);
        }
    }

    /**
     * Appends a change to the ListEventAssembler.
     *
     * <p>This is to handle the case where more verbosity could add value to
     * lists listening to changes on the unique list.
     *
     * @param index The index of the change
     * @param type The type of this change
     * @param mandatory Whether or not to propagate this change to all listeners
     */
    private void addChange(int type, int index, boolean mandatory) {
        if(mandatory) {
            updates.addChange(type, index);
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
        getReadWriteLock().writeLock().lock();
        try {
            if(!duplicatesList.isEmpty()) throw new IllegalStateException();

            for(int i = 0; i < source.size(); i++) {
                if(!valueIsDuplicate(i)) {
                    duplicatesList.add(i, Boolean.TRUE);
                } else {
                    duplicatesList.add(i, null);
                }
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
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
