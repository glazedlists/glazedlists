/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.test;

import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.jtable.*;
import com.odellengineeringltd.glazedlists.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A combo box for dynamically choosing the selection mode on
 * a ListTable.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SelectionModeComboBox implements ItemListener {
        
    /** the list table to change selection on */
    private ListTable listTable;
    
    /** the selection options */
    private String single = "Single";
    private String singleInterval = "Single Interval";
    private String multipleInterval = "Multiple Interval";
    private String multipleIntervalDefensive = "Multiple Interval Defensive";
    
    /** the combo box for choosing selection */
    private JComboBox select;
    
    /**
     * Creates a new SelectionModeComboBox that changes the selection
     * mode on the specified ListTable.
     */
    public SelectionModeComboBox(ListTable listTable) {
        this.listTable = listTable;

        // create and populate the combo box
        select = new JComboBox();
        select.addItem(single);
        select.addItem(singleInterval);
        select.addItem(multipleInterval);
        select.addItem(multipleIntervalDefensive);

        // prepare the combo box for the initial state
        itemStateChanged(null);
        
        // listen for changes to the combo box
        select.addItemListener(this);
    }
    
    /**
     * Fetches the combo box for adding to a panel.
     */
    public JComboBox getComboBox() {
        return select;
    }
    
    /**
     * When the combo box is changed, this changes the selection
     * mode on the ListTable.
     */
    public void itemStateChanged(java.awt.event.ItemEvent e) {
        if(select.getSelectedItem() == single) listTable.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        else if(select.getSelectedItem() == singleInterval) listTable.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        else if(select.getSelectedItem() == multipleInterval) listTable.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        else if(select.getSelectedItem() == multipleIntervalDefensive) listTable.getTable().getSelectionModel().setSelectionMode(com.odellengineeringltd.glazedlists.listselectionmodel.SelectionModelEventList.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE);
    }
}
