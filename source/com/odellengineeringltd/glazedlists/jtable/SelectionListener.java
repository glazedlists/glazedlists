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
