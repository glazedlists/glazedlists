/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import org.eclipse.swt.graphics.Point;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableModel;

/**
 * A {@link KTableModel} that displays an {@link EventList}. Each element of the
 * {@link EventList} corresponds to a row in the {@link KTableModel}. The columns
 * of the table must be specified using a {@link TableFormat}.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan="2"><font size="+2"><b>Extension: KTable</b></font></td></tr>
 * <tr><td  colspan="2">This Glazed Lists <i>extension</i> requires the third party library <b>KTable</b>.</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Tested Version:</b></td><td>2.1.2</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Home page:</b></td><td><a href="http://ktable.sourceforge.net/">http://ktable.sourceforge.net/</a></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>License:</b></td><td><a href="http://www.eclipse.org/legal/epl-v10.html">Eclipse Public License</a></td></tr>
 * </td></tr>
 * </table>
 *
 * <p>The DefaultEventKTableModel class is <strong>not thread-safe</strong>. Unless otherwise
 * noted, all methods are only safe to be called from the SWT event dispatch thread.
 * To do this programmatically, use {@link org.eclipse.swt.widgets.Display#asyncExec(Runnable)}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author Holger Brands
 */
public class DefaultEventKTableModel implements KTableModel, ListEventListener {

    /** the table we're providing the model for */
    private KTable table;

    /** indicator to dispose source list */
    private boolean disposeSource;

    /** the proxy moves events to the SWT thread */
    protected EventList source;

    /** maps row objects to cell values */
    private KTableFormat kTableFormat;

    /**
     * Create a new {@link DefaultEventKTableModel} that uses elements from the
     * specified {@link EventList} as rows, and the specified {@link TableFormat}
     * to divide row objects across columns.
     *
     * @param table the KTable the model is created for
     * @param source the {@link EventList}
     * @param tableFormat provides logic to divide row objects across columns.
     *      If the value implements the {@link KTableFormat} interface, those
     *      methods will be used to provide further details such as cell renderers,
     *      cell editors and row heights.
     */
    public DefaultEventKTableModel(KTable table, EventList source, TableFormat tableFormat) {
    	this(table, source, tableFormat, false);
    }

    /**
     * Create a new {@link DefaultEventKTableModel} that uses elements from the
     * specified {@link EventList} as rows, and the specified {@link TableFormat}
     * to divide row objects across columns.
     *
     * @param table the KTable the model is created for
     * @param source the {@link EventList}
     * @param tableFormat provides logic to divide row objects across columns.
     *      If the value implements the {@link KTableFormat} interface, those
     *      methods will be used to provide further details such as cell renderers,
     *      cell editors and row heights.
     * @param disposeSource <code>true</code> if the source list should be disposed when disposing
     *            this model, <code>false</code> otherwise
     *
     */
    protected DefaultEventKTableModel(KTable table, EventList source, TableFormat tableFormat, boolean disposeSource) {
    	this.disposeSource = disposeSource;
        this.table = table;
        this.source = source;

        // this TableFormat supports KTable directly
        if(tableFormat instanceof KTableFormat) {
            this.kTableFormat = (KTableFormat)tableFormat;

        // adapt a regular TableFormat for use with KTable
        } else {
            this.kTableFormat = new TableFormatKTableFormat(tableFormat);
        }

        // listen for events on the SWT display thread
        this.source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent listChanges) {
        // KTable has no fine-grained event notification,
        // so each time the data changes we'll probably break
        // selection. Hopefully we can resolve this problem
        // in the future by saving selection and adjusting it
        // as the model changes
        table.redraw();
    }

