/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList.ExpansionProvider;
import ca.odell.glazedlists.TreeList;

import java.util.Map;
import java.util.List;
import java.util.IdentityHashMap;


/**
 * An {@link ExpansionProvider} that uses a {@link Map} to remember
 * the expanded/collapsed state of elements.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class DefaultExternalExpansionProvider<E> implements ExpansionProvider<E> {

    /** keep track of the expanded state of each element, by its identity */
    private Map<E,Boolean> elementsExpandedStates = new IdentityHashMap<E,Boolean>();

    /** the {@link ExpansionProvider} to delegate to for unknown elements */
    private ExpansionProvider<E> defaultsProvider;

    public DefaultExternalExpansionProvider(ExpansionProvider<E> defaultsProvider) {
        this.defaultsProvider = defaultsProvider;
    }

    public DefaultExternalExpansionProvider() {
        this(TreeList.NODES_START_EXPANDED);
    }

    public boolean isExpanded(E element, List<E> path) {
        Boolean expanded = elementsExpandedStates.get(element);
        if(expanded == null) {
            expanded = Boolean.valueOf(defaultsProvider.isExpanded(element, path));
            setExpanded(element, path, expanded.booleanValue());
        }
        return expanded.booleanValue();
    }

    public void setExpanded(E element, List<E> path, boolean expanded) {
        elementsExpandedStates.put(element, Boolean.valueOf(expanded));
    }
}
