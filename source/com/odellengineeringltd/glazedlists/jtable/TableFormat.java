/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jtable;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// records fit inside of tables
import javax.swing.JTable;

/**
 * Specifies how a set of records are rendered in a table.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface TableFormat {

    /**
     * The number of columns to display.
     */
    public int getFieldCount();

    /**
     * Gets the title of the specified column. 
     */
    public String getFieldName(int column);
    
    /**
     * Gets the value of the specified field for the specified object. This
     * is the value that will be passed to the editor and renderer for the
     * column. If you have defined a custom renderer, you may choose to return
     * simply the baseObject.
     */
    public Object getFieldValue(Object baseObject, int column);

    /**
     * Allow the record model to customize the table to its liking
     * This method is called after the table is created so that the
     * record can ensure the table behaves appropriately. This may
     * include setting the editors and renderers for the table.
     */
    public void configureTable(JTable table);
    
    /**
     * For editing fields
     */
    //public boolean isFieldEditable(int column);
    //public void setFieldValue(int column, Object value);

}
