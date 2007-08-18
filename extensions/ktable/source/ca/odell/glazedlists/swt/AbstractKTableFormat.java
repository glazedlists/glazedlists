/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import java.util.ArrayList;
import java.util.List;

/**
 * Make implementing {@link KTableFormat} easier.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public abstract class AbstractKTableFormat implements KTableFormat {

    /** keep track of column widths, in pixels */ 
    private final List<Integer> columnWidths = new ArrayList<Integer>();

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
        return columnWidths.get(column).intValue();
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
