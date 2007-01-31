/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.IteratorAsEnumeration;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.Enumeration;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * A {@link TableColumnModel} that holds an {@link EventList}. Each element of
 * the list corresponds to a {@link TableColumn} in the model.
 *
 * <p>The EventTableColumnModel class is <strong>not thread-safe</strong>.
 * Unless otherwise noted, all methods are only safe to be called from the
 * event dispatch thread. To do this programmatically, use
 * {@link SwingUtilities#invokeAndWait(Runnable)}.
 *
 * @author James Lemieux
 */
public class EventTableColumnModel implements TableColumnModel, PropertyChangeListener, ListSelectionListener, ListEventListener<TableColumn> {

    /** the proxy moves events to the Swing Event Dispatch thread */
    protected TransformedList<TableColumn, TableColumn> swingThreadSource;

    /** <tt>true</tt> indicates that disposing this TableColumnModel should dispose of the swingThreadSource as well */
    private final boolean disposeSwingThreadSource;

    /** list of TableColumnModelListeners */
    private final EventListenerList listenerList = new EventListenerList();

    /** change event (only one needed) */
    private final transient ChangeEvent changeEvent = new ChangeEvent(this);

    /** model for keeping track of column selections */
    private ListSelectionModel selectionModel;

    /** column selection allowed in this column model */
    private boolean columnSelectionAllowed;

    /** width of the margin between each column */
    private int columnMargin;

    /** a local cache of the combined width of all columns */
    private int totalColumnWidth;

    /**
     * Creates a new model that contains the {@link TableColumn} objects from
     * the given <code>source</code>. Changes to the <code>source</code> are
     * reflected in this model.
     */
    public EventTableColumnModel(EventList<TableColumn> source) {
        setSelectionModel(createSelectionModel());
        setColumnMargin(1);
        invalidateWidthCache();
        setColumnSelectionAllowed(false);

        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableColumnModel
        source.getReadWriteLock().readLock().lock();
        try {
            // ensure all of the TableColumns are non-null
            for (int i = 0, n = source.size(); i < n; i++) {
                if (source.get(i) == null)
                    throw new IllegalStateException("null TableColumn objects are not allowed in EventTableColumnModel");
            }

            // start listening to each of the TableColumns for property changes that may resize the table header
            for (int i = 0, n = source.size(); i < n; i++)
                source.get(i).addPropertyChangeListener(this);

            disposeSwingThreadSource = !GlazedListsSwing.isSwingThreadProxyList(source);
            swingThreadSource = disposeSwingThreadSource ? GlazedListsSwing.swingThreadProxyList(source) : (TransformedList<TableColumn, TableColumn>) source;
            swingThreadSource.addListEventListener(this);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /** @inheritDoc */
    public void addColumn(TableColumn column) {
        swingThreadSource.getReadWriteLock().writeLock().lock();
        try {
            swingThreadSource.add(column);
        } finally {
            swingThreadSource.getReadWriteLock().writeLock().unlock();
        }
    }

    /** @inheritDoc */
    public void removeColumn(TableColumn column) {
        swingThreadSource.getReadWriteLock().writeLock().lock();
        try {
            swingThreadSource.remove(column);
        } finally {
            swingThreadSource.getReadWriteLock().writeLock().unlock();
        }
    }

    /** @inheritDoc */
    public void moveColumn(int columnIndex, int newIndex) {
        if (columnIndex < 0 || columnIndex >= getColumnCount())
            throw new IllegalArgumentException("columnIndex out of range");

        if (newIndex < 0 || newIndex >= getColumnCount())
            throw new IllegalArgumentException("newIndex out of range");

        // If the column has not yet moved far enough to change positions 
        // post the event anyway, the "draggedDistance" property of the
        // tableHeader will say how far the column has been dragged.
        // Here we are really trying to get the best out of an
        // API that could do with some rethinking. We preserve backward
        // compatibility by slightly bending the meaning of these methods.
        if (columnIndex == newIndex) {
            fireColumnMoved(new TableColumnModelEvent(this, columnIndex, newIndex));
            return;
        }

        swingThreadSource.getReadWriteLock().writeLock().lock();
        try {
            final boolean selected = selectionModel.isSelectedIndex(columnIndex);
            swingThreadSource.add(newIndex, swingThreadSource.remove(columnIndex));

            // preserve the selection after the move if one existed
            if (selected)
                selectionModel.addSelectionInterval(newIndex, newIndex);
        } finally {
            swingThreadSource.getReadWriteLock().writeLock().unlock();
        }
    }

    /** @inheritDoc */
    public void setColumnMargin(int newMargin) {
        if (newMargin != columnMargin) {
            columnMargin = newMargin;
            fireColumnMarginChanged();
        }
    }

    /** @inheritDoc */
    public int getColumnMargin() {
        return columnMargin;
    }

    /** @inheritDoc */
    public int getColumnCount() {
        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            return swingThreadSource.size();
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** @inheritDoc */
    public Enumeration<TableColumn> getColumns() {
        return new IteratorAsEnumeration<TableColumn>(swingThreadSource.iterator());
    }

    /** @inheritDoc */
    public int getColumnIndex(Object identifier) {
        if (identifier == null)
            throw new IllegalArgumentException("identifier is null");

        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            for (int i = 0, n = swingThreadSource.size(); i < n; i++) {
                if (identifier.equals(swingThreadSource.get(i).getIdentifier()))
		            return i;
            }

            throw new IllegalArgumentException("Identifier not found");
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** @inheritDoc */
    public TableColumn getColumn(int columnIndex) {
        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            return swingThreadSource.get(columnIndex);
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /** @inheritDoc */
    public int getColumnIndexAtX(int x) {
        if (x < 0) return -1;

        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            for (int i = 0, n = swingThreadSource.size(); i < n; i++) {
                TableColumn column = swingThreadSource.get(i);
                x = x - column.getWidth();
                if (x < 0)
                    return i;
            }
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }

        return -1;
    }

    /** @inheritDoc */
    public int getTotalColumnWidth() {
        if (totalColumnWidth == -1)
            recalcWidthCache();

        return totalColumnWidth;
    }

    /**
     * Recalculates the total combined width of all columns.
     */
    private void recalcWidthCache() {
        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            totalColumnWidth = 0;
            for (int i = 0, n = swingThreadSource.size(); i < n; i++)
                totalColumnWidth += swingThreadSource.get(i).getWidth();
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Mark the cached value of the total width of all columns as dirty and in
     * need of being recalculated.
     */
    private void invalidateWidthCache() {
	    totalColumnWidth = -1;
    }

    /** @inheritDoc */
    public void setColumnSelectionAllowed(boolean flag) {
	    columnSelectionAllowed = flag;
    }

    /** @inheritDoc */
    public boolean getColumnSelectionAllowed() {
	    return columnSelectionAllowed;
    }

    /** @inheritDoc */
    public int[] getSelectedColumns() {
        if (selectionModel != null) {
            int iMin = selectionModel.getMinSelectionIndex();
            int iMax = selectionModel.getMaxSelectionIndex();

            if (iMin == -1 || iMax == -1)
                return new int[0];

            int[] rvTmp = new int[1 + (iMax - iMin)];
            int n = 0;
            for (int i = iMin; i <= iMax; i++) {
                if (selectionModel.isSelectedIndex(i)) {
                    rvTmp[n++] = i;
                }
            }

            int[] rv = new int[n];
            System.arraycopy(rvTmp, 0, rv, 0, n);
            return rv;
        }
        return new int[0];
    }

    /** @inheritDoc */
    public int getSelectedColumnCount() {
        if (selectionModel != null) {
            int iMin = selectionModel.getMinSelectionIndex();
            int iMax = selectionModel.getMaxSelectionIndex();
            int count = 0;

            for (int i = iMin; i <= iMax; i++) {
                if (selectionModel.isSelectedIndex(i))
                    count++;
            }
            return count;
        }
        return 0;
    }

    /** @inheritDoc */
    public void setSelectionModel(ListSelectionModel newModel) {
        if (newModel == null)
            throw new IllegalArgumentException("newModel may not be null");

        if (newModel != selectionModel) {
            if (selectionModel != null)
                selectionModel.removeListSelectionListener(this);

            selectionModel = newModel;

            selectionModel.addListSelectionListener(this);
        }
    }

    /** @inheritDoc */
    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /** @inheritDoc */
    public void addColumnModelListener(TableColumnModelListener listener) {
	    listenerList.add(TableColumnModelListener.class, listener);
    }

    /** @inheritDoc */
    public void removeColumnModelListener(TableColumnModelListener listener) {
	    listenerList.remove(TableColumnModelListener.class, listener);
    }

    /**
     * Watch for changes to the column width or preferred column width and
     * trigger a relayout of the table header when they change.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();

        if (name == "width" || name == "preferredWidth") {
            invalidateWidthCache();
            fireColumnMarginChanged();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        fireColumnSelectionChanged(e);
    }

    public void listChanged(ListEvent<TableColumn> listChanges) {
        // arbitrary changes have occurred so we begin by invalidating the cached total width of all TableColumns
        invalidateWidthCache();

        while (listChanges.next()) {
            final int index = listChanges.getIndex();
            final int changeType = listChanges.getType();

            if (changeType == ListEvent.DELETE) {
                if (selectionModel != null)
                    selectionModel.removeIndexInterval(index, index);

                final TableColumn oldColumn = listChanges.getOldValue();
                oldColumn.removePropertyChangeListener(this);
                fireColumnRemoved(new TableColumnModelEvent(this, index, 0));

            } else if (changeType == ListEvent.INSERT) {
                final TableColumn newColumn = listChanges.getSourceList().get(index);
                if (newColumn == null)
                    throw new IllegalStateException("null TableColumn objects are not allowed in EventTableColumnModel");

                newColumn.addPropertyChangeListener(this);
                fireColumnAdded(new TableColumnModelEvent(this, 0, getColumnCount() - 1));

            } else if (changeType == ListEvent.UPDATE) {
                final TableColumn oldColumn = listChanges.getOldValue();
                final TableColumn newColumn = listChanges.getSourceList().get(index);
                if (newColumn == null)
                    throw new IllegalStateException("null TableColumn objects are not allowed in EventTableColumnModel");

                if (oldColumn != newColumn) {
                    oldColumn.removePropertyChangeListener(this);
                    newColumn.addPropertyChangeListener(this);
                }

                fireColumnMoved(new TableColumnModelEvent(this, index, index));
            }
        }
    }

    /**
     * Releases the resources consumed by this {@link EventTableColumnModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventTableColumnModel} will be garbage collected without a
     * call to {@link #dispose()}, but not before its source {@link EventList}
     * is garbage collected. By calling {@link #dispose()}, you allow the
     * {@link EventTableColumnModel} to be garbage collected before its source
     * {@link EventList}. This is necessary for situations where an
     * {@link EventTableColumnModel} is short-lived but its source
     * {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an {@link EventTableColumnModel} after it has been
     * disposed.
     */
    public void dispose() {
        swingThreadSource.getReadWriteLock().readLock().lock();

        try {
            // stop listening to each of the TableColumns for property changes
            for (int i = 0, n = swingThreadSource.size(); i < n; i++)
                swingThreadSource.get(i).removePropertyChangeListener(this);

            swingThreadSource.removeListEventListener(this);

            // if we created the swingThreadSource then we must also dispose it
            if (disposeSwingThreadSource)
                swingThreadSource.dispose();

        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }

        // this encourages exceptions to be thrown if this model is incorrectly accessed again
        swingThreadSource = null;
    }

    /**
     * Creates a new default list selection model.
     */
    protected ListSelectionModel createSelectionModel() {
        return new DefaultListSelectionModel();
    }

    //
    // Convenience methods to fire types of TableColumnModelEvent objects to
    // registered TableColumnModelListeners.
    //

    protected void fireColumnAdded(TableColumnModelEvent e) {
        final Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableColumnModelListener.class)
                ((TableColumnModelListener) listeners[i + 1]).columnAdded(e);
        }
    }

    protected void fireColumnRemoved(TableColumnModelEvent e) {
        final Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableColumnModelListener.class)
                ((TableColumnModelListener) listeners[i + 1]).columnRemoved(e);
        }
    }

    protected void fireColumnMoved(TableColumnModelEvent e) {
        final Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableColumnModelListener.class)
                ((TableColumnModelListener) listeners[i + 1]).columnMoved(e);
        }
    }

    protected void fireColumnSelectionChanged(ListSelectionEvent e) {
        final Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableColumnModelListener.class)
                ((TableColumnModelListener) listeners[i + 1]).columnSelectionChanged(e);
        }
    }

    protected void fireColumnMarginChanged() {
        final Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableColumnModelListener.class)
                ((TableColumnModelListener) listeners[i + 1]).columnMarginChanged(changeEvent);
        }
    }
}