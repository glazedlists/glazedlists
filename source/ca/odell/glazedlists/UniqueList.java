/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// for event list utilities, iterators and comparators
import ca.odell.glazedlists.util.*;
// volatile implementation support
import ca.odell.glazedlists.util.impl.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * A UniqueList is a list that guarantees the uniqueness of its elements.
 *
 * <p>The goal of the UniqueList is to provide a simple and fast implementation
 * of a unique list view for a given list.
 *
 * <p>As such, this list is explicitly sorted via the provided Comparator or by
 * requiring all elements in the list to implement {@link Comparable}. This
 * allows the provision of uniquness without the need for exhaustive searches.
 * Also, this avoids having to define heuristics for unique entry ordering
 * (i.e. First Found, Last Found, First Occurrence, etc) which would add
 * a significant and unecessary level of complexity.
 *
 * <p><strong>Note:</strong> When values are indistinguishable to the given
 * {@link Comparator} (or by using {@link Comparable} elements),
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
public final class UniqueList extends TransformedList implements ListEventListener {

    /** the comparator used to determine equality */
    private Comparator comparator;

    /** the sparse list tracks which elements are duplicates */
    private SparseList duplicatesList = new SparseList();

    /** a count of the inserts to be processed by an active listChanged() method */
    private int updateIndexOffset = 0;

    /** duplicates list entries are either unique or duplicates */
    private static final Object UNIQUE = Boolean.TRUE;
    private static final Object DUPLICATE = null;

    /** some inserts are unique until they can be proven otherwise */
    private static final Object TEMP_UNIQUE = Boolean.FALSE;

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
        this.comparator = comparator;

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
     * Gets the index into the source list for the object with the specified
     * index in this list.
     */
    protected int getSourceIndex(int index) {
        return duplicatesList.getIndex(index);
    }

    /**
     * The UniqueList is writable.
     */
    protected boolean isWritable() {
        return true;
    }

