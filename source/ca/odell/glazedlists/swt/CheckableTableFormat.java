/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// records fit inside of tables
import javax.swing.JTable;

/**
 * Specifies how to check table elements.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface CheckableTableFormat extends TableFormat {
    
    /**
     * Sets the specified object as checked.
     */
    public void setChecked(Object baseObject, boolean checked);
    
    /**
     * Gets whether the specified object is checked.
     */
    public boolean getChecked(Object baseObject);
}
