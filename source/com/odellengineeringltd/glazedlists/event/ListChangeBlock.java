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
    int getStartIndex() {
        return startIndex;
    }
    /**
     * Get the last index in the range of this change.
     */
    int getEndIndex() {
        return endIndex;
    }

    /**
     * Get the type of this change.
     */
    int getType() {
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
