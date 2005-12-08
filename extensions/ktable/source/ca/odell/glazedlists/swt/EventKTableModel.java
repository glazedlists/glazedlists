/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import de.kupzog.ktable.KTableModel;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTable;
import de.kupzog.ktable.renderers.TextCellRenderer;
import org.eclipse.swt.graphics.Point;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swt.GlazedListsSWT;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.gui.TableFormat;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventKTableModel implements KTableModel, ListEventListener {

    private KTable table;

    /** the proxy moves events to the SWT thread */
    private TransformedList swtThreadSource = null;

    /** maps row objects to cell values */
    private KTableFormat tableFormat;

    public EventKTableModel(KTable table, EventList source, KTableFormat tableFormat) {
        this.table = table;
        this.swtThreadSource = GlazedListsSWT.swtThreadProxyList(source, table.getDisplay());
        this.tableFormat = tableFormat;

        // listen for events on the SWT display thread
        swtThreadSource.addListEventListener(this);
    }
    public EventKTableModel(KTable table, EventList source, TableFormat tableFormat) {
        this(table, source, new TableFormatKTableFormat(tableFormat));
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
        if(row < getFixedHeaderRowCount()) return tableFormat.getColumnHeaderValue(row, column);

        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            return tableFormat.getColumnValue(swtThreadSource.get(row - getFixedHeaderRowCount()), column);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public String getTooltipAt(int column, int row) {
        if(row < getFixedHeaderRowCount()) return null;

        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            return tableFormat.getColumnTooltip(swtThreadSource.get(row - getFixedHeaderRowCount()), column);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public KTableCellEditor getCellEditor(int column, int row) {
        if(row < getFixedHeaderRowCount()) return null;

        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            Object baseObject = swtThreadSource.get(row);
            return tableFormat.getColumnEditor(baseObject, column);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public void setContentAt(int column, int row, Object value) {
        if(row < getFixedHeaderRowCount()) return;

        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            tableFormat.setColumnValue(swtThreadSource.get(row - getFixedHeaderRowCount()), value, column);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public KTableCellRenderer getCellRenderer(int column, int row) {
        if(row < getFixedHeaderRowCount()) return KTableCellRenderer.defaultRenderer;

        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            return tableFormat.getColumnRenderer(swtThreadSource.get(row - getFixedHeaderRowCount()), column);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public Point belongsToCell(int column, int row) {
        return new Point(column, row);
    }

    /** {@inheritDoc} */
    public int getRowCount() {
        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            return swtThreadSource.size() + getFixedHeaderRowCount();
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public int getFixedHeaderRowCount() {
        return tableFormat.getFixedHeaderRowCount();
    }

    /** {@inheritDoc} */
    public int getFixedSelectableRowCount() {
        return tableFormat.getFixedSelectableColumnCount();
    }

    /** {@inheritDoc} */
    public int getColumnCount() {
        return tableFormat.getColumnCount();
    }

    /** {@inheritDoc} */
    public int getFixedHeaderColumnCount() {
        return tableFormat.getFixedHeaderColumnCount();
    }

    /** {@inheritDoc} */
    public int getFixedSelectableColumnCount() {
        return tableFormat.getFixedSelectableColumnCount();
    }

    /** {@inheritDoc} */
    public int getColumnWidth(int col) {
        return tableFormat.getColumnWidth(col);
    }

    /** {@inheritDoc} */
    public boolean isColumnResizable(int col) {
        return tableFormat.isColumnResizable(col);
    }

    /** {@inheritDoc} */
    public void setColumnWidth(int col, int width) {
        tableFormat.setColumnWidth(col, width);
    }

    /** {@inheritDoc} */
    public int getRowHeight(int row) {
        if(row < getFixedHeaderRowCount()) {
            return 20;
        } else if(row < getRowCount()) {
            swtThreadSource.getReadWriteLock().readLock().lock();
            try {
                return tableFormat.getRowHeight(swtThreadSource.get(row - getFixedHeaderRowCount()));
            } finally {
                swtThreadSource.getReadWriteLock().readLock().unlock();
            }
        } else {
            return 20;
        }
    }

    /** {@inheritDoc} */
    public boolean isRowResizable(int row) {
        if(row < getFixedHeaderRowCount()) return false;

        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            return tableFormat.isRowResizable(swtThreadSource.get(row - getFixedHeaderRowCount()));
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public int getRowHeightMinimum() {
        return tableFormat.getRowHeightMinimum();
    }

    /** {@inheritDoc} */
    public void setRowHeight(int row, int value) {
        if(row < getFixedHeaderRowCount()) return;

        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            tableFormat.setRowHeight(swtThreadSource.get(row - getFixedHeaderRowCount()), value);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }
}