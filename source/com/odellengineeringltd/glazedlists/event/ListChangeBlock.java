/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.event;

// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
// for sorting the list
import java.util.List;

/**
 * Models a change to a list that may require a GUI object
 * to be repainted or another object to be updated.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class ListChangeBlock {
    
    /** different types of changes */
    public static final int DELETE = 0;
    public static final int UPDATE = 1;
    public static final int INSERT = 2;

    /** the range of indicies that have changed, inclusive */
    private int startIndex;
    private int endIndex;
    
    /** the type of change, INSERT, UPDATE or DELETE */
    private int type;
    
    /** a change which is not valid can be re-used to avoid new calls */
    private boolean valid;
    
    
    /**
     * Create a new single-entry list change of the specified index and type.
     */
    ListChangeBlock(int index, int type) {
        this.startIndex = index;
        this.endIndex = index;
        this.type = type;
        this.valid = true;
        assert(startIndex >= 0 && endIndex >= startIndex);
    }
    /**
     * Create a new single-entry list change of the specified start index, end
     * index and type.
     */
    ListChangeBlock(int startIndex, int endIndex, int type) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.type = type;
        this.valid = true;
        assert(startIndex >= 0 && endIndex >= startIndex);
    }
    ListChangeBlock() {
        this.valid = false;
    }
    void setData(int index, int type) {
        this.startIndex = index;
        this.endIndex = index;
        this.type = type;
        this.valid = true;
        assert(startIndex >= 0 && endIndex >= startIndex);
    }
    void setData(int startIndex, int endIndex, int type) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.type = type;
        this.valid = true;
        assert(startIndex >= 0 && endIndex >= startIndex);
    }
    
    /**
     * Setting a change to being valid means it is in use. Invalid
     * means that it can be recycled via object pooling.
     */
    void setInvalid() {
        this.valid = false;
    }
    boolean isValid() {
        return valid;
    }

    /**
     * Get the first index in the range of this change.
     */
    public int getStartIndex() {
        return startIndex;
    }
    /**
     * Get the last index in the range of this change.
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Get the type of this change.
     */
    public int getType() {
        return type;
    }
    
    /**
     * Attempts to append the specified event information to the current list
     * change. If the two events are similar enough (same type and contiguous),
     * then the append can proceed. If the events are not similar enough, a new
     * list change will be created.
     *
     * The two events must occur sequentially in the change, and the append
     * must be called with an index that reflects the list after the first
     * change.
     *
     * @return a new list change if such is created. Otherwise this returns
     *      null. The caller should compare the result to null and add the
     *      change if it is non-null. Otherwise the source list change has
     *      increased in size and no new object is necessary.
     */
    ListChangeBlock append(int appendStartIndex, int appendEndIndex, int type) {
        // bail if the types are different
        if(type != this.type) return null;
        // insert events: join if the ends touch
        if(type == INSERT && (appendStartIndex > endIndex + 1 || appendStartIndex < startIndex)) return null;
        // delete events: same if deleted from start index or one before start index
        else if(type == DELETE && (appendEndIndex < startIndex - 1 || appendStartIndex > startIndex)) return null;
        // update events: same if update is one from beginning or end
        else if(type == UPDATE && (appendEndIndex < startIndex - 1 || appendStartIndex > endIndex + 1)) return null;
        // on insert and delete, merge the current change by concatenating the lengths to the earliest start
        if(type == INSERT || type == DELETE) {
            int length = (endIndex - startIndex + 1) + (appendEndIndex - appendStartIndex + 1);
            startIndex = Math.min(appendStartIndex, startIndex);
            endIndex = startIndex + length - 1;
        // on update, simply take the lowest start and the biggest stop to eliminate common area
        } else if(type == UPDATE) {
            startIndex = Math.min(appendStartIndex, startIndex);
            endIndex = Math.max(appendEndIndex, endIndex);
        }
        assert(startIndex >= 0 && endIndex >= startIndex);
        return this;
    }
    
    /**
     * Gets the length of this block.
     */
    public int getLength() {
        return endIndex - startIndex + 1;
    }
    
    /**
     * Sorts the blocks of the specified list of changes. This ensures that
     * the user iterating the list of change blocks can view changes in
     * increasing order. This ensures that indicies will not be shifted.
     *
     * <p>This performs a bubble sort, swapping adjacent change blocks if
     * they should be swapped. The bubble sort is used instead of a more
     * efficient sort because it is necessary to adjust offsets when swapping
     * and therefore it is preferred to only swap adjacent elements. Bubble
     * sort is a sort that only swaps adjacent elements.
     */
    static void sortListChangeBlocks(List changes) {
        // bubblesort the changes
        while(true) {
            // count the number of swaps made on this repetition
            int swapCount = 0;
            // for each adjacent pair, make any swaps necessary
            int j = 0;
            while(j < changes.size() - 1) {
                ListChangeBlock first = (ListChangeBlock)changes.get(j);
                ListChangeBlock second = (ListChangeBlock)changes.get(j+1);
                
                if(canBeCombined(first, second)) {
                    combine(first, second);
                    changes.remove(j+1);
                    if(j > 0) j--;
                } else if(requiresSwap(first, second)) {
                    swapCount++;
                    shift(first, second);
                    changes.set(j, second);
                    changes.set(j+1, first);
                    j++;
                } else {
                    j++;
                }
            }
            // we're done if there were no changes this iteration
            if(swapCount == 0) break;
        }
    }
    
    /**
     * When there is a sequence of list changes, sometimes these changes are not
     * created in increasing order. Users usually need to receive changes in
     * increasing order, so it becomes necessary to reorder list change blocks.
     * This returns true if two adjacent blocks are out of order.
     */
    private static boolean requiresSwap(ListChangeBlock first, ListChangeBlock second) {
        // verify no intersection
        if(first.type != DELETE && first.type != second.type) {
            if(first.endIndex >= second.startIndex && first.startIndex <= second.endIndex) {
                throw new IllegalStateException("Change blocks " + first + " and " + second + " intersect");
            }
        }
        
        // test if these two require an swap
        if(second.type == INSERT) {
            return second.startIndex <= first.startIndex;
        } else {
            return second.startIndex < first.startIndex;
        }
    }
    
    /**
     * Tests if the specified pair of list change blocks can be combined into
     * a single larget list change block.
     */
    private static boolean canBeCombined(ListChangeBlock first, ListChangeBlock second) {
        if(first.type != second.type) return false;
        
        if(first.type == INSERT) {
            return (second.startIndex >= first.startIndex && second.startIndex <= first.endIndex + 1);
        } else if(first.type == DELETE) {
            return (second.startIndex <= first.startIndex && second.endIndex >= first.startIndex - 1);
        } else if(first.type == UPDATE) {
            return (second.startIndex <= first.endIndex && second.endIndex >= first.startIndex);
        } else {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Combines the specified pair of list change blocks. After the change blocks
     * have been combined, the first block contains all data from the first and second
     * blocks.
     */
    private static void combine(ListChangeBlock first, ListChangeBlock second) {
        if(first.type == INSERT || first.type == DELETE) {
            int startIndex = Math.min(first.startIndex, second.startIndex);
            int length = first.getLength() + second.getLength();
            first.startIndex = startIndex;
            first.endIndex = startIndex + length - 1;
        } else if(first.type == UPDATE) {
            int startIndex = Math.min(first.startIndex, second.startIndex);
            int endIndex = Math.max(first.endIndex, second.endIndex);
            first.startIndex = startIndex;
            first.endIndex = endIndex;
        }
        second.valid = false;
    }
    
    /**
     * When reordering blocks, the indicies within each block should be shifted
     * so that they represent the same change. This shifts this ListChangeBlock
     * as a consequence of the specified list change block being inserted in front
     * of it in a sequence.
     */
    private static void shift(ListChangeBlock alpha, ListChangeBlock beta) {
        int movedLength = beta.getLength();
        
        if(beta.type == INSERT) {
            alpha.startIndex += movedLength;
            alpha.endIndex += movedLength;
        } else if(beta.type == UPDATE) {
            // no shift
        } else if(beta.type == DELETE) {
            alpha.startIndex -= movedLength;
            alpha.endIndex -= movedLength;
            assert(alpha.startIndex >= 0);
        }
    }
    

    /**
     * Gets this ListChangeBlock represented as a String
     */
    public String toString() {
        String result = "";
        if(type == DELETE) result = result + "D.";
        else if(type == UPDATE) result = result + "U.";
        else if(type == INSERT) result = result + "I.";
        if(startIndex == endIndex) result = result + startIndex;
        else result = result + startIndex + "-" + endIndex;
        return result;
    }
}
