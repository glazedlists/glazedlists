/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl;

// the core Glazed Lists package
import ca.odell.glazedlists.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * Returns the List itself for a List of Lists.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListCollectionListModel implements CollectionListModel {
    public List getChildren(Object parent) {
        if(parent == null) return Collections.EMPTY_LIST;
        return (List)parent;
    }
}
