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
 * @deprecated This class will not be available in future releases of Glazed Lists.
 *      It exists to help users migrate between Glazed Lists < 0.8 and Glazed Lists >= 0.9.
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
