/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.event;

// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;

/**
 * Models a change to a list that may require a GUI object
 * to be repainted or another object to be updated.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListChangeBlock {
    
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
    public ListChangeBlock(int index, int type) {
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
    public ListChangeBlock(int startIndex, int endIndex, int type) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.type = type;
        this.valid = true;
        assert(startIndex >= 0 && endIndex >= startIndex);
    }
    public ListChangeBlock() {
        this.valid = false;
    }
    public void setData(int index, int type) {
        this.startIndex = index;
        this.endIndex = index;
        this.type = type;
        this.valid = true;
        assert(startIndex >= 0 && endIndex >= startIndex);
    }
    public void setData(int startIndex, int endIndex, int type) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.type = type;
        this.valid = true;
        assert(startIndex >= 0 && endIndex >= startIndex);
    }
    
    /**
     * Setting a change to being valid means it is active, wheras invalid
     * means that it can be re-used (ie. object pooling).
     */
    public void setInvalid() {
        this.valid = false;
    }
    public boolean isValid() {
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
    public ListChangeBlock append(int appendStartIndex, int appendEndIndex, int type) {
        // bail if the types are different
        if(type != this.type) return null;
        // insert events: join if the ends touch
        if(type == INSERT && (appendStartIndex > endIndex + 1 || appendEndIndex < startIndex)) return null;
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
    /*public ListChangeBlock append(int index, int type) {
        // bail if the types are different
        if(type != this.type) return null;
        // insert events: same if inserted at beginning, end or middle; only end increases
        if(type == INSERT) {
            if(index < startIndex || index > endIndex + 1) return null; //new ListChange(index, type);
            endIndex++;
        // delete events: same if deleted from start index or one before start index
        } else if(type == DELETE) {
            if(index < startIndex - 1 || index > startIndex) return null; //new ListChange(index, type);
            if(index == startIndex - 1) startIndex--;
            else if(index == startIndex) endIndex++;
        // update events: same if update is one from beginning or end
        } else if(type == UPDATE) {
            if(index < startIndex - 1 || index > endIndex + 1) return null; //new ListChange(index, type);
            if(index == startIndex - 1) startIndex--;
            else if(index == endIndex + 1) endIndex++;
        }
        return this;
    }*/
    
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
