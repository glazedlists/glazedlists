/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// volatile implementation support
import ca.odell.glazedlists.impl.adt.*;
import ca.odell.glazedlists.impl.sort.*;
import ca.odell.glazedlists.util.concurrent.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * An {@link EventList} that shows the unique elements from its source
 * {@link EventList}.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>88 bytes per element, plus 52 bytes per unique element</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class UniqueList extends TransformedList {

    /** the comparator used to determine equality */
    private Comparator comparator;

    /** the sparse list tracks which elements are duplicates */
    private Barcode duplicatesList = new Barcode();

    /** duplicates list entries are either unique or duplicates */
    private static final Object UNIQUE = Barcode.BLACK;
    private static final Object DUPLICATE = Barcode.WHITE;

    /** the index of an update event that has not yet been added to the change */
    private int pendingUpdateIndex = -1;

    /** the index of the add event that was most recently added to the change */
    private int lastInsertIndex = -1;

    /** whether changes in an objects count should be fired as events */
    private boolean includeCountChangeEvents = false;

    /** for tracking which neighbours of an element are equal */
    private static final int NEIGHBOUR_LEFT = -1;
    private static final int NEIGHBOUR_NONE = 0;
    private static final int NEIGHBOUR_RIGHT = 1;

    /**
     * Creates a {@link UniqueList} that determines uniqueness using the specified
     * {@link Comparator}.
     *
     * @param source The {@link EventList} containing duplicates to remove.
     * @param comparator The {@link Comparator} used to determine equality.
     */
    public UniqueList(EventList source, Comparator comparator) {
        // <p>As such, this list is explicitly sorted via the provided Comparator or by
        // requiring all elements in the list to implement {@link Comparable}. This
        // allows the provision of uniquness without the need for exhaustive searches.
        // Also, this avoids having to define heuristics for unique entry ordering
        // (i.e. First Found, Last Found, First Occurrence, etc) which would add
        // a significant and unecessary level of complexity.
        //
        // <p><strong>Note:</strong> When values are indistinguishable to the given
        // {@link Comparator} (or by using {@link Comparable} elements),
        // this list does not forward mandatory change events if those values are
        // added, removed, or updated in a way that they remain indistinguishable.
        // For example, if you have 5 String elements that are all equal to "B",
        // removing, or adding one does not result in a change event being forwarded.
        // This should be taken into consideration whenever making use of the
        // UniqueList.  It has been implemented this way because forwarding events when
        // an underlying Object is changed in the UniqueList (with no change in
        // uniqueness) would have non-deterministic results.
        super(new SortedList(source, comparator));
        SortedList sortedSource = (SortedList)super.source;
        this.comparator = comparator;

        populateDuplicatesList();
        sortedSource.addListEventListener(this);
    }

    /**
     * Creates a {@link UniqueList} that determines uniqueness via the
     * {@link Comparable} interface. All elements of the source {@link EventList}
     * must impelement {@link Comparable}.
     */
    public UniqueList(EventList source) {
        this(source, GlazedLists.comparableComparator());
    }

    /** {@inheritDoc} */
    public int size() {
        return duplicatesList.blackSize();
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int index) {
        return duplicatesList.getIndex(index, Barcode.BLACK);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // When the list is changed the change may create new duplicates or remove
        // duplicates.  The list is then altered to restore uniqeness and the events
        // describing the alteration of the unique view are forwarded on.
        //
        // <p>The approach to handling list changes in UniqueList uses two passes
        // through the list of change events. In the first pass, the duplicates list
        // is updated to reflect the changes yet no events are fired. All inserted
        // values receive a value of null in the duplicates list. Deleted
        // and updated original duplicates list entries are stored in a temporary
        // array. Updated values' duplicates list entries are set to UNIQUE. In
        // pass 2, the change events are reviewed again. Inserted elements are tested
        // to see if the inserted value equals any elements in the source list that
        // existed prior to this change. If they are equal, the insert is not new
        // and an UPDATE is fired, otherwise the INSERT is fired. In either case, the
        // null is replaced with a UNIQUE in the duplicates list. For update events
        // the value is tested for equality with adjacent values, and the result may
        // be a forwarded DELETE, INSERT or UPDATE. Finally DELETED events may be
        // forwarded with a resultant DELETE or UPDATE event, depending on whether
        // the deleted value was unique.

        // a Barcode to track values that could be unique but it can't be
        // determined until a later pass through the change events.
        Barcode tempUniqueList = new Barcode();
        tempUniqueList.addWhite(0, duplicatesList.size());

        // first pass, update unique list
        LinkedList removedValues = new LinkedList();
        while(listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            if(changeType == ListEvent.INSERT) {
                duplicatesList.addBlack(changeIndex, 1);
                tempUniqueList.addBlack(changeIndex, 1);

            } else if(changeType == ListEvent.UPDATE) {
                Object replaced = duplicatesList.get(changeIndex);
                // if the deleted value has a duplicate, remove the dup instead
                if(replaced == UNIQUE) {
                    if(changeIndex+1 < duplicatesList.size() && duplicatesList.get(changeIndex+1) == DUPLICATE) {
                        duplicatesList.setBlack(changeIndex, 2);
                        tempUniqueList.setBlack(changeIndex, 1);
                    }
                }

            } else if(changeType == ListEvent.DELETE) {
                Object deleted = duplicatesList.get(changeIndex);
                duplicatesList.remove(changeIndex, 1);
                tempUniqueList.remove(changeIndex, 1);
                // if the deleted value has a duplicate, remove the dup instead
                if(deleted == UNIQUE) {
                    if(changeIndex < duplicatesList.size() && duplicatesList.get(changeIndex) == DUPLICATE) {
                        duplicatesList.setBlack(changeIndex, 1);
                        deleted = null;
                    }
                }
                removedValues.addLast(deleted);
            }
        }

        // second pass, fire events
        updates.beginEvent();
        listChanges.reset();
        while(listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            // inserts can result in UPDATE or INSERT events
            if(changeType == ListEvent.INSERT) {
                int neighbourSide = handleOldNeighbour(changeIndex, tempUniqueList);
                // finally fire the event
                if(neighbourSide != NEIGHBOUR_NONE) {
                    enqueueEvent(ListEvent.UPDATE, duplicatesList.getBlackIndex(changeIndex, true), false);
                } else {
                    enqueueEvent(ListEvent.INSERT, duplicatesList.getBlackIndex(changeIndex), true);
                }
            // updates can result in INSERT, UPDATE or DELETE events
            } else if(changeType == ListEvent.UPDATE) {

                // get the previous state, the side where duplicates were found
                int oldNeighbourSide = 0;
                Object updated = duplicatesList.get(changeIndex);
                if(tempUniqueList.get(changeIndex) == Barcode.BLACK) updated = null;
                if(updated == DUPLICATE) oldNeighbourSide = NEIGHBOUR_LEFT;
                else if(updated == null) oldNeighbourSide = NEIGHBOUR_RIGHT;
                else if(updated == UNIQUE) oldNeighbourSide = NEIGHBOUR_NONE;

                // get the current state, the side where a duplicate value is found
                int newNeighbourSide = handleOldNeighbour(changeIndex, tempUniqueList);

                // fire events
                int compressedIndex = duplicatesList.getBlackIndex(changeIndex, true);

                // we're unique now
                if(newNeighbourSide == NEIGHBOUR_NONE) {
                    if(oldNeighbourSide == NEIGHBOUR_NONE) {
                        enqueueEvent(ListEvent.UPDATE, compressedIndex, true);
                    } else if(oldNeighbourSide == NEIGHBOUR_LEFT) {
                        enqueueEvent(ListEvent.UPDATE, compressedIndex-1, false);
                        enqueueEvent(ListEvent.INSERT, compressedIndex, true);
                    } else if(oldNeighbourSide == NEIGHBOUR_RIGHT) {
                        enqueueEvent(ListEvent.INSERT, compressedIndex, true);
                        enqueueEvent(ListEvent.UPDATE, compressedIndex+1, false);
                    }

                // we are a duplicate of a previous element
                } else if(newNeighbourSide == NEIGHBOUR_LEFT) {
                    if(oldNeighbourSide == NEIGHBOUR_NONE) {
                        enqueueEvent(ListEvent.UPDATE, compressedIndex, false);
                        enqueueEvent(ListEvent.DELETE, compressedIndex+1, true);
                    } else if(oldNeighbourSide == NEIGHBOUR_LEFT) {
                        enqueueEvent(ListEvent.UPDATE, compressedIndex, false);
                    } else if(oldNeighbourSide == NEIGHBOUR_RIGHT) {
                        enqueueEvent(ListEvent.UPDATE, compressedIndex, false);
                        // if(compressedIndex+1 < duplicatesList.getCompressedList().size()) enqueueEvent(ListEvent.UPDATE, compressedIndex+1, false);
                        if(compressedIndex+1 < duplicatesList.blackSize()) enqueueEvent(ListEvent.UPDATE, compressedIndex+1, false);
                    }

                // we have a duplicate that follows
                } else if(newNeighbourSide == NEIGHBOUR_RIGHT) {
                    if(oldNeighbourSide == NEIGHBOUR_NONE) {
                        enqueueEvent(ListEvent.DELETE, compressedIndex, true);
                        enqueueEvent(ListEvent.UPDATE, compressedIndex, false);
                    } else if(oldNeighbourSide == NEIGHBOUR_LEFT) {
                        if(compressedIndex-1 >= 0) enqueueEvent(ListEvent.UPDATE, compressedIndex-1, false);
                        enqueueEvent(ListEvent.UPDATE, compressedIndex, false);
                    } else if(oldNeighbourSide == NEIGHBOUR_RIGHT) {
                        enqueueEvent(ListEvent.UPDATE, compressedIndex, false);
                    }
                }

            // deletes can result in UPDATE or DELETE events
            } else if(changeType == ListEvent.DELETE) {
                Object deleted = removedValues.removeFirst();
                // calculate the uncompressed index where the delete occured
                int uncompressedIndex = -1;
                if(deleted == DUPLICATE) uncompressedIndex = changeIndex - 1;
                else uncompressedIndex = changeIndex;
                // calculate the compressed index where the delete occured
                int deletedIndex = -1;
                if(uncompressedIndex < duplicatesList.size()) deletedIndex = duplicatesList.getBlackIndex(uncompressedIndex, true);
                else deletedIndex = duplicatesList.blackSize();
                // fire the change event
                if(deleted == UNIQUE) {
                    enqueueEvent(ListEvent.DELETE, deletedIndex, true);
                } else {
                    enqueueEvent(ListEvent.UPDATE, deletedIndex, false);
                }
            }
        }
        flushEnqueuedEvents();
        updates.commitEvent();
    }

    /**
     * Handles whether this change index has a neighbour that existed prior
     * to a current change and that the values are equal. This adjusts the
     * duplicates list a found pair of such changes if they exist.
     *
     * @return NEIGHBOUR_NONE if no neighbour was found. In this case the value at the specified
     *      index will be marked as unique, but not temporarily so. Returns NEIGHBOUR_LEFT if
     *      a neighbour was found on the left, and NEIGHBOUR_RIGHT if a neighbour was found on the
     *      right. In non-zero cases the duplicates list is updated.
     */
    private int handleOldNeighbour(int changeIndex, Barcode tempUniqueList) {
        // test if equal by value to predecessor which is always old
        if(valuesEqual(changeIndex-1, changeIndex)) {
            duplicatesList.setWhite(changeIndex, 1);
            return NEIGHBOUR_LEFT;
        }

        // search for an old follower that is equal
        int followerIndex = changeIndex + 1;
        while(true) {
            // we have an equal follower
            if(valuesEqual(changeIndex, followerIndex)) {
                Object followerType;
                if(tempUniqueList.get(followerIndex) == Barcode.BLACK) {
                    followerType = null;
                } else {
                    followerType = duplicatesList.get(followerIndex);
                }
                // this insert equals the following insert, continue looking for an old match
                if(followerType == null) {
                    followerIndex++;
                // this insert equals an existing value, swap & update
                } else {
                    duplicatesList.setBlack(changeIndex, 1);
                    duplicatesList.setWhite(followerIndex, 1);
                    return NEIGHBOUR_RIGHT;
                }
            // we have no equal follower that is old, this is a new value
            } else {
                duplicatesList.setBlack(changeIndex, 1);
                return NEIGHBOUR_NONE;
            }
        }
    }

    /**
     * Appends a change to the ListEventAssembler. If the change is an UPDATE,
     * the index is queued and not added to the change event immediately. This
     * allows UPDATE events to be discarded if they intersect with an INSERT or
     * REMOVE event. Because of this queueing, it is always necessary to call
     * {@link #flushPendingEvents()} before {@link #commitEvent()} to guarantee
     * that all UPDATE events have been sent.
     *
     * @param index The index of the change
     * @param type The type of this change
     * @param mandatory Whether or not to propagate this change to all listeners
     */
    private void enqueueEvent(int type, int index, boolean mandatory) {
        if(mandatory || includeCountChangeEvents) {
            // fire queued update events if necessary
            if(pendingUpdateIndex != -1 && pendingUpdateIndex != index) {
                updates.addChange(ListEvent.UPDATE, pendingUpdateIndex);
            }
            pendingUpdateIndex = -1;

            // queue update events
            if(type == ListEvent.UPDATE && index != lastInsertIndex) {
                pendingUpdateIndex = index;

            // fire insert events immediately and save the inserted index
            } else if(type == ListEvent.INSERT) {
                lastInsertIndex = index;
                updates.addChange(type, index);

            // fire remove events immediately
            } else if(type == ListEvent.DELETE) {
                updates.addChange(type, index);
            }
        }
    }

    /**
     * Flush any events that were not fired immediately.
     */
    private void flushEnqueuedEvents() {
        if(pendingUpdateIndex != -1) {
            updates.addChange(ListEvent.UPDATE, pendingUpdateIndex);
        }
        pendingUpdateIndex = -1;
        lastInsertIndex = -1;
    }

    /**
     * Configure this UniqueList to fire count change events. Because this method
     * affects how all listeners receive their change events, it should only be called
     * if there is a single listener to the UniqueList.
     */
    void setFireCountChangeEvents(boolean includeCountChangeEvents) {
        this.includeCountChangeEvents = includeCountChangeEvents;
    }

    /**
     * Gets the number of duplicates of the value found at the specified index.
     */
    public int getCount(int index) {
        // if this is before the end, its everything up to the first different element
        if(index < size() - 1) {
            return duplicatesList.getIndex(index + 1, Barcode.BLACK) - duplicatesList.getIndex(index, Barcode.BLACK);
        // if this is at the end, its everything after
        } else {
            return source.size() - duplicatesList.getIndex(index, Barcode.BLACK);
        }
    }

    /**
     * Gets the number of duplicates of the specified value.
     */
    public int getCount(Object value) {
        int index = indexOf(value);
        if(index == -1) return 0;
        return getCount(index);
    }

    /**
     * Gets a {@link List} of the duplicates of the value at the specified index.
     *
     * <p><strong>Warning:</strong> the returned {@link List} is only valid until
     * the next list change occurs. If this {@link UniqueList} is shared between
     * multiple threads, it is necessary to have aquired the {@link ReadWriteLock}
     * while accessing the returned {@link List}.
     */
    public List getAll(int index) {
        // if this is before the end, its everything up to the first different element
        if(index < size() - 1) {
            return source.subList(duplicatesList.getIndex(index, Barcode.BLACK), duplicatesList.getIndex(index + 1, Barcode.BLACK));
        // if this is at the end, its everything after
        } else {
            return source.subList(duplicatesList.getIndex(index, Barcode.BLACK), source.size());
        }
    }

    /**
     * Gets a {@link List} of the duplicates of the specified value.
     *
     * <p><strong>Warning:</strong> the returned {@link List} is only valid until
     * the next list change occurs. If this {@link UniqueList} is shared between
     * multiple threads, it is necessary to have aquired the {@link ReadWriteLock}
     * while accessing the returned {@link List}.
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
        if(!duplicatesList.isEmpty()) throw new IllegalStateException();

        for(int i = 0; i < source.size(); i++) {
            if(!valuesEqual(i, i-1)) {
                duplicatesList.addBlack(i, 1);
            } else {
                duplicatesList.addWhite(i, 1);
            }
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

    /** {@inheritDoc} */
    public Object remove(int index) {
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
    }

    /** {@inheritDoc} */
    public boolean remove(Object toRemove) {
        if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
        int index = indexOf(toRemove);

        if(index == -1) return false;

        remove(index);
        return true;
    }

    /** {@inheritDoc} */
    public Object set(int index, Object value) {
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
    }

    /**
     * Replaces the contents of this {@link UniqueList} with the contents of the
     * specified {@link SortedSet}. If this {@link UniqueList} uses a {@link Comparator}
     * to determine equality of elements, the specified {@link SortedList} must use
     * an equal {@link Comparator}.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
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
    }

    /**
     * Gets the element at the specified index of the specified list
     * or null if the list is too small.
     */
    private Object getOrNull(List source, int index) {
        if(index < source.size()) return source.get(index);
        else return null;
    }


    /** {@inheritDoc} */
    public boolean contains(Object object) {
        return (indexOf(object) != -1);
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        int sourceIndex = source.indexOf(object);
        if(sourceIndex == -1) return -1;
        return duplicatesList.getBlackIndex(sourceIndex, true);
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object object) {
        return indexOf(object);
    }

    /** {@inheritDoc} */
    public void dispose() {
        SortedList sortedSource = (SortedList)source;
        super.dispose();
        sortedSource.dispose();
    }
}