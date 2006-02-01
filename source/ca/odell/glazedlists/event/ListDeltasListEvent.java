/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.EventList;

import java.util.List;

/**
 * A list event that iterates {@link ListDeltas} as the
 * datastore.
 *
 * <p><font color="#FF0000"><strong>Warning: </strong></font> this
 * class is part of an experimental new API.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class ListDeltasListEvent<E> extends ListEvent<E> {

    private ListDeltas.Iterator iterator;

    private ListEventAssembler.ListDeltasAssembler deltasAssembler;

    public ListDeltasListEvent(ListEventAssembler.ListDeltasAssembler deltasAssembler, EventList<E> sourceList) {
        super(sourceList);
        this.deltasAssembler = deltasAssembler;

        // start at the beginning of the iterator
        this.iterator = deltasAssembler.getListDeltas().iterator();
    }

    public ListEvent copy() {
        throw new UnsupportedOperationException();
    }

    public void reset() {
        iterator = deltasAssembler.getListDeltas().iterator();
    }

    public boolean next() {
        return iterator.next();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public boolean nextBlock() {
        return next();
    }

    public boolean isReordering() {
        return (deltasAssembler.getReorderMap() != null);
    }

    public int[] getReorderMap() {
        int[] reorderMap = deltasAssembler.getReorderMap();
        if(reorderMap == null) throw new IllegalStateException("Cannot get reorder map for a non-reordering change");
        return reorderMap;
    }

    public int getIndex() {
        return iterator.getIndex();
    }

    public int getBlockStartIndex() {
        return getIndex();
    }

    public int getBlockEndIndex() {
        return getIndex();
    }

    public int getType() {
        return iterator.getType();
    }

    List getBlocks() {
        throw new UnsupportedOperationException();
    }

    public int getBlocksRemaining() {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return "ListEvent: " + deltasAssembler.getListDeltas().toString();
    }
}