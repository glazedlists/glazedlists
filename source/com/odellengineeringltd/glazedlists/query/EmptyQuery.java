/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.query;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A query of zero elements.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EmptyQuery implements Query {

    /**
     * Gets the name of this query for display in a logging message.
     */
    public String getName() {
        return "Empty Query";
    }
    
    /**
     * Returns an empty set.
     */
    public SortedSet doQuery() throws InterruptedException {
        return new TreeSet();
    }

    /**
     * The empty query matches no objects.
     */
    public boolean matchesObject(Comparable object) {
        return false;
    }
}
