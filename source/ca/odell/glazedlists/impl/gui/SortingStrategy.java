/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.gui;

import ca.odell.glazedlists.impl.gui.SortingState;

/**
 * Behaviour that defines how to interpret a mouse click on a table's header.
 *
 * <p>This interface is intentionally stateless so that a single
 * instance can be shared between multiple <code>TableComparatorChooser</code>s.
 *
 * <p>This interface is <strong>not</strong> designed to be implemented by
 * users of <code>TableComparatorChooser</code>. Instead, use the predefined
 * constant values.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface SortingStrategy {
    public void columnClicked(SortingState sortingState, int column, int clicks, boolean shift, boolean control);
}