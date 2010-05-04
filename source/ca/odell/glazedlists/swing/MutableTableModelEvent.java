/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.event.ListEvent;

import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

/**
 * A frequently changing table or a table that changes in several
 * places simultaneously will cause several TableModelEvents to
 * be created. This hurts speed. This is a mutable table model
 * event, so that the object can be recycled.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class MutableTableModelEvent extends TableModelEvent {

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

    /**
     * Sets the table model event to notify that the table structure
     * has changed.
     */
    public void setStructureChanged() {
        firstRow = HEADER_ROW;
        lastRow = HEADER_ROW;
        column = ALL_COLUMNS;
        type = UPDATE;
    }

    /**
     * Sets the table model event to notify that all table data
     * has changed.
     */
    public void setAllDataChanged() {
        firstRow = 0;
        lastRow = Integer.MAX_VALUE;
        column = ALL_COLUMNS;
        type = UPDATE;
    }

    /**
     * Sets the table model event to reflect the specified changes.
     */
    public void setValues(int startIndex, int endIndex, int listChangeType) {
        this.firstRow = startIndex;
        this.lastRow = endIndex;
        if(listChangeType == ListEvent.INSERT) type = INSERT;
        else if(listChangeType == ListEvent.DELETE) type = DELETE;
        else if(listChangeType == ListEvent.UPDATE) type = UPDATE;
        column = ALL_COLUMNS;
    }
}