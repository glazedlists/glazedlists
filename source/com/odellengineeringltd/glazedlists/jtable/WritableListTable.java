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

/**
 * A writable list table is a list table that allows the user to edit the contents
 * of the table. This requires that the user supply a WritableTableFormat object to
 * set up the table instead of a TableFormat, which provides implementation on how
 * to handle updates.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class WritableListTable extends ListTable {

    /** the writable table format provides implementation on how to handle edits */
    private WritableTableFormat writableTableFormat;
    
    /**
     * Creates a new table that renders the specified list in the specified
     * format.
     */
    public WritableListTable(EventList source, WritableTableFormat writableTableFormat) {
        super(source, writableTableFormat);
        this.writableTableFormat = writableTableFormat;
    }

    /**
     * The table format knows how to interpret an edit on a column relative
     * to the data object in that column.
     */
    public boolean isCellEditable(int row, int column) {
        return writableTableFormat.isFieldEditable(column);
    }

    /**
     * Sets the edited value in the specified column to the object in the
     * specified row.
     */
    public void setValueAt(Object editedValue, int row, int column) {
        // get the object being edited from the source list
        Object baseObject = source.get(row);
        // tell the table format to set the value based on what it knows
        writableTableFormat.setFieldValue(baseObject, editedValue, column);
    }
}
