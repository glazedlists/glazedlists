/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.event.*;
import javax.swing.table.*;

public class EventTreeTableModel<E> extends AbstractTableModel implements ListEventListener<E>, TreeTableModel {

    /** the proxy moves events to the Swing Event Dispatch thread */
    private TransformedList<E,E> swingThreadSource;

    /** <tt>true</tt> indicates that disposing this TableModel should dispose of the swingThreadSource as well */
    private final boolean disposeSwingThreadSource;

    private final EventTableModel<E> tableModel;

    private final TreeFormat<E> treeFormat;

    /** reusable table event for broadcasting changes */
    private final MutableTableModelEvent tableModelEvent = new MutableTableModelEvent(this);

    /**
     * Creates a new table that renders the specified list in the specified
     * format.
     */
    public EventTreeTableModel(EventList<E> source, TableFormat<E> tableFormat, TreeFormat<E> treeFormat) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableModel
        source.getReadWriteLock().readLock().lock();
        try {
            this.disposeSwingThreadSource = !GlazedListsSwing.isSwingThreadProxyList(source);
            this.swingThreadSource = disposeSwingThreadSource ? GlazedListsSwing.swingThreadProxyList(source) : (TransformedList<E,E>) source;

            this.tableModel = new EventTableModel<E>(this.swingThreadSource, tableFormat);
            this.swingThreadSource.removeListEventListener(this.tableModel);

            this.treeFormat = treeFormat;

            // prepare listeners
            swingThreadSource.addListEventListener(this);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns the height of the row object at the given <code>rowIndex</code>
     * within the tree represented by this {@link TreeTableModel}.
     */
    public int getDepth(int rowIndex) {
        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            return TreeTableSupport.getDepth(treeFormat, swingThreadSource.get(rowIndex));
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * For implementing the ListEventListener interface. This sends changes
     * to the table which repaint the table cells. Because this class is backed
     * by {@link GlazedListsSwing#swingThreadProxyList}, all natural calls to
     * this method are guaranteed to occur on the Swing EDT.
     */
    public void listChanged(ListEvent<E> listChanges) {
        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            // for all changes, one block at a time
            while(listChanges.nextBlock()) {
                // get the current change info
                int startIndex = listChanges.getBlockStartIndex();
                int endIndex = listChanges.getBlockEndIndex();
                int changeType = listChanges.getType();
                // create a table model event for this block
                tableModelEvent.setValues(startIndex, endIndex, changeType);
                fireTableChanged(tableModelEvent);
            }
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** @inheritDoc */
    public TableFormat<E> getTableFormat() {
        return tableModel.getTableFormat();
    }

    /** @inheritDoc */
    public void setTableFormat(TableFormat<E> tableFormat) {
        tableModel.setTableFormat(tableFormat);
    }

    /** @inheritDoc */
    public E getElementAt(int index) {
        return tableModel.getElementAt(index);
    }

    /** @inheritDoc */
    public String getColumnName(int column) {
        return tableModel.getColumnName(column);
    }

    /** @inheritDoc */
    public int getRowCount() {
        return tableModel.getRowCount();
    }

    /** @inheritDoc */
    public int getColumnCount() {
        return tableModel.getColumnCount();
    }

    /** @inheritDoc */
	public Class getColumnClass(int columnIndex) {
		return tableModel.getColumnClass(columnIndex);
	}

    /** @inheritDoc */
    public Object getValueAt(int row, int column) {
        return tableModel.getValueAt(row, column);
    }

    /** @inheritDoc */
    public boolean isCellEditable(int row, int column) {
        return tableModel.isCellEditable(row, column);
    }

    /** @inheritDoc */
    public void setValueAt(Object editedValue, int row, int column) {
        tableModel.setValueAt(editedValue, row, column);
    }

    /** @inheritDoc */
    public void dispose() {
        swingThreadSource.removeListEventListener(this);

        tableModel.dispose();

        if (this.disposeSwingThreadSource)
            tableModel.dispose();

        // this encourages exceptions to be thrown if this model is incorrectly accessed again
        swingThreadSource = null;
    }
}