    /**
     * When the list is changed the change may create new duplicates or remove
     * duplicates.  The list is then altered to restore uniqeness and the events
     * describing the alteration of the unique view are forwarded on.
     *
     * <p>The approach to handling list changes in UniqueList uses two passes
     * through the list of change events. In the first pass, the duplicates list
     * is updated to reflect the changes yet no events are fired. All inserted
     * values receive a value of TEMP_UNIQUE in the duplicates list. Deleted
     * and updated original duplicates list entries are stored in a temporary
     * array. Updated values' duplicates list entries are set to UNIQUE. In
     * pass 2, the change events are reviewed again. Inserted elements are tested
     * to see if the inserted value equals any elements in the source list that
     * existed prior to this change. If they are equal, the insert is not new
     * and an UPDATE is fired, otherwise the INSERT is fired. In either case, the
     * TEMP_UNIQUE is replaced with a UNIQUE in the duplicates list. For update events
     * the value is tested for equality with adjacent values, and the result may
     * be a forwarded DELETE, INSERT or UPDATE. Finally DELETED events may be
     * forwarded with a resultant DELETE or UPDATE event, depending on whether
     * the deleted value was unique.
     *
     *
     * @param listChanges The group of list changes to process
     */
    public void listChanged(ListEvent listChanges) {

        // divide the change event for two passes
        ListEvent firstPass = new ListEvent(listChanges);
        ListEvent secondPass = listChanges;

        // first pass, update unique list
        LinkedList removedValues = new LinkedList();
        while(firstPass.next()) {
            int changeIndex = firstPass.getIndex();
            int changeType = firstPass.getType();

            if(changeType == ListEvent.INSERT) {
                duplicatesList.add(changeIndex, TEMP_UNIQUE);
            } else if(changeType == ListEvent.UPDATE) {
                Object replaced = duplicatesList.get(changeIndex);
                // if the deleted value has a duplicate, remove the dup instead
                if(replaced == UNIQUE) {
                    if(changeIndex+1 < duplicatesList.size() && duplicatesList.get(changeIndex+1) == DUPLICATE) {
                        duplicatesList.set(changeIndex, TEMP_UNIQUE);
                        duplicatesList.set(changeIndex+1, UNIQUE);
                        replaced = null;
                    }
                }
                //removedValues.addLast(replaced);
            } else if(changeType == ListEvent.DELETE) {
                Object deleted = duplicatesList.remove(changeIndex);
                // if the deleted value has a duplicate, remove the dup instead
                if(deleted == UNIQUE) {
                    if(changeIndex < duplicatesList.size() && duplicatesList.get(changeIndex) == DUPLICATE) {
                        duplicatesList.set(changeIndex, UNIQUE);
                        deleted = null;
                    }
                }
                removedValues.addLast(deleted);
            }
        }


        // second pass, fire events
        updates.beginEvent();
        while(secondPass.next()) {
            int changeIndex = secondPass.getIndex();
            int changeType = secondPass.getType();

            // inserts can result in UPDATE or INSERT events
            if(changeType == ListEvent.INSERT) {
                boolean hasNeighbour = handleOldNeighbour(changeIndex);
                // finally fire the event
                if(hasNeighbour) {
                    addEvent(ListEvent.UPDATE, duplicatesList.getCompressedIndex(changeIndex, true), false);
                } else {
                    addEvent(ListEvent.INSERT, duplicatesList.getCompressedIndex(changeIndex), true);
                }
            // updates can result in INSERT, UPDATE or DELETE events
            } else if(changeType == ListEvent.UPDATE) {
                boolean wasUnique = (duplicatesList.get(changeIndex) == UNIQUE);
                boolean hasNeighbour = handleOldNeighbour(changeIndex);
                if(hasNeighbour) {
                    if(wasUnique) {
                        addEvent(ListEvent.DELETE, duplicatesList.getCompressedIndex(changeIndex, true), true);
                    } else {
                        addEvent(ListEvent.UPDATE, duplicatesList.getCompressedIndex(changeIndex, true), false);
                    }
                } else {
                    if(wasUnique) {
                        addEvent(ListEvent.UPDATE, duplicatesList.getCompressedIndex(changeIndex), true);
                    } else {
                        addEvent(ListEvent.INSERT, duplicatesList.getCompressedIndex(changeIndex), true);
                    }
                }
            // deletes can result in UPDATE or DELETE events
            } else if(changeType == ListEvent.DELETE) {
                boolean wasUnique = (removedValues.removeFirst() == UNIQUE);
                // calculate the compressed index where the delete occured
                int deletedIndex = -1;
                if(changeIndex < duplicatesList.size()) deletedIndex = duplicatesList.getCompressedIndex(changeIndex, true);
                else deletedIndex = duplicatesList.getCompressedList().size();
                // fire the change event
                if(wasUnique) {
                    addEvent(ListEvent.DELETE, deletedIndex, true);
                } else {
                    addEvent(ListEvent.UPDATE, deletedIndex, false);
                }
            }
        }

        updates.commitEvent();
    }

