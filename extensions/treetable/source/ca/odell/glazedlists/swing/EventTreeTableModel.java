/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.event.*;
import javax.swing.table.*;

public class EventTreeTableModel<E> extends AbstractTableModel implements TreeTableModel, ListEventListener<E> {

    /** the proxy moves events to the Swing Event Dispatch thread */
    private final TransformedList<E,E> swingThreadSource;

    /** Specifies how to render table headers and sort */
    private TreeTableFormat<E> treeTableFormat;

    /** Reusable table event for broadcasting changes */
    private final MutableTableModelEvent tableModelEvent = new MutableTableModelEvent(this);

    /**
     * Creates a new table that renders the specified list in the specified format.
     */
    public EventTreeTableModel(EventList<E> source, TreeTableFormat<E> treeTableFormat) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTreeTableModel
        source.getReadWriteLock().readLock().lock();
        try {
            this.swingThreadSource = GlazedListsSwing.swingThreadProxyList(source);
            this.treeTableFormat = treeTableFormat;

            // prepare listeners
            swingThreadSource.addListEventListener(this);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Gets the Tree Table Format.
     */
    public TreeTableFormat<E> getTreeTableFormat() {
        return treeTableFormat;
    }
    /**
     * Sets this table to be rendered by a different tree table format. This has
     * some very important consequences. The selection will be lost - this is
     * due to the fact that the tree table formats may have different numbers of
     * columns. Another consequence is that the entire table will require
     * repainting. In a ScrollPane, only the currently displayed cells and
     * those above (before) them will require repainting. In order to provide
     * the best performance, the scroll pane may be scrolled to the top to
     * prevent a delay while rendering off-screen cells.
     */
    public void setTreeTableFormat(TreeTableFormat<E> treeTableFormat) {
        this.treeTableFormat = treeTableFormat;
        tableModelEvent.setStructureChanged();
        fireTableChanged(tableModelEvent);
    }

    /**
     * Retrieves the value at the specified location from the table.
     *
     * <p>This may be used by renderers to paint the cells of a row differently
     * based on the entire value for that row.
     *
     * @see #getValueAt(int,int)
     */
    public E getElementAt(int index) {
        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            return swingThreadSource.get(index);
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns the height of the row object at the given <code>rowIndex</code>
     * within the tree represented by this {@link TreeTableModel}.
     */
    public int getHeight(int rowIndex) {
        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            return TreeTableSupport.getHeight(treeTableFormat, swingThreadSource.get(rowIndex));
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

    /**
     * Fetch the name for the specified column.
     */
    public String getColumnName(int column) {
        return treeTableFormat.getColumnName(column);
    }

    /**
     * The number of rows equals the number of entries in the source event list.
     */
    public int getRowCount() {
        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            return swingThreadSource.size();
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Get the column count as specified by the table format.
     */
    public int getColumnCount() {
        return treeTableFormat.getColumnCount();
    }


	/**
     * Gets the class of elements in the specified column. This behaviour can be
     * customized by implementing the {@link AdvancedTableFormat} interface.
	 */
	public Class getColumnClass(int columnIndex) {
		// See if the TableFormat is specifies a column class
		if(treeTableFormat instanceof AdvancedTableFormat) {
			return ((AdvancedTableFormat)treeTableFormat).getColumnClass(columnIndex);
		// If not, use the default...
		} else {
            return super.getColumnClass(columnIndex);
        }
	}

    /**
     * Retrieves the value at the specified location from the table.
     *
     * <p>Before every get, we need to validate the row because there may be an
     * update waiting in the event queue. For example, it is possible that
     * the source list has been updated by a database thread. Such a change
     * may have been sent as notification, but after this request in the
     * event queue. In the case where a row is no longer available, null is
     * returned. The value returned is insignificant in this case because the
     * Event queue will very shortly be repainting (or removing) the row
     * anyway.
     */
    public Object getValueAt(int row, int column) {
        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            return treeTableFormat.getColumnValue(swingThreadSource.get(row), column);
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * The list table is not editable. For an editable list table, use the
     * WritableListTable instead.
     */
    public boolean isCellEditable(int row, int column) {
        // ensure this is a writable table
        if(treeTableFormat instanceof WritableTableFormat) {
            WritableTableFormat<E> writableTableFormat = (WritableTableFormat<E>)treeTableFormat;
            swingThreadSource.getReadWriteLock().readLock().lock();
            try {
                final E toEdit = swingThreadSource.get(row);
                return writableTableFormat.isEditable(toEdit, column);
            } finally {
                swingThreadSource.getReadWriteLock().readLock().unlock();
            }
        // this is not a writable table
        } else {
            return false;
        }
    }

    /**
     * The list table is not editable. For an editable list table, use the
     * WritableListTable instead.
     */
    public void setValueAt(Object editedValue, int row, int column) {
        // ensure this is a writable table
        if(treeTableFormat instanceof WritableTableFormat) {
            swingThreadSource.getReadWriteLock().writeLock().lock();
            try {
                final WritableTableFormat<E> writableTableFormat = (WritableTableFormat<E>)treeTableFormat;
                // get the object being edited from the source list
                final E baseObject = swingThreadSource.get(row);

                // tell the table format to set the value based on what it knows
                final E updatedObject = writableTableFormat.setColumnValue(baseObject, editedValue, column);

                // try to update the list with the revised value
                if(updatedObject != null) {
                    // check if updating the baseObject has caused it to be removed from this
                    // TableModel (FilterList) or moved to another location (SortedList)
                    final boolean baseObjectHasNotMoved = row < this.getRowCount() && swingThreadSource.get(row) == baseObject;

                    // if the row is still present, update it
                    if(baseObjectHasNotMoved)
                        swingThreadSource.set(row, updatedObject);
                }
            } finally {
                swingThreadSource.getReadWriteLock().writeLock().unlock();
            }
        // this is not a writable table
        } else {
            throw new UnsupportedOperationException("Unexpected setValueAt() on read-only table");
        }
    }

    /**
     * Releases the resources consumed by this {@link EventTreeTableModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventTreeTableModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventTreeTableModel}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link EventTreeTableModel} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link EventTreeTableModel} after it has been disposed.
     */
    public void dispose() {
        swingThreadSource.dispose();
    }
}