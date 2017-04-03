/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.Grouper;
import ca.odell.glazedlists.impl.adt.barcode2.Element;
import ca.odell.glazedlists.impl.adt.barcode2.SimpleTree;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A grouping list contains elements which are themselves Lists. Those Lists
 * are infact elements of the source list which have been grouped together into
 * a List. The logic of how to group the source elements is specified via a
 * Comparator. Elements for which the Comparator returns 0 are guaranteed to be
 * contained within the same group within this GroupingList. This implies
 * that source elements may only participate in a single group within this
 * GroupingList.
 *
 * <p>Further transformations may be layered on top of this GroupingList to
 * transform the group lists into any other desirable form.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>GroupingListTest</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=281">281</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=491">491</a>
 * </td></tr>
 * </table>
 *
 * @author James Lemieux
 */
public final class GroupingList<E> extends TransformedList<E, List<E>> {

    /** The GroupLists defined by the comparator. They are stored in an SimpleTree so their indices can be quickly updated. */
    private SimpleTree<GroupList> groupLists = new SimpleTree<GroupList>();

    /** The Grouper manages creating and deleting groups. */
    private final Grouper<E> grouper;

    /**
     * Creates a {@link GroupingList} that determines groupings via the
     * {@link Comparable} interface which all elements of the <code>source</code>
     * are assumed to implement.
     */
    public static <E extends Comparable<? super E>> GroupingList<E> create(EventList<E> source) {
        return new GroupingList<E>(source);
    }

    /**
     * Creates a {@link GroupingList} that determines groupings via the
     * {@link Comparable} interface which all elements of the <code>source</code>
     * are assumed to implement.
     * <p>Usage of factory method {@link #create(EventList)} is preferable.
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
    public GroupingList(EventList<E> source, Comparator<? super E> comparator) {
        this(new SortedList<E>(source, comparator), comparator, null);
    }

    /**
     * A private constructor which provides a convenient handle to the
     * {@link SortedList} which will serve as the source of this list.
     *
     * @param source the elements to be grouped arranged in sorted order
     * @param comparator the {@link Comparator} used to determine groupings
     * @param dummyParameter dummy parameter to differentiate between the different
     *      {@link GroupingList} constructors.
     */
    private GroupingList(SortedList<E> source, Comparator<? super E> comparator, Void dummyParameter) {
        super(source);

        // the grouper handles changes to the SortedList
        this.grouper = new Grouper<E>(source, new GrouperClient());

        // initialize the tree of GroupLists
        rebuildGroupListTreeFromBarcode();

        source.addListEventListener(this);
    }

    /**
     * After the barcode has been updated in response to a change in the
     * grouping {@link Comparator}, this method is used to rebuild the tree of
     * GroupLists which map those GroupLists to their overall indices.
     */
    private void rebuildGroupListTreeFromBarcode() {
        // clear the contents of the GroupList tree
        groupLists.clear();

        // fetch our GrouperClient
        final GrouperClient grouperClient = (GrouperClient) grouper.getClient();

        // build the tree of GroupLists from the barcode
        for (int i = 0, n = grouper.getBarcode().colourSize(Grouper.UNIQUE); i < n; i++) {
            grouperClient.insertGroupList(i);
        }
    }

    /**
     * Return the index of the group to which the <code>groupElement</code>
     * would belong if it were hypothetically added to the source list. Note
     * that <code>groupElement</code> does <strong>NOT</strong> have to exist
     * in a group. This method is essentially a convenient way to locate a
     * group based on a prototypical element of that group.
     *
     * @param groupElement a prototype element of the group to locate
     * @return the index of the group that would contain <code>groupElement</code>
     *      if it were added to the source list or <code>-1</code> if no
     *      currently existing group would contain the <code>groupElement</code>
     */
    public int indexOfGroup(E groupElement) {
        // determine where the groupElement would be positioned in the source List
        final int sourceIndex = ((SortedList<E>) source).sortIndex(groupElement);

        // if the groupElement is not a member of the group, return -1
        if (sourceIndex == source.size() || grouper.getComparator().compare(source.get(sourceIndex), groupElement) != 0)
            return -1;

        // return the index of the group that includes the element at the source index
        return grouper.getBarcode().getColourIndex(sourceIndex, Grouper.UNIQUE);
    }

