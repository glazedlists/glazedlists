/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.util.*;
// SWT toolkit stuff for displaying widgets
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
// standard collections as support
import java.util.*;

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
public class EventTableViewer implements ListEventListener, Selectable {

    /** the heavyweight table */
    private Table table;

    /** whether the underlying table is Virtual */
    private boolean tableIsVirtual = false;

    /** the complete list of messages before filters */
    protected EventList source;

    /** Specifies how to render table headers and sort */
    private TableFormat tableFormat;

    /** Enables check support */
    private TableCheckFilterList checkFilter = null;

    /** For selection management */
    private SelectionList selectionList = null;

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}.  The
     * {@link Table} is formatted with an automatically generated
     * {@link TableFormat}. It uses JavaBeans and Reflection to create a
     * {@link TableFormat} as specified.
     */
    public EventTableViewer(EventList source, Table table, String[] propertyNames, String[] columnLabels) {
		this(source, table, BeanToolFactory.tableFormat(propertyNames, columnLabels));
	}

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}.  The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     */
    public EventTableViewer(EventList source, Table table, TableFormat tableFormat) {
        // insert a checked source if supported by the table
        if((table.getStyle() & SWT.CHECK) > 0) {
            this.checkFilter = new TableCheckFilterList(source, table, tableFormat);
            source = checkFilter;
        }

        // save table, source list and table format
        this.table = table;
        this.source = source;
        this.tableFormat = tableFormat;

		// Enable the selection lists
		selectionList = new SelectionList(source, this);

        // determine if the provided table is Virtual
        tableIsVirtual = SWT.VIRTUAL == (table.getStyle() & SWT.VIRTUAL);

        initTable();
        if(!tableIsVirtual) {
            populateTable();
        } else {
            table.setItemCount(source.size());
            table.addListener(SWT.SetData, new VirtualTableListener());
        }

        // listen for events, using the user interface thread
        if(source == checkFilter) {
            source.addListEventListener(this);
        } else {
            source.addListEventListener(new UserInterfaceThreadProxy(this, table.getDisplay()));
        }
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
        for(int r = 0; r < source.size(); r++) {
            addRow(r, source.get(r));
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
    public List getAllChecked() {
        return checkFilter.getAllChecked();
    }

    /**
     * Get the source of this {@link EventTableViewer}.
     */
    public EventList getSourceList() {
        return source;
    }

    /**
     * Provides access to an {@link EventList} that contains items from the
     * viewed {@link Table} that are not currently selected.
     */
	public EventList getDeselected() {
		return selectionList.getDeselected();
	}

    /**
     * Provides access to an {@link EventList} that contains items from the
     * viewed {@link Table} that are currently selected.
     */
	public EventList getSelected() {
		return selectionList.getSelected();
	}

    /**
     * When the source list is changed, this forwards the change to the
     * displayed {@link Table}.
     */
    public void listChanged(ListEvent listChanges) {
        source.getReadWriteLock().readLock().lock();
        int firstModified = source.size();
        try {
            // Apply changes to the list
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();

                if(changeType == ListEvent.INSERT) {
                    addRow(changeIndex, source.get(changeIndex));
                    firstModified = Math.min(changeIndex, firstModified);
                } else if(changeType == ListEvent.UPDATE) {
                    updateRow(changeIndex, source.get(changeIndex));
                } else if(changeType == ListEvent.DELETE) {
                    table.remove(changeIndex);
                    firstModified = Math.min(changeIndex, firstModified);
                }
            }

            // Reapply selection to the Table
            for(int i = firstModified;i < table.getItemCount();i++) {
				if(selectionList.isSelected(i)) {
					table.select(i);
				} else {
					table.deselect(i);
				}
			}
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

	/** Methods for the Selectable Interface */

    /** {@inheritDoc} */
	public void addSelectionListener(SelectionListener listener) {
		table.addSelectionListener(listener);
	}

	/** {@inheritDoc} */
	public void removeSelectionListener(SelectionListener listener) {
		table.removeSelectionListener(listener);
	}

	/** {@inheritDoc} */
	public void addListener(int type, Listener listener) {
		table.addListener(type, listener);
	}

	/** {@inheritDoc}*/
	public void removeListener(int type, Listener listener) {
		table.removeListener(type, listener);
	}

	/** {@inheritDoc} */
	public int getSelectionCount() {
		return table.getSelectionCount();
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
	public boolean isSelected(int index) {
		return table.isSelected(index);
	}

    /**
     * Respond to view changes on a {@link Table} that is created with the
     * {@link SWT#VIRTUAL} style flag.
     */
    protected final class VirtualTableListener implements Listener {
        public void handleEvent(Event e) {
            TableItem item = (TableItem)e.item;
            int tableIndex = table.indexOf(item);
            Object value = source.get(tableIndex);
            setItemText(item, value);
        }
    }
}
