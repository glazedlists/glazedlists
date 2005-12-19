/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.gui;

import ca.odell.glazedlists.impl.gui.SortingState;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface SortingStrategy {
    public void setSortingState(SortingState sortingState);
    public void dispose();
}