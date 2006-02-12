/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.Grouper;
import ca.odell.glazedlists.impl.adt.Barcode;
import ca.odell.glazedlists.impl.adt.BarcodeIterator;
import ca.odell.glazedlists.impl.adt.IndexedTree;
import ca.odell.glazedlists.impl.adt.IndexedTreeNode;
import ca.odell.glazedlists.event.ListEvent;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * A list that adds separator objects before each group of elements.
 *
 * <p><strong>Warning:</strong> this class won't work very well with generics
 * because separators are mixed in, which will be a different class than the
 * other list elements.
 *
 * <p><strong>Developer Preview</strong> this class is still under heavy development
 * and subject to API changes. It's also really slow at the moment and won't scale
 * to lists of size larger than a hundred or so efficiently.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SeparatorList<E> extends TransformedList<E, E> {

    /** the grouping service manages finding where to insert groups */
    private final Grouper<E> grouper;

    /** manage collapsed elements */
    private Barcode collapsedElements = new Barcode();

    /** the separators list is black for separators, white for everything else
     *
     *
     * <p>The following demonstrates the layout of the barcode for the
     * given source list:
     * <pre><code>
     *           INDICES 0         1         2
     *                   012345678901234567890
     *       SOURCE LIST AAAABBBCCCDEFF
     *   GROUPER BARCODE X___X__X__XXX_
     * SEPARATOR BARCODE X____X___X___X_X_X__
     * </pre></code>
     *
     * <p>To read this structure:
     * <li>the grouper barcode is an "X" for the first element in each
     *     group (called uniques), and an "_" for the following
     *     elements (called duplicates).
     * <li>the separator barcode is very similar to the grouper barcode.
     *     In this barcode, there is an "X" for each separator and an "_"
     *     for each element in the source list. We use the structure of the
     *     grouper barcode to derive and maintain the separator barcode.
     *
     * <p>When accessing elements, the separator barcode is queried. If it
     * holds an "X", the element is a separator and that separator is returned.
     * Otherwise if it is an "_", the corresponding source index is obtained
     * (by removing the number of preceding "X" elements) and the element is
     * retrieved from the source list.
     */
    private Barcode insertedSeparators = new Barcode();
    private static final Object SEPARATOR = Barcode.BLACK;
    private static final Object SOURCE_ELEMENT = Barcode.WHITE;

    /** a list of {@link Separator}s, one for each separator in the list */
    private IndexedTree<GroupSeparator> separators = new IndexedTree<GroupSeparator>();

    /**
     * Create a new {@link UniqueList} that determines groups using the specified
     * {@link Comparator}. Elements that the {@link Comparator} determines are
     * equal will share a common separator.
     *
     * @see GlazedLists#beanPropertyComparator
     */
    public SeparatorList(EventList<E> source, Comparator<E> comparator) {
        this(new SortedList<E>(source, comparator), comparator, null);
    }

    private SeparatorList(SortedList<E> source, Comparator<E> comparator, Void dummyParameter) {
        super(source);

        // prepare the groups
        GrouperClient grouperClient = new GrouperClient();
        this.grouper = new Grouper<E>(source, grouperClient);

        // prepare the separator list
        insertedSeparators.add(0, SOURCE_ELEMENT, source.size());
        for(BarcodeIterator i = grouper.getBarcode().iterator(); i.hasNextColour(Grouper.UNIQUE); ) {
            i.nextColour(Grouper.UNIQUE);
            int groupIndex = i.getColourIndex(Grouper.UNIQUE);
            int sourceIndex = i.getIndex();
            insertedSeparators.add(groupIndex + sourceIndex, SEPARATOR, 1);
            IndexedTreeNode<GroupSeparator> node = separators.addByNode(groupIndex, new GroupSeparator());
            node.getValue().setNode(node);
        }

        // prepare the collapsed elements
        collapsedElements.addBlack(0, size());

        // handle changes via the grouper
        source.addListEventListener(this);
    }

    /**
     * Fire two events, one for the group (the separator) and another for the
     * actual list element.
     */
    private class GrouperClient implements Grouper.Client {
        public void groupChanged(int index, int groupIndex, int groupChangeType, boolean primary, int elementChangeType) {
            // handle the group change first
            if(groupChangeType == ListEvent.INSERT) {
                int expandedIndex = index + groupIndex;
                insertedSeparators.add(expandedIndex, SEPARATOR, 1);
                updates.addInsert(expandedIndex);
                // add the separator and link the separator to its node
                IndexedTreeNode<GroupSeparator> node = separators.addByNode(groupIndex, new GroupSeparator());
                node.getValue().setNode(node);
            } else if(groupChangeType == ListEvent.UPDATE) {
                int expandedIndex = insertedSeparators.getIndex(groupIndex, SEPARATOR);
                updates.addUpdate(expandedIndex);
            } else if(groupChangeType == ListEvent.DELETE) {
                int expandedIndex = insertedSeparators.getIndex(groupIndex, SEPARATOR);
                insertedSeparators.remove(expandedIndex, 1);
                updates.addDelete(expandedIndex);
                // invalidate the node
                IndexedTreeNode<GroupSeparator> node = separators.removeByIndex(groupIndex);
                node.getValue().setNode(null);
                // this group has gone away, make sure the other changes fired reflect that
                groupIndex--;
            }

            // then handle the element change
            if(elementChangeType == ListEvent.INSERT) {
                int expandedIndex = index + groupIndex + 1;
                insertedSeparators.add(expandedIndex, SOURCE_ELEMENT, 1);
                updates.addInsert(expandedIndex);
            } else if(elementChangeType == ListEvent.UPDATE) {
                int expandedIndex = index + groupIndex + 1;
                updates.addUpdate(expandedIndex);
            } else if(elementChangeType == ListEvent.DELETE) {
                int expandedIndex = index + groupIndex + 1;
                insertedSeparators.remove(expandedIndex, 1);
                updates.addDelete(expandedIndex);
            }
        }
    }

    /** {@inheritDoc} */
    public E get(int index) {
        Object type = insertedSeparators.get(index);
        if(type == SEPARATOR) return (E)separators.get(getSeparatorIndex(index));
        else if(type == SOURCE_ELEMENT) return source.get(getSourceIndex(index));
        else throw new IllegalStateException();
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        Object type = insertedSeparators.get(mutationIndex);
        if(type == SEPARATOR) return -1;
        else if(type == SOURCE_ELEMENT) return insertedSeparators.getColourIndex(mutationIndex, SOURCE_ELEMENT);
        else throw new IllegalStateException();
    }
    protected int getSeparatorIndex(int mutationIndex) {
        Object type = insertedSeparators.get(mutationIndex);
        if(type == SEPARATOR) return insertedSeparators.getColourIndex(mutationIndex, SEPARATOR);
        else if(type == SOURCE_ELEMENT) return -1;
        else throw new IllegalStateException();
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        updates.beginEvent(true);
        grouper.listChanged(listChanges);
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    public int size() {
        return insertedSeparators.size();
    }

    /**
     * A separator heading the elements of a group.
     */
    public interface Separator<E> {
        /**
         * Get the maximum number of elements in this group to show.
         */
        public int getLimit();

        /**
         * Set the maximum number of elements in this group to show. This is
         * useful to collapse a group (limit of 0), cap the elements of a group
         * (limit of 5) or reverse those actions.
         */
        public void setLimit(int limit);

        /**
         * Get the {@link List} of all elements in this group.
         */
        public List<E> getGroup();

        /**
         * A convenience method to get the first element from this group. This
         * is useful to render the separator's name.
         */
        public E first();

        /**
         * A convenience method to get the number of elements in this group. This
         * is useful to render the separator.
         */
        public int size();
    }

    /**
     * Implement the {@link Separator} interface in the most natural way.
     */
    private class GroupSeparator implements Separator<E> {
        private int limit = Integer.MAX_VALUE;

        /**
         * The node allows the separator to figure out which
         * group in the overall list its representing.
         */
        private IndexedTreeNode<GroupSeparator> node = null;

        /** {@inheritDoc} */
        public int getLimit() {
            return limit;
        }
        /** {@inheritDoc} */
        public void setLimit(int limit) {
            this.limit = limit;
            // handle limit change
            throw new UnsupportedOperationException();
        }
        /** {@inheritDoc} */
        public List<E> getGroup() {
            return source.subList(start(), end());
        }
        /** {@inheritDoc} */
        public E first() {
            return source.get(start());
        }
        /** {@inheritDoc} */
        public int size() {
            return end() - start();
        }

        /**
         * Set the {@link IndexedTreeNode} that this {@link Separator} can
         * use to find its index in the overall list of {@link Separator}s;
         */
        public void setNode(IndexedTreeNode<GroupSeparator> node) {
            this.node = node;
        }

        /**
         * The first index in the source containing an element from this group.
         */
        public int start() {
            if(this.node == null) throw new IllegalStateException();
            int separatorIndex = node.getIndex();
            if(separatorIndex == -1) throw new IllegalStateException();
            int groupStartIndex = insertedSeparators.getIndex(separatorIndex, SEPARATOR);
            return groupStartIndex - separatorIndex;
        }
        /**
         * The last index in the source containing an element from this group.
         */
        public int end() {
            if(this.node == null) throw new IllegalStateException();
            int nextSeparatorIndex = node.getIndex() + 1;
            if(nextSeparatorIndex == 0) throw new IllegalStateException();
            int nextGroupStartIndex = nextSeparatorIndex == insertedSeparators.colourSize(SEPARATOR) ? insertedSeparators.size() : insertedSeparators.getIndex(nextSeparatorIndex, SEPARATOR);
            return nextGroupStartIndex - nextSeparatorIndex;
        }

        /** {@inheritDoc} */
        public String toString() {
            return "" + size() + " elements starting with \"" + first() + "\"";
        }
    }
}