/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.adt.Barcode;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * This helper class manages the groups created by dividing up a
 * {@link SortedList} using a {@link Comparator}. This class uses a delegate
 * interface {@link Client} to fire the appropriate events as groups are
 * inserted, updated and changed.
 *
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Grouper<E> {

    /** Entries in barcode are one of these constants to indicate if the element at an index is a UNIQUE or DUPLICATE. */
    public static final Object UNIQUE = Barcode.BLACK;
    public static final Object DUPLICATE = Barcode.WHITE;

    /** Used only in temporary data structures to flag deleting of the FIRST group element when more elements exist. */
    private static final Object UNIQUE_WITH_DUPLICATE = null;

    /**
     * A temporary barcode data structure is created when processing ListEvents in this GroupingList.
     * These constants identify which indexes remain to be processed.
     */
    private static final Object TODO = Barcode.BLACK;
    private static final Object DONE = Barcode.WHITE;

    /** For reporting which side of an element its group is located. */
    private static final int LEFT_GROUP = -1;
    private static final int NO_GROUP = 0;
    private static final int RIGHT_GROUP = 1;

    /** the sorted source of the grouping service */
    private SortedList<E> sortedList;

    /** The comparator used to determine the groups. */
    private Comparator<? super E> comparator;

    /** the grouping list client to notify of group changes */
    private Client<E> client;

    /**
     * The data structure which tracks which source elements are considered UNIQUE
     * (the first element of a group) and which are DUPLICATE (any group element NOT the first).
     */
    private Barcode barcode;

    /**
     * Create a new {@link Grouper} that manages groups for the
     * specified {@link SortedList}.
     */
    public Grouper(SortedList<E> sortedList, Client client) {
        this.sortedList = sortedList;
        this.client = client;
        setComparator(sortedList.getComparator());
    }

    /**
     * Set the comparator used to determine which elements are grouped together. As
     * a consequence this will rebuild the grouping state.
     */
    public void setComparator(Comparator<? super E> comparator) {
        if(this.comparator == comparator) return;
        this.comparator = comparator;

        // Populate the barcode by examining adjacent entries within the
        // source SortedList to check if they belong to the same group.
        barcode = new Barcode();
        for (int i = 0, n = sortedList.size(); i < n; i++) {
            barcode.add(i, groupTogether(i, i-1) ? DUPLICATE : UNIQUE, 1);
        }
    }

    /**
     * Get the comparator used to determine which elements are grouped together.
     */
    public Comparator<? super E> getComparator() {
        return comparator;
    }

    /**
     * Get the client which is notified of group changes.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Get the barcode that designates where groups start. {@link #UNIQUE}
     * colored elements are the starts of new groups, with a
     * {@link #DUPLICATE} element for each following group memeber.
     */
    public Barcode getBarcode() {
        return barcode;
    }

    /**
     * Handle changes from the {@link SortedList} by modifying the grouping state.
     * During this method, callbacks will be made to the {@link Client} who is
     * responsible for firing the appropriate change events to its listeners.
     */
    public void listChanged(ListEvent<E> listChanges) {
        // The approach to handling list uses two passes through the list of
        // change events.
        //
        // In pass 1, the barcode is changed and GroupingList is updated to
        // reflect the changes but no events are fired. Deleted and updated
        // original barcode values are stored in a temporary LinkedList. Updated
        // values' barcode entries are set to UNIQUE.
        //
        // In pass 2, the change events are reviewed again. During this second pass
        // we bring the IndexedTree of GroupLists up to date and also fire ListEvents.
        //
        // a) Inserted elements are tested to see if they were simply added to
        // existing GroupLists (in which case an update event should be fired
        // rather than an insert)
        //
        // b) For update events the value is tested for group membership with
        // adjacent values, and the result may be a combination of DELETE, INSERT
        // or UPDATE events.
        //
        // c) Deleted events may be forwarded with a resultant DELETE or UPDATE
        // event, depending on whether the deleted value was the last member of
        // its group.

        // a Barcode to track values that must be revisited to determine if they are UNIQUE or DUPLICATE
        final Barcode toDoList = new Barcode();
        toDoList.addWhite(0, barcode.size());

        // first pass -> update the barcode and accumulate the type of values removed (UNIQUE or DUPLICATE or UNIQUE_WITH_DUPLICATE)
        final LinkedList removedValues = new LinkedList();
        int lastFakedUniqueChangeIndex = -1;
        while (listChanges.next()) {
            final int changeIndex = listChanges.getIndex();
            final int changeType = listChanges.getType();

            if (changeType == ListEvent.INSERT) {
                // assume the inserted element is unique until we determine otherwise
                barcode.add(changeIndex, UNIQUE, 1);
                // indicate we must revisit this index later to determine its uniqueness
                toDoList.add(changeIndex, TODO, 1);

            } else if (changeType == ListEvent.UPDATE) {
                // case: AACCC -> AABCC
                // if the updated index is a UNIQUE index, we MAY have just created a
                // new group (by modifying an element in place). Consequently, we must
                // mark the NEXT element as UNIQUE and revisit it later to determine
                // if it really is
                if (barcode.get(changeIndex) == UNIQUE) {
                    if (changeIndex+1 < barcode.size() && barcode.get(changeIndex+1) == DUPLICATE) {
                        // however, we need to make sure that the barcode UNIQUE entry we are looking at
                        // was part of the barcode state before we started this iteration of listChanges.
                        // Specifically, we are concerned about the case where an update on the first element
                        // causes the barcode goes from:
                        //   X__ to XX_
                        // In this case, when looking at element 1, we should not treat it as if it were
                        // a UNIQUE node that is getting updated.  Instead, it should be treated as the
                        // DUPLICATE node that it really is.
                        // We track the index of the last element that we set to UNIQUE in this way using
                        // lastFakedUniqueChangeIndex
                        if (changeIndex != lastFakedUniqueChangeIndex){
                            barcode.set(changeIndex, UNIQUE, 2);
                            toDoList.set(changeIndex, TODO, 1);
                            lastFakedUniqueChangeIndex = changeIndex+1;
                        }
                    }
                }

            } else if (changeType == ListEvent.DELETE) {
                Object deleted = barcode.get(changeIndex);
                barcode.remove(changeIndex, 1);
                toDoList.remove(changeIndex, 1);

                if (deleted == UNIQUE) {
                    // if the deleted UNIQUE value has a DUPLICATE, promote the DUPLICATE to be the new UNIQUE
                    if (changeIndex < barcode.size() && barcode.get(changeIndex) == DUPLICATE) {
                        // case: AABB -> AAB
                        barcode.set(changeIndex, UNIQUE, 1);
                        deleted = UNIQUE_WITH_DUPLICATE;
                    }
                }

                removedValues.addLast(deleted);
            }
        }

        TryJoinResult<E> tryJoinResult = new TryJoinResult<E>();

        // second pass, handle toDoList, update groupLists, and fire events
        listChanges.reset();
        while(listChanges.next()) {
            final int changeIndex = listChanges.getIndex();
            final int changeType = listChanges.getType();
            E newValue = listChanges.getNewValue();
            E oldValue = listChanges.getOldValue();

            // inserts can result in UPDATE or INSERT events
            if(changeType == ListEvent.INSERT) {

                // if no group already exists to join, create a new group
                tryJoinExistingGroup(changeIndex, toDoList, tryJoinResult);
                if(tryJoinResult.group == NO_GROUP) {
                    client.groupChanged(changeIndex, tryJoinResult.groupIndex, ListEvent.INSERT, true, changeType, ListEvent.<E>unknownValue(), tryJoinResult.newFirstInGroup);
                } else {
                    client.groupChanged(changeIndex, tryJoinResult.groupIndex, ListEvent.UPDATE, true, changeType, tryJoinResult.oldFirstInGroup, tryJoinResult.newFirstInGroup);
                }

            // updates can result in INSERT, UPDATE and DELETE events
            } else if(changeType == ListEvent.UPDATE) {

                // get the location of the group before the update occurred
                int oldGroup = 0;
                if(toDoList.get(changeIndex) == TODO) {
                	if (changeIndex + 1 < barcode.size()) {
                		oldGroup = RIGHT_GROUP;
                	} else {
                		// case: AACC -> AAC (list change __UX)
                		// an update occured on the UNIQUE element of the last group,
                		// but the following duplicates of this group were deleted.
                		// as it's the last element remaining, treat it as UNIQUE.
                		// in the second iteration this will lead to a group update event
                		oldGroup = NO_GROUP;
                	}
                }
                else if(barcode.get(changeIndex) == DUPLICATE) oldGroup = LEFT_GROUP;
                else if(barcode.get(changeIndex) == UNIQUE) oldGroup = NO_GROUP;

                // get the new group location
                tryJoinExistingGroup(changeIndex, toDoList, tryJoinResult);

                // the index of the GroupList being updated (it may or may not exist yet)
                int groupIndex = tryJoinResult.groupIndex;

                // we're the first element in a new group
                if(tryJoinResult.group == NO_GROUP) {
                    if(oldGroup == NO_GROUP) {
                        client.groupChanged(changeIndex, groupIndex, ListEvent.UPDATE, true, changeType, oldValue, tryJoinResult.newFirstInGroup);
                    } else if(oldGroup == LEFT_GROUP) {
                        E firstFromPreviousGroup = sortedList.get(barcode.getIndex(groupIndex - 1, UNIQUE));
                        client.groupChanged(changeIndex, groupIndex - 1, ListEvent.UPDATE, false, changeType, firstFromPreviousGroup, firstFromPreviousGroup);
                        client.groupChanged(changeIndex, groupIndex, ListEvent.INSERT, true, changeType, ListEvent.<E>unknownValue(), tryJoinResult.newFirstInGroup);
                    } else if(oldGroup == RIGHT_GROUP) {
                        E firstFromNextGroup = sortedList.get(barcode.getIndex(groupIndex + 1, UNIQUE));
                        client.groupChanged(changeIndex, groupIndex, ListEvent.INSERT, true, changeType, ListEvent.<E>unknownValue(), tryJoinResult.newFirstInGroup);
                        client.groupChanged(changeIndex, groupIndex + 1, ListEvent.UPDATE, false, changeType, oldValue, firstFromNextGroup);
                    }

                // we are joining an existing group to our left
                } else if(tryJoinResult.group == LEFT_GROUP) {
                    if(oldGroup == NO_GROUP) {
                        client.groupChanged(changeIndex, groupIndex, ListEvent.UPDATE, true, changeType, tryJoinResult.oldFirstInGroup, tryJoinResult.newFirstInGroup);
                        client.groupChanged(changeIndex, groupIndex + 1, ListEvent.DELETE, false, changeType, oldValue, ListEvent.<E>unknownValue());
                    } else if(oldGroup == LEFT_GROUP) {
                        client.groupChanged(changeIndex, groupIndex, ListEvent.UPDATE, true, changeType, tryJoinResult.oldFirstInGroup, tryJoinResult.newFirstInGroup);
                    } else if(oldGroup == RIGHT_GROUP) {
                        client.groupChanged(changeIndex, groupIndex, ListEvent.UPDATE, true, changeType, tryJoinResult.oldFirstInGroup, tryJoinResult.newFirstInGroup);
                        if(groupIndex + 1 < barcode.blackSize()) {
                            E firstFromNextGroup = sortedList.get(barcode.getIndex(groupIndex + 1, UNIQUE));
                            client.groupChanged(changeIndex, groupIndex + 1, ListEvent.UPDATE, false, changeType, oldValue, firstFromNextGroup);
                        }
                    }

                // we are joining an existing group to our right
                } else if(tryJoinResult.group == RIGHT_GROUP) {
                    if (oldGroup == NO_GROUP) {
                        client.groupChanged(changeIndex, groupIndex, ListEvent.DELETE, false, changeType, oldValue, ListEvent.<E>unknownValue());
                        client.groupChanged(changeIndex, groupIndex, ListEvent.UPDATE, true, changeType, tryJoinResult.oldFirstInGroup, tryJoinResult.newFirstInGroup);
                    } else if(oldGroup == LEFT_GROUP) {
                        if(groupIndex - 1 >= 0) {
                            E firstFromPreviousGroup = sortedList.get(barcode.getIndex(groupIndex - 1, UNIQUE));
                            client.groupChanged(changeIndex, groupIndex - 1, ListEvent.UPDATE, false, changeType, firstFromPreviousGroup, firstFromPreviousGroup);
                        }
                        client.groupChanged(changeIndex, groupIndex, ListEvent.UPDATE, true, changeType, tryJoinResult.oldFirstInGroup, tryJoinResult.newFirstInGroup);
                    } else if(oldGroup == RIGHT_GROUP) {
                        client.groupChanged(changeIndex, groupIndex, ListEvent.UPDATE, true, changeType, tryJoinResult.oldFirstInGroup, tryJoinResult.newFirstInGroup);
                    }
                }

            // deletes can result in UPDATE or DELETE events
            } else if(changeType == ListEvent.DELETE) {
                // figure out if we deleted a UNIQUE or DUPLICATE or UNIQUE_WITH_DUPLICATE
                final Object deleted = removedValues.removeFirst();

                // get the index of the element removed from the source list
                final int sourceDeletedIndex = deleted == DUPLICATE ? changeIndex - 1 : changeIndex;

                // determine the index of the GroupList the removal impacts
                final int groupDeletedIndex = sourceDeletedIndex < barcode.size() ? barcode.getBlackIndex(sourceDeletedIndex, true) : barcode.blackSize();

                // fire the change event
                if(deleted == UNIQUE) {
                	if (changeIndex == lastFakedUniqueChangeIndex) {
                		// case: AACC -> AAC (list change __UX)
                		// in the last group we have deleted a duplicate element that was marked as UNIQUE
                		// because of an update event of the preceding UNIQUE element in the first iteration.
                		// Duplicate deletion is a group update, but it was already triggered by the UNIQUE element update,
                		// so nothing to do here
                	} else {
                		// if we removed a UNIQUE element then it was the last one and we must remove the group
                		client.groupChanged(changeIndex, groupDeletedIndex, ListEvent.DELETE, true, changeType, oldValue, ListEvent.<E>unknownValue());
                	}
                } else {
                    E oldValueInGroup;
                    E newValueInGroup;

                    // there's only a new value if the group still exists
                    if (groupDeletedIndex < barcode.blackSize()) {
                        int firstInGroupIndex = barcode.getIndex(groupDeletedIndex, UNIQUE);
                        newValueInGroup = sortedList.get(firstInGroupIndex);
                    } else {
                        newValueInGroup = ListEvent.<E>unknownValue();
                    }

                    if (deleted == UNIQUE_WITH_DUPLICATE) {
                        oldValueInGroup = oldValue;
                    } else {
                        // assume the old value and the new value are the same. If they're not,
                        // we'll already have the correct old value and this will be thrown away
                        oldValueInGroup = newValueInGroup;
                    }

                    client.groupChanged(changeIndex, groupDeletedIndex, ListEvent.UPDATE, true, changeType, oldValueInGroup, newValueInGroup);
                }
            }
        }
    }

    /**
     * Tests if the specified values should be grouped together.
     *
     * @param sourceIndex0 the first index of the source list to test
     * @param sourceIndex1 the second index of the source list to test
     *
     * @return true iff the values at the specified index are equal according
     *      to the Comparator which defines the grouping logic; false if
     *      either index is out of range or the values shall not be grouped
     */
    private boolean groupTogether(int sourceIndex0, int sourceIndex1) {
        if(sourceIndex0 < 0 || sourceIndex0 >= sortedList.size()) return false;
        if(sourceIndex1 < 0 || sourceIndex1 >= sortedList.size()) return false;
        return comparator.compare(sortedList.get(sourceIndex0), sortedList.get(sourceIndex1)) == 0;
    }

    /**
     * Handles whether this change index has a neighbour that existed prior
     * to a current change and that the values are equal. This adjusts the
     * duplicates list a found pair of such changes if they exist.
     *
     * @return NO_GROUP if no neighbour was found. In this case the value at the specified
     *      index will be marked as unique, but not temporarily so. Returns LEFT_GROUP if
     *      a neighbour was found on the left, and RIGHT_GROUP if a neighbour was found on the
     *      right. In non-zero cases the duplicates list is updated.
     */
    private TryJoinResult tryJoinExistingGroup(int changeIndex, Barcode toDoList, TryJoinResult<E> result) {
        // test if values at changeIndex and its predecessor should be grouped
        int predecessorIndex = changeIndex - 1;
        if (groupTogether(predecessorIndex, changeIndex)) {
            barcode.set(changeIndex, DUPLICATE, 1);
            int groupIndex = barcode.getColourIndex(changeIndex, true, UNIQUE);
            int indexOfFirstInGroup = barcode.getIndex(groupIndex, UNIQUE);
            E firstElementInGroup = sortedList.get(indexOfFirstInGroup);
            return result.set(LEFT_GROUP, groupIndex, firstElementInGroup, firstElementInGroup);
        }

        // search for an OLD successor that should be in the same group as changeIndex
        int successorIndex = changeIndex + 1;
        while (true) {
            // we have found a successor that belongs in the same group
            if (groupTogether(changeIndex, successorIndex)) {
                // if the successor is OLD, have changeIndex join the existing group
                if (toDoList.get(successorIndex) == DONE) {
                    barcode.set(changeIndex, UNIQUE, 1);
                    barcode.set(successorIndex, DUPLICATE, 1);
                    int groupIndex = barcode.getColourIndex(changeIndex, UNIQUE);
                    E oldFirstElementInGroup = sortedList.get(successorIndex);
                    E newFirstElementInGroup = sortedList.get(changeIndex);
                    return result.set(RIGHT_GROUP, groupIndex, oldFirstElementInGroup, newFirstElementInGroup);

                // this successor is NEW, not OLD, so skip it
                } else {
                    successorIndex++;
                }

            // we have no more successors that belong in the same group
            } else {
                barcode.set(changeIndex, UNIQUE, 1);
                int groupIndex = barcode.getColourIndex(changeIndex, UNIQUE);
                E onlyElementInGroup = sortedList.get(changeIndex);
                return result.set(NO_GROUP, groupIndex, ListEvent.<E>unknownValue(), onlyElementInGroup);
            }
        }
    }

    /**
     * Reusable object to provide 2 return values from
     * {@link Grouper#tryJoinExistingGroup}.
     */
    private static class TryJoinResult<E> {
        int group;
        int groupIndex;
        E oldFirstInGroup;
        E newFirstInGroup;
        public TryJoinResult set(int group, int groupIndex, E oldFirstElementInGroup, E newFirstElementInGroup) {
            this.group = group;
            this.groupIndex = groupIndex;
            this.oldFirstInGroup = oldFirstElementInGroup;
            this.newFirstInGroup = newFirstElementInGroup;
            return this;
        }
    }

    /**
     * The grouper client is responsible for turning grouping events into
     * {@link ListEvent}s if desired. The client receives callbacks as groups
     * are created, modified and destroyed.
     */
    public interface Client<E> {
        /**
         * Handle the structure of a group changing.
         *
         * @param index the location in the source {@link SortedList} that
         *      was inserted, updated or deleted.
         * @param groupIndex the corresponding group that the {@link SortedList}
         *      element now belongs to, or belonged to in the case of
         *      {@link ListEvent#DELETE} events.
         * @param groupChangeType one of {@link ListEvent#INSERT},
         *      {@link ListEvent#UPDATE} or {@link ListEvent#DELETE} signalling
         *      what happened to the group in response to this element change.
         *      This is potentially different from what happened to the original
         *      list element.
         * @param primary whether this is a first event for this change,
         *      impacting the current group for this element. Sometimes multiple
         *      groups will be effected, in which case only a single callback will
         *      be the primary callback.
         * @param elementChangeType the change type that caused this  Sometimes
         *      an {@link ListEvent#UPDATE} event will cause a group to become
         *      inserted or deleted, in which case the elementChangeType
         *      represents the original event type.
         */
        public void groupChanged(int index, int groupIndex, int groupChangeType, boolean primary, int elementChangeType, E oldValue, E newValue);
    }
}