/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.listselectionmodel;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
import com.odellengineeringltd.glazedlists.util.*;
import com.odellengineeringltd.glazedlists.jtable.*;
// for listening to list selection events
import javax.swing.*;
import javax.swing.event.*;
// for keeping track of selection listeners
import java.util.ArrayList;

/**
 * A selection notifier listens to a SelectionList and notifies selection
 * listeners when there is a change. It is a utility class currently used
 * only by ListTable to provide notification of selection events.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SelectionNotifier implements ListChangeListener {
    
    /** whom to notify of selection changes */
    private ArrayList selectionListeners = new ArrayList();

    /** the list that knows what is selected */
    private EventList source;
    
    /**
     * Create a new SelectionNotifier that notifies listeners of changes to
     * the specified SelectionList.
     */
    public SelectionNotifier(EventList source) {
        this.source = source;
        source.addListChangeListener(this);
    }
    
    /**
     * When the SelectionList changes, send notification to selection listeners
     * of the new selection.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        if(!listChanges.hasNext()) return;
        
        // clearing the event queue marks this event as handled
        listChanges.clearEventQueue();

        // update the list listeners to display the new selection
        if(source.size() == 0) {
            for(int r = 0; r < selectionListeners.size(); r++) {
                ((SelectionListener)selectionListeners.get(r)).clearSelection();
            }
        } else {
            Object selected = source.get(0);
            for(int r = 0; r < selectionListeners.size(); r++) {
                ((SelectionListener)selectionListeners.get(r)).setSelection(selected);
            }
        }
    }
    
    /**
     * The SelectionNotifier is also responsible to propogate double-click
     * events to all selection listeners. Because the SelectionNotifier cannot
     * detect double-click events on its own, it is notified by whichever
     * widget owns this SelectionNotifier.
     */
    public void notifyDoubleClicked(Object doubleClicked) {
        for(int r = 0; r < selectionListeners.size(); r++) {
            ((SelectionListener)selectionListeners.get(r)).setDoubleClicked(doubleClicked);
        }
    }

    /**
     * Registers the specified SelectionListener to receive updates
     * when the selection changes.
     *
     * This will tell the specified SelectionListener about the current
     * status of the selection.
     */
    public void addSelectionListener(SelectionListener selectionListener) {
        selectionListeners.add(selectionListener);
        // notify the new listener of the current status 
        if(source.size() == 0) {
            selectionListener.clearSelection();
        } else {
            selectionListener.setSelection(source.get(0));
        }
    }
    
    /**
     * Deregisters the specified SelectionListener from receiving updates
     * when the selection changes.
     */
    public void removeSelectionListener(SelectionListener selectionListener) {
        selectionListeners.remove(selectionListener);
    }
}
