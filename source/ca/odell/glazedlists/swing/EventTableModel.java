/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * A {@link TableModel} that holds an {@link EventList}. Each element of the list
 * corresponds to a row in the {@link TableModel}. The columns of the table are
 * specified using a {@link TableFormat}.
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
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventTableModel<E> extends AbstractTableModel implements ListEventListener<E> {

    /** the proxy moves events to the Swing Event Dispatch thread */
    protected TransformedList<E,E> swingThreadSource;

    /** the source of data for this TableModel, which may or may not be {@link #swingThreadSource} */
    protected EventList<E> source;

    /** specifies how column data is extracted from each row object */
    private TableFormat<? super E> tableFormat;

    /** reusable TableModelEvent for broadcasting changes */
    private final MutableTableModelEvent tableModelEvent = new MutableTableModelEvent(this);

    /**
     * Creates a new table model that extracts column data from the given
     * <code>source</code> using the the given <code>tableFormat</code>.
     *
     * @param source the EventList that provides the row objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     */
    public EventTableModel(EventList<E> source, TableFormat<? super E> tableFormat) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableModel
        source.getReadWriteLock().readLock().lock();
        try {
            final TransformedList<E,E> decorated = createSwingThreadProxyList(source);

            // if the create method actually returned a decorated form of the source,
            // record it so it may later be disposed
            if (decorated != null && decorated != source)
                this.source = swingThreadSource = decorated;
            else
                this.source = source;

            this.tableFormat = tableFormat;

            // prepare listeners
            this.source.addListEventListener(this);
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
     * @param source the EventList that provides the row objects
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
        this(source, GlazedLists.tableFormat(propertyNames, columnLabels, writable));
    }

    /**
     * This method exists as a hook for subclasses that may have custom
     * threading needs within their EventTableModels. By default, this method
     * will wrap the given <code>source</code> in a SwingThreadProxyList if it
     * is not already a SwingThreadProxyList. Subclasses may replace this logic
     * and return either a custom ThreadProxyEventList of their choosing, or
     * return <code>null</code> or the <code>source</code> unchanged in order
     * to indicate that <strong>NO</strong> ThreadProxyEventList is desired.
     * In these cases it is expected that some external mechanism will ensure
     * that threading is handled correctly.
     *
     * @param source the EventList that provides the row objects
     * @return the source wrapped in some sort of ThreadProxyEventList if
     *      Thread-proxying is desired, or either <code>null</code> or the
     *      <code>source</code> unchanged to indicate that <strong>NO</strong>
     *      Thread-proxying is desired
     */
    protected TransformedList<E,E> createSwingThreadProxyList(EventList<E> source) {
        return GlazedListsSwing.isSwingThreadProxyList(source) ? null : GlazedListsSwing.swingThreadProxyList(source);
    }

    /**
     * Gets the Table Format.
     */
    public TableFormat<? super E> getTableFormat() {
        return tableFormat;
    }
    /**
     * Sets the {@link TableFormat} that will extract column data from each
     * element. This has some very important consequences. Any cell selections
     * will be lost - this is due to the fact that the TableFormats may have
     * different numbers of columns, and JTable has no event to specify columns
     * changing without rows.
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
     * to the table which repaints the table cells. Because this class is
     * backed by {@link GlazedListsSwing#swingThreadProxyList}, all natural
     * calls to this method are guaranteed to occur on the Swing EDT.
     */
    public void listChanged(ListEvent<E> listChanges) {
        // for all changes, one block at a time
        while (listChanges.nextBlock()) {
            // get the current change info
            int startIndex = listChanges.getBlockStartIndex();
            int endIndex = listChanges.getBlockEndIndex();
            int changeType = listChanges.getType();
            // create a table model event for this block
            tableModelEvent.setValues(startIndex, endIndex, changeType);
            fireTableChanged(tableModelEvent);
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
     * Retrieves the value at the specified location of the table.
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
     * to call any method on an {@link EventTableModel} after it has been disposed.
     * As such, this {@link EventTableModel} should be detached from its
     * corresponding Component <strong>before</strong> it is disposed.
     */
    public void dispose() {
        source.removeListEventListener(this);

        // if we created the swingThreadSource then we must also dispose it
        if (swingThreadSource != null)
            swingThreadSource.dispose();

        // this encourages exceptions to be thrown if this model is incorrectly accessed again
        swingThreadSource = null;
        source = null;
    }
}