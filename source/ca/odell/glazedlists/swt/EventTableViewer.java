/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// SWT toolkit stuff for displaying widgets
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
// standard collections as support
import java.util.*;

/**
 * A helper that displays an EventList in an SWT table.
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

    /** the complete list of messages before filters */
    protected EventList source;

    /** Specifies how to render table headers and sort */
    private TableFormat tableFormat;

    /**
     * Creates a new table that renders the specified list in the specified format.
     */
    public EventTableViewer(EventList source, Table table, TableFormat tableFormat) {
        this.table = table;
        this.source = source;
        this.tableFormat = tableFormat;

        // prepare event list listeners
        source.getReadWriteLock().readLock().lock();
        try {
            populateTable();
            source.addListEventListener(this);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }
    
    /**
     * Populates the table with the initial data from the list.
     */
    private void populateTable() {
        // set the headers
        table.setHeaderVisible(true);
        for(int c = 0; c < tableFormat.getColumnCount(); c++) {
            TableColumn column = new TableColumn(table, SWT.LEFT, c);
            column.setText((String)tableFormat.getColumnName(c));
            column.setWidth(80);
        }

        // set the initial data
        for(int r = 0; r < source.size(); r++) {
            addRow(r, source.get(r));
        }
    }
    
    /**
     * Adds the item at the specified row.
     */
    private void addRow(int row, Object value) {
        TableItem item = new TableItem(table, 0, row);
        for(int c = 0; c < tableFormat.getColumnCount(); c++) {
            item.setText(c, (String)tableFormat.getColumnValue(value, c));
        }
    }
    
    /**
     * Updates the item at the specified row.
     */
    private void updateRow(int row, Object value) {
        TableItem item = table.getItem(row);
        for(int c = 0; c < tableFormat.getColumnCount(); c++) {
            item.setText(c, (String)tableFormat.getColumnValue(value, c));
        }
    }
    
    /**
     * Gets the Table Format.
     */
    public TableFormat getTableFormat() {
        return tableFormat;
    }

    /**
     * Sets this table to be rendered by a different table format.
     */
    public void setTableFormat(TableFormat tableFormat) {
        throw new UnsupportedOperationException();
        //this.tableFormat = tableFormat;
        //tableModelEvent.setStructureChanged();
        //fireTableChanged(tableModelEvent);
    }
    
    /**
     * When the source list is changed, this forwards the change to the
     * displayed table.
     *
     * <p>This implementation saves the entire table's selection in an ArrayList before
     * walking through the list of changes. It then walks through the table's changes.
     * Finally it adjusts the selection on the table in response to the changes.
     * Although simple, this implementation has much higher memory and runtime
     * requirements than necessary. It is desirable to optimize this method by
     * not storing a second copy of the selection list. Such an implementation would
     * use only the selection data available in the table plus a list of entries
     * which have been since overwritten.
     */
    public void listChanged(ListEvent listChanges) {

        // save the former selection
        List selection = new ArrayList();
        for(int i = 0; i < table.getItemCount(); i++) {
            selection.add(i, Boolean.valueOf(table.isSelected(i)));
        }
        
        // walk the list
        while(listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();
            
            if(changeType == ListEvent.INSERT) {
                selection.add(changeIndex, Boolean.FALSE);
                addRow(changeIndex, source.get(changeIndex));
            } else if(changeType == ListEvent.UPDATE) {
                updateRow(changeIndex, source.get(changeIndex));
            } else if(changeType == ListEvent.DELETE) {
                selection.remove(changeIndex);
                table.remove(changeIndex);
            }
        }
        
        // apply the saved selection
        for(int i = 0; i < table.getItemCount(); i++) {
            boolean selected = ((Boolean)selection.get(i)).booleanValue();
            if(selected) {
                table.select(i);
            } else {
                table.deselect(i);
            }
        }
    }
}
