/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jcombobox;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import java.awt.GridBagLayout;
// for responding to user actions
import java.awt.event.*;
// for displaying lists in combo boxes
import javax.swing.ListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
// for keeping track of a set of listeners
import java.util.ArrayList;


/**
 * A combo box model for displaying Glazed Lists in a combo box.
 *
 * The implementation of setSelection and getSelection is not in any way tied
 * to the contents of the list.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListComboBoxModel implements ListChangeListener, ComboBoxModel {

    /** the complete list of messages before filters */
    protected EventList source;
        
    /** whom to notify of data changes */
    private ArrayList listeners = new ArrayList();
    
    /** the currently selected item, should belong to the source list */
    private Object selected;
    
    /** recycle the list data event to prevent unnecessary object creation */
    private MutableListDataEvent listDataEvent;
    
    /**
     * Creates a new combo box model that displays the specified source list
     * in the combo box.
     */
    public ListComboBoxModel(EventList source) {
        this.source = source;
        listDataEvent = new MutableListDataEvent(this);
        source.addListChangeListener(new ListChangeListenerEventThreadProxy(this));
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
     *
     * @todo implement redrawing the list instead of forwarding events where
     *      that would be more efficient.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        // when all events hae already been processed by clearing the event queue
        if(!listChanges.hasNext()) {
            return;

        // notify all changes simultaneously
        /*} else if(listChanges.getBlocksRemaining() >= changeSizeRepaintAllThreshhold) {
            listChanges.clearEventQueue();
            // first scroll to row zero
            //tableScrollPane.getViewport().setViewPosition(table.getCellRect(0, 0, true).getLocation());
            fireListDataEvent(listDataEvent);

            listDataEvent.setRange(startIndex, endIndex);
            if(changeType == ListChangeBlock.INSERT) listDataEvent.setType(ListDataEvent.INTERVAL_ADDED);
            else if(changeType == ListChangeBlock.DELETE) listDataEvent.setType(ListDataEvent.INTERVAL_REMOVED);
            else if(changeType == ListChangeBlock.UPDATE) listDataEvent.setType(ListDataEvent.CONTENTS_CHANGED);

            fireTableDataChanged();
            // clear the selection
            selectedIndex = -1;*/

        // for all changes, one block at a time
        } else {
            while(listChanges.nextBlock()) {
                // get the current change info
                int startIndex = listChanges.getBlockStartIndex();
                int endIndex = listChanges.getBlockEndIndex();
                int changeType = listChanges.getType();

                // create a table model event for this block
                listDataEvent.setRange(startIndex, endIndex);
                if(changeType == ListChangeBlock.INSERT) listDataEvent.setType(ListDataEvent.INTERVAL_ADDED);
                else if(changeType == ListChangeBlock.DELETE) listDataEvent.setType(ListDataEvent.INTERVAL_REMOVED);
                else if(changeType == ListChangeBlock.UPDATE) listDataEvent.setType(ListDataEvent.CONTENTS_CHANGED);

                // fire an event to notify all listeners
                fireListDataEvent(listDataEvent);

                // update the selection
                /*if(startIndex <= selectedIndex) {
                    if(changeType == ListChangeBlock.INSERT) {
                        selectedIndex += (endIndex - startIndex + 1);
                    } else if(changeType == ListChangeBlock.DELETE) {
                        if(endIndex >= selectedIndex) {
                            selectedIndex = -1;
                        } else {
                            selectedIndex -= (endIndex - startIndex + 1);
                        }
                    }
                }*/
            }
        }
    }


    /**
     * Gets the currently selected item.
     */
    public Object getSelectedItem() {
        return selected;
    }

    /**
     * Sets the currently selected item.
     *
     * The interface API says that this method should notify all listeners
     * that the selection has changed, however the ListDataListener provides
     * no selection notification methods.
     */
    public void setSelectedItem(Object selected) {
        this.selected = selected;
    }

    
    /**
     * Gets the size of the list.
     */
    public int getSize() {
        return source.size();
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
    public Object getElementAt(int index) {
        if(index < getSize()) {
            return source.get(index);
        } else {
            //new Exception("Returning null for removed row " + row).printStackTrace();
            return null;
        }
    }


    /**
     * Registers the specified ListDataListener to receive updates whenever
     * this list changes.
     *
     * The specified ListDataListener must <strong>not</strong> save a
     * reference to the ListDataEvent beyond the end of the notification
     * method. This is because the ListDataEvent is re-used to increase
     * the performance of this implementation.
     */
    public void addListDataListener(ListDataListener listDataListener) {
        listeners.add(listDataListener);
    }
    /**
     * Deregisters the specified ListDataListener from receiving updates
     * whenever this list changes.
     */
    public void removeListDataListener(ListDataListener listDataListener) {
        listeners.remove(listDataListener);
    }
    
    /**
     * Notifies all ListDataListeners about one block of changes in the list.
     */
    protected void fireListDataEvent(ListDataEvent listDataEvent) {
        // notify all listeners about the event
        for(int i = 0; i < listeners.size(); i++) {
            ListDataListener listDataListener = (ListDataListener)listeners.get(i);
            if(listDataEvent.getType() == ListDataEvent.CONTENTS_CHANGED) {
                listDataListener.contentsChanged(listDataEvent);
            } else if(listDataEvent.getType() == ListDataEvent.INTERVAL_ADDED) {
                listDataListener.intervalAdded(listDataEvent);
            } else if(listDataEvent.getType() == ListDataEvent.INTERVAL_REMOVED) {
                listDataListener.intervalRemoved(listDataEvent);
            }
        }
    }

}
