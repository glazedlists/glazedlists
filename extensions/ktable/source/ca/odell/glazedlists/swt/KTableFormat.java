/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.gui.TableFormat;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface KTableFormat {

    /**
     * @see de.kupzog.ktable.KTableModel#getContentAt
     */
    public Object getColumnValue(Object baseObject, int column);

    /**
     * @see de.kupzog.ktable.KTableModel#setContentAt
     */
    public Object setColumnValue(Object baseObject, Object value, int column);

    /**
     * @see de.kupzog.ktable.KTableModel#getContentAt
     */
    public Object getColumnHeaderValue(int headerRow, int column);

    /**
     * @see de.kupzog.ktable.KTableModel#getFixedHeaderRowCount
     */
    public int getFixedHeaderRowCount();

    /**
     * @see de.kupzog.ktable.KTableModel#getRowHeight
     */
    public int getRowHeight(Object rowObject);

    /**
     * @see de.kupzog.ktable.KTableModel#setRowHeight
     */
    public void setRowHeight(Object rowObject, int rowHeight);

    /**
     * @see de.kupzog.ktable.KTableModel#isRowResizable
     */
    public boolean isRowResizable(Object rowObject);

    /**
     * @see de.kupzog.ktable.KTableModel#getRowHeightMinimum
     */
    public int getRowHeightMinimum();

    /**
     * @see de.kupzog.ktable.KTableModel#getFixedHeaderColumnCount
     */
    public int getFixedHeaderColumnCount();

    /**
     * @see de.kupzog.ktable.KTableModel#getFixedSelectableColumnCount
     */
    public int getFixedSelectableColumnCount();

    /**
     * @see de.kupzog.ktable.KTableModel#getColumnCount
     */
    public int getColumnCount();

    /**
     * @see de.kupzog.ktable.KTableModel#getColumnWidth
     */
    public int getColumnWidth(int column);

    /**
     * @see de.kupzog.ktable.KTableModel#setColumnWidth
     */
    public void setColumnWidth(int column, int width);

    /**
     * @see de.kupzog.ktable.KTableModel#isColumnResizable
     */
    public boolean isColumnResizable(int column);

    /**
     * @see de.kupzog.ktable.KTableModel#getTooltipAt
     */
    public String getColumnTooltip(Object baseObject, int column);

    /**
     * @see de.kupzog.ktable.KTableModel#getCellEditor
     */
    public KTableCellEditor getColumnEditor(Object baseObject, int column);

    /**
     * @see de.kupzog.ktable.KTableModel#getCellRenderer
     */
    public KTableCellRenderer getColumnRenderer(Object baseObject, int column);
}