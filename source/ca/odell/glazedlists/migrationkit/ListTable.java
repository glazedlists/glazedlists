/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.migrationkit;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.swing.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import java.awt.GridBagLayout;
import java.awt.Point;
// for responding to user actions
import javax.swing.event.*;
import java.awt.event.*;
// tables for displaying lists
import javax.swing.table.*;
// standard collections as support
import java.util.*;

/**
 * An adaptor for the <code>ListTable</code> class found in Glazed Lists v.0.8 and
 * prior that uses the <code>EventTableModel</code> for implementation.
 *
 * <p>The ListTable class is <strong>not thread-safe</strong>. Unless otherwise
 * noted, all methods are only safe to be called from the event dispatch thread.
 * To do this programmatically, use <code>SwingUtilities.invokeAndWait()</code>.
 *
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part1/index.html#listtable">Glazed
 * Lists Tutorial Part 1 - Basics</a>
 *
 * @see SwingUtilities#invokeAndWait(Runnable)
 *
 * @deprecated This class will not be available in future releases of Glazed Lists.
 *      It exists to help users migrate between Glazed Lists < 0.8 and Glazed Lists >= 0.9.
 *      Users of ListTable should consider EventTableModel which does not restrict
 *      them to a specific JTable base class.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListTable implements MouseListener {

    /** The event table model to defer */
    private EventTableModel eventTableModel;
    
    /** The Swing table for selecting a message from a list */
    private JTable table;
    private JScrollPane tableScrollPane;

    /** the complete list of messages before filters */
    protected EventList source;

    /** selection managent is all by a SelectionModelEventList */
    private EventSelectionModel eventSelectionModel;
    private EventList selectionList;
    private ListSelectionModel listSelectionModel;
    
    /** the selection notifier works with SelectionListeners */
    private SelectionNotifier selectionNotifier;
    
    /**
     * Creates a new table that renders the specified list in the specified
     * format.
     */
    public ListTable(EventList source, TableFormat tableFormat) {
        this.source = source;
        this.eventTableModel = new EventTableModel(source, tableFormat);

        // create the selection model
        eventSelectionModel = new EventSelectionModel(source);
        selectionList = eventSelectionModel.getEventList();
        listSelectionModel = eventSelectionModel.getListSelectionModel();
        selectionNotifier = new SelectionNotifier(selectionList);

        // construct widgets
        table = new JTable(eventTableModel);
        table.setSelectionModel(listSelectionModel);
        //tableFormat.configureTable(table);
        tableScrollPane = new JScrollPane(table);
        
        // prepare listeners
        table.addMouseListener(this);
    }
    
    
    /**
     * Gets an event list that contains the current selection in
     * this list table. That list changes dynamically as elements
     * are selected and deselected from the list.
     *
     * <p>Because the list is dynamic, users should be careful of changes
     * when accessing the SelectionList. It is safer to access the 
     * SelectionList on the Swing event dispatch thread because no
     * selection changes can occur while that thread is executing code. 
     * It is still possible for changes to occur if the base list is
     * being modified on another thread.
     */
    public EventList getSelectionList() {
        return eventSelectionModel.getEventList();
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
        return eventTableModel.getTableFormat();
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
        eventTableModel.setTableFormat(tableFormat);
        //tableFormat.configureTable(table);
    }
    
    /**
     * Gets the currently selected object, or null if there is currently no
     * selection.
     *
     * <p>Unlike most methods in the ListTable, this method is safe to be called
     * from threads that are not the event dispatch thread. This uses a helper
     * class that executes on the event dispatch thread to perform the lookup if
     * the method is called by an otherwise unsafe thread.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=12">Bug 12</a>
     *
     * @throws RuntimeException if the current thread is interrupted while 
     * it is waiting for the event dispatch thread to do the lookup
     */
    public Object getSelected() {
        return new SelectionGetter().getSelection();
    }
    /**
     * The selection getter is a helper class that can fetch the selection on
     * the event dispatch thread.
     */
    class SelectionGetter implements Runnable {
        /** the selected object */
        Object selected = null;
        /**
         * Fetch the currently selected object in a thread safe way.
         *
         * @throws RuntimeException if the current thread is interrupted while 
         * it is waiting for the event dispatch thread to do the lookup
         */
        public Object getSelection() {
            if(SwingUtilities.isEventDispatchThread()) {
                run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(this);
                } catch(InterruptedException e) {
                    throw new RuntimeException("Unexpected interruption fetching selection", e);
                } catch(java.lang.reflect.InvocationTargetException e) {
                    throw new RuntimeException("Unexpected exception fetching selection", e);
                }
            }
            return selected;
        }
        /**
         * Lookup the currently selected object on the current thread.
         */
        public void run() {
            if(selectionList.size() == 0) selected = null;
            else selected = selectionList.get(0);
        }
    }
    
    /**
     * For implementing the MouseListener interface. When the cell is double
     * clicked, update the listeners.
     */
    public void mouseClicked(MouseEvent mouseEvent) {
        if(mouseEvent.getSource() != table) return;

        // get the object which was clicked on
        Object clicked = null;
        source.getReadWriteLock().readLock().lock();
        try {
            int row = table.rowAtPoint(new Point(mouseEvent.getX(), mouseEvent.getY()));
            int col = table.columnAtPoint(new Point(mouseEvent.getX(), mouseEvent.getY()));
            clicked = source.get(row);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }

        // notify listeners on a double click
        if(mouseEvent.getClickCount() == 2) {
            selectionNotifier.notifyDoubleClicked(clicked);
        }
    }
    public void mouseEntered(MouseEvent mouseEvent) {}
    public void mouseExited(MouseEvent mouseEvent) {}
    public void mousePressed(MouseEvent mouseEvent) {}
    public void mouseReleased(MouseEvent mouseEvent) {}

    /**
     * Registers the specified SelectionListener to receive updates
     * when the selection changes.
     *
     * <p>This will tell the specified SelectionListener about the current
     * status of the table.
     */
    public void addSelectionListener(SelectionListener selectionListener) {
        selectionNotifier.addSelectionListener(selectionListener);
    }
    /**
     * Desregisters the specified SelectionListener from receiving
     * updates when the selection changes.
     */
    public void removeSelectionListener(SelectionListener selectionListener) {
        selectionNotifier.removeSelectionListener(selectionListener);
    }

    /**
     * Gets the minimum number of changes that will be combined into one uniform
     * change and cause selection and scrolling to be lost.
     */
    public int getRepaintAllThreshhold() {
        return eventTableModel.getRepaintAllThreshhold();
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
     * @see <a href="https://glazedlists.dev.java.net/tutorial/part8/index.html#rowheight">Glazed
     * Lists Tutorial Part 8 - Performance Tuning</a>
     */
    public void setRepaintAllThreshhold(int repaintAllThreshhold) {
        eventTableModel.setRepaintAllThreshhold(repaintAllThreshhold);
    }
}
