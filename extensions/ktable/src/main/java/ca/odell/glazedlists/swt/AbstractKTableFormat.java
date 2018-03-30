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
 *
 * @deprecated The ktable extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@Deprecated
public abstract class AbstractKTableFormat implements KTableFormat {

    /** keep track of column widths, in pixels */
    private final List<Integer> columnWidths = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public int getFixedHeaderRowCount() {
        return 0;
    }

    @Override
    public int getRowHeight(Object rowObject) {
        /** {@inheritDoc} */
        return 20;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRowResizable(Object rowObject) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnWidth(int column) {
        prepareDefaultColumnWidths();
        return columnWidths.get(column).intValue();
    }

    /** {@inheritDoc} */
    @Override
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
    @Override
    public int getRowHeightMinimum() {
        return 20;
    }

    /** {@inheritDoc} */
    @Override
    public int getFixedSelectableColumnCount() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int getFixedHeaderColumnCount() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isColumnResizable(int column) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setRowHeight(Object rowObject, int rowHeight) {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnTooltip(Object baseObject, int column) {
        return null;
    }
}
