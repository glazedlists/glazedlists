/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.event.BlockSequence;
import ca.odell.glazedlists.impl.event.Tree4Deltas;

/**
 * A list event that iterates {@link Tree4Deltas} as the
 * datastore.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class Tree4DeltasListEvent<E> extends ListEvent<E> {

    private Tree4Deltas.Iterator deltasIterator;
    private BlockSequence.Iterator linearIterator;

    private ListEventAssembler deltasAssembler;

    public Tree4DeltasListEvent(ListEventAssembler deltasAssembler, EventList<E> sourceList) {
        super(sourceList);
        this.deltasAssembler = deltasAssembler;
    }

    /**
     * Create a copy of this list event.
     */
    @Override
    public ListEvent<E> copy() {
        Tree4DeltasListEvent<E> result = new Tree4DeltasListEvent<E>(deltasAssembler, sourceList);
        result.deltasIterator = deltasIterator != null ? deltasIterator.copy() : null;
        result.linearIterator = linearIterator != null ? linearIterator.copy() : null;
        result.deltasAssembler = deltasAssembler;
        return result;
    }

    @Override
    public void reset() {
        // prefer to use the linear blocks, which are faster
        if(deltasAssembler.getUseListBlocksLinear()) {
            this.linearIterator = deltasAssembler.getListBlocksLinear().iterator();
            this.deltasIterator = null;

        // otherwise use the deltas, which are more general
        } else {
            this.deltasIterator = deltasAssembler.getListDeltas().iterator();
            this.linearIterator = null;
        }
    }

    @Override
    public boolean next() {
        if(linearIterator != null) return linearIterator.next();
        else return deltasIterator.next();
    }

    @Override
    public boolean hasNext() {
        if(linearIterator != null) return linearIterator.hasNext();
        else return deltasIterator.hasNext();
    }

    @Override
    public boolean nextBlock() {
        if(linearIterator != null) return linearIterator.nextBlock();
        else return deltasIterator.nextNode();
    }

    @Override
    public boolean isReordering() {
        return (deltasAssembler.getReorderMap() != null);
    }

    @Override
    public int[] getReorderMap() {
        int[] reorderMap = deltasAssembler.getReorderMap();
        if(reorderMap == null) throw new IllegalStateException("Cannot get reorder map for a non-reordering change");
        return reorderMap;
    }

    @Override
    public int getIndex() {
        if(linearIterator != null) return linearIterator.getIndex();
        else return deltasIterator.getIndex();
    }

    @Override
    public int getBlockStartIndex() {
        if(linearIterator != null) return linearIterator.getBlockStart();
        else return deltasIterator.getIndex();
    }

    @Override
    public int getBlockEndIndex() {
        if(linearIterator != null) return linearIterator.getBlockEnd() - 1;
        else return deltasIterator.getEndIndex() - 1;
    }

    @Override
    public int getType() {
        if(linearIterator != null) {
            return linearIterator.getType();
        } else {
            return deltasIterator.getType();
        }
    }

    @Override
    public E getOldValue() {
        if(linearIterator != null) {
            return (E)linearIterator.getOldValue();
        } else {
            return (E)deltasIterator.getOldValue();
        }
    }

    @Override
    public E getNewValue() {
        // TODO(jessewilson):
        return ListEvent.<E>unknownValue();
    }

    @Override
    public int getBlocksRemaining() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        if(linearIterator != null) {
            return "ListEvent: " + deltasAssembler.getListBlocksLinear().toString();
        } else {
            return "ListEvent: " + deltasAssembler.getListDeltas().toString();
        }
    }
}
