/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import de.kupzog.ktable.KTableModel;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTable;
import org.eclipse.swt.graphics.Point;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swt.GlazedListsSWT;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.gui.WritableTableFormat;

import java.util.List;
import java.util.ArrayList;

/**
 * Make implementing {@link KTableFormat} easier.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public abstract class AbstractKTableFormat implements KTableFormat {

    /** keep track of column widths, in pixels */ 
    private final List columnWidths = new ArrayList();

    /** {@inheritDoc} */
    public Object setColumnValue(Object baseObject, Object value, int column) {
        return null;
    }

    /** {@inheritDoc} */
    public int getFixedHeaderRowCount() {
        return 0;
    }

    public int getRowHeight(Object rowObject) {
        /** {@inheritDoc} */
        return 20;
    }

    /** {@inheritDoc} */
    public boolean isRowResizable(Object rowObject) {
        return false;
    }

    /** {@inheritDoc} */
    public int getColumnWidth(int column) {
        prepareDefaultColumnWidths();
        return ((Integer)columnWidths.get(column)).intValue();
    }

    /** {@inheritDoc} */
    public void setColumnWidth(int column, int width) {
        prepareDefaultColumnWidths();
        columnWidths.set(column, new Integer(width));
    }

    /** {@inheritDoc} */
    private void prepareDefaultColumnWidths() {
        while(columnWidths.size() < getColumnCount()) {
            columnWidths.add(new Integer(100));
        }
    }

    /** {@inheritDoc} */
    public int getRowHeightMinimum() {
        return 20;
    }

    /** {@inheritDoc} */
    public int getFixedSelectableColumnCount() {
        return 0;
    }

    /** {@inheritDoc} */
    public int getFixedHeaderColumnCount() {
        return 0;
    }

    /** {@inheritDoc} */
    public boolean isColumnResizable(int column) {
        return true;
    }

    /** {@inheritDoc} */
    public void setRowHeight(Object rowObject, int rowHeight) {
        // do nothing
    }

    /** {@inheritDoc} */
    public String getColumnTooltip(Object baseObject, int column) {
        return null;
    }
}
