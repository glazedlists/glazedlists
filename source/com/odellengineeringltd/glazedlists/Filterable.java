/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists;

// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;

/**
 * An item that can be compared to a list of filters to see if it
 * matches.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface Filterable {

    /**
     * Gets this object as a list of Strings. These Strings
     * should contain all object information so that it can be compared
     * to the filter set.
     */
    public String[] getFilterStrings();
}
