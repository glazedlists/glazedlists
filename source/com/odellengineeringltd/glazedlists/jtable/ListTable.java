/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jtable;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// for responding to selection the Glazed Lists way
import com.odellengineeringltd.glazedlists.listselectionmodel.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import java.awt.GridBagLayout;
// for responding to user actions
import java.awt.event.*;
// tables for displaying lists
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
// this class uses tables for displaying message lists
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.SortedSet;

/**
 * A table that displays the contents of an event-driven list.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListTable extends AbstractTableModel implements ListChangeListener, ListSelectionListener, MouseListener {

    /** The Swing table for selecting a message from a list */
    private JTable table;
    private JScrollPane tableScrollPane;
    private ListSelectionModel tableSelectionModel;

    /** the complete list of messages before filters */
    protected EventList source;
        
    /** whom to notify of selection changes */
    private SelectionList selectionList;
    private ArrayList selectionListeners = new ArrayList();
    
    /** Specifies how to render table headers and sort */
    private TableFormat tableFormat;

    /** Save the selected record to eliminate unnecessary selection updates */
    private int selectedIndex = -1;
    /** When the list is changing and we should ignore selection events */
    private boolean ignoreSelectionEvents = false;
    
    /** Reusable table event for broadcasting changes */
    private MutableTableModelEvent tableModelEvent;
    
    /**
     * Creates a new table that renders the specified list in the specified
     * format.
     */
    public ListTable(EventList source, TableFormat tableFormat) {
        this.source = source;
        tableModelEvent = new MutableTableModelEvent(this);
        this.tableFormat = tableFormat;
        constructWidgets();
        
        selectionList = new SelectionList(source, table.getSelectionModel());
        source.addListChangeListener(new ListChangeListenerEventThreadProxy(this));
    }
    
    
    /**
     * Construct all widgets. Add action listeners to the widgets and
     * get them generally ready for display.
     */
    private void constructWidgets() {
        table = new JTable(this);
        tableSelectionModel = table.getSelectionModel();
        //tableSelectionModel.setSelectionMode(tableSelectionModel.SINGLE_SELECTION);
        tableSelectionModel.addListSelectionListener(this);
        tableFormat.configureTable(table);
        tableScrollPane = new JScrollPane(table);
        table.addMouseListener(this);
    }
    
    /**
     * Gets an event list that contains the current selection in
     * this list table. That list changes dynamically as elements
     * are selected and deselected from the list.
     */
    public EventList getSelectionList() {
        return selectionList;
    }
    
    /**
     * Gets the components for display in a user-constructed panel. 
     */
    public JScrollPane getTableScrollPane() {
        return tableScrollPane;
    }
    /**
     * Gets just the raw table. This should only be used to retrieve
     * information from the table model. To display the table, use the
     * getTableScrollPane() method.
     */
    public JTable getTable() {
        return table;
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
        tableFormat.configureTable(table);
        tableModelEvent.setValues(0, 0, TableModelEvent.HEADER_ROW);
        fireTableChanged(tableModelEvent);
    }
    
    /** whenever a list change covers greater than this many rows, redraw the whole thing */
    public int changeSizeRepaintAllThreshhold = 25;
    
    /**
     * For implementing the ListChangeListener interface. This sends changes
     * to the table which can repaint the table cells. Because this class uses
     * a ListChangeListenerEventThreadProxy, it is guaranteed that all natural
     * calls to this method use the Swing thread.
     *
     * This tests the size of the change to determine how to handle it. If the
     * size of the change is greater than the changeSizeRepaintAllThreshhold,
     * then the entire table is notified as changed. Otherwise only the descrete
     * areas that changed are notified.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        // for avoiding extra selection events
        ignoreSelectionEvents = true;
        // when all events hae already been processed by clearing the event queue
        if(!listChanges.hasNext()) {
            return;
        // notify all changes simultaneously
        } else if(listChanges.getBlocksRemaining() >= changeSizeRepaintAllThreshhold) {
            listChanges.clearEventQueue();
            // first scroll to row zero
            tableScrollPane.getViewport().setViewPosition(table.getCellRect(0, 0, true).getLocation());
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
        // trigger a selection event if necessary
        ignoreSelectionEvents = false;
        // fire a selection event to update the selection
        selectedIndex = -1;
        valueChanged(null);
    }

    /**
     * For implementing the ListSelectionListener interface, this listens
     * when the selection changes and notifies the SelectionListeners of the
     * change.
     */
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        // if the list is adjusting, another event is on its way, so ignore this one
        if(ignoreSelectionEvents) return;
        if(listSelectionEvent != null && listSelectionEvent.getValueIsAdjusting()) return;
        // figure out which row (if any) is now selected and return it
        int newSelectedIndex = tableSelectionModel.getMinSelectionIndex();
        // quit while we're ahead if this is the same selection
        if(newSelectedIndex == selectedIndex) return;
        // save the selected record for next time
        selectedIndex = newSelectedIndex;
        Object selected = getSelected();
        // update the list listeners to display the new selection
        if(selected == null) {
            for(int r = 0; r < selectionListeners.size(); r++) {
                ((SelectionListener)selectionListeners.get(r)).clearSelection();
            }
        } else {
            for(int r = 0; r < selectionListeners.size(); r++) {
                ((SelectionListener)selectionListeners.get(r)).setSelection(selected);
            }
        }
    }
    /**
     * Gets the currently selected object, or null if there is currently no
     * selection.
     */
    public Object getSelected() {
        if(selectedIndex == -1) return null;
        return source.get(selectedIndex);
    }
    /**
     * For implementing the MouseListener interface. When the cell is double
     * clicked, update the listeners.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) { 
            // move clicked contact into current contact panel, and then activate episodes for that contact.
            Object selected = getSelected();
            if(selected == null) return;
            for(int r = 0; r < selectionListeners.size(); r++) {
                ((SelectionListener)selectionListeners.get(r)).setDoubleClicked(selected);
            }
        }
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    /**
     * Registers the specified SelectionListener to receive updates
     * when the selection changes.
     *
     * This will tell the specified SelectionListener about the current
     * status of the table.
     */
    public void addSelectionListener(SelectionListener selectionListener) {
        selectionListeners.add(selectionListener);
        // notify the new listener of the current status 
        if(selectedIndex == -1) {
            selectionListener.clearSelection();
        } else {
            selectionListener.setSelection(getSelected());
        }
    }


    /**
     * Access methods for getting contact information to display. These are 
     * table model methods that sit <strong>behind</strong> the sorted table.
     * This means that all row-based access is not translated via the
     * RecordSorter.
     */
    public String getColumnName(int column) {
        return tableFormat.getFieldName(column);
    }
    public int getRowCount() {
        return source.size();
    }
    public int getColumnCount() {
        return tableFormat.getFieldCount();
    }
    /**
     * Retrieves the value at the specified location from the table.
     * 
     * Before every get, we need to validate the row because there may be an
     * update waiting in the event queue. For example, it is possible that
     * the source list has been updated by a database thread. Such a change
     * may have been sent as notification, but after this request in the
     * event queue. In the case where a row is no longer available, null is
     * returned. The value returned is insignificant in this case because the
     * Event queue will very shortly be repainting (or removing) the row
     * anyway.
     */
    public Object getValueAt(int row, int column) {
        // ensure that this value still exists before retrieval
        if(row < getRowCount()) {
            return tableFormat.getFieldValue(source.get(row), column);
        } else {
            //new Exception("Returning null for removed row " + row).printStackTrace();
            return null;
        }
    }

    /**
     * The list table is not editable. For an editable list table, use the
     * WritableListTable instead.
     */
    public boolean isCellEditable(int row, int column) {
        return false;
    }
    public void setValueAt(Object value, int row, int column) {
        throw new UnsupportedOperationException("The basic list table is not editable, use a WritableListTable instead");
    }
}
