/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.event;

// the core Glazed Lists package
import ca.odell.glazedlists.*;
// for when the user asks for a change that's not there
import java.util.NoSuchElementException;
// for keeping a list of changes
import java.util.*;

/**
 * A ListEvent models a change to a list.
 *
 * <p>The lists may change over time, causing this sequence of changes to grow
 * indefinitely. The event is accessed like an iterator, with the user calling
 * next() repeatedly to view the changes in sequence.
 *
 * <p>The user must call next() until it returns false in order to increment the
 * iterator until its end. Otherwise the next change event notification will
 * first include unseen changes from the current change. In order to clear the
 * current location of the iterator, use the clearEventQueue() method. This will
 * clear the values of pending events.
 *
 * <p>It is also possible to view changes in blocks, which may provide some
 * performance benefit. To use this, use the nextBlock() method instead of the
 * next() method.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class ListEvent extends EventObject {
    
    /** different types of changes */
    public static final int DELETE = 0;
    public static final int UPDATE = 1;
    public static final int INSERT = 2;

    /** the number of atomic changes seen by this view */
    private int atomicCount;
    /** the number of blocks of changes seen by this view */
    private int blockCount;
    /** the current change */
    private ListEventBlock listChange = null;
    /** the row index into the current change */
    private int rowIndex;
    
    /** the list that has changed */
    private EventList sourceList;
    /** the master sequence that this is a view of */
    private ListEventAssembler masterSequence;
    
    /**
     * Create a new list change sequence that uses the source master list
     * for the source of changes.
     */
    ListEvent(ListEventAssembler masterSequence, EventList sourceList) {
        super(sourceList);

        // keep track of the origin sequence and list
        this.masterSequence = masterSequence;
        this.sourceList = sourceList;
        
        // start this event where the sequence is currently
        atomicCount = masterSequence.getAtomicCount();
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
    public ListEvent(ListEvent original) {
        super(original.sourceList);
        this.blockCount = original.blockCount;
        this.listChange = original.listChange;
        this.rowIndex = original.rowIndex;
        this.masterSequence = original.masterSequence;
        this.sourceList = original.sourceList;
        this.atomicCount = original.atomicCount;
    }
    
    
    /**
     * Clears the queue of all unprocessed changes. This is used when a
     * listener reloads the source list rather than modifying it by
     * differences.
     *
     * <p>If the user is manually clearing the event queue with this method,
     * it is also necessary to call hasNext() before calling next() when
     * receiving new events. This is because it is possible that pending
     * events will be cleared before they are processed. Otherwise the
     * call to next() may fail.
     */
    public void clearEventQueue() {
        atomicCount = masterSequence.getAtomicCount();
        listChange = null;
        blockCount = 0;
    }
    
    /**
     * Increments the change sequence to view the next change. This will
     * return true if such a change exists and false when there is no
     * change to view.
     */
    public boolean next() {
        // if we need to get a new change block from the queue
        if(listChange == null || rowIndex == listChange.getEndIndex()) {
            // if we are at the end of the current block
            return nextBlock();
        // if we can just increment the row on the current change
        } else {
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
        if(listChange == null || rowIndex == listChange.getEndIndex()) {
            // if there are no more atomic changes
            if(atomicCount == masterSequence.getAtomicCount()) {
                return false;
            // there are no more blocks in this atomic change
            } else if(blockCount == masterSequence.getBlockCount(atomicCount)) {
                return false;
            // there are more blocks in this atomic change
            } else {
                return true;
            }
        // there is another change in the current block
        } else {
            return true;
        }
    }

    /**
     * Increments the change sequence to view the next change block.
     */
    public boolean nextBlock() {
        // if we have no blocks left in the current atomic change
        if(blockCount == masterSequence.getBlockCount(atomicCount)) {
            // clear the list change
            listChange = null;
            rowIndex = -5;
            blockCount = 0;
            // prepare for the next atomic change
            if(atomicCount >= masterSequence.getAtomicCount()) {
                throw new NoSuchElementException("Cannot iterate past the total number of changes!");
            }
            atomicCount++;
            // notify that this change is over
            return false;
        // if we have more blocks left
        } else {
            listChange = masterSequence.getBlock(atomicCount, blockCount);
            blockCount++;
            rowIndex = listChange.getStartIndex();
            return true;
        }
    }

    /**
     * Gets the current row index. If the listChange type is delete, this
     * will always return the startIndex of the current list change.
     */
    public int getIndex() {
        if(listChange.getType() == DELETE) return listChange.getStartIndex();
        return rowIndex;
    }

    /**
     * Gets the first row of the current block of changes. Inclusive.
     */
    public int getBlockStartIndex() {
        return listChange.getStartIndex();
    }

    /**
     * Gets the last row of the current block of changes. Inclusive.
     */
    public int getBlockEndIndex() {
        return listChange.getEndIndex();
    }

    /**
     * Gets the type of the current change, which should be one of
     * ListEvent.INSERT, UPDATE, or DELETE.
     */
    public int getType() {
        return listChange.getType();
    }

    /**
     * Gets the count of the number of blocks seen by this view. This is used
     * by the master list in order to get rid of blocks that have been seen by
     * all views.
     */
    public int getAtomicChangeCount() {
        return atomicCount;
    }

    /**
     * Gets the number of blocks currently remaining in this atomic change.
     */
    public int getBlocksRemaining() {
        // if we're not at the end of the current block, add one for that
        if(listChange != null && rowIndex < listChange.getEndIndex()) {
            return masterSequence.getBlockCount(atomicCount) - blockCount + 1;
        } else {
            return masterSequence.getBlockCount(atomicCount) - blockCount;
        }
    }

    /**
     * Gets the List where this event originally occured.
     */
    public EventList getSourceList() {
        return sourceList;
    }
    
    /**
     * Gets this event as a String. This simply iterates through all blocks
     * and concatenates them.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("Event " + atomicCount + ": ");
        for(int b = 0; b < masterSequence.getBlockCount(atomicCount); b++) {
            if(b != 0) result.append(", ");
            result.append(masterSequence.getBlock(atomicCount, b));
        }
        return result.toString();
    }
}
