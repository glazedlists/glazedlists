/* Glazed Lists                                                 (c) 2003-2006 */
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
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

/**
 * A {@link KTableModel} that displays an {@link EventList}. Each element of the
 * {@link EventList} corresponds to a row in the {@link KTableModel}. The columns
 * of the table must be specified using a {@link TableFormat}.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan="2"><font size="+2"><b>Extension: KTable</b></font></td></tr>
 * <tr><td  colspan="2">This Glazed Lists <i>extension</i> requires the third party library <b>KTable</b>.</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Tested Version:</b></td><td>2.1.2</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Home page:</b></td><td><a href="http://ktable.sourceforge.net/">http://ktable.sourceforge.net/</a></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>License:</b></td><td><a href="http://www.eclipse.org/legal/epl-v10.html">Eclipse Public License</a></td></tr>
 * </td></tr>
 * </table>
 *
 * <p>The EventTableModel class is <strong>not thread-safe</strong>. Unless otherwise
 * noted, all methods are only safe to be called from the SWT event dispatch thread.
 * To do this programmatically, use {@link org.eclipse.swt.widgets.Display#asyncExec(Runnable)}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventKTableModel implements KTableModel, ListEventListener {

    /** the table we're providing the model for */
    private KTable table;

    /** the proxy moves events to the SWT thread */
    private TransformedList swtThreadSource = null;

    /** maps row objects to cell values */
    private KTableFormat kTableFormat;

    /**
     * Create a new {@link EventKTableModel} that uses elements from the
     * specified {@link EventList} as rows, and the specified {@link TableFormat}
     * to divide row objects across columns.
     *
     * @param tableFormat provides logic to divide row objects across columns.
     *      If the value implements the {@link KTableFormat} interface, those
     *      methods will be used to provide further details such as cell renderers,
     *      cell editors and row heights.
     */
    public EventKTableModel(KTable table, EventList source, TableFormat tableFormat) {
        this.table = table;
        this.swtThreadSource = GlazedListsSWT.swtThreadProxyList(source, table.getDisplay());

        // this TableFormat supports KTable directly
        if(tableFormat instanceof KTableFormat) {
            this.kTableFormat = (KTableFormat)tableFormat;

        // adapt a regular TableFormat for use with KTable
        } else {
            this.kTableFormat = new TableFormatKTableFormat(tableFormat);
        }

        // listen for events on the SWT display thread
        swtThreadSource.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // KTable has no fine-grained event notification,
        // so each time the data changes we'll probably break
        // selection. Hopefully we can resolve this problem
        // in the future by saving selection and adjusting it
        // as the model changes
        table.redraw();
    }

    /** {@inheritDoc} */
    public Object getContentAt(int column, int row) {
        // get header content
        if(row < getFixedHeaderRowCount()) {
            return kTableFormat.getColumnHeaderValue(row, column);

        // get regular cell content
        } else {
            swtThreadSource.getReadWriteLock().readLock().lock();
            try {
                return kTableFormat.getColumnValue(swtThreadSource.get(row - getFixedHeaderRowCount()), column);
            } finally {
                swtThreadSource.getReadWriteLock().readLock().unlock();
            }
        }
    }

    /** {@inheritDoc} */
    public String getTooltipAt(int column, int row) {
        // headers have no tooltips
        if(row < getFixedHeaderRowCount()) {
            return null;

        // get regular row tooltips
        } else {
            swtThreadSource.getReadWriteLock().readLock().lock();
            try {
                return kTableFormat.getColumnTooltip(swtThreadSource.get(row - getFixedHeaderRowCount()), column);
            } finally {
                swtThreadSource.getReadWriteLock().readLock().unlock();
            }
        }
    }

    /** {@inheritDoc} */
    public KTableCellEditor getCellEditor(int column, int row) {
        // header rows aren't editable
        if(row < getFixedHeaderRowCount()) {
            return null;

        // regular rows are editable if the tableformat is writable
        } else if(kTableFormat instanceof WritableTableFormat) {
            swtThreadSource.getReadWriteLock().readLock().lock();
            try {
                Object baseObject = swtThreadSource.get(row);
                return kTableFormat.getColumnEditor(baseObject, column);
            } finally {
                swtThreadSource.getReadWriteLock().readLock().unlock();
            }

        // this table isn't editable
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public void setContentAt(int column, int row, Object value) {
        // header rows aren't editable
        if(row < getFixedHeaderRowCount()) {
            throw new UnsupportedOperationException("Unexpected set() on column header");

        // regular rows are editable if the tableformat is writable
        } else if(kTableFormat instanceof WritableTableFormat) {
            swtThreadSource.getReadWriteLock().readLock().lock();
            try {
                WritableTableFormat writableTableFormat = (WritableTableFormat)kTableFormat;
                Object baseObject = swtThreadSource.get(row - getFixedHeaderRowCount());
                Object updatedObject = writableTableFormat.setColumnValue(baseObject, value, column);
                if(updatedObject != null) {
                    swtThreadSource.set(row - getFixedHeaderRowCount(), updatedObject);
                }
            } finally {
                swtThreadSource.getReadWriteLock().readLock().unlock();
            }

        // this table isn't editable
        } else {
            throw new UnsupportedOperationException("Unexpected set() on read-only table");
        }
    }

    /** {@inheritDoc} */
    public KTableCellRenderer getCellRenderer(int column, int row) {
        // headers get the default renderer
        if(row < getFixedHeaderRowCount()) {
            return KTableCellRenderer.defaultRenderer;

        // regular rows may have a custom renderer
        } else {
            swtThreadSource.getReadWriteLock().readLock().lock();
            try {
                return kTableFormat.getColumnRenderer(swtThreadSource.get(row - getFixedHeaderRowCount()), column);
            } finally {
                swtThreadSource.getReadWriteLock().readLock().unlock();
            }
        }
    }

    /** {@inheritDoc} */
    public Point belongsToCell(int column, int row) {
        // no spanning by default
        return new Point(column, row);
    }

    /** {@inheritDoc} */
    public int getRowCount() {
        // a row for every list element, plus the headers
        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            return swtThreadSource.size() + getFixedHeaderRowCount();
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public int getFixedHeaderRowCount() {
        return kTableFormat.getFixedHeaderRowCount();
    }

    /** {@inheritDoc} */
    public int getFixedSelectableRowCount() {
        return kTableFormat.getFixedSelectableColumnCount();
    }

    /** {@inheritDoc} */
    public int getColumnCount() {
        return kTableFormat.getColumnCount();
    }

    /** {@inheritDoc} */
    public int getFixedHeaderColumnCount() {
        return kTableFormat.getFixedHeaderColumnCount();
    }

    /** {@inheritDoc} */
    public int getFixedSelectableColumnCount() {
        return kTableFormat.getFixedSelectableColumnCount();
    }

    /** {@inheritDoc} */
    public int getColumnWidth(int col) {
        return kTableFormat.getColumnWidth(col);
    }

    /** {@inheritDoc} */
    public boolean isColumnResizable(int col) {
        return kTableFormat.isColumnResizable(col);
    }

    /** {@inheritDoc} */
    public void setColumnWidth(int col, int width) {
        kTableFormat.setColumnWidth(col, width);
    }

    /** {@inheritDoc} */
    public int getRowHeight(int row) {
        // header row height
        if(row < getFixedHeaderRowCount()) {
            return 20;

        // regular row height
        } else if(row < getRowCount()) {
            swtThreadSource.getReadWriteLock().readLock().lock();
            try {
                return kTableFormat.getRowHeight(swtThreadSource.get(row - getFixedHeaderRowCount()));
            } finally {
                swtThreadSource.getReadWriteLock().readLock().unlock();
            }

        // KTable queries for heights beyond the table's rows
        } else {
            return 20;
        }
    }

    /** {@inheritDoc} */
    public boolean isRowResizable(int row) {
        // header rows are not resizable
        if(row < getFixedHeaderRowCount()) {
            return false;

        // regular rows may be resizable
        } else {
            swtThreadSource.getReadWriteLock().readLock().lock();
            try {
                return kTableFormat.isRowResizable(swtThreadSource.get(row - getFixedHeaderRowCount()));
            } finally {
                swtThreadSource.getReadWriteLock().readLock().unlock();
            }
        }
    }

    /** {@inheritDoc} */
    public int getRowHeightMinimum() {
        return kTableFormat.getRowHeightMinimum();
    }

    /** {@inheritDoc} */
    public void setRowHeight(int row, int value) {
        // header rows are not resizable
        if(row < getFixedHeaderRowCount()) {
            return;

        // regular rows may be resizable
        } else {
            swtThreadSource.getReadWriteLock().readLock().lock();
            try {
                kTableFormat.setRowHeight(swtThreadSource.get(row - getFixedHeaderRowCount()), value);
            } finally {
                swtThreadSource.getReadWriteLock().readLock().unlock();
            }
        }
    }
}