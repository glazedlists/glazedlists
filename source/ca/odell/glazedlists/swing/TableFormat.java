/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// records fit inside of tables
import javax.swing.JTable;

/**
 * Specifies how a set of records are rendered in a table.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial-0.9.1/">Glazed
 * Lists Tutorial</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 *
 * @see AdvancedTableFormat
 * @see WritableTableFormat
 */
public interface TableFormat {

    /**
     * The number of columns to display.
     */
    public int getColumnCount();

    /**
     * Gets the title of the specified column. 
     */
    public String getColumnName(int column);
    
    /**
     * Gets the value of the specified field for the specified object. This
     * is the value that will be passed to the editor and renderer for the
     * column. If you have defined a custom renderer, you may choose to return
     * simply the baseObject.
     */
    public Object getColumnValue(Object baseObject, int column);
}
