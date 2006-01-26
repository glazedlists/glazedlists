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

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SeparatorList<E> extends TransformedList<E, E> {

    /** the grouping service manages finding where to insert groups */
    private final Grouper<E> grouper;

    /** a barcode where separator values are mixed in with regular values */
    private final Barcode separatorsBarcode = new Barcode();
    private static final Object SEPARATOR = Barcode.BLACK;
    private static final Object REGULAR = Barcode.WHITE;


    public SeparatorList(EventList<E> source) {
        this(source, (Comparator<E>) GlazedLists.comparableComparator());
    }

    public SeparatorList(EventList<E> source, Comparator comparator) {
        this(new SortedList<E>(source, comparator), comparator, null);
    }

    private SeparatorList(SortedList<E> source, Comparator comparator, Void dummyParameter) {
        super(source);

        // prepare the grouper
        GrouperClient grouperClient = new GrouperClient();
        this.grouper = new Grouper<E>(source, grouperClient);

        // update the separators
        separatorsBarcode.add(0, REGULAR, source.size());
        for(BarcodeIterator i = grouper.getBarcode().iterator(); i.hasNextColour(Grouper.UNIQUE); ) {
            i.nextColour(Grouper.UNIQUE);
            separatorsBarcode.add(i.getIndex(), SEPARATOR, 1);
        }

        source.addListEventListener(this);
    }

    /**
     * Fire two events, one for the group (the separator) and another for the
     * actual list element.
     */
    private class GrouperClient implements Grouper.Client {
        public void groupChanged(int index, int groupIndex, int groupChangeType, boolean primary, int elementChangeType) {
            // we can tell if its the '1st' element in a group via the barcode
            // the best approach updates the group and element simultaneously?

            // divide and conquer for now
            if(groupChangeType == ListEvent.DELETE) {
                if(primary) {
                    int outputIndex = separatorsBarcode.getIndex(index, REGULAR);
                    if(elementChangeType == ListEvent.UPDATE) {
                        updates.addChange(ListEvent.UPDATE, outputIndex);
                    } else if(elementChangeType == ListEvent.DELETE) {
                        separatorsBarcode.remove(outputIndex, 1);
                        updates.addChange(ListEvent.DELETE, outputIndex);
                    } else {
                        throw new IllegalStateException();
                    }
                }
                int separatorIndex = separatorsBarcode.getIndex(groupIndex, SEPARATOR);
                separatorsBarcode.remove(separatorIndex, 1);
                updates.addChange(ListEvent.DELETE, separatorIndex);

            } else if(groupChangeType == ListEvent.UPDATE) {
                throw new UnsupportedOperationException();
//                if(primary) {
//                    if(elementChangeType == ListEvent.INSERT) {
//                        int leadingSeparators = grouper.getBarcode().getColourIndex(index, true, Grouper.UNIQUE);
//                        int outputIndex = index + leadingSeparators;
//                        separatorsBarcode.add(outputIndex, REGULAR, 1);
//                        updates.addChange(ListEvent.INSERT, outputIndex);
//                    }
//
//
//                }

            } else if(groupChangeType == ListEvent.INSERT) {
                throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * Convert an index from source to view. This needs to offset for any
     * additional separators that wouldn't have been in the source list.
     */
    private int sourceIndexToViewIndex(int index) {
        int leadingSeparatorCount = grouper.getBarcode().getColourIndex(index, true, Grouper.UNIQUE) + 1;
        return index + leadingSeparatorCount;
    }
    private int groupIndexToSeparatorIndex(int groupIndex) {
        int regularIndex = grouper.getBarcode().getIndex(groupIndex, Grouper.UNIQUE);
        return groupIndex + regularIndex;
    }
    private int groupIndex(int index) {
        return grouper.getBarcode().getColourIndex(index, Grouper.UNIQUE);
    }

    public E get(int index) {
        int sourceIndex = getSourceIndex(index);
        if(sourceIndex == -1) return (E)"SEPARATOR";
        return source.get(sourceIndex);
    }

    /**
     * Convert an index from view to source.
     *
     * @return the index into the source list if this corresponds to a
     *      regular element. If this corresponds to a separator, the index
     *      of that separator will be returned.
     */
    protected int getSourceIndex(int transformedIndex) {
        throw new UnsupportedOperationException();
    }

    public void listChanged(ListEvent<E> listChanges) {
        updates.beginEvent(true);
        grouper.listChanged(listChanges);
        updates.commitEvent();
    }

    public int size() {
        return grouper.getBarcode().colourSize(Grouper.UNIQUE) + source.size();
    }

    /**
     * A separator inserted into the {@link EventList} at the start of a new
     * group.
     */
    public interface Separator {
        List getGroup();
        void setLimit(int limit);
        int getLimit();
    }
}