    /**
     * Handles whether this change index has a neighbour that existed prior
     * to a current change and that the values are equal. This adjusts the
     * duplicates list a found pair of such changes if they exist.
     *
     * @return true if a neighbour was found and the duplicates list has been
     *      updated in response. returns false if the specified index has no such
     *      neighbour. In this case the value at the specified index will be
     *      marked as unique (but not temporarily so).
     */
    private boolean handleOldNeighbour(int changeIndex) {
        // test if equal by value to predecessor which is always old
        if(valuesEqual(changeIndex-1, changeIndex)) {
            duplicatesList.set(changeIndex, DUPLICATE);
            return true;
        }

        // search for an old follower that is equal
        int followerIndex = changeIndex + 1;
        while(true) {
            // we have an equal follower
            if(valuesEqual(changeIndex, followerIndex)) {
                Object followerType = duplicatesList.get(followerIndex);
                // this insert equals the following insert, continue looking for an old match
                if(followerType == TEMP_UNIQUE) {
                    followerIndex++;
                // this insert equals an existing value, swap & update
                } else {
                    duplicatesList.set(changeIndex, UNIQUE);
                    duplicatesList.set(followerIndex, null);
                    return true;
                }
            // we have no equal follower that is old, this is a new value
            } else {
                duplicatesList.set(changeIndex, UNIQUE);
                return false;
            }
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
    private void addEvent(int type, int index, boolean mandatory) {
        if(mandatory) {
            // jesse, 23-june-04: the subtract of updateIndexOffset is experimental
            //updates.addChange(type, index);
            updates.addChange(type, index - updateIndexOffset);
        } else {
            // Does nothing currently
            // This is a hook for overlaying the Bag ADT over top of the UniqueList
        }
    }

    /**
     * Gets the count of repetitions of the value at the specified index.
     */
    public int getCount(int index) {
        // if this is before the end, its everything up to the first different element
        if(index < size() - 1) {
            return duplicatesList.getIndex(index + 1) - duplicatesList.getIndex(index);
        // if this is at the end, its everything after
        } else {
            return source.size() - duplicatesList.getIndex(index);
        }
    }

    /**
     * Gets the count of repetitions of the specified value.
     */
    public int getCount(Object value) {
        int index = indexOf(value);
        if(index == -1) return 0;
        return getCount(index);
    }

    /**
     * Gets a list of the repetitions of the value at the specified index.
     *
     * <p><strong>Warning:</strong> the returned list provides a view of the returned
     * data that is only valid until the next list change occurs. Therefore users of
     * this method should call it and do all of their access to it while continuously
     * holding this list's read lock. Holding the read lock will guarantee that
     * the result will not change while it is being read.
     */
    public List getAll(int index) {
        // if this is before the end, its everything up to the first different element
        if(index < size() - 1) {
            return source.subList(duplicatesList.getIndex(index), duplicatesList.getIndex(index + 1));
        // if this is at the end, its everything after
        } else {
            return source.subList(duplicatesList.getIndex(index), source.size());
        }
    }

    /**
     * Gets a list of the repetitions of the specified value.
     *
     * <p><strong>Warning:</strong> the returned list provides a view of the returned
     * data that is only valid until the next list change occurs. Therefore users of
     * this method should call it and do all of their access to it while continuously
     * holding this list's read lock. Holding the read lock will guarantee that
     * the result will not change while it is being read.
     */
    public List getAll(Object value) {
        int index = indexOf(value);
        if(index == -1) return Collections.EMPTY_LIST;
        return getAll(index);
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
                if(!valuesEqual(i, i-1)) {
                    duplicatesList.add(i, UNIQUE);
                } else {
                    duplicatesList.add(i, DUPLICATE);
                }
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Tests if the specified values match.
     *
     * @param index0 the first index to test
     * @param index1 the second index to test
     *
     * @return true iff the values at the specified index are equal. false if
     *      either index is out of range or the values are not equal.
     */
    private boolean valuesEqual(int index0, int index1) {
        if(index0 < 0 || index0 >= source.size()) return false;
        if(index1 < 0 || index1 >= source.size()) return false;
        return (0 == comparator.compare(source.get(index0), source.get(index1)));
    }

    /**
     * Removes the element at the specified index from the source list.
     *
     * <p>This has been modified for UniqueList in order to remove all of
     * the duplicate elements from the source list.
     */
    public Object remove(int index) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot remove at " + index + " on list of size " + size());

            // keep the removed object to return
            Object removed = get(index);

            // calculate the start (inclusive) and end (exclusive) of the range to remove
            int removeStart = getSourceIndex(index);
            int removeEnd = removeStart + getCount(index);

            // remove the range from the source list
            source.subList(removeStart, removeEnd).clear();

            // return the first of the removed objects
            return removed;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Removes the specified element from the source list.
     *
     * <p>This has been modified for UniqueList in order to remove all of
     * the duplicate elements from the source list.
     */
    public boolean remove(Object toRemove) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            int index = indexOf(toRemove);

            if(index == -1) return false;

            remove(index);
            return true;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Replaces the object at the specified index in the source list with
     * the specified value.
     *
     * <p>This removes all duplicates of the replaced value, then sets the
     * replacement value on the source list.
     *
     * <p><strong>Warning:</strong> Because the set() method is implemented
     * using <i>multiple</i> modifying calls to the source list, <i>multiple</i>
     * events will be propogated. Therefore this method has been carefully
     * implemented to keep this list in a consistent state for each such modifying
     * operation.
     */
    public Object set(int index, Object value) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot set at " + index + " on list of size " + size());

            // save the replaced value
            Object replaced = get(index);

            // wrap this update in a nested change set
            updates.beginEvent(true);

            // calculate the start (inclusive) and end (exclusive) of the duplicates to remove
            int removeStart = getSourceIndex(index) + 1;
            int removeEnd = removeStart + getCount(index) - 1;
            // remove the range from the source list if it is non-empty
            if(removeStart < removeEnd) {
                source.subList(removeStart, removeEnd).clear();
            }

            // replace the non-duplicate with the new value
            source.set(getSourceIndex(index), value);

            // commit the nested change set
            updates.commitEvent();

            return replaced;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Replaces the contents of this list with the contents of the specified
     * SortedSet. This walks through both lists in parallel in order to retain
     * objects that exist in both the current and the new revision.
     */
    public void replaceAll(SortedSet revision) {

        // skip these results if the set is null
        if(revision == null) return;

        // verify we are using consistent comparators
        if(revision.comparator() == null
            ? !(comparator instanceof ComparableComparator)
            : !(revision.comparator().equals(comparator))) {
            throw new IllegalArgumentException("SortedSet comparator " + revision.comparator() + " != " + comparator);
        }

        getReadWriteLock().writeLock().lock();
        try {

            // nest changes and let the other methods compose the event
            updates.beginEvent(true);

            // set up the current objects to examine
            int originalIndex = 0;
            Object originalElement = getOrNull(this, originalIndex);

            // for all elements in the revised set
            for(Iterator revisionIterator = revision.iterator(); revisionIterator.hasNext(); ) {
                Object revisionElement = revisionIterator.next();

                // when the before list holds items smaller than the after list item,
                // the before list items are out-of-date and must be deleted
                while(originalElement != null && comparator.compare(originalElement, revisionElement) < 0) {
                    remove(originalIndex);
                    // replace the original element
                    originalElement = getOrNull(this, originalIndex);
                }

                // when the before list holds an item identical to the after list item,
                // the item has not changed
                if(originalElement != null && comparator.compare(originalElement, revisionElement) == 0) {
                    set(originalIndex, revisionElement);
                    // replace the original element
                    originalIndex++;
                    originalElement = getOrNull(this, originalIndex);

                // when the before list holds no more items or an item that is larger than
                // the current after list item, insert the after list item
                } else {
                    add(originalIndex, revisionElement);
                    // adjust the index of the original element
                    originalIndex++;
                }
            }

            // when the before list holds items larger than the largest after list item,
            // the before list items are out-of-date and must be deleted
            while(originalIndex < size()) {
                remove(originalIndex);
            }

            // fire the composed event
            updates.commitEvent();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Gets the element at the specified index of the specified list
     * or null if the list is too small.
     */
    private Object getOrNull(List source, int index) {
        if(index < source.size()) return source.get(index);
        else return null;
    }


    /**
     * Returns true if this list contains the specified element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */

    public boolean contains(Object object) {
        return (indexOf(object) != -1);
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int indexOf(Object object) {
        int sourceIndex = source.indexOf(object);
        if(sourceIndex == -1) return -1;
        return duplicatesList.getCompressedIndex(sourceIndex, true);
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.  Since
     * uniqueness is guaranteed for this list, the value returned by this
     * method will always be indentical to calling <code>indexOf()</code>.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int lastIndexOf(Object object) {
        return indexOf(object);
    }

    /**
     * Release the resources consumed by this TransformedList so that it may be garbage
     * collected. It is an error to call any method on a TransformedList after it
     * has been disposed.
     *
     * <p>This implementation of UniqueList extends dispose() in order to first
     * dispose the SortedList on which this implementation depends.
     */
    public void dispose() {
        ((TransformedList)source).dispose();
        super.dispose();
    }
}