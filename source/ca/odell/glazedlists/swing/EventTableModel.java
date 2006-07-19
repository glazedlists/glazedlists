/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.swing.SwingThreadProxyEventList;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.event.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
// tables for displaying lists
import javax.swing.table.*;

/**
 * A {@link TableModel} that holds an {@link EventList}. Each element of the list
 * corresponds to a row in the {@link TableModel}. The columns of the table must
 * be specified using a {@link TableFormat}.
 *
 * <p>The EventTableModel class is <strong>not thread-safe</strong>. Unless otherwise
 * noted, all methods are only safe to be called from the event dispatch thread.
 * To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)}.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial/">Glazed Lists Tutorial</a>
 *
 * @see SwingUtilities#invokeAndWait(Runnable)
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=112">Bug 112</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=146">Bug 146</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=177">Bug 177</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventTableModel<E> extends AbstractTableModel implements ListEventListener<E> {

    /** the proxy moves events to the Swing Event Dispatch thread */
    private final TransformedList<E,E> swingThreadSource;

    /** Specifies how to render table headers and sort */
    private TableFormat<E> tableFormat;

    /** Reusable table event for broadcasting changes */
    private final MutableTableModelEvent tableModelEvent = new MutableTableModelEvent(this);

    /**
     * Creates a new table that renders the specified list in the specified
     * format.
     */
    public EventTableModel(EventList<E> source, TableFormat<E> tableFormat) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableModel
        source.getReadWriteLock().readLock().lock();
        try {
            if (source instanceof SwingThreadProxyEventList)
                this.swingThreadSource = (SwingThreadProxyEventList<E>) source;
            else
                this.swingThreadSource = GlazedListsSwing.swingThreadProxyList(source);
            this.tableFormat = tableFormat;

            // prepare listeners
            swingThreadSource.addListEventListener(this);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Creates a new table that renders the specified list with an automatically
     * generated {@link TableFormat}. It uses JavaBeans and reflection to create
     * a {@link TableFormat} as specified.
     *
     * <p>Note that the classes which will be obfuscated may not work with
     * reflection. In this case, implement a {@link TableFormat} manually.
     *
     * @param propertyNames an array of property names in the JavaBeans format.
     *      For example, if your list contains Objects with the methods getFirstName(),
     *      setFirstName(String), getAge(), setAge(Integer), then this array should
     *      contain the two strings "firstName" and "age". This format is specified
     *      by the JavaBeans {@link java.beans.PropertyDescriptor}.
     * @param columnLabels the corresponding column names for the listed property
     *      names. For example, if your columns are "firstName" and "age", then
     *      your labels might be "First Name" and "Age".
     * @param writable an array of booleans specifying which of the columns in
     *      your table are writable.
     */
    public EventTableModel(EventList<E> source, String[] propertyNames, String[] columnLabels, boolean[] writable) {
        this(source, (TableFormat<E>) GlazedLists.tableFormat(propertyNames, columnLabels, writable));
    }

    /**
     * Gets the Table Format.
     */
    public TableFormat<E> getTableFormat() {
        return tableFormat;
    }
    /**
     * Sets this table to be rendered by a different table format. This has
     * some very important consequences. The selection will be lost - this is
     * due to the fact that the table formats may have different numbers of
     * columns, and JTable has no event to specify columns changing without
     * rows.
     */
    public void setTableFormat(TableFormat<E> tableFormat) {
        this.tableFormat = tableFormat;
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
        return tableFormat.getColumnName(column);
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
        return tableFormat.getColumnCount();
    }


	/**
     * Gets the class of elements in the specified column. This behaviour can be
     * customized by implementing the {@link AdvancedTableFormat} interface.
	 */
	public Class getColumnClass(int columnIndex) {
		// See if the TableFormat is specifies a column class
		if(tableFormat instanceof AdvancedTableFormat) {
			return ((AdvancedTableFormat)tableFormat).getColumnClass(columnIndex);
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
            return tableFormat.getColumnValue(swingThreadSource.get(row), column);
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
        if(tableFormat instanceof WritableTableFormat) {
            WritableTableFormat<E> writableTableFormat = (WritableTableFormat<E>)tableFormat;
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
        if(tableFormat instanceof WritableTableFormat) {
            swingThreadSource.getReadWriteLock().writeLock().lock();
            try {
                final WritableTableFormat<E> writableTableFormat = (WritableTableFormat<E>)tableFormat;
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
     * Releases the resources consumed by this {@link EventTableModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventTableModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventTableModel}
     * to be garbage collected before its source {@link EventList}. This is 
     * necessary for situations where an {@link EventTableModel} is short-lived but
     * its source {@link EventList} is long-lived.
     * 
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link EventTableModel} after it has been disposed.
     */
    public void dispose() {
        swingThreadSource.dispose();
    }
}