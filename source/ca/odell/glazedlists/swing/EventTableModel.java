/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
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
 * @see <a href="http://publicobject.com/glazedlists/tutorial-0.9.1/">Glazed
 * Lists Tutorial</a>
 *
 * @see SwingUtilities#invokeAndWait(Runnable)
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventTableModel extends AbstractTableModel implements ListEventListener {

    /** the proxy moves events to the Swing Event Dispatch thread */
    private TransformedList swingSource = null;

    /** Specifies how to render table headers and sort */
    private TableFormat tableFormat;

    /** Reusable table event for broadcasting changes */
    private MutableTableModelEvent tableModelEvent = new MutableTableModelEvent(this);

    /** whenever a list change covers greater than this many rows, redraw the whole thing */
    private int changeSizeRepaintAllThreshhold = Integer.MAX_VALUE;

    /**
     * Creates a new table that renders the specified list in the specified
     * format.
     */
    public EventTableModel(EventList source, TableFormat tableFormat) {
        swingSource = GlazedListsSwing.swingThreadProxyList(source);
        this.tableFormat = tableFormat;

        // prepare listeners
        swingSource.addListEventListener(this);
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
    public EventTableModel(EventList source, String[] propertyNames, String[] columnLabels, boolean[] writable) {
        this(source, GlazedLists.tableFormat(propertyNames, columnLabels, writable));
    }

    /**
     * Gets the Table Format.
     */
    public TableFormat getTableFormat() {
        return tableFormat;
    }
    /**
     * Sets this table to be rendered by a different table format. This has
     * some very important consequences. The selection will be lost - this is
     * due to the fact that the table formats may have different numbers of
     * columns. Another consequence is that the entire table will require
     * repainting. In a ScrollPane, only the currently displayed cells and
     * those above (before) them will require repainting. In order to provide
     * the best performance, the scroll pane may be scrolled to the top to
     * prevent a delay while rendering off-screen cells.
     */
    public void setTableFormat(TableFormat tableFormat) {
        this.tableFormat = tableFormat;
        tableModelEvent.setStructureChanged();
        fireTableChanged(tableModelEvent);
    }

    /**
     * For implementing the ListEventListener interface. This sends changes
     * to the table which can repaint the table cells. Because this class uses
     * a EventThreadProxy, it is guaranteed that all natural
     * calls to this method use the Swing thread.
     *
     * <p>This tests the size of the change to determine how to handle it. If the
     * size of the change is greater than the changeSizeRepaintAllThreshhold,
     * then the entire table is notified as changed. Otherwise only the descrete
     * areas that changed are notified.
     */
    public void listChanged(ListEvent listChanges) {
        swingSource.getReadWriteLock().readLock().lock();
        try {
            // when all events hae already been processed by clearing the event queue
            if(!listChanges.hasNext()) return;

            // notify all changes simultaneously
            if(listChanges.getBlocksRemaining() >= changeSizeRepaintAllThreshhold) {
                listChanges.clearEventQueue();
                // first scroll to row zero
                //tableScrollPane.getViewport().setViewPosition(table.getCellRect(0, 0, true).getLocation());
                fireTableDataChanged();

            // for all changes, one block at a time
            } else {
                while(listChanges.nextBlock()) {
                    // get the current change info
                    int startIndex = listChanges.getBlockStartIndex();
                    int endIndex = listChanges.getBlockEndIndex();
                    int changeType = listChanges.getType();
                    // create a table model event for this block
                    tableModelEvent.setValues(startIndex, endIndex, changeType);
                    fireTableChanged(tableModelEvent);
                }
            }
        } finally {
            swingSource.getReadWriteLock().readLock().unlock();
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
        swingSource.getReadWriteLock().readLock().lock();
        try {
            return swingSource.size();
        } finally {
            swingSource.getReadWriteLock().readLock().unlock();
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
        swingSource.getReadWriteLock().readLock().lock();
        try {
            // ensure that this value still exists before retrieval
            if(row < getRowCount()) {
                return tableFormat.getColumnValue(swingSource.get(row), column);
            } else {
                return null;
            }
        } finally {
            swingSource.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * The list table is not editable. For an editable list table, use the
     * WritableListTable instead.
     */
    public boolean isCellEditable(int row, int column) {
        // ensure this is a writable table
        if(tableFormat instanceof WritableTableFormat) {
            WritableTableFormat writableTableFormat = (WritableTableFormat)tableFormat;
            swingSource.getReadWriteLock().readLock().lock();
            try {
                Object toEdit = swingSource.get(row);
                return writableTableFormat.isEditable(toEdit, column);
            } finally {
                swingSource.getReadWriteLock().readLock().unlock();
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
            swingSource.getReadWriteLock().writeLock().lock();
            try {
                WritableTableFormat writableTableFormat = (WritableTableFormat)tableFormat;
                // get the object being edited from the source list
                Object baseObject = swingSource.get(row);
                // tell the table format to set the value based on what it knows
                Object updatedObject = writableTableFormat.setColumnValue(baseObject, editedValue, column);
                // update the list with the revised value
                if(updatedObject != null) {
                    swingSource.set(row, updatedObject);
                }
            } finally {
                swingSource.getReadWriteLock().writeLock().unlock();
            }
        // this is not a writable table
        } else {
            throw new UnsupportedOperationException("Unexpected set() on read-only table");
        }
    }

    /**
     * Gets the minimum number of changes that will be combined into one uniform
     * change and cause selection and scrolling to be lost.
     */
    public int getRepaintAllThreshhold() {
        return changeSizeRepaintAllThreshhold;
    }
    /**
     * Sets the threshhold of the number of change blocks that will be handled
     * individually before the ListTable collapses such changes into one and simply
     * repaints the entire table. This is a work around to the JTable's poor
     * performance when handling large sets of small changes. <strong>This
     * work-around is only necessary when the JTable has variable row height</strong>.
     * When the JTable has a fixed row height, there is no performance problem and
     * this work around is unnecessary.
     *
     * <p>Two problems occur when using this work around. It will cause the table's
     * selection to be destroyed and it will cause the table's scrolling to be lost.
     *
     * <p>By default, this work around is disabled and users must enable it by calling
     * <code>setRepaintAllThreshhold()</code> to enable it. In practice, tests have shown
     * that 100 is a decent value for the repaintAllThreshhold of tables that have variable
     * height rows.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=30">Bug 30</a>
     */
    public void setRepaintAllThreshhold(int repaintAllThreshhold) {
        this.changeSizeRepaintAllThreshhold = repaintAllThreshhold;
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
        swingSource.dispose();
    }
}
