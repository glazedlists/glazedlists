/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jlist;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// for responding to selection the Glazed Lists way
import com.odellengineeringltd.glazedlists.listselectionmodel.*;
// for sharing selection listeners with jtable
import com.odellengineeringltd.glazedlists.jtable.*;
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
 * To do this programmatically, use <code>SwingUtilities.invokeAndWait()</code>.
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
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=14">Bug 14</a>
 *
 * @see SwingUtilities#invokeAndWait(Runnable)
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventJList extends AbstractListModel implements ListEventListener, MouseListener {

    /** The Swing list */
    private JList jList;

    /** the list data */
    protected EventList source;

    /** selection managent is all by a SelectionModelEventList */
    private SelectionModelEventList selectionModelEventList;
    private EventList selectionList;
    private ListSelectionModel listSelectionModel;
    private SelectionNotifier selectionNotifier;
    
    /** whenever a list change covers greater than this many rows, redraw the whole thing */
    public int changeSizeRepaintAllThreshhold = 25;
    
    /**
     * Creates a new widget that renders the specified list.
     */
    public EventJList(EventList source) {
        this.source = source;

        // create the selection model
        selectionModelEventList = new SelectionModelEventList(source);
        selectionList = selectionModelEventList.getEventList();
        listSelectionModel = selectionModelEventList.getListSelectionModel();
        selectionNotifier = new SelectionNotifier(selectionList);

        // construct widgets
        jList = new JList(this);
        jList.setSelectionModel(listSelectionModel);
        
        // prepare listeners
        source.addListEventListener(new EventThreadProxy(this));
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
        return selectionModelEventList.getEventList();
    }
    
    /**
     * Gets just the raw JList for display.
     */
    public JList getJList() {
        return jList;
    }
    
    /**
     * For implementing the ListEventListener interface. This sends changes
     * to the list which can repaint the table rows. Because this class uses
     * a EventThreadProxy, it is guaranteed that all natural
     * calls to this method use the Swing thread.
     */
    public void listChanged(ListEvent listChanges) {
        while(listChanges.nextBlock()) {
            // get the current change info
            int startIndex = listChanges.getBlockStartIndex();
            int endIndex = listChanges.getBlockEndIndex();
            int changeType = listChanges.getType();
            if(changeType == ListEvent.INSERT) {
                fireIntervalAdded(this, startIndex, endIndex);
            } else if(changeType == ListEvent.UPDATE) {
                fireContentsChanged(this, startIndex, endIndex);
            } else if(changeType == ListEvent.DELETE) {
                fireIntervalRemoved(this, startIndex, endIndex);
            }
        }
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
    
    /**
     * Retrieves the value at the specified index from the list.
     *
     * Before each get, we need to validate the index because there may be
     * an update waiting in the event queue.
     *
     * @see com.odellengineeringltd.glazedlists.jtable.ListTable#getValueAt(int,int) ListTable
     */
    public Object getElementAt(int index) {
        source.getReadWriteLock().readLock().lock();
        try {
            // ensure that this value still exists before retrieval
            if(index < source.size()) {
                return source.get(index);
            } else {
                return null;
            }
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }
    
    /**
     * Gets the number of objects to display in this list.
     */
    public int getSize() {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.size();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }
}