    /**
     * Handle changes to the grouping list groups.
     */
    private class GrouperClient implements Grouper.Client<E> {
        @Override
        public void groupChanged(int index, int groupIndex, int groupChangeType, boolean primary, int elementChangeType, E oldValue, E newValue, boolean updateNextSeparator) {
            if(groupChangeType == ListEvent.INSERT) {
                insertGroupList(groupIndex);
                updates.addInsert(groupIndex);
            } else if(groupChangeType == ListEvent.DELETE) {
                removeGroupList(groupIndex);
                updates.addDelete(groupIndex);
            } else if(groupChangeType == ListEvent.UPDATE) {
                updates.addUpdate(groupIndex);
            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * Creates and inserts a new GroupList at the specified
         * <code>index</code>.
         *
         * @param index the location at which to insert an empty GroupList
         */
        private void insertGroupList(int index) {
            final GroupList groupList = new GroupList();
            final Element<GroupList> indexedTreeNode = groupLists.add(index, groupList, 1);
            groupList.setTreeNode(indexedTreeNode);
        }

        /**
         * Removes the GroupList at the given <code>index</code>.
         *
         * @param index the location at which to remove a GroupList
         */
        private void removeGroupList(int index) {
            final Element<GroupList> indexedTreeNode = groupLists.get(index);
            groupLists.remove(indexedTreeNode);

            // for safety, null out the GroupList's reference to its now defunct indexedTreeNode
            indexedTreeNode.get().setTreeNode(null);
        }
    }

    /**
     * Change the {@link Comparator} which determines the groupings presented
     * by this List
     *
     * @param comparator the {@link Comparator} used to determine groupings;
     *      <tt>null</tt> will be treated as {@link GlazedLists#comparableComparator()}
     */
    public void setComparator(Comparator<? super E> comparator) {
        if (comparator == null)
            comparator = (Comparator) GlazedLists.comparableComparator();
        ((SortedList<E>) source).setComparator(comparator);
    }

    /** {@inheritDoc} */
    @Override
    protected int getSourceIndex(int index) {
        return grouper.getBarcode().getIndex(index, Grouper.UNIQUE);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        updates.beginEvent(true);

        // check if this ListEvent was caused due to a change in the
        // Comparator that creates the groups
        final SortedList<E> sortedSource = (SortedList<E>) source;
        final Comparator<? super E> sourceComparator = sortedSource.getComparator();
        if (sourceComparator != grouper.getComparator()) {
            // when the grouping comparator is changed in the source list, let
            // the grouper know so we can rebuild our groups from scratch

            // record the impending removal of all groups before adjusting the barcode
            for (int i = 0, n = size(); i < n; i++)
                updates.elementDeleted(0, get(i));

            // adjust the Comparator used by the Grouper (which will change the barcode)
            grouper.setComparator(sourceComparator);

            // rebuild the tree which maps GroupLists to indices (so the tree matches the new barcode)
            rebuildGroupListTreeFromBarcode();

            // insert all new groups (represented by the newly formed barcode)
            updates.addInsert(0, size() - 1);

        } else {
            grouper.listChanged(listChanges);
        }
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    @Override
    public List<E> get(int index) {
        return groupLists.get(index).get();
    }

    /** {@inheritDoc} */
    @Override
    public List<E> remove(int index) {
        if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot remove at " + index + " on list of size " + size());

        final List<E> removed = get(index);

        // make a copy of the list to return
        final List<E> result = new ArrayList<E>(removed);

        removed.clear();

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public List<E> set(int index, List<E> value) {
        if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot set at " + index + " on list of size " + size());

        updates.beginEvent(true);

        final List<E> result = remove(index);
        add(index, value);

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
     * breaks the contract required by {@link List#add(int, Object)}.
     */
    @Override
    public void add(int index, List<E> value) {
        source.addAll(value);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return grouper.getBarcode().colourSize(Grouper.UNIQUE);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        ((SortedList) source).dispose();
        super.dispose();
    }

    /**
     * This is the List implementation used to store groups created by this
     * GroupingList. It defines all mutator methods by mapping them to mutations
     * on the source list of GroupList. Thus, writes to this GroupList effect
     * all Lists sitting under GroupList.
     */
    private class GroupList extends AbstractList<E> {

        /**
         * The node within {@link GroupingList#groupLists} that records the
         * index of this GroupList within the GroupingList.
         */
        private Element<GroupList> treeNode;

        /**
         * Attach the Element that tracks this GroupLists position to the
         * GroupList itself so it can look up its own position.
         */
        private void setTreeNode(Element<GroupList> treeNode) {
            this.treeNode = treeNode;
        }

        /**
         * Returns the inclusive index of the start of this {@link GroupList}
         * within the source {@link SortedList}.
         */
        private int getStartIndex() {
            if (treeNode == null) return -1;
            final int groupIndex = groupLists.indexOfNode(treeNode, (byte)1);
            return GroupingList.this.getSourceIndex(groupIndex);
        }

        /**
         * Returns the exclusive index of the end of this {@link GroupList}
         * within the source {@link SortedList}.
         */
        private int getEndIndex() {
            if (treeNode == null) return -1;
            final int groupIndex = groupLists.indexOfNode(treeNode, (byte)1);

            // if this is before the end, its everything up to the first different element
            if(groupIndex < grouper.getBarcode().blackSize() - 1) {
                return grouper.getBarcode().getIndex(groupIndex + 1, Grouper.UNIQUE);
            // if this is at the end, its everything after
            } else {
                return grouper.getBarcode().size();
            }
        }

        private int getSourceIndex(int index) {
            return getStartIndex() + index;
        }

        /** {@inheritDoc} */
        @Override
        public E set(int index, E element) {
            return source.set(getSourceIndex(index), element);
        }

        /** {@inheritDoc} */
        @Override
        public E get(int index) {
            return source.get(getSourceIndex(index));
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return getEndIndex() - getStartIndex();
        }

        /** {@inheritDoc} */
        @Override
        public void clear() {
            source.subList(getStartIndex(), getEndIndex()).clear();
        }

        /** {@inheritDoc} */
        @Override
        public E remove(int index) {
            return source.remove(getSourceIndex(index));
        }

        /** {@inheritDoc} */
        @Override
        public void add(int index, E element) {
            source.add(getSourceIndex(index), element);
        }
    }
}