/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// the core Glazed Lists package
import ca.odell.glazedlists.CollectionList;

import java.util.Collections;
import java.util.List;

/**
 * Returns the List itself for a List of Lists.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListCollectionListModel<E> implements CollectionList.Model<List<E>,E> {
    @Override
    public List<E> getChildren(List<E> parent) {
        if(parent == null) return Collections.emptyList();
        return parent;
    }
}