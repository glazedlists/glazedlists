/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import de.kupzog.ktable.*;
import ca.odell.glazedlists.gui.TableFormat;

/**
 * Adapt {@link TableFormat} to {@link KTableFormat}. This class
 * is used internally by {@link EventKTableModel} so that
 * {@link TableFormat}s and {@link KTableFormat}s can be treated
 * interchangeably.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class TableFormatKTableFormat extends AbstractKTableFormat {

    /** the TableFormat being adapted, methods will be delegated to */
    private final TableFormat tableFormat;

    /**
     * Adapt the specified {@link TableFormat} to be used in
     * a {@link KTable}.
     */
    public TableFormatKTableFormat(TableFormat tableFormat) {
        this.tableFormat = tableFormat;
    }

    /** {@inheritDoc} */
    public int getFixedHeaderRowCount() {
        return 1;
    }

    /** {@inheritDoc} */
    public String getColumnName(int column) {
        return tableFormat.getColumnName(column);
    }

    /** {@inheritDoc} */
    public Object getColumnValue(Object baseObject, int column) {
        return tableFormat.getColumnValue(baseObject, column);
    }

    /** {@inheritDoc} */
    public Object getColumnHeaderValue(int headerRow, int column) {
        return tableFormat.getColumnName(column);
    }

    /** {@inheritDoc} */
    public int getColumnCount() {
        return tableFormat.getColumnCount();
    }

    /** {@inheritDoc} */
    public KTableCellEditor getColumnEditor(Object baseObject, int column) {
        return null;
    }

    /** {@inheritDoc} */
    public KTableCellRenderer getColumnRenderer(Object baseObject, int column) {
        return KTableCellRenderer.defaultRenderer;
    }
}
