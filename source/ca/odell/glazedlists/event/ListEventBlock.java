/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

// for sorting the list
import java.util.List;

/**
 * Models a change to a list that may require a GUI object
 * to be repainted or another object to be updated.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
final class ListEventBlock {
    
    /** the range of indicies that have changed, inclusive */
    private int startIndex;
    private int endIndex;
    
    /** the type of change, INSERT, UPDATE or DELETE */
    private int type;
    
    /**
     * Create a new single-entry list change of the specified index and type.
     */
    ListEventBlock(int index, int type) {
        this(index, index, type);
    }
    
    /**
     * Create a new single-entry list change of the specified start index, end
     * index and type.
     */
    ListEventBlock(int startIndex, int endIndex, int type) {
        setData(startIndex, endIndex, type);
    }

    /**
     * Set this list change block to use the specified data.
     */
    void setData(int index, int type) {
        setData(index, index, type);
    }

    /**
     * Set this list change block to use the specified data.
     */
    void setData(int startIndex, int endIndex, int type) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.type = type;
        if(startIndex < 0 || endIndex < startIndex) throw new IndexOutOfBoundsException("Illegal range: " + startIndex + ", " + endIndex);
        if(type != ListEvent.INSERT && type != ListEvent.UPDATE && type != ListEvent.DELETE) throw new IllegalArgumentException();
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
     * <p>The two events must occur sequentially in the change, and the append
     * must be called with an index that reflects the list after the first
     * change.
     *
     * @return true if the append was successful or false if the user must
     *      use a different list change block for the appended change. 
     */
    boolean append(int appendStartIndex, int appendEndIndex, int type) {
        // bail if the types are different
        if(type != this.type) return false;
        // insert events: join if the ends touch
        if(type == ListEvent.INSERT && (appendStartIndex > endIndex + 1 || appendStartIndex < startIndex)) return false;
        // delete events: same if deleted from start index or one before start index
        else if(type == ListEvent.DELETE && (appendEndIndex < startIndex - 1 || appendStartIndex > startIndex)) return false;
        // update events: same if update is one from beginning or end
        else if(type == ListEvent.UPDATE && (appendEndIndex < startIndex - 1 || appendStartIndex > endIndex + 1)) return false;
        // on insert and delete, merge the current change by concatenating the lengths to the earliest start
        if(type == ListEvent.INSERT || type == ListEvent.DELETE) {
            int length = (endIndex - startIndex + 1) + (appendEndIndex - appendStartIndex + 1);
            startIndex = Math.min(appendStartIndex, startIndex);
            endIndex = startIndex + length - 1;
        // on update, simply take the lowest start and the biggest stop to eliminate common area
        } else if(type == ListEvent.UPDATE) {
            startIndex = Math.min(appendStartIndex, startIndex);
            endIndex = Math.max(appendEndIndex, endIndex);
        }
        if(startIndex < 0 || endIndex < startIndex) throw new IllegalStateException();
        return true;
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
     *
     * @param allowContradictingEvents whether a pair of contradicting events shall
     *      be allowed. This would typically be an insert that is wiped away with a
     *      delete, an update that is wiped away with a delete, or an insert that
     *      is later updated.
     */
    static void sortListEventBlocks(List<ListEventBlock> changes, boolean allowContradictingEvents) {
        // bubblesort the changes
        while(true) {
            // count the number of swaps made on this repetition
            int swapCount = 0;
            // for each adjacent pair, make any swaps necessary
            int j = 0;
            while(j < changes.size() - 1) {
                ListEventBlock first = changes.get(j);
                ListEventBlock second = changes.get(j+1);
                
                if(blocksContradict(first, second)) {
                    if(!allowContradictingEvents) throw new IllegalStateException("Change blocks " + first + " and " + second + " intersect");
                    simplifyContradiction(changes.subList(j, j+2));
                    swapCount++;
                } else if(requiresSplit(first, second)) {
                    ListEventBlock third = split(first, second);
                    changes.add(j+2, third);
                } else if(canBeCombined(first, second)) {
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
     * When one block undoes part of another, we have a contradiction.
     * Types of contradictions:
     * <ul>
     *    <li>an insert is later updated
     *    <li>an insert is later deleted
     *    <li>an update is later deleted
     *    <li>an update is later updated
     * </ul>
     *
     * @return true if the blocks contradict.
     */
    private static boolean blocksContradict(ListEventBlock first, ListEventBlock second) {
        // if the ranges intersect
        boolean rangesIntersect = (first.endIndex >= second.startIndex && first.startIndex <= second.endIndex);
        if(!rangesIntersect) return false;
        
        // (insert or update) is later (deleted or updated)
        if(first.type != ListEvent.DELETE && second.type != ListEvent.INSERT) {
            return true;
        }
        
        // can't be an contradiction
        return false;
    }
    
    /**
     * Removes the contradiction contained within the specified list of two blocks.
     */
    private static void simplifyContradiction(List<ListEventBlock> contradictingPair) {
        if(contradictingPair.size() != 2) throw new IllegalStateException();
        ListEventBlock first = contradictingPair.get(0);
        ListEventBlock second = contradictingPair.get(1);
        
        // get the overlap range
        int commonStart = Math.max(first.startIndex, second.startIndex);
        int commonEnd = Math.min(first.endIndex, second.endIndex);
        int commonLength = (commonEnd - commonStart + 1);
        
        // insert then delete kill each other
        if(first.type == ListEvent.INSERT && second.type == ListEvent.DELETE) {
            first.endIndex -= commonLength;
            second.endIndex -= commonLength;
            if(second.getLength() == 0) contradictingPair.remove(1);
            if(first.getLength() == 0) contradictingPair.remove(0);
            return;

        // insert then update shortens update and reorders the two changes
        } else if(first.type == ListEvent.INSERT && second.type == ListEvent.UPDATE) {
            // remove the update and shorten it
            contradictingPair.remove(1);
            second.endIndex -= commonLength;
            int secondLength = second.getLength();
            if(secondLength == 0) return;
            // shift the update and make it first, chronologically
            second.startIndex = Math.min(first.startIndex, second.startIndex);
            second.endIndex = second.startIndex + secondLength - 1;
            contradictingPair.add(0, second); 

        // update then delete shortens update and reorders the two changes
        } else if(first.type == ListEvent.UPDATE && second.type == ListEvent.DELETE) {
            // remove the update and shorten it
            contradictingPair.remove(0);
            first.endIndex -= commonLength;
            int firstLength = first.getLength();
            if(firstLength == 0) return;
            // shift the update and make it second, chronologically
            first.startIndex = Math.min(first.startIndex, second.startIndex);
            first.endIndex = first.startIndex + firstLength - 1;
            contradictingPair.add(1, first);

        // update then update is the span of both
        } else if(first.type == ListEvent.UPDATE && second.type == ListEvent.UPDATE) {
            first.startIndex = Math.min(first.startIndex, second.startIndex);
            first.endIndex = Math.max(first.endIndex, second.endIndex);
            contradictingPair.remove(1);
        }
    }
    
    /**
     * When there is a sequence of list changes, sometimes these changes are not
     * created in increasing order. Users usually need to receive changes in
     * increasing order, so it becomes necessary to reorder list change blocks.
     *
     * @return true if two adjacent blocks are out of order.
     */
    private static boolean requiresSwap(ListEventBlock first, ListEventBlock second) {
        // verify no intersection
        /*if(first.type == ListEvent.INSERT && second.type != ListEvent.INSERT) {
            if(first.endIndex >= second.startIndex && first.startIndex <= second.endIndex) {
                throw new IllegalStateException("Change blocks " + first + " and " + second + " intersect");
            }
        }*/
        
        // test if these two require an swap
        if(second.type == ListEvent.INSERT && first.type != ListEvent.DELETE) {
            return second.startIndex <= first.startIndex;
        } else {
            return second.startIndex < first.startIndex;
        }
    }
    
    /**
     * Tests if the specified pair of list change blocks can be combined into
     * a single larget list change block.
     */
    private static boolean canBeCombined(ListEventBlock first, ListEventBlock second) {
        if(first.type != second.type) return false;
        
        if(first.type == ListEvent.INSERT) {
            return (second.startIndex >= first.startIndex && second.startIndex <= first.endIndex + 1);
        } else if(first.type == ListEvent.DELETE) {
            return (second.startIndex <= first.startIndex && second.endIndex >= first.startIndex - 1);
        } else if(first.type == ListEvent.UPDATE) {
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
    private static void combine(ListEventBlock first, ListEventBlock second) {
        if(first.type == ListEvent.INSERT || first.type == ListEvent.DELETE) {
            int startIndex = Math.min(first.startIndex, second.startIndex);
            int length = first.getLength() + second.getLength();
            first.startIndex = startIndex;
            first.endIndex = startIndex + length - 1;
        } else if(first.type == ListEvent.UPDATE) {
            int startIndex = Math.min(first.startIndex, second.startIndex);
            int endIndex = Math.max(first.endIndex, second.endIndex);
            first.startIndex = startIndex;
            first.endIndex = endIndex;
        }
    }
    
    /**
     * When reordering blocks, the indicies within each block should be shifted
     * so that they represent the same change. This shifts this ListEventBlock
     * as a consequence of the specified list change block being inserted in front
     * of it in a sequence.
     */
    private static void shift(ListEventBlock alpha, ListEventBlock beta) {
        int movedLength = beta.getLength();
        
        if(beta.type == ListEvent.INSERT) {
            alpha.startIndex += movedLength;
            alpha.endIndex += movedLength;
        } else if(beta.type == ListEvent.UPDATE) {
            // no shift
        } else if(beta.type == ListEvent.DELETE) {
            alpha.startIndex -= movedLength;
            alpha.endIndex -= movedLength;
            if(alpha.startIndex < 0) throw new IllegalStateException();
        }
    }
    
    /**
     * Tests if the second block must be split in two before the blocks can
     * be sorted. This is only the case when the second block is an UPDATE
     * event that spans a DELETE OR INSERT event of the first block.
     */
    private static boolean requiresSplit(ListEventBlock first, ListEventBlock second) {
        // verify we have one update, and one non-update
        if(first.type != ListEvent.UPDATE && second.type != ListEvent.UPDATE) return false;
        if(first.type == second.type) return false;
        
        // find which block is update, which one is INSERT/DELETE
        boolean updateIsFirst = (first.type == ListEvent.UPDATE);
        final ListEventBlock updateBlock;
        final ListEventBlock otherBlock;
        if(updateIsFirst) {
            updateBlock = first;
            otherBlock = second;
        } else {
            updateBlock = second;
            otherBlock = first;
        }
        
        // verify we have a span for a split
        if(updateBlock.startIndex >= otherBlock.startIndex) return false;
        if(updateBlock.endIndex < otherBlock.startIndex) return false;

        return true;
    }
        
    /**
     * Breaks an update block into two smaller update blocks. One part is stored
     * in the update block parameter and the other part is returned.
     */
    private static ListEventBlock split(ListEventBlock first, ListEventBlock second) {
        // find which block is update, which one is INSERT/DELETE
        boolean updateIsFirst = (first.type == ListEvent.UPDATE);
        final ListEventBlock updateBlock;
        final ListEventBlock otherBlock;
        if(updateIsFirst) {
            updateBlock = first;
            otherBlock = second;
        } else {
            updateBlock = second;
            otherBlock = first;
        }

        // find the split location & offset index
        int splitLocation = otherBlock.startIndex;
        int part2Offset = -1;
        if(otherBlock.type == ListEvent.DELETE) {
            part2Offset = 0;
        } else if(otherBlock.type == ListEvent.INSERT) {
            part2Offset = otherBlock.getLength();
        }

        // apply the changes
        ListEventBlock updateBlockPart2 = new ListEventBlock(splitLocation + part2Offset, updateBlock.endIndex + part2Offset, ListEvent.UPDATE);
        updateBlock.endIndex = splitLocation - 1;
        
        // return the new part
        return updateBlockPart2;
    }
    
    /**
     * Gets this ListEventBlock represented as a String
     */
    public String toString() {
        String result = "";
        if(type == ListEvent.DELETE) result = result + "D.";
        else if(type == ListEvent.UPDATE) result = result + "U.";
        else if(type == ListEvent.INSERT) result = result + "I.";
        if(startIndex == endIndex) result = result + startIndex;
        else result = result + startIndex + "-" + endIndex;
        return result;
    }
}