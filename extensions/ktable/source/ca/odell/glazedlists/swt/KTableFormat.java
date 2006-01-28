/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.gui.TableFormat;
import de.kupzog.ktable.*;

/**
 * Specify how to split row objects into cells for use with {@link KTable}.
 *
 * <p>To take full advantage of {@link KTable} over the regular SWT
 * <code>Table</code> class, the {@link KTableFormat} interface should
 * be preferred over the simpler {@link TableFormat} class.
 *
 * <p>In times where advanced features of {@link KTable} are not needed,
 * the simpler {@link TableFormat} can be used.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface KTableFormat extends TableFormat {

    /**
     * Provide a value for the specified header row and column. Unlike
     * simple {@link TableFormat}, the {@link KTableFormat} supports
     * multiple header rows per table. Therefore this method is the
     * preferred way to define the header values for {@link EventKTableModel}.
     *
     * @see TableFormat#getColumnName(int)
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