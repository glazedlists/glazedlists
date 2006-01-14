/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

// the core Glazed Lists package
import ca.odell.glazedlists.*;
// for keeping a list of changes
import java.util.*;

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
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class ListEvent<E> extends EventObject {
    
    /** different types of changes */
    public static final int DELETE = 0;
    public static final int UPDATE = 1;
    public static final int INSERT = 2;

    /** the number of blocks of changes seen by this view */
    private int blockCount;
    /** the current change block */
    private ListEventBlock currentBlock = null;
    /** the row index into the current change */
    private int rowIndex;
    
    /** the list that has changed */
    private EventList<E> sourceList;
    /** the master sequence that this is a view of */
    private ListEventAssembler<E> masterSequence;
    
    /**
     * Create a new list change sequence that uses the source master list
     * for the source of changes.
     */
    ListEvent(ListEventAssembler<E> masterSequence, EventList<E> sourceList) {
        super(sourceList);

        // keep track of the origin sequence and list
        this.masterSequence = masterSequence;
        this.sourceList = sourceList;
        
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
    public ListEvent(ListEvent<E> original) {
        super(original.sourceList);
        this.blockCount = original.blockCount;
        this.currentBlock = original.currentBlock;
        this.rowIndex = original.rowIndex;
        this.masterSequence = original.masterSequence;
        this.sourceList = original.sourceList;
    }
    
    /**
     * Resets this event's position to the previously-marked position. This should
     * be used for {@link TransformedList}s that require multiple-passes of the 
     * {@link ListEvent} in order to process it.
     */
    public void reset() {
        currentBlock = null;
        blockCount = 0;
    }

    /**
     * Increments the change sequence to view the next change. This will
     * return true if such a change exists and false when there is no
     * change to view.
     */
    public boolean next() {
        // we need to get a new change block from the queue
        if(currentBlock == null || rowIndex == currentBlock.getEndIndex()) {
            return nextBlock();
            
        // we can just increment the row on the current change
        } else {
            if(rowIndex >= currentBlock.getEndIndex()) throw new IllegalStateException();
            rowIndex++;
            return true;
        }
    }
    
    /**
     * Without incrementing the implicit iterator, this tests if there is another
     * change to view. The user will still need to call next() to view
     * such a change.
     */
    public boolean hasNext() {
        // we are at the end of the current block
        if(currentBlock == null || rowIndex == currentBlock.getEndIndex()) {
            return blockCount < getBlocks().size();

        // there is another change in the current block
        } else {
            return true;
        }
    }

    /**
     * Increments the change sequence to view the next change block.
     */
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
            rowIndex = currentBlock.getStartIndex();
            return true;
        }
    }
    
    /**
     * Tests if this change is a complete reordering of the list.
     */
    public boolean isReordering() {
        return (masterSequence.getReorderMap() != null);
    }
    
    /**
     * Gets the reorder map of this list. This will also increment the change
     * sequence to the next change.
     *
     * @return an array of integers where the the previous index of a value is
     *      stored at the current index of that value.
     */
    public int[] getReorderMap() {
        int[] reorderMap = masterSequence.getReorderMap();
        if(reorderMap == null) throw new IllegalStateException("Cannot get reorder map for a non-reordering change");
        // clear the list change
        //currentBlock = null;
        //rowIndex = -5;
        //blockCount = 0;
        return reorderMap;
    }

    /**
     * Gets the current row index. If the block type is delete, this
     * will always return the startIndex of the current list change.
     */
    public int getIndex() {
        return getType() == DELETE ? getBlockStartIndex() : rowIndex;
    }

    /**
     * Gets the first row of the current block of changes. Inclusive.
     */
    public int getBlockStartIndex() {
        if (currentBlock == null)
            throw new IllegalStateException("The ListEvent is not currently in a state to return a block start index");

        return currentBlock.getStartIndex();
    }

    /**
     * Gets the last row of the current block of changes. Inclusive.
     */
    public int getBlockEndIndex() {
        if (currentBlock == null)
            throw new IllegalStateException("The ListEvent is not currently in a state to return a block end index");

        return currentBlock.getEndIndex();
    }

    /**
     * Gets the type of the current change, which should be one of
     * ListEvent.INSERT, UPDATE, or DELETE.
     */
    public int getType() {
        if (currentBlock == null)
            throw new IllegalStateException("The ListEvent is not currently in a state to return a type");

        return currentBlock.getType();
    }
    
    /**
     * Get the List of ListEventBlocks for this change.
     */
    List<ListEventBlock> getBlocks() {
        return masterSequence.getBlocks();
    }
    
    /**
     * Gets the number of blocks currently remaining in this atomic change.
     */
    public int getBlocksRemaining() {
        // if we're not at the end of the current block, add one for that
        if(currentBlock != null && rowIndex < currentBlock.getEndIndex()) {
            return getBlocks().size() - blockCount + 1;
        } else {
            return getBlocks().size() - blockCount;
        }
    }

    /**
     * Gets the List where this event originally occured.
     */
    public EventList<E> getSourceList() {
        return sourceList;
    }
    
    /**
     * Gets this event as a String. This simply iterates through all blocks
     * and concatenates them.
     */
    public String toString() {
        return "ListEvent: " + masterSequence.getBlocks();
    }
}