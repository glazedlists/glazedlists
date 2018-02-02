/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.gui.TableFormat;

import org.eclipse.swt.widgets.TableColumn;

/**
 * Optional interface to be implemented by a {@link TableFormat} implementation usable by an
 * {@link DefaultEventTableViewer}. For each table column the viewer creates it calls the
 * {@link #configure(TableColumn, int)} method to allow customization of the table column.
 *
 * @author hbrands
 */
@FunctionalInterface
public interface TableColumnConfigurer {

    /**
     * Callback method to allow customization of the specified table column.
     *
     * @param tableColumn the table column
     * @param column the corresponding column index
     */
    void configure(TableColumn tableColumn, int column);
}
