/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jcombobox;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import java.awt.GridBagLayout;
// for responding to user actions
import java.awt.event.*;
// for displaying lists in combo boxes
import javax.swing.ListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
// for keeping track of a set of listeners
import java.util.ArrayList;


/**
 * The mutable list data event is a list data event that can be rewritten
 * for performance gains. The class is completely re-implemented and it only
 * extends ListDataEvent to fit the ListDataListener interface.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class MutableListDataEvent extends ListDataEvent {

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
    public int getIndex0() {
        return index0;
    }
    public int getIndex1() {
        return index1;
    }
    public int getType() {
        return type;
    }
    
    /**
     * Gets this event as a String for debugging.
     */
    public String toString() {
        return "" + type + "[" + index0 + "," + index1 + "]";
    }
}
