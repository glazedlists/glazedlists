/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// Swing toolkit stuff for displaying widgets
import javax.swing.event.ListDataEvent;

/**
 * The mutable list data event is a list data event that can be rewritten
 * for performance gains. The class is completely re-implemented and it only
 * extends ListDataEvent to fit the ListDataListener interface.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
final class MutableListDataEvent extends ListDataEvent {

    /** what the change is, currently */
    private int index0;
    private int index1;
    private int type;

    /**
     * Creates a new mutable data event that always uses the specified object
     * as its source.
     */
    public MutableListDataEvent(Object source) {
        super(source, CONTENTS_CHANGED, 0, 0);
    }

    /**
     * Sets the start and end range for this event. The values are inclusive.
     */
    public void setRange(int index0, int index1) {
        this.index0 = index0;
        this.index1 = index1;
    }

    /**
     * Sets the type of change. This value must be either CONTENTS_CHANGED,
     * INTERVAL_ADDED, or INTERVAL REMOVED.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Accessors for the change information do not use any information
     * in the parent class.
     */
    @Override
    public int getIndex0() {
        return index0;
    }
    @Override
    public int getIndex1() {
        return index1;
    }
    @Override
    public int getType() {
        return type;
    }

    /**
     * Gets this event as a String for debugging.
     */
    @Override
    public String toString() {
        return "" + type + "[" + index0 + "," + index1 + "]";
    }
}