/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;

import java.util.EventObject;
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
public abstract class ListEvent<E> extends EventObject {
    
    /** different types of changes */
    public static final int DELETE = 0;
    public static final int UPDATE = 1;
    public static final int INSERT = 2;

    /** indicates a removed element whose value is unknown */
    public static final Object UNKNOWN_VALUE = new String("UNKNOWN VALUE");

    /** the list that has changed */
    protected EventList<E> sourceList;

    /**
     * Create a new list change sequence that uses the source master list
     * for the source of changes.
     */
    ListEvent(EventList<E> sourceList) {
        super(sourceList);

        // keep track of the origin sequence and list
        this.sourceList = sourceList;
    }

    /**
     * Create a bitwise copy of this {@link ListEvent}.
     */
    public abstract ListEvent<E> copy();

    /**
     * Resets this event's position to the previously-marked position. This should
     * be used for {@link TransformedList}s that require multiple-passes of the 
     * {@link ListEvent} in order to process it.
     */
    public abstract void reset();

    /**
     * Increments the change sequence to view the next change. This will
     * return true if such a change exists and false when there is no
     * change to view.
     */
    public abstract boolean next();
    
    /**
     * Without incrementing the implicit iterator, this tests if there is another
     * change to view. The user will still need to call next() to view
     * such a change.
     */
    public abstract boolean hasNext();

    /**
     * Increments the change sequence to view the next change block.
     */
    public abstract boolean nextBlock();
    
    /**
     * Tests if this change is a complete reordering of the list.
     */
    public abstract boolean isReordering();
    /**
     * Gets the reorder map of this list. This will also increment the change
     * sequence to the next change.
     *
     * @return an array of integers where the the previous index of a value is
     *      stored at the current index of that value.
     */
    public abstract int[] getReorderMap();

    /**
     * Gets the current row index. If the block type is delete, this
     * will always return the startIndex of the current list change.
     */
    public abstract int getIndex();

    /**
     * Gets the first row of the current block of changes. Inclusive.
     */
    public abstract int getBlockStartIndex();

    /**
     * Gets the last row of the current block of changes. Inclusive.
     */
    public abstract int getBlockEndIndex();

    /**
     * Gets the type of the current change, which should be one of
     * ListEvent.INSERT, UPDATE, or DELETE.
     */
    public abstract int getType();

    /**
     * Gets the previous value for a deleted or updated element. If that data is
     * not available, this will return {@link ListEvent#UNKNOWN_VALUE}.
     *
     * @deprecated this is a <strong>developer preview</strong> API that is not
     * yet fit for human consumption. Hopefully the full implementation is
     * complete for Glazed Lists 2.0.
     */
    public abstract E getOldValue();

    /**
     * Gets the current value for an inserted or updated element. If that data is
     * not available, this will return {@link ListEvent#UNKNOWN_VALUE}.
     *
     * @deprecated this is a <strong>developer preview</strong> API that is not
     * yet fit for human consumption. Hopefully the full implementation is
     * complete for Glazed Lists 2.0.
     */
    public abstract E getNewValue();

    /**
     * Get the List of ListEventBlocks for this change.
     *
     * @deprecated this method depends on a particular implementation of
     *      how list events are stored internally, and this implementation has
     *      since changed.
     */
    abstract List<Block> getBlocks();

    /**
     * Gets the number of blocks currently remaining in this atomic change.
     *
     * @deprecated this method depends on a particular implementation of
     *      how list events are stored internally, and this implementation has
     *      since changed.
     */
    public abstract int getBlocksRemaining();

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
    public abstract String toString();
}