    /** {@inheritDoc} */
    @Override
    public Object getContentAt(int column, int row) {
        // get header content
        if(row < getFixedHeaderRowCount()) {
            return kTableFormat.getColumnHeaderValue(row, column);

        // get regular cell content
        } else {
            source.getReadWriteLock().readLock().lock();
            try {
                return kTableFormat.getColumnValue(source.get(row - getFixedHeaderRowCount()), column);
            } finally {
                source.getReadWriteLock().readLock().unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getTooltipAt(int column, int row) {
        // headers have no tooltips
        if(row < getFixedHeaderRowCount()) {
            return null;

        // get regular row tooltips
        } else {
            source.getReadWriteLock().readLock().lock();
            try {
                return kTableFormat.getColumnTooltip(source.get(row - getFixedHeaderRowCount()), column);
            } finally {
                source.getReadWriteLock().readLock().unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public KTableCellEditor getCellEditor(int column, int row) {
        // header rows aren't editable
        if(row < getFixedHeaderRowCount()) {
            return null;

        // regular rows are editable if the tableformat is writable
        } else if(kTableFormat instanceof WritableTableFormat) {
            source.getReadWriteLock().readLock().lock();
            try {
                Object baseObject = source.get(row);
                return kTableFormat.getColumnEditor(baseObject, column);
            } finally {
                source.getReadWriteLock().readLock().unlock();
            }

        // this table isn't editable
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setContentAt(int column, int row, Object value) {
        // header rows aren't editable
        if(row < getFixedHeaderRowCount()) {
            throw new UnsupportedOperationException("Unexpected set() on column header");

        // regular rows are editable if the tableformat is writable
        } else if(kTableFormat instanceof WritableTableFormat) {
            source.getReadWriteLock().readLock().lock();
            try {
                WritableTableFormat writableTableFormat = (WritableTableFormat)kTableFormat;
                Object baseObject = source.get(row - getFixedHeaderRowCount());
                Object updatedObject = writableTableFormat.setColumnValue(baseObject, value, column);
                if(updatedObject != null) {
                    source.set(row - getFixedHeaderRowCount(), updatedObject);
                }
            } finally {
                source.getReadWriteLock().readLock().unlock();
            }

        // this table isn't editable
        } else {
            throw new UnsupportedOperationException("Unexpected set() on read-only table");
        }
    }

    /** {@inheritDoc} */
    @Override
    public KTableCellRenderer getCellRenderer(int column, int row) {
        // headers get the default renderer
        if(row < getFixedHeaderRowCount()) {
            return KTableCellRenderer.defaultRenderer;

        // regular rows may have a custom renderer
        } else {
            source.getReadWriteLock().readLock().lock();
            try {
                return kTableFormat.getColumnRenderer(source.get(row - getFixedHeaderRowCount()), column);
            } finally {
                source.getReadWriteLock().readLock().unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Point belongsToCell(int column, int row) {
        // no spanning by default
        return new Point(column, row);
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        // a row for every list element, plus the headers
        source.getReadWriteLock().readLock().lock();
        try {
            return source.size() + getFixedHeaderRowCount();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getFixedHeaderRowCount() {
        return kTableFormat.getFixedHeaderRowCount();
    }

    /** {@inheritDoc} */
    @Override
    public int getFixedSelectableRowCount() {
        return kTableFormat.getFixedSelectableColumnCount();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return kTableFormat.getColumnCount();
    }

    /** {@inheritDoc} */
    @Override
    public int getFixedHeaderColumnCount() {
        return kTableFormat.getFixedHeaderColumnCount();
    }

    /** {@inheritDoc} */
    @Override
    public int getFixedSelectableColumnCount() {
        return kTableFormat.getFixedSelectableColumnCount();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnWidth(int col) {
        return kTableFormat.getColumnWidth(col);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isColumnResizable(int col) {
        return kTableFormat.isColumnResizable(col);
    }

    /** {@inheritDoc} */
    @Override
    public void setColumnWidth(int col, int width) {
        kTableFormat.setColumnWidth(col, width);
    }

    /** {@inheritDoc} */
    @Override
    public int getRowHeight(int row) {
        // header row height
        if(row < getFixedHeaderRowCount()) {
            return 20;

        // regular row height
        } else if(row < getRowCount()) {
            source.getReadWriteLock().readLock().lock();
            try {
                return kTableFormat.getRowHeight(source.get(row - getFixedHeaderRowCount()));
            } finally {
                source.getReadWriteLock().readLock().unlock();
            }

        // KTable queries for heights beyond the table's rows
        } else {
            return 20;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRowResizable(int row) {
        // header rows are not resizable
        if(row < getFixedHeaderRowCount()) {
            return false;

        // regular rows may be resizable
        } else {
            source.getReadWriteLock().readLock().lock();
            try {
                return kTableFormat.isRowResizable(source.get(row - getFixedHeaderRowCount()));
            } finally {
                source.getReadWriteLock().readLock().unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getRowHeightMinimum() {
        return kTableFormat.getRowHeightMinimum();
    }

    /** {@inheritDoc} */
    @Override
    public void setRowHeight(int row, int value) {
        // header rows are not resizable
        if(row < getFixedHeaderRowCount()) {
            return;

        // regular rows may be resizable
        } else {
            source.getReadWriteLock().readLock().lock();
            try {
                kTableFormat.setRowHeight(source.get(row - getFixedHeaderRowCount()), value);
            } finally {
                source.getReadWriteLock().readLock().unlock();
            }
        }
    }

    /**
     * Releases the resources consumed by this {@link DefaultEventKTableModel} so that it
     * may eventually be garbage collected.
     *
     * <p>A {@link DefaultEventKTableModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link DefaultEventKTableModel}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where a {@link DefaultEventKTableModel} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link DefaultEventKTableModel} after it has been disposed.
     */
    public void dispose() {
        source.removeListEventListener(this);
        if (disposeSource) {
            source.dispose();
        }
    }
}