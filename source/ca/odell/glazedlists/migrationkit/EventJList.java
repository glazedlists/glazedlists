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
// for responding to user actions
import java.awt.event.*;
import java.awt.Point;
import javax.swing.event.*;
// this class uses tables for displaying message lists
import java.util.*;

/**
 * A JList that displays the contents of an event-driven list.
 *
 * <p>The EventJList class is <strong>not thread-safe</strong>. Unless otherwise
 * noted, all methods are only safe to be called from the event dispatch thread.
 * To do this programmatically, use {@link SwingUtilities.invokeAndWait()}.
 *
 * <p>I have implemented EventJList. The class shares the following with ListTable:
 * <li>SelectionListener interface
 * <li>SelectionList / Selection Model
 *
 * <p>This class never batches groups of changes like ListTable does. It also
 * does not use a Mutable change event. It may be necessary to create a mutable
 * ListDataEvent if change event creation proves to be a bottleneck.
 *
 * <p>This class still does not have any extra renderer support. For now if
 * styled rendering is necessary, the use of ListTable is a sufficient work
 * around.
 *
 * @deprecated This class will not be available in future releases of Glazed Lists.
 *      It exists to help users migrate between Glazed Lists < 0.8 and Glazed Lists >= 0.9.
 *      Users of EventJList should consider EventListModel which does not restrict
 *      them to a specific JList base class.
 * 
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=14">Bug 14</a>
 *
 * @see SwingUtilities#invokeAndWait(Runnable)
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventJList implements MouseListener {

    /** the delegate event list model */
    private EventListModel eventListModel;
    
    /** The Swing list */
    private JList jList;

    /** the list data */
    protected EventList source;

    /** selection managent is all by a SelectionModelEventList */
    private EventSelectionModel eventSelectionModel;
    private EventList selectionList;
    private ListSelectionModel listSelectionModel;
    private SelectionNotifier selectionNotifier;
    
    /**
     * Creates a new widget that renders the specified list.
     */
    public EventJList(EventList source) {
        this.source = source;

        // create the delegate model
        eventListModel = new EventListModel(source);
        
        // create the selection model
        eventSelectionModel = new EventSelectionModel(source);
        selectionList = eventSelectionModel.getEventList();
        listSelectionModel = eventSelectionModel.getListSelectionModel();
        selectionNotifier = new SelectionNotifier(selectionList);

        // construct widgets
        jList = new JList(eventListModel);
        jList.setSelectionModel(listSelectionModel);
        
        // prepare listeners
        jList.addMouseListener(this);
    }
    
    
    /**
     * Gets an event list that contains the current selection in
     * this list table. That list changes dynamically as elements
     * are selected and deselected from the list.
     *
     * Because the list is dynamic, users should be careful of changes
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
     * Gets just the raw JList for display.
     */
    public JList getJList() {
        return jList;
    }

    /**
     * Gets the currently selected object, or null if there is currently no
     * selection.
     *
     * <p>This method is thread unsafe and should be called from the Swing
     * Event dispatch thread.
     */
    public Object getSelected() {
        if(selectionList.size() == 0) return null;
        return selectionList.get(0);
    }
    
    /**
     * For implementing the MouseListener interface. When the cell is double
     * clicked, update the listeners.
     */
    public void mouseClicked(MouseEvent mouseEvent) {
        if(mouseEvent.getSource() != jList) return;
    
        source.getReadWriteLock().readLock().lock();
        try {
            // get the object which was clicked on
            int index = jList.locationToIndex(mouseEvent.getPoint());
            Object clicked = source.get(index);
    
            // notify listeners on a double click
            if(mouseEvent.getClickCount() == 2) {
                selectionNotifier.notifyDoubleClicked(clicked);
            }
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }
    public void mouseEntered(MouseEvent mouseEvent) { }
    public void mouseExited(MouseEvent mouseEvent) { }
    public void mousePressed(MouseEvent mouseEvent) { }
    public void mouseReleased(MouseEvent mouseEvent) { }

    /**
     * Registers the specified SelectionListener to receive updates
     * when the selection changes.
     *
     * This will tell the specified SelectionListener about the current
     * status of the EventJList.
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
}
