/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
public class ListCollectionListModel implements CollectionList.Model {
    public List getChildren(Object parent) {
        if(parent == null) return Collections.EMPTY_LIST;
        return (List)parent;
    }
}
