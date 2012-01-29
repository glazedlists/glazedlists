/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.adt.Barcode;
import ca.odell.glazedlists.impl.adt.BarcodeIterator;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.*;


/**
 * A view helper that displays an EventList in an SWT table.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author Holger Brands
 */
public class DefaultEventTableViewer<E> implements ListEventListener<E> {

    /** the heavyweight table */
    private Table table;

    /** Enables check support */
    private TableCheckFilterList<E,E> checkFilterList;

    /** the original source EventList to which this EventTableViewer is listening */
    private EventList<E> originalSource;

    /** the actual EventList to which this EventTableViewer is listening */
    protected EventList<E> source;

    /** to manipulate Tables in a generic way */
    private TableHandler<E> tableHandler;

    /** Specifies how to render table headers and sort */
    private TableFormat<? super E> tableFormat;

    /** Specifies how to render column values represented by TableItems. */
    private TableItemConfigurer<? super E> tableItemConfigurer;

    /** For selection management */
    private SelectionManager<E> selection;

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     */
    public DefaultEventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat) {
        this(source, table, tableFormat, TableItemConfigurer.DEFAULT);
    }

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     * @param tableItemConfigurer responsible for configuring table items
     */
    public DefaultEventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat,
            TableItemConfigurer<? super E> tableItemConfigurer) {
    	this(source, table, tableFormat, tableItemConfigurer, false);
    }

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     * @param tableItemConfigurer responsible for configuring table items
     * @param diposeSource <code>true</code> if the source list should be disposed when disposing
     *            this model, <code>false</code> otherwise
     */
    protected DefaultEventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat,
            TableItemConfigurer<? super E> tableItemConfigurer, boolean disposeSource) {
        // check for valid arguments early
        if (source == null)
            throw new IllegalArgumentException("source list may not be null");
        if (table == null)
            throw new IllegalArgumentException("Table may not be null");
        if (tableFormat == null)
            throw new IllegalArgumentException("TableFormat may not be null");
        if (tableItemConfigurer == null)
            throw new IllegalArgumentException("TableItemConfigurer may not be null");

        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableViewer
        source.getReadWriteLock().writeLock().lock();
        try {
            this.source = source;
            if (disposeSource) {
            	this.originalSource = source;
            }
            // insert a checked source if supported by the table
            if ((table.getStyle() & SWT.CHECK) == SWT.CHECK) {
                this.source = checkFilterList = new TableCheckFilterList<E,E>(this.source, table, tableFormat);
            }
            this.table = table;
            this.tableFormat = tableFormat;
            this.tableItemConfigurer = tableItemConfigurer;

            // enable the selection lists
            selection = new SelectionManager<E>(this.source, new SelectableTable());

            // configure how the Table will be manipulated
            if(isTableVirtual()) {
                tableHandler = new VirtualTableHandler();
            } else {
                tableHandler = new DefaultTableHandler();
            }

            // setup the Table with initial values
            initTable();
            tableHandler.populateTable();

            // prepare listeners
            this.source.addListEventListener(this);

            // indicate the dependency between this EventTableViewer & the SelectionManager's ListSelection
            // (this is crucial because it ensures the ListEventPublisher delivers events to this EventTableViewer
            // *before* the SelectionManager's ListSelection, which is the correct relative order of notification)
            this.source.getPublisher().setRelatedListener(selection.getSelectionList(), this);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Gets wether the table is virtual or not.
     */
    private boolean isTableVirtual() {
        return ((table.getStyle() & SWT.VIRTUAL) == SWT.VIRTUAL);
    }

    /**
     * Builds the columns and headers for the {@link Table}
     */
    private void initTable() {
        table.setHeaderVisible(true);
        final TableColumnConfigurer configurer = getTableColumnConfigurer();
        for(int c = 0; c < tableFormat.getColumnCount(); c++) {
            TableColumn column = new TableColumn(table, SWT.LEFT, c);
            column.setText(tableFormat.getColumnName(c));
            column.setWidth(80);
            if (configurer != null) {
                configurer.configure(column, c);
            }
        }
    }

    /**
     * Sets all of the column values on a {@link TableItem}.
     */
    private void renderTableItem(TableItem item, E value, int row) {
        for(int i = 0; i < tableFormat.getColumnCount(); i++) {
            final Object cellValue = tableFormat.getColumnValue(value, i);
            tableItemConfigurer.configure(item, value, cellValue, row, i);
        }
    }

    /**
     * Gets the {@link TableFormat}.
     */
    public TableFormat<? super E> getTableFormat() {
        return tableFormat;
    }

    /**
     * Sets this {@link Table} to be formatted by a different
     * {@link TableFormat}.  This method is not yet implemented for SWT.
     */
    public void setTableFormat(TableFormat<E> tableFormat) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the {@link TableItemConfigurer}.
     */
    public TableItemConfigurer<? super E> getTableItemConfigurer() {
        return tableItemConfigurer;
    }

    /**
     * Sets a new {@link TableItemConfigurer}. The cell values of existing,
     * non-virtual table items will be reconfigured with the specified configurer.
     */
    public void setTableItemConfigurer(TableItemConfigurer<? super E> tableItemConfigurer) {
        if (tableItemConfigurer == null)
            throw new IllegalArgumentException("TableItemConfigurer may not be null");

        this.tableItemConfigurer = tableItemConfigurer;
        // determine the index of the last, non-virtual table item
        final int maxIndex = tableHandler.getLastIndex();
        if (maxIndex < 0) return;
        // Disable redraws so that the table is updated in bulk
        table.setRedraw(false);
        source.getReadWriteLock().readLock().lock();
        try {
            // reprocess all table items between indexes 0 and maxIndex
            for (int i = 0; i <= maxIndex; i++) {
    //            System.out.println("setTableItemConfigurer: Reconfigure Item " + i);
                final E rowValue = source.get(i);
                for (int c = 0; c < tableFormat.getColumnCount(); c++) {
                    final Object columnValue = tableFormat.getColumnValue(rowValue, c);
                    tableItemConfigurer.configure(table.getItem(i), rowValue, columnValue, i, c);
                }
            }
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
        // Re-enable redraws to update the table
        table.setRedraw(true);
    }

    /**
     * Gets the {@link TableColumnConfigurer} or <code>null</code> if not
     * available.
     */
    private TableColumnConfigurer getTableColumnConfigurer() {
        if (tableFormat instanceof TableColumnConfigurer) {
            return (TableColumnConfigurer) tableFormat;
        }
        return null;
    }

    /**
     * Gets the {@link Table} that is being managed by this
     * {@link DefaultEventTableViewer}.
     */
    public Table getTable() {
        return table;
    }

    /**
     * Set whether this shall show only checked elements.
     */
    public void setCheckedOnly(boolean checkedOnly) {
        checkFilterList.setCheckedOnly(checkedOnly);
    }
    /**
     * Get whether this is showing only checked elements.
     */
    public boolean getCheckedOnly() {
        return checkFilterList.getCheckedOnly();
    }

    /**
     * Gets all checked items.
     */
    public List<E> getAllChecked() {
        checkFilterList.getReadWriteLock().readLock().lock();
        try {
            return checkFilterList.getAllChecked();
        } finally {
            checkFilterList.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Get the source of this {@link DefaultEventTableViewer}.
     */
    public EventList<E> getSourceList() {
        return source;
    }

    /**
     * Provides access to an {@link EventList} that contains items from the
     * viewed {@link Table} that are not currently selected.
     */
    public EventList<E> getDeselected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return selection.getSelectionList().getDeselected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Gets an {@link EventList} that contains only deselected values and
     * modifies the selection state on mutation.
     *
     * Adding an item to this list deselects it and removing an item selects it.
     * If an item not in the source list is added an
     * {@link IllegalArgumentException} is thrown
     */
    public EventList<E> getTogglingDeselected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return selection.getSelectionList().getTogglingDeselected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Provides access to an {@link EventList} that contains items from the
     * viewed {@link Table} that are currently selected.
     */
    public EventList<E> getSelected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return selection.getSelectionList().getSelected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Gets an {@link EventList} that contains only selected
     * values and modifies the selection state on mutation.
     *
     * Adding an item to this list selects it and removing an item deselects it.
     * If an item not in the source list is added an
     * {@link IllegalArgumentException} is thrown.
     */
    public EventList<E> getTogglingSelected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return selection.getSelectionList().getTogglingSelected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * When the source list is changed, this forwards the change to the
     * displayed {@link Table}.
     */
    public void listChanged(ListEvent listChanges) {
        // if the table is no longer available, we don't want to do anything as
        // it will result in a "Widget is disposed" exception
        if (table.isDisposed()) return;

        Barcode deletes = new Barcode();
        deletes.addWhite(0, source.size());
        int firstChange = source.size();
        // Disable redraws so that the table is updated in bulk
        table.setRedraw(false);

        // Apply changes to the list
        while (listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int adjustedIndex = deletes.getIndex(changeIndex, Barcode.WHITE);
            int changeType = listChanges.getType();

            // Insert a new element in the Table and the Barcode
            if (changeType == ListEvent.INSERT) {
                deletes.addWhite(adjustedIndex, 1);
                tableHandler.addRow(adjustedIndex, source.get(changeIndex));
                firstChange = Math.min(changeIndex, firstChange);

                // Update the element in the Table
            } else if (changeType == ListEvent.UPDATE) {
                tableHandler.updateRow(adjustedIndex, source.get(changeIndex));

                // Just mark the element as deleted in the Barcode
            } else if (changeType == ListEvent.DELETE) {
                deletes.setBlack(adjustedIndex, 1);
                firstChange = Math.min(changeIndex, firstChange);
            }
        }

        // Process the deletes as a single Table change
        if (deletes.blackSize() > 0) {
            int[] deletedIndices = new int[deletes.blackSize()];
            for (BarcodeIterator i = deletes.iterator(); i.hasNextBlack();) {
                i.nextBlack();
                deletedIndices[i.getBlackIndex()] = i.getIndex();
            }
            tableHandler.removeAll(deletedIndices);
        }

        // Re-enable redraws to update the table
        table.setRedraw(true);
    }

    /**
     * Inverts the current selection.
     */
    public void invertSelection() {
        selection.getSelectionList().invertSelection();
    }

    /**
     * Releases the resources consumed by this {@link DefaultEventTableViewer} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link DefaultEventTableViewer} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link DefaultEventTableViewer}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link DefaultEventTableViewer} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link DefaultEventTableViewer} after it has been disposed.
     */
    public void dispose() {
        tableHandler.dispose();
        selection.dispose();
        source.removeListEventListener(this);
        source.getPublisher().clearRelatedListener(selection.getSelectionList(), this);

        // if we created the checkFilterList then we must also dispose it
        if (checkFilterList != null)
            checkFilterList.dispose();

        if (originalSource != null)
        	originalSource.dispose();

        // this encourages exceptions to be thrown if this model is incorrectly accessed again
        checkFilterList = null;
        tableHandler = null;
        selection = null;
        source = null;
        originalSource = null;
    }

    /**
     * To use common selectable widget logic in a widget unaware fashion.
     */
    private final class SelectableTable implements Selectable {
        /** {@inheritDoc} */
        public void addSelectionListener(SelectionListener listener) {
            table.addSelectionListener(listener);
        }

        /** {@inheritDoc} */
        public void removeSelectionListener(SelectionListener listener) {
            table.removeSelectionListener(listener);
        }

        /** {@inheritDoc} */
        public int getSelectionIndex() {
            return table.getSelectionIndex();
        }

        /** {@inheritDoc} */
        public int[] getSelectionIndices() {
            return table.getSelectionIndices();
        }

        /** {@inheritDoc} */
        public int getStyle() {
            return table.getStyle();
        }

        /** {@inheritDoc} */
        public void select(int index) {
            table.select(index);
        }

        /** {@inheritDoc} */
        public void deselect(int index) {
            table.deselect(index);
        }
    }

    /**
     * Defines how Tables will be manipulated.
     */
    private interface TableHandler<E> {

        /**
         * Populate the Table with data.
         */
        public void populateTable();

        /**
         * Add a row with the given value.
         */
        public void addRow(int row, E value);

        /**
         * Update a row with the given value.
         */
        public void updateRow(int row, E value);

        /**
         * Removes a set of rows in a single call
         */
        public void removeAll(int[] rows);

        /**
         * Disposes of this TableHandler
         */
        public void dispose();

        /**
         * Gets the last real, non-virtual row index. -1 means empty or
         * completely virtual table
         */
        public int getLastIndex();
    }

    /**
     * Allows manipulation of standard SWT Tables.
     */
    private final class DefaultTableHandler implements TableHandler<E> {
        /**
         * Populate the Table with initial data.
         */
        public void populateTable() {
            for(int i = 0, n = source.size(); i < n; i++) {
                addRow(i, source.get(i));
            }
        }

        /**
         * Adds a row with the given value.
         */
        public void addRow(int row, E value) {
            TableItem item = new TableItem(table, 0, row);
            renderTableItem(item, value, row);
        }

        /**
         * Updates a row with the given value.
         */
        public void updateRow(int row, E value) {
            TableItem item = table.getItem(row);
            renderTableItem(item, value, row);
        }

        /**
         * Removes a set of rows in a single call
         */
        public void removeAll(int[] rows) {
            table.remove(rows);
        }

        /**
         * Disposes of this TableHandler.
         */
        public void dispose() {
            // no-op for default Tables
        }

        /** {@inheritedDoc} */
        public int getLastIndex() {
            return table.getItemCount() - 1;
        }
    }

    /**
     * Allows manipulation of Virtual Tables and handles additional aspects
     * like providing the SetData callback method and tracking which values
     * are Virtual.
     */
    private final class VirtualTableHandler implements TableHandler<E>, Listener {

        /** to keep track of what's been requested */
        private final Barcode requested = new Barcode();

        /**
         * Create a new VirtualTableHandler.
         */
        public VirtualTableHandler() {
            requested.addWhite(0, source.size());
            table.addListener(SWT.SetData, this);
        }

        /**
         * Populate the Table with initial data.
         */
        public void populateTable() {
            table.setItemCount(source.size());
        }

        /**
         * Adds a row with the given value.
         */
        public void addRow(int row, E value) {
            // Adding before the last non-Virtual value
            if(row <= getLastIndex()) {
                requested.addBlack(row, 1);
                TableItem item = new TableItem(table, 0, row);
                renderTableItem(item, value, row);

            // Adding in the Virtual values at the end
            } else {
                requested.addWhite(requested.size(), 1);
                table.setItemCount(table.getItemCount() + 1);
            }
        }

        /**
         * Updates a row with the given value.
         */
        public void updateRow(int row, E value) {
            // Only set a row if it is NOT Virtual
            if(!isVirtual(row)) {
                requested.setBlack(row, 1);
                TableItem item = table.getItem(row);
                renderTableItem(item, value, row);
            }
        }

        /**
         * Removes a set of rows in a single call
         */
        public void removeAll(int[] rows) {
            // Sync the requested barcode to clear values that have been removed
            for(int i = 0; i < rows.length; i++) {
                requested.remove(rows[i] - i, 1);
            }
            table.remove(rows);
        }

        /**
         * Returns the highest index that has been requested or -1 if the
         * Table is entirely Virtual.
         */
        public int getLastIndex() {
            // Everything is Virtual
            if(requested.blackSize() == 0) return -1;

            // Return the last index
            else return requested.getIndex(requested.blackSize() - 1, Barcode.BLACK);
        }

        /**
         * Returns whether a particular row is Virtual in the Table.
         */
        private boolean isVirtual(int rowIndex) {
            return requested.getBlackIndex(rowIndex) == -1;
        }

        /**
         * Respond to requests for values to fill Virtual rows.
         */
        public void handleEvent(Event e) {
            // Get the TableItem from the Table
            TableItem item = (TableItem)e.item;

            // Calculate the index that should be requested because the Table
            // might be sending incorrectly indexed TableItems in the event.
            int whiteIndex = requested.getWhiteIndex(table.indexOf(item), false);
            int index = requested.getIndex(whiteIndex, Barcode.WHITE);
//            System.out.println("ETV.handleEvent: e.index|index|topindex|lastindex=" + e.index + "|"
//                    + index + "|" + table.getTopIndex() + "|" + getLastIndex());
            // Set the value on the Virtual element
            requested.setBlack(index, 1);
            renderTableItem(item, source.get(index), index);
        }

        /**
         * Allows this handler to clean up after itself.
         */
        public void dispose() {
            table.removeListener(SWT.SetData, this);
        }
    }
}