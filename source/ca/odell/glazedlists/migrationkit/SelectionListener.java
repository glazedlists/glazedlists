/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.migrationkit;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;

/**
 * Listens and responds to changes in the selection of a table.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface SelectionListener {

    /**
     * Sets the selection to the specified object.
     */
    public void setSelection(Object selected);
    
    /**
     * Sets the selection to no object.
     */
    public void clearSelection();

    /**
     * Sets the specified object as double clicked.
     */
    public void setDoubleClicked(Object doubleClicked);
}
