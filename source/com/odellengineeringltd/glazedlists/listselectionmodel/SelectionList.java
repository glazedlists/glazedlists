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
// for listening to list selection events
import javax.swing.*;
import javax.swing.event.*;

/**
 * A selection list is a list that holds only the selected items
 * in a JTable or a JList. As elements are selected or deselected,
 * the SelectionList changes.
 *
 * The selection list responds to changes from two places - an
 * EventList provides all the data and a ListSelectionModel provides
 * which elements in that data to view.
 *
 * Internally this maintains a flag list to keep track of which elements
 * are selected and not and where they are in the source list. It uses
 * a sparse list that corresponds directly to the source list. The sparse
 * list contains null for all unselected elements and Boolean.TRUE for
 * elements which are selected. This is used to map indexes between the
 * selection subset and the full source list.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SelectionList extends MutationList implements ListSelectionListener {
    
    /** the list selection model knows which cells are selected */
    private ListSelectionModel selectionModel;
    
    /** the flag list contains Boolean.TRUE for selected items and null for others */
    private SparseList flagList = new SparseList();
    
    /**
     * Creates a new Mutation list that uses the specified source list.
     */
    public SelectionList(EventList source, ListSelectionModel selectionModel) {
        super(source);
        this.selectionModel = selectionModel;
        
        prepareFlagList();
        
        source.addListChangeListener(new ListChangeListenerEventThreadProxy(this));
        selectionModel.addListSelectionListener(this);
    }
    
    /**
     * When the FlagList is prepared, it populates it with information from
     * the source list and the initial selection model.
     */
    private void prepareFlagList() {
        for(int i = 0; i < source.size(); i++) {
            if(selectionModel.isSelectedIndex(i)) flagList.add(Boolean.TRUE);
            else flagList.add(null);
        }
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * This gets the object with the specified index from the source list.
     *
     * Before every get, we need to validate the row because there may be an
     * update waiting in the event queue. For example, it is possible that
     * the selection has changed. Such a change may have been sent as notification,
     * but after this request in the event queue. In the case where a row is no longer
     * available, null is returned. The value returned is insignificant in this case
     * because the Event queue will very shortly be repainting (or removing) the row
     * anyway. This strategy is also used in ListTable where changes are frequently
     * queued to be processed.
     */
    public Object get(int index) {
        int sourceIndex = flagList.getIndex(index);

        // ensure that this value still exists before retrieval
        if(sourceIndex < source.size()) {
            return source.get(sourceIndex);
        } else {
            //new Exception("Returning null for removed selection " + row).printStackTrace();
            return null;
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * This is the number of elements currently selected.
     */
    public int size() {
        return flagList.getCompressedList().size();
    }

    /**
     * Notifies this SelectionList about changes to its underlying list store.
     *
     * This changes the flag list. Changes to the source list may cause simultaneous
     * changes to the corresponding selection list.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        synchronized(getRootList()) {
        
            // prepare a sequence of changes
            updates.beginAtomicChange();
            
            // for all changes simply update the flag list
            while(listChanges.next()) {
                int index = listChanges.getIndex();
                int changeType = listChanges.getType();
                
                // learn about what it was
                boolean previouslySelected = (flagList.size() > index) && (flagList.get(index) != null);
                int previousSelectionIndex = -1;
                if(previouslySelected) previousSelectionIndex = flagList.getCompressedIndex(index);
                
                // when an element is deleted, blow it away
                if(changeType == ListChangeBlock.DELETE) {
                    flagList.remove(index);
    
                    // fire a change to the selection list if a selected object is changed
                    if(previouslySelected) {
                        updates.appendChange(previousSelectionIndex, ListChangeBlock.DELETE);
                    }
                    
                // when an element is inserted, it is selected if its index was selected
                } else if(changeType == ListChangeBlock.INSERT) {
                    
                    // when selected, add the flag and fire a selection event
                    if(previouslySelected) {
                        flagList.add(index, Boolean.TRUE);
                        updates.appendChange(previousSelectionIndex, ListChangeBlock.INSERT);

                    // when not selected, just add the space
                    } else {
                        flagList.add(index, null);
                    }
                    
                // when an element is changed, assume selection stays the same
                } else if(changeType == ListChangeBlock.UPDATE) {
    
                    // fire a change to the selection list if a selected object is changed
                    if(previouslySelected) {
                        updates.appendChange(previousSelectionIndex, ListChangeBlock.UPDATE);
                    }
                }
            }
    
            // fire the changes
            updates.commitAtomicChange();
        }
    }
    
    /**
     * When the selection changes we need to perform two actions. First
     * we must update the flag list. Wherever the flag list changes we
     * must also fire an event.
     */
    public void valueChanged(ListSelectionEvent event) {
        synchronized(getRootList()) {
            
            // prepare a sequence of changes
            updates.beginAtomicChange();
            
            // for each element in the change range, update the selection
            for(int i = event.getFirstIndex(); i <= event.getLastIndex() && i < flagList.size(); i++) {
                // learn about what it was and what it will be
                boolean currentlySelected = selectionModel.isSelectedIndex(i);
                boolean previouslySelected = (flagList.get(i) != null);
                
                // if the value was selected before this event
                if(previouslySelected) {
                    // if its still selected, we're done
                    if(currentlySelected) continue;
                    
                    // it is no longer selected so remove it from the selection list
                    int previousSelectionIndex = flagList.getCompressedIndex(i);
                    flagList.set(i, null);
                    updates.appendChange(previousSelectionIndex, ListChangeBlock.DELETE);
                    
                // if the value was not selected before this event
                } else {
                    // if it's still not selected, we're done
                    if(!currentlySelected) continue;
                    
                    // it is newly selected so add it to the selection list
                    flagList.set(i, Boolean.TRUE);
                    int currentSelectionIndex = flagList.getCompressedIndex(i);
                    updates.appendChange(currentSelectionIndex, ListChangeBlock.INSERT);
                }
            }
            // fire the changes
            updates.commitAtomicChange();
        }
    }
}
