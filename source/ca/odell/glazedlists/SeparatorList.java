/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.Grouper;
import ca.odell.glazedlists.event.ListEvent;

import java.util.Comparator;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SeparatorList<E> extends TransformedList<E, E> {

    /** the grouping service manages finding where to insert groups */
    private final Grouper<E> grouper;

    public SeparatorList(EventList<E> source) {
        this(source, (Comparator<E>) GlazedLists.comparableComparator());
    }

    public SeparatorList(EventList<E> source, Comparator comparator) {
        this(new SortedList<E>(source, comparator), comparator, null);
    }

    private SeparatorList(SortedList<E> source, Comparator comparator, Void dummyParameter) {
        super(source);

        GrouperClient grouperClient = new GrouperClient();
        this.grouper = new Grouper<E>(source, grouperClient);

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
        }
    }

    /**
     * Convert an index from source to view. This needs to offset for any
     * additional separators that wouldn't have been in the source list.
     */
    private int fromSourceIndex(int index) {

//      0 1 2 3 4 5 6 7 8 9 0 1 2 3
//      S D _ _ _ S D _ _ _ S D _ _
//      0 1 2 3 4 5 6 7 8 9 0 1 2 3
//      D _ _ _ D _ _ _ D _ _ D D
//      INDEX + # SEPARATORS IN (0, index inclusive

        int leadingSeparatorCount = grouper.getBarcode().getColourIndex(index, true, Grouper.UNIQUE) + 1;
        return index + leadingSeparatorCount;
    }
    private int fromGroupIndex(int groupIndex) {
        int regularIndex = grouper.getBarcode().getColourIndex(groupIndex, Grouper.UNIQUE);
        return groupIndex + regularIndex;
    }

    public E get(int index) {
        int sourceIndex = getSourceIndex(index);
        if(sourceIndex == -1) return (E)"SEPARATOR";
        return source.get(sourceIndex);
    }

    /**
     * Convert an index from view to source.
     */
    protected int getSourceIndex(int mutationIndex) {
        // SLOW
        for(int i = 0; i < source.size(); i++) {
            if(fromSourceIndex(i) == mutationIndex) {
                return i;
            }
        }
        return -1;
    }

    public void listChanged(ListEvent<E> listChanges) {
        updates.beginEvent(true);
        grouper.listChanged(listChanges);
        updates.commitEvent();
    }

    public int size() {
        return grouper.getBarcode().colourSize(Grouper.UNIQUE) + source.size();
    }
}