/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import ca.odell.glazedlists.gui.TableFormat;

/**
 * Adapt {@link TableFormat} to {@link KTableFormat}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TableFormatKTableFormat extends AbstractKTableFormat {

    private final TableFormat tableFormat;

    public int getFixedHeaderRowCount() {
        return 1;
    }

    public TableFormatKTableFormat(TableFormat tableFormat) {
        this.tableFormat = tableFormat;
    }

    public Object getColumnValue(Object baseObject, int column) {
        return tableFormat.getColumnValue(baseObject, column);
    }

    public Object getColumnHeaderValue(int headerRow, int column) {
        return tableFormat.getColumnName(column);
    }

    public int getColumnCount() {
        return tableFormat.getColumnCount();
    }

    public KTableCellEditor getColumnEditor(Object baseObject, int column) {
        return null;
    }

    public KTableCellRenderer getColumnRenderer(Object baseObject, int column) {
        return KTableCellRenderer.defaultRenderer;
    }
}
