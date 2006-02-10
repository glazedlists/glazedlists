/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.Grouper;
import ca.odell.glazedlists.impl.adt.Barcode;
import ca.odell.glazedlists.impl.adt.BarcodeIterator;
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
    private List<Separator<E>> separators = new ArrayList<Separator<E>>();

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

        // prepare the separator and collapsed elements
        collapsedElements.addBlack(0, size());
        for(BarcodeIterator i = grouper.getBarcode().iterator(); i.hasNextBlack(); ) {
            i.nextBlack();
            separators.add(new GroupSeparator());
        }

        source.addListEventListener(this);
    }

    /**
     * Fire two events, one for the group (the separator) and another for the
     * actual list element.
     */
    private class GrouperClient implements Grouper.Client {
        public void groupChanged(int index, int groupIndex, int groupChangeType, boolean primary, int elementChangeType) {
            // add an event for the actual list element
            if(primary) {
                updates.addChange(elementChangeType, fromSourceIndex(index));
            }

            // add the group event
            updates.addChange(groupChangeType, fromGroupIndex(groupIndex));

            // update the list of separators
            if(groupChangeType == ListEvent.INSERT) {
                separators.add(groupIndex, new GroupSeparator());
            } else if(groupChangeType == ListEvent.DELETE) {
                separators.remove(groupIndex);
            }
        }
    }

    /**
     * Convert an index from source to view. This needs to offset for any
     * additional separators that wouldn't have been in the source list.
     */
    private int fromSourceIndex(int index) {
        int leadingSeparatorCount = grouper.getBarcode().getColourIndex(index, true, Grouper.UNIQUE) + 1;
        return index + leadingSeparatorCount;
    }
    private int fromGroupIndex(int groupIndex) {
        int regularIndex = grouper.getBarcode().getColourIndex(groupIndex, Grouper.UNIQUE);
        return groupIndex + regularIndex;
    }

    /** {@inheritDoc} */
    public E get(int index) {
        int sourceIndex = getSourceIndex(index);
        if(sourceIndex != -1) return source.get(sourceIndex);
        else return (E)separators.get(getSeparatorIndex(index));
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        // SLOW. . . . .
        // This method is dangerously slow and requires rework, possibly of the
        // datastructures for this entire class. The problem with this method is
        // that it does a linear search for the element, trying to reverse the
        // index. Aside from a binary search, there's no faster method to find
        // this value due to the limited amount of information available from our
        // barcode.
        for(int i = 0; i < source.size(); i++) {
            if(fromSourceIndex(i) == mutationIndex) {
                return i;
            }
        }
        return -1;
    }
    protected int getSeparatorIndex(int mutationIndex) {
        // SLOW . . . .
        for(int i = 0; i < grouper.getBarcode().colourSize(Grouper.UNIQUE); i++) {
            if(mutationIndex == i + grouper.getBarcode().getIndex(i, Grouper.UNIQUE)) {
                return i;
            }
        }
        return -1;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        updates.beginEvent(true);
        grouper.listChanged(listChanges);
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    public int size() {
        return grouper.getBarcode().colourSize(Grouper.UNIQUE) + source.size();
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
         * The first index in the source containing an element from this group.
         */
        public int start() {
            int separatorIndex = separators.indexOf(this);
            if(separatorIndex == -1) throw new IllegalStateException();
            return grouper.getBarcode().getIndex(separatorIndex, Grouper.UNIQUE);
        }
        /**
         * The last index in the source containing an element from this group.
         */
        public int end() {
            int separatorIndex = separators.indexOf(this);
            if(separatorIndex == -1) throw new IllegalStateException();
            return ((separatorIndex + 1) == grouper.getBarcode().blackSize()) ? grouper.getBarcode().size() : grouper.getBarcode().getIndex(separatorIndex + 1, Grouper.UNIQUE);
        }
    }
}