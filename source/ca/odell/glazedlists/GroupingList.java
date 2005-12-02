/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// volatile implementation support
import ca.odell.glazedlists.impl.adt.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * A grouping list contains elements which are themselves Lists. Those Lists
 * are infact elements of the source list which have been grouped together into
 * a List. The logic of how to group the source elements into groups is specified
 * via a Comparator. Elements for which the Comparator returns 0 are guaranteed
 * to be contained within the same group within this GroupingList. This implies
 * that source elements may only participate in a single group within this
 * GroupingList.
 *
 * <p> Further transformations may be layered on top of this GroupingList to
 * transform the group lists into any other desirable form.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>GroupingListTest</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 */
public final class GroupingList<E> extends TransformedList<List<E>,E> {

    /**
     * A temporary barcode data structure is created when processing ListEvents in this GroupingList.
     * These constants identify which indexes remain to be processed.
     */
    private static final Object TODO = Barcode.BLACK;
    private static final Object DONE = Barcode.WHITE;

    /** The comparator used to determine the groups. */
    private Comparator<E> comparator;

    /** The GroupLists defined by the comparator. They are stored in an IndexedTree so their indices can be quickly updated. */
    private IndexedTree<GroupList> groupLists = new IndexedTree<GroupList>();

    /**
     * The data structure which tracks which source elements are considered UNIQUE
     * (the first element of a group) and which are DUPLICATE (any group element NOT the first).
     */
    private Barcode barcode = new Barcode();

    /** Entries in barcode are one of these constants to indicate if the element at an index is a UNIQUE or DUPLICATE. */
    private static final Object UNIQUE = Barcode.BLACK;
    private static final Object DUPLICATE = Barcode.WHITE;

    /** Used only in temporary data structures to flag deleting of the FIRST group element when more elements exist. */
    private static final Object UNIQUE_WITH_DUPLICATE = null;

    /** For reporting which side of an element its group is located. */
    private static final int LEFT_GROUP = -1;
    private static final int NO_GROUP = 0;
    private static final int RIGHT_GROUP = 1;

    /**
     * Creates a {@link GroupingList} that determines groupings via the
     * {@link Comparable} interface which all elements of the <code>source</code>
     * are assumed to implement.
     */
    public GroupingList(EventList<E> source) {
        this(source, (Comparator<E>) GlazedLists.comparableComparator());
    }

    /**
     * Creates a {@link GroupingList} that determines groups using the specified
     * {@link Comparator}.
     *
     * @param source the {@link EventList} containing elements to be grouped
     * @param comparator the {@link Comparator} used to determine groupings
     */
    public GroupingList(EventList<E> source, Comparator<E> comparator) {
        this(new SortedList<E>(source, comparator), comparator);
    }

    /**
     * A private constructor which provides a convenient handle to the
     * {@link SortedList} which will serve as the source of this list.
     *
     * @param source the elements to be grouped arranged in sorted order
     * @param comparator the {@link Comparator} used to determine groupings
     */
    private GroupingList(SortedList<E> source, Comparator<E> comparator) {
        super(source);
        this.comparator = comparator;

        this.buildDataStructures();

        source.addListEventListener(this);
    }

    /**
     * Populates the internal data structures of GroupingList by examining
     * adjacent entries within the source SortedList to check if they belong
     * to the same group.
     */
    private void buildDataStructures() {
        if (!barcode.isEmpty()) throw new IllegalStateException("Attempted to build internal data structures twice.");

        // initialize the barcode
        for (int i = 0; i < source.size(); i++)
            barcode.add(i, groupTogether(i, i-1) ? DUPLICATE : UNIQUE, 1);

        // initialize the tree of GroupLists
        for (int i = 0; i < barcode.colourSize(UNIQUE); i++)
            this.insertGroupList(i);
    }

    /**
     * Creates and inserts a new GroupList at the specified <code>index</code>.
     *
     * @param index the location at which to insert an empty GroupList
     */
    private void insertGroupList(int index) {
        final GroupList groupList = new GroupList();
        final IndexedTreeNode<GroupList> indexedTreeNode = this.groupLists.addByNode(index, groupList);
        groupList.setTreeNode(indexedTreeNode);
    }

    /**
     * Removes the GroupList at the given <code>index</code>.
     *
     * @param index the location at which to remove a GroupList
     */
    private void removeGroupList(int index) {
        final IndexedTreeNode<GroupList> indexedTreeNode = this.groupLists.removeByIndex(index);

        // for safety, null out the GroupList's reference to its now defunct indexedTreeNode
        indexedTreeNode.getValue().setTreeNode(null);
    }

