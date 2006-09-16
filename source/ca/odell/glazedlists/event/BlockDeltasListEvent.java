/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

// the core Glazed Lists package
import ca.odell.glazedlists.EventList;

import java.util.List;

/**
 * A ListEvent models a change to a list.
 *
 * <p>The lists may change over time, causing this sequence of changes to grow
 * indefinitely. The event is accessed like an iterator, with the user calling
 * next() repeatedly to view the changes in sequence.
 *
 * <p>It is also possible to view changes in blocks, which may provide some
 * performance benefit. To use this, use the nextBlock() method instead of the
 * next() method.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
final class BlockDeltasListEvent<E> extends ListEvent<E> {
    
    /** the number of blocks of changes seen by this view */
    private int blockCount;
    /** the current change block */
    private Block currentBlock = null;
    /** the row index into the current change */
    private int rowIndex;

    private ListEventAssembler.BlockDeltasAssembler blocksAssembler;
    /**
     * Create a new list change sequence that uses the source master list
     * for the source of changes.
     */
    BlockDeltasListEvent(ListEventAssembler.BlockDeltasAssembler blocksAssembler, EventList<E> sourceList) {
        super(sourceList);
        this.blocksAssembler = blocksAssembler;

        // start this event where the sequence is currently
        blockCount = 0;
    }
    
    /**
     * Creates a clone of this ListEvent, in order to iterate
     * through the changes multiple times. You should always be sure to
     * iterate through <strong>all</strong> values in the original instance
     * in order to reach the freshest changes.
     *
     * <p>Because the master change sequence does not know about clones, it is
     * possible that the change information stored in the master list will
     * be cleaned up without notifying the clone. In order to prevent this,
     * you should always use the clone list before using the original list -
     * this way the existance of values in the original list guarantees that
     * such values will be visible to the clone.
     */
    public BlockDeltasListEvent(BlockDeltasListEvent<E> original) {
        super(original.sourceList);
        this.blocksAssembler = original.blocksAssembler;
        this.blockCount = original.blockCount;
        this.currentBlock = original.currentBlock;
        this.rowIndex = original.rowIndex;
        this.sourceList = original.sourceList;
    }

    /** {@inheritDoc} */
    public ListEvent copy() {
        return new BlockDeltasListEvent<E>(this);
    }

    /** {@inheritDoc} */
    public void reset() {
        currentBlock = null;
        blockCount = 0;
    }

    /** {@inheritDoc} */
    public boolean next() {
        // we need to get a new change block from the queue
        if(currentBlock == null || rowIndex == getBlockEndIndex()) {
            return nextBlock();
            
        // we can just increment the row on the current change
        } else {
            if(rowIndex >= getBlockEndIndex()) throw new IllegalStateException();
            rowIndex++;
            return true;
        }
    }
    
    /** {@inheritDoc} */
    public boolean hasNext() {
        // we are at the end of the current block
        if(currentBlock == null || rowIndex == getBlockEndIndex()) {
            return blockCount < getBlocks().size();

        // there is another change in the current block
        } else {
            return true;
        }
    }

    /** {@inheritDoc} */
    public boolean nextBlock() {
        // we have no blocks left
        if(blockCount == getBlocks().size()) {
            currentBlock = null;
            rowIndex = -5;
            blockCount = 0;
            return false;

        // we have more blocks left
        } else {
            currentBlock = getBlocks().get(blockCount);
            blockCount++;
            rowIndex = getBlockStartIndex();
            return true;
        }
    }
    
    /** {@inheritDoc} */
    public boolean isReordering() {
        return (blocksAssembler.getReorderMap() != null);
    }
    
    /** {@inheritDoc} */
    public int[] getReorderMap() {
        int[] reorderMap = blocksAssembler.getReorderMap();
        if(reorderMap == null) throw new IllegalStateException("Cannot get reorder map for a non-reordering change");
        // clear the list change
        //currentBlock = null;
        //rowIndex = -5;
        //blockCount = 0;
        return reorderMap;
    }

    /** {@inheritDoc} */
    public int getIndex() {
        return getType() == DELETE ? getBlockStartIndex() : rowIndex;
    }

    /** {@inheritDoc} */
    public int getBlockStartIndex() {
        if (currentBlock == null) throw new IllegalStateException();

        return currentBlock.getStartIndex();
    }

    /** {@inheritDoc} */
    public int getBlockEndIndex() {
        if (currentBlock == null) throw new IllegalStateException();

        return currentBlock.getEndIndex();
    }

    /** {@inheritDoc} */
    public int getType() {
        if (currentBlock == null) throw new IllegalStateException();

        return currentBlock.getType();
    }
    
    /** {@inheritDoc} */
    public E getRemovedValue() {
        return (E)ListEvent.UNKNOWN_VALUE;
    }

    /** {@inheritDoc} */
    List<Block> getBlocks() {
        return blocksAssembler.getBlocks();
    }
    
    /** {@inheritDoc} */
    public int getBlocksRemaining() {
        // if we're not at the end of the current block, add one for that
        if(currentBlock != null && rowIndex < getBlockEndIndex()) {
            return getBlocks().size() - blockCount + 1;
        } else {
            return getBlocks().size() - blockCount;
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return "ListEvent: " + blocksAssembler.getBlocks();
    }
}