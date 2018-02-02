/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;

import org.eclipse.swt.widgets.TableItem;

/**
 * A <code>TableItemConfigurer</code> can be provided to an
 * {@link DefaultEventTableViewer} to customize the initial format and appearance of
 * column values, each represented by a {@link TableItem}.
 *
 * @see DefaultEventTableViewer#setTableItemConfigurer(TableItemConfigurer)
 *
 * @author hbrands
 */
@FunctionalInterface
public interface TableItemConfigurer<E> {

    /**
     * Default configurer that converts the column value to a string and sets it
     * as the text of the TableItem.
     */
    public static final TableItemConfigurer DEFAULT = new DefaultTableItemConfigurer();

    /**
     * Callback method that allows the configuration of the TableItem properties
     * for the specified row and column.
     *
     * @param item the TableItem at index <code>row</code>
     * @param rowValue the list element from the source {@link EventList} at
     *        index <code>row</code>
     * @param columnValue the column value, e.g. the value returned by
     *        {@link TableFormat#getColumnValue(Object, int)}
     * @param row the row index
     * @param column the column index
     */
    void configure(TableItem item, E rowValue, Object columnValue, int row, int column);

    /**
     * Default configurer that converts the column value to a string and sets it
     * as the text of the TableItem.
     */
    class DefaultTableItemConfigurer<E> implements TableItemConfigurer<E> {

        /** {@inheritDoc} */
        @Override
        public void configure(TableItem item, E rowValue, Object columnValue, int row, int column) {
            item.setText(column, columnValue == null ? "" : columnValue.toString());
        }
    }
}
