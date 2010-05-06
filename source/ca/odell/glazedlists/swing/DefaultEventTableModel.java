/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import java.awt.EventQueue;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * A {@link TableModel} that holds an {@link EventList}. Each element of the list
 * corresponds to a row in the {@link TableModel}. The columns of the table are
 * specified using a {@link TableFormat}.
 *
 * <p>The EventTableModel class is <strong>not thread-safe</strong>. Unless otherwise
 * noted, all methods are only safe to be called from the event dispatch thread.
 * To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)} and
 * wrap the source list (or some part of the source list's pipeline) using
 * GlazedListsSwing#swingThreadProxyList(EventList).</p>
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial/">Glazed Lists Tutorial</a>
 *
 * @see GlazedListsSwing#swingThreadProxyList(EventList)
 * @see SwingUtilities#invokeAndWait(Runnable)
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=112">Bug 112</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=146">Bug 146</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=177">Bug 177</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class DefaultEventTableModel<E> extends AbstractTableModel implements AdvancedTableModel<E>, ListEventListener<E> {

    /** the source of data for this TableModel, which may or may not be {@link #swingThreadSource} */
    protected EventList<E> source;

    /** indicator to dispose source list */
    private boolean disposeSource;

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
    public DefaultEventTableModel(EventList<E> source, TableFormat<? super E> tableFormat) {
        this(source, false, tableFormat);
    }

    /**
     * Creates a new table model that extracts column data from the given
     * <code>source</code> using the the given <code>tableFormat</code>.
     *
     * @param source the EventList that provides the row objects
     * @param diposeSource <code>true</code> if the source list should be disposed when disposing
     *            this model, <code>false</code> otherwise
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     */
    protected DefaultEventTableModel(EventList<E> source, boolean disposeSource, TableFormat<? super E> tableFormat) {
        this.source = source;
        this.disposeSource = disposeSource;
        this.tableFormat = tableFormat;
        source.addListEventListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public TableFormat<? super E> getTableFormat() {
        return tableFormat;
    }

    /**
     * {@inheritDoc}
     */
    public void setTableFormat(TableFormat<? super E> tableFormat) {
        this.tableFormat = tableFormat;
        tableModelEvent.setStructureChanged();
        fireTableChanged(tableModelEvent);
    }

    /**
     * {@inheritDoc}
     */
    public E getElementAt(int index) {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.get(index);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * For implementing the ListEventListener interface. This sends changes
     * to the table which repaints the table cells. Because this class is
     * backed by {@link GlazedListsSwing#swingThreadProxyList}, all natural
     * calls to this method are guaranteed to occur on the Swing EDT.
     */
    public void listChanged(ListEvent<E> listChanges) {
        handleListChange(listChanges);
    }

    /**
     * Default implementation for converting a {@link ListEvent} to
     * TableModelEvents. There will be one TableModelEvent per ListEvent block.
     * Subclasses may choose to implement a different conversion.
     *
     * @param listChanges ListEvent to translate
     */
    protected void handleListChange(ListEvent<E> listChanges) {
        if (!EventQueue.isDispatchThread())
            throw new IllegalStateException("Events to " + this.getClass().getSimpleName() + " must arrive on the EDT - consider adding GlazedListsSwing.swingThreadProxyList(source) somewhere in your list pipeline");

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
     * @return reusable TableModelEvent for broadcasting changes
     */
    protected final MutableTableModelEvent getMutableTableModelEvent() {
        return tableModelEvent;
    }

    /**
     * Fetch the name for the specified column.
     */
    @Override
    public String getColumnName(int column) {
        return tableFormat.getColumnName(column);
    }

    /**
     * The number of rows equals the number of entries in the source event list.
     */
    public int getRowCount() {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.size();
        } finally {
            source.getReadWriteLock().readLock().unlock();
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
	@Override
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
        source.getReadWriteLock().readLock().lock();
        try {
            return tableFormat.getColumnValue(source.get(row), column);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Delegates the question of whether the cell is editable or not to the
     * backing TableFormat if it is a {@link WritableTableFormat}. Otherwise,
     * the column is assumed to be uneditable.
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        if (!(tableFormat instanceof WritableTableFormat))
            return false;

        source.getReadWriteLock().readLock().lock();
        try {
            final E toEdit = source.get(row);
            return ((WritableTableFormat<E>) tableFormat).isEditable(toEdit, column);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Attempts to update the object for the given row with the
     * <code>editedValue</code>. This requires the backing TableFormat
     * be a {@link WritableTableFormat}. {@link WritableTableFormat#setColumnValue}
     * is expected to contain the logic for updating the object at the given
     * <code>row</code> with the <code>editedValue</code> which was in the
     * given <code>column</code>.
     */
    @Override
    public void setValueAt(Object editedValue, int row, int column) {
        // ensure this is a writable table
        if (!(tableFormat instanceof WritableTableFormat))
            throw new UnsupportedOperationException("Unexpected setValueAt() on read-only table");

        source.getReadWriteLock().writeLock().lock();
        try {
            // get the object being edited from the source list
            final E baseObject = source.get(row);

            // tell the table format to set the value based
            final WritableTableFormat<E> writableTableFormat = (WritableTableFormat<E>) tableFormat;
            final E updatedObject = writableTableFormat.setColumnValue(baseObject, editedValue, column);

            // if the edit was discarded we have nothing to do
            if (updatedObject != null) {
                // check if updating the baseObject has caused it to be removed from this
                // TableModel (FilterList) or moved to another location (SortedList)
                final boolean baseObjectHasNotMoved = row < getRowCount() && source.get(row) == baseObject;

                // if the row is still present in its original location, update it to induce a
                // TableModelEvent that will redraw that row in the table
                if (baseObjectHasNotMoved)
                    source.set(row, updatedObject);
            }
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        source.removeListEventListener(this);
        if (disposeSource) source.dispose();
        // this encourages exceptions to be thrown if this model is incorrectly accessed again
        source = null;
    }
}
