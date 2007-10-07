/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.gui.TableFormat;

import org.eclipse.swt.widgets.TableItem;

/**
 * A <code>TableItemRenderer</code> can be provided to an {@link EventTableViewer} to customize
 * the format and appearance of column values each represented by a {@link TableItem}.
 * 
 * @author hbrands
 */
public interface TableItemRenderer {

    /**
     * Default renderer that converts the column value to a string and sets it as
     * the text of the TableItem.
     */
    public static final TableItemRenderer DEFAULT = new ColumnValueToStringRenderer();
    
    /**
     * Callback method that allows the configuration of the TableItem properties.
     * 
     * @param item the TableItem for a column
     * @param columnValue the column value, e.g. the value returned by
     *        {@link TableFormat#getColumnValue(Object, int)}
     * @param column the column index
     */
    void render(TableItem item, Object columnValue, int column);
    
    /**
     * Default renderer that converts the column value to a string and sets it as
     * the text of the TableItem.
     */
    class ColumnValueToStringRenderer implements TableItemRenderer {

        /** {@inheritDoc} */
        public void render(TableItem item, Object columnValue, int column) {
            item.setText(column, columnValue == null ? "" : columnValue.toString());
        }
    }
}
