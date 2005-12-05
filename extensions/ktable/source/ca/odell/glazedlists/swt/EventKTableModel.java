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
        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            return tableFormat.getColumnValue(swtThreadSource.get(row), column);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    public String getTooltipAt(int column, int row) {
        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            return tableFormat.getColumnTooltip(swtThreadSource.get(row), column);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    public KTableCellEditor getCellEditor(int column, int row) {
        if(!(tableFormat instanceof WritableTableFormat)) return null;

        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            Object baseObject = swtThreadSource.get(row);
            if(!((WritableTableFormat)tableFormat).isEditable(baseObject, column)) return null;
            return tableFormat.getColumnEditor(baseObject, column);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    public void setContentAt(int column, int row, Object value) {
        if(!(tableFormat instanceof WritableTableFormat)) throw new IllegalStateException();

        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            ((WritableTableFormat)tableFormat).setColumnValue(swtThreadSource.get(row), value, column);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    public KTableCellRenderer getCellRenderer(int column, int row) {
        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            return tableFormat.getColumnRenderer(swtThreadSource.get(row), column);
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    public Point belongsToCell(int column, int row) {
        return new Point(column, row);
    }

    public int getRowCount() {
        swtThreadSource.getReadWriteLock().readLock().lock();
        try {
            return swtThreadSource.size();
        } finally {
            swtThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    public int getFixedHeaderRowCount() {
        return 0;
    }

    public int getFixedSelectableRowCount() {
        return 0;
    }

    public int getColumnCount() {
        return tableFormat.getColumnCount();
    }

    public int getFixedHeaderColumnCount() {
        return 0;
    }

    public int getFixedSelectableColumnCount() {
        return 0;
    }

    public int getColumnWidth(int col) {
        return 100;
    }

    public boolean isColumnResizable(int col) {
        return true;
    }

    public void setColumnWidth(int col, int width) {
        // do nothing
    }

    public int getRowHeight(int row) {
        return 20;
    }

    public boolean isRowResizable(int row) {
        return true;
    }

    public int getRowHeightMinimum() {
        return 10;
    }

    public void setRowHeight(int row, int value) {
        // do nothing
    }
}