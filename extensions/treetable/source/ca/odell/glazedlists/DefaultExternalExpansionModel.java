/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link TreeList.ExpansionModel} that uses a {@link Map} to remember
 * the expanded/collapsed state of elements.
 *
 * TODO(jessewilson): USE WEAK REFERENCES
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class DefaultExternalExpansionModel<E> implements TreeList.ExpansionModel<E> {

    /** keep track of the expanded state of each element, by its identity */
    private HashMap<E,Boolean> elementsExpandedStates = new HashMap<E,Boolean>();

    /** the {@link TreeList.ExpansionModel} to delegate to for unknown elements */
    private TreeList.ExpansionModel<E> defaultsModel;

    public DefaultExternalExpansionModel(TreeList.ExpansionModel<E> defaultsModel) {
        this.defaultsModel = defaultsModel;
    }

    public DefaultExternalExpansionModel() {
        this(TreeList.<E>nodesStartExpanded());
    }

    public boolean isExpanded(E element, List<E> path) {
        Boolean expanded = elementsExpandedStates.get(element);
        if(expanded == null) {
            expanded = Boolean.valueOf(defaultsModel.isExpanded(element, path));
            setExpanded(element, path, expanded.booleanValue());
        }
        return expanded.booleanValue();
    }

    public void setExpanded(E element, List<E> path, boolean expanded) {
        elementsExpandedStates.put(element, Boolean.valueOf(expanded));
    }
}