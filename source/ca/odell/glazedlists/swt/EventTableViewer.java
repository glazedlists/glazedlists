/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.event.*;
// to make of use Barcode
import ca.odell.glazedlists.impl.adt.*;
// SWT toolkit stuff for displaying widgets
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.SelectionListener;

/**
 * A view helper that displays an EventList in an SWT table.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * <p><strong>Warning:</strong> This class is a a developer preview and subject to
 * many bugs and API changes.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventTableViewer implements ListEventListener {

    /** the heavyweight table */
    private Table table;

    /** whether the underlying table is Virtual */
    private boolean tableIsVirtual = false;

    /** the first source event list to dispose */
    private TransformedList disposeSource = null;

    /** the proxy moves events to the SWT user interface thread */
    private TransformedList swtSource = null;

    /** Enables check support */
    private TableCheckFilterList checkFilter = null;

    /** Specifies how to render table headers and sort */
    private TableFormat tableFormat;

    /** For selection management */
    private SelectionManager selection = null;

    /** To track multiple removes in one change to fix issue 197 */
    private Barcode deletes = new Barcode();

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}.  The
     * {@link Table} is formatted with an automatically generated
     * {@link TableFormat}. It uses JavaBeans and Reflection to create a
     * {@link TableFormat} as specified.
     */
    public EventTableViewer(EventList source, Table table, String[] propertyNames, String[] columnLabels) {
        this(source, table, GlazedLists.tableFormat(propertyNames, columnLabels));
    }

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}.  The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     */
    public EventTableViewer(EventList source, Table table, TableFormat tableFormat) {
        swtSource = GlazedListsSWT.swtThreadProxyList(source, table.getDisplay());
        disposeSource = swtSource;
        deletes.addWhite(0, swtSource.size());

        // insert a checked source if supported by the table
        if((table.getStyle() & SWT.CHECK) > 0) {
            checkFilter = new TableCheckFilterList(swtSource, table, tableFormat);
            swtSource = checkFilter;
        }

        // save table, source list and table format
        this.table = table;
        this.tableFormat = tableFormat;

        // Enable the selection lists
        selection = new SelectionManager(swtSource, new SelectableTable());

        // determine if the provided table is Virtual
        tableIsVirtual = SWT.VIRTUAL == (table.getStyle() & SWT.VIRTUAL);

        // setup initial values
        initTable();
        if(!tableIsVirtual) {
            populateTable();
        } else {
            table.setItemCount(source.size());
            table.addListener(SWT.SetData, new VirtualTableListener());
        }

        // listen for events, using the user interface thread
        swtSource.addListEventListener(this);
    }

    /**
     * Builds the columns and headers for the {@link Table}
     */
    private void initTable() {
        table.setHeaderVisible(true);
        for(int c = 0; c < tableFormat.getColumnCount(); c++) {
            TableColumn column = new TableColumn(table, SWT.LEFT, c);
            column.setText((String)tableFormat.getColumnName(c));
            column.setWidth(80);
        }
    }

    /**
     * Populates the table with the initial data from the list.
     */
    private void populateTable() {
        for(int r = 0; r < swtSource.size(); r++) {
            addRow(r, swtSource.get(r));
        }
    }

    /**
     * Adds the item at the specified row.
     */
    private void addRow(int row, Object value) {
        // Table isn't Virtual, or adding in the middle
        if(!tableIsVirtual || row < table.getItemCount()) {
            TableItem item = new TableItem(table, 0, row);
            setItemText(item, value);

        // Table is Virtual and adding at the end
        } else {
            table.setItemCount(table.getItemCount() + 1);
        }
    }

    /**
     * Updates the item at the specified row.
     */
    private void updateRow(int row, Object value) {
        TableItem item = table.getItem(row);
        setItemText(item, value);
    }

    /**
     * Sets all of the column values on a {@link TableItem}.
     */
    private void setItemText(TableItem item, Object value) {
        for(int i = 0; i < tableFormat.getColumnCount(); i++) {
            Object cellValue = tableFormat.getColumnValue(value, i);
            if(cellValue != null) item.setText(i, cellValue.toString());
            else item.setText(i, "");
        }
    }

    /**
     * Gets the {@link TableFormat}.
     */
    public TableFormat getTableFormat() {
        return tableFormat;
    }

    /**
     * Gets the {@link Table} that is being managed by this
     * {@link EventTableViewer}.
     */
    public Table getTable() {
        return table;
    }


    /**
     * Sets this {@link Table} to be formatted by a different
     * {@link TableFormat}.  This method is not yet implemented for SWT.
     */
    public void setTableFormat(TableFormat tableFormat) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set whether this shall show only checked elements.
     */
    public void setCheckedOnly(boolean checkedOnly) {
        checkFilter.setCheckedOnly(checkedOnly);
    }
    /**
     * Get whether this is showing only checked elements.
     */
    public boolean getCheckedOnly() {
        return checkFilter.getCheckedOnly();
    }

    /**
     * Gets all checked items.
     */
    public java.util.List getAllChecked() {
        return checkFilter.getAllChecked();
    }

    /**
     * Get the source of this {@link EventTableViewer}.
     */
    public EventList getSourceList() {
        return swtSource;
    }

    /**
     * Provides access to an {@link EventList} that contains items from the
     * viewed {@link Table} that are not currently selected.
     */
    public EventList getDeselected() {
        return selection.getSelectionList().getDeselected();
    }

    /**
     * Provides access to an {@link EventList} that contains items from the
     * viewed {@link Table} that are currently selected.
     */
    public EventList getSelected() {
        return selection.getSelectionList().getSelected();
    }

    /**
     * When the source list is changed, this forwards the change to the
     * displayed {@link Table}.
     */
    public void listChanged(ListEvent listChanges) {
        swtSource.getReadWriteLock().readLock().lock();
        int firstChange = swtSource.size();
        try {
            // Disable redraws so that the table is updated in bulk
            table.setRedraw(false);

            // Apply changes to the list
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int adjustedIndex = deletes.getIndex(changeIndex, Barcode.WHITE);
                int changeType = listChanges.getType();

                // Insert a new element in the Table and the Barcode
                if(changeType == ListEvent.INSERT) {
                    deletes.addWhite(adjustedIndex, 1);
                    addRow(adjustedIndex, swtSource.get(changeIndex));
                    firstChange = Math.min(changeIndex, firstChange);

                // Update the element in the Table
                } else if(changeType == ListEvent.UPDATE) {
                    updateRow(adjustedIndex, swtSource.get(changeIndex));

                // Just mark the element as deleted in the Barcode
                } else if(changeType == ListEvent.DELETE) {
                    deletes.setBlack(adjustedIndex, 1);
                    firstChange = Math.min(changeIndex, firstChange);
                }
            }

            // Process the deletes as a single Table change
            if(deletes.blackSize() > 0) {
                int[] deletedIndices = new int[deletes.blackSize()];
                for(BarcodeIterator i = deletes.iterator(); i.hasNextBlack(); ) {
                    i.nextBlack();
                    deletedIndices[i.getBlackIndex()] = i.getIndex();
                }
                table.remove(deletedIndices);
                deletes.clear();
                deletes.addWhite(0, swtSource.size());
            }

            // Reapply selection to the Table
            if(firstChange < swtSource.size()) {
                selection.fireSelectionChanged(firstChange, swtSource.size() - 1);
            }

            // Re-enable redraws to update the table
            table.setRedraw(true);
        } finally {
            swtSource.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Inverts the current selection.
     */
    public void invertSelection() {
        selection.getSelectionList().invertSelection();
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
     * Respond to view changes on a {@link Table} that is created with the
     * {@link SWT#VIRTUAL} style flag.
     */
    protected final class VirtualTableListener implements Listener {
        public void handleEvent(Event e) {
            // Get the TableItem from the Table
            TableItem item = (TableItem)e.item;
            int tableIndex = table.indexOf(item);

            // Adjust the index to where it should be if it is beyond size
            if(tableIndex >= swtSource.size()) {
                tableIndex = deletes.getWhiteIndex(tableIndex, false);
            }



            // Set the value on the Virtual element
            setItemText(item, swtSource.get(tableIndex));
        }
    }

    /**
     * Releases the resources consumed by this {@link EventTableViewer} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventTableViewer} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventTableViewer}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link EventTableViewer} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link EventTableViewer} after it has been disposed.
     */
    public void dispose() {
        selection.dispose();
        disposeSource.dispose();
    }
}
