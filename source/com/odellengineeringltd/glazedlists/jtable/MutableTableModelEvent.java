/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jtable;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// i'm a table model event
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

/**
 * A frequently changing table or a table that changes in several
 * places simultaneously will cause several TableModelEvents to
 * be created. This hurts speed. This is a mutable table model
 * event, so that the object can be recycled.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class MutableTableModelEvent extends TableModelEvent {
    
    /**
     * Constructors simply call the same on the superclass.
     */
    public MutableTableModelEvent(TableModel source) {
        super(source);
    }
    
    /**
     * Changes this table model event. The event <strong>must not</strong>
     * be changed while it is being viewed by a listener.
     */
    public void setRange(int firstRow, int lastRow) {
        this.firstRow = firstRow;
        this.lastRow = lastRow;
    }
    public void setType(int type) {
        this.type = type;
    }
    
    /*
     * Sets the table model event to reflect the changes from the
     * specified ListChange event.
     */
    /*public void setValues(ListChange listChange) {
        firstRow = listChange.getStartIndex();
        lastRow = listChange.getEndIndex();
        if(listChange.getType() == ListChange.INSERT) type = INSERT;
        else if(listChange.getType() == ListChange.DELETE) type = DELETE;
        else if(listChange.getType() == ListChange.UPDATE) type = UPDATE;
        column = ALL_COLUMNS;
    }*/

    /**
     * Sets the table model event to reflect the specified changes.
     */
    public void setValues(int startIndex, int endIndex, int listChangeType) {
        this.firstRow = startIndex;
        this.lastRow = endIndex;
        if(listChangeType == ListChangeBlock.INSERT) type = INSERT;
        else if(listChangeType == ListChangeBlock.DELETE) type = DELETE;
        else if(listChangeType == ListChangeBlock.UPDATE) type = UPDATE;
        column = ALL_COLUMNS;
    }

}