    /** {@inheritDoc} */
    public int size() {
        return barcode.blackSize();
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int index) {
        return barcode.getIndex(index, UNIQUE);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        // The approach to handling list changes in GroupingList uses two passes
        // through the list of change events.
        //
        // In the first pass, the barcode is updated to reflect the changes but
        // no events are fired. Deleted and updated original barcode values are
        // stored in a temporary LinkedList. Updated values' barcode entries are
        // set to UNIQUE.
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
                        barcode.set(changeIndex, UNIQUE, 2);
                        toDoList.set(changeIndex, TODO, 1);
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

        // second pass, handle toDoList, update groupLists, and fire events
        updates.beginEvent(true);
        listChanges.reset();
        while (listChanges.next()) {
            final int changeIndex = listChanges.getIndex();
            final int changeType = listChanges.getType();

            // inserts can result in UPDATE or INSERT events
            if (changeType == ListEvent.INSERT) {

                // if no group already exists to join, create a new group
                if (tryJoinExistingGroup(changeIndex, toDoList) == NO_GROUP) {
                    final int groupIndex = barcode.getColourIndex(changeIndex, UNIQUE);
                    insertGroupList(groupIndex);
                    updates.addInsert(groupIndex);
                } else {
                    updates.addUpdate(barcode.getColourIndex(changeIndex, true, UNIQUE));
                }

            // updates can result in INSERT, UPDATE and DELETE events
            } else if (changeType == ListEvent.UPDATE) {

                // get the location of the group before the update occurred
                int oldGroup = 0;
                if (toDoList.get(changeIndex) == TODO) oldGroup = RIGHT_GROUP;
                else if (barcode.get(changeIndex) == DUPLICATE) oldGroup = LEFT_GROUP;
                else if (barcode.get(changeIndex) == UNIQUE) oldGroup = NO_GROUP;

                // get the new group location
                int newGroup = tryJoinExistingGroup(changeIndex, toDoList);

                // the index of the GroupList being updated (it may or may not exist yet)
                int groupIndex = barcode.getColourIndex(changeIndex, true, UNIQUE);

                // we're the first element in a new group
                if (newGroup == NO_GROUP) {
                    if (oldGroup == NO_GROUP) {
                        updates.addUpdate(groupIndex);
                    } else if (oldGroup == LEFT_GROUP) {
                        updates.addUpdate(groupIndex-1);
                        insertGroupList(groupIndex);
                        updates.addInsert(groupIndex);
                    } else if (oldGroup == RIGHT_GROUP) {
                        insertGroupList(groupIndex);
                        updates.addInsert(groupIndex);
                        updates.addUpdate(groupIndex+1);
                    }

                // we are joining an existing group to our left
                } else if (newGroup == LEFT_GROUP) {
                    if (oldGroup == NO_GROUP) {
                        updates.addUpdate(groupIndex);
                        removeGroupList(groupIndex+1);
                        updates.addDelete(groupIndex+1);
                    } else if (oldGroup == LEFT_GROUP) {
                        updates.addUpdate(groupIndex);
                    } else if (oldGroup == RIGHT_GROUP) {
                        updates.addUpdate(groupIndex);
                        if (groupIndex+1 < barcode.blackSize()) updates.addUpdate(groupIndex+1);
                    }

                // we are joining an existing group to our right
                } else if (newGroup == RIGHT_GROUP) {
                    if (oldGroup == NO_GROUP) {
                        removeGroupList(groupIndex);
                        updates.addDelete(groupIndex);
                        updates.addUpdate(groupIndex);
                    } else if (oldGroup == LEFT_GROUP) {
                        if(groupIndex-1 >= 0) updates.addUpdate(groupIndex-1);
                        updates.addUpdate(groupIndex);
                    } else if (oldGroup == RIGHT_GROUP) {
                        updates.addUpdate(groupIndex);
                    }
                }

            // deletes can result in UPDATE or DELETE events
            } else if (changeType == ListEvent.DELETE) {
                // figure out if we deleted a UNIQUE or DUPLICATE or UNIQUE_WITH_DUPLICATE
                final Object deleted = removedValues.removeFirst();

                // get the index of the element removed from the source list
                final int sourceDeletedIndex = deleted == DUPLICATE ? changeIndex - 1 : changeIndex;

                // determine the index of the GroupList the removal effects
                final int groupDeletedIndex = sourceDeletedIndex < barcode.size() ? barcode.getBlackIndex(sourceDeletedIndex, true) : barcode.blackSize();

                // fire the change event
                if (deleted == UNIQUE) {
                    // if we removed a UNIQUE element then it was the last one and we must remove the group
                    removeGroupList(groupDeletedIndex);
                    updates.addDelete(groupDeletedIndex);
                } else {
                    updates.addUpdate(groupDeletedIndex);
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
     * @return NO_GROUP if no neighbour was found. In this case the value at the specified
     *      index will be marked as unique, but not temporarily so. Returns LEFT_GROUP if
     *      a neighbour was found on the left, and RIGHT_GROUP if a neighbour was found on the
     *      right. In non-zero cases the duplicates list is updated.
     */
    private int tryJoinExistingGroup(int changeIndex, Barcode toDoList) {
        // test if values at changeIndex and its predecessor should be grouped
        int predecessorIndex = changeIndex - 1;
        if (groupTogether(predecessorIndex, changeIndex)) {
            barcode.set(changeIndex, DUPLICATE, 1);
            return LEFT_GROUP;
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
                    return RIGHT_GROUP;

                // this successor is NEW, not OLD, so skip it
                } else {
                    successorIndex++;
                }

            // we have no more successors that belong in the same group
            } else {
                barcode.set(changeIndex, UNIQUE, 1);
                return NO_GROUP;
            }
        }
    }

    public List<E> get(int index) {
        return this.groupLists.get(index);
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
        if(sourceIndex0 < 0 || sourceIndex0 >= source.size()) return false;
        if(sourceIndex1 < 0 || sourceIndex1 >= source.size()) return false;
        return comparator.compare(source.get(sourceIndex0), source.get(sourceIndex1)) == 0;
    }

    /** {@inheritDoc} */
    public List<E> remove(int index) {
        if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot remove at " + index + " on list of size " + size());

        final List<E> removed = (List<E>)this.get(index);

        // make a copy of the list to return
        final List<E> result = new ArrayList<E>(removed);

        removed.clear();

        return result;
    }

    /** {@inheritDoc} */
    public List<E> set(int index, List<E> value) {
        if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot set at " + index + " on list of size " + size());

        updates.beginEvent(true);

        final List<E> result = (List<E>)this.remove(index);
        this.add(index, value);

        updates.commitEvent();

        return result;
    }

    /**
     * This version of add will distribute all elements within the given
     * <code>value</code> List into groups. Existing groups will be reused and
     * new groups will be created as needed. As such, the <code>index</code>
     * argument is meaningless.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method
     * breaks the contract required by {@link List#add(int, Object}.
     */
    public void add(int index, List<E> value) {
        source.addAll(value);
    }

    /**
     * This is the List implementation used to store groups created by this
     * GroupingList. It defines all mutator methods by mapping them to mutations
     * on the source list of GroupList. Thus, writes to this GroupList effect
     * all Lists sitting under GroupList.
     */
    private class GroupList extends AbstractList<E> {

        /** The node within {@link groupLists} that records the index of this GroupList within the GroupingList. */
        private IndexedTreeNode<GroupList> treeNode;

        /**
         * Attach the IndexedTreeNode that tracks this GroupLists position to the
         * GroupList itself so it can look up its own position.
         */
        private void setTreeNode(IndexedTreeNode<GroupList> treeNode) {
            this.treeNode = treeNode;
        }

        /**
         * Returns the inclusive index of the start of this {@link GroupList}
         * within the source {@link SortedList}.
         */
        private int getStartIndex() {
            final int groupIndex = this.treeNode.getIndex();
            return GroupingList.this.getSourceIndex(groupIndex);
        }

        /**
         * Returns the exclusive index of the end of this {@link GroupList}
         * within the source {@link SortedList}.
         */
        private int getEndIndex() {
            final int groupIndex = this.treeNode.getIndex();

            // if this is before the end, its everything up to the first different element
            if(groupIndex < barcode.blackSize() - 1) {
                return barcode.getIndex(groupIndex + 1, UNIQUE);
            // if this is at the end, its everything after
            } else {
                return barcode.size();
            }
        }

        private int getSourceIndex(int index) {
            return this.getStartIndex() + index;
        }

        /** {@inheritDoc} */
        public E set(int index, E element) {
            return source.set(this.getSourceIndex(index), element);
        }

        /** {@inheritDoc} */
        public E get(int index) {
            return source.get(this.getSourceIndex(index));
        }

        /** {@inheritDoc} */
        public int size() {
            return this.getEndIndex() - this.getStartIndex();
        }

        /** {@inheritDoc} */
        public void clear() {
            source.subList(this.getStartIndex(), this.getEndIndex()).clear();
        }

        /** {@inheritDoc} */
        public E remove(int index) {
            return source.remove(this.getSourceIndex(index));
        }

        /** {@inheritDoc} */
        public void add(int index, E element) {
            source.add(this.getSourceIndex(index), element);
        }
    }
}