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
// for enabling actions upon a row being selected
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.*;
    
/** 
 * Simple class remembers the currently selected object. This class
 * can enable and disable actions based on whether or not a record
 * is selected.
 */
public class BasicSelectionListener implements SelectionListener {

    /** actions to set enabled when a record is selected */
    private List listeningActions = new ArrayList();
    /** actions to activate when a record is double clicked  */
    private List targetActions = new ArrayList();
    
    /** the currently selected object */
    private Object selected = null;
    
    /** the event fired when a record is double-clicked */
    private ActionEvent actionEvent = null;
    
    /**
     * Creates a basic selection listener that listens to selection
     * events on the specified table.
     */
    public BasicSelectionListener(ListTable listTable) {
        actionEvent = new ActionEvent(listTable, 0, "double click");
        listTable.addSelectionListener(this);
    }
    
    /**
     * When an object is selected, listening actions are enabled.
     */
    public void setSelection(Object selected) {
        this.selected = selected;
        updateListeningActions(true);
    }
    /**
     * When an object is deselected, listening actions are disabled.
     */
    public void clearSelection() {
        selected = null;
        updateListeningActions(false);
    }
    /**
     * When an object is double-clicked, target actions are fired.
     */
    public void setDoubleClicked(Object doubleClicked) {
        fireTargetActions();
    }

    /**
     * Update all the listening actions when the selected record changes.
     */
    private void updateListeningActions(boolean enabled) {
        for(Iterator a = listeningActions.iterator(); a.hasNext(); ) {
            Action action = (Action)a.next();
            action.setEnabled(enabled);
        }
    }
    /**
     * Activate all target actions when a record is double clicked.
     */
    private void fireTargetActions() {
        for(Iterator a = targetActions.iterator(); a.hasNext(); ) {
            Action action = (Action)a.next();
            action.actionPerformed(actionEvent);
        }
    }
    
    /**
     * Sets the specified action to enabled when a record
     * is selected, and disabled when no record is selected.
     */
    public void addListeningAction(Action action) {
        // set this action to the current status
        if(selected == null) {
            action.setEnabled(false);
        } else {
            action.setEnabled(true);
        }
        // add this action to receive notification
        listeningActions.add(action);
    }
    
    /**
     * Sets the specified action to be fired whenever a record
     * is double-clicked.
     */
    public void addTargetAction(Action action) {
        targetActions.add(action);
    }
}
