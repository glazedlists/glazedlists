/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jtable;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// the Glazed Lists util package includes default comparators
import com.odellengineeringltd.glazedlists.util.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import javax.swing.table.TableColumnModel;
// for responding to user actions
import java.awt.event.*;
// this class uses tables for displaying contact lists
import java.awt.event.MouseAdapter;
// for keeping lists of comparators
import java.util.ArrayList;
// for implementing sorting by comparison
import java.util.Comparator;

/**
 * A table comparator selector allows the user to choose which
 * sorter will be used on an underlying EventList by clicking
 * on the table's header.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TableComparatorSelector extends MouseAdapter {
    
    /** the table being sorted */
    private ListTable listTable;
    private JTable table;
    
    /** keep track of the total number of clicks on each column */
    private int[] columnClicks;
    
    /** keep track of the comparators for each column */
    private ArrayList[] comparators;
    
    /** the names of the comparators in each column */
    private ArrayList[] comparatorNames;
    
    /** the sorted list to choose the comparators for */
    private SortedList sortedList;
    
    /**
     * Creates a new TableComparatorSelector that responds to clicks
     * on the specified table and uses them to sort the specified list.
     */
    public TableComparatorSelector(ListTable listTable, SortedList sortedList) {
        this.listTable = listTable;
        table = listTable.getTable();
        this.sortedList = sortedList;
        
        // set up the lists for comparators and counting clicks
        int columnCount = table.getColumnCount();
        columnClicks = new int[columnCount];
        comparators = new ArrayList[columnCount];
        comparatorNames = new ArrayList[columnCount];
        for(int i = 0; i < columnCount; i++) {
            columnClicks[i] = 0;
            comparators[i] = new ArrayList();
            comparatorNames[i] = new ArrayList();
        }
        
        // listen for events on the specified table
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().addMouseListener(this);
        
    }
    
    /**
     * Adds a new comparator for this table. The comparators for each
     * column can be cylced in sequence by repeatedly clicking the table's
     * header.
     */
    public void addComparator(int column, String name, Comparator comparator) {
        comparators[column].add(comparator);
        comparatorNames[column].add(name);
    }
    
    /**
     * When the mouse is clicked, this selects the next comparator in
     * sequence for the specified table. This will re-sort the table
     * by a new criterea.
     *
     * This code is based on the Java Tutorial's TableSorter
     * @see <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/table.html#sorting">The Java Tutorial</a>
     */
    public void mouseClicked(MouseEvent e) {
        TableColumnModel columnModel = table.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
        int column = table.convertColumnIndexToModel(viewColumn); 
        int clicks = e.getClickCount();
        if(clicks >= 1 && column != -1) {
            columnClicks[column] += clicks;
            int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK; 
            boolean ascending = (shiftPressed == 0);
            chooseComparator(column, ascending);
        }
    }
    
    /**
     * Sets the table to use the appropriate comparator for this column
     *
     * @todo the setHeaderValue method won't work permanently due to the fact
     *      that the TableFormat class overrides its value. Work out a way
     *      to set the value directly on the TableFormat?
     */
    public void chooseComparator(int column, boolean ascending) {
        Comparator selected;
        String sortedHeader = listTable.getTableFormat().getFieldName(column);
        // if there are comparators for this column, choose it!
        if(comparators[column].size() > 0) {
            int comparatorIndex = columnClicks[column] % comparators[column].size();
            sortedHeader = sortedHeader + " " + comparatorNames[column].get(comparatorIndex);
            selected = (Comparator)comparators[column].get(comparatorIndex);
        // otherwise choose the default comparator for this column
        } else {
            selected = new ComparableComparator();
        }
        // reverse the comparator for shift-clicks
        if(!ascending) selected = new ReverseComparator(selected);
        // now assign the comparator to the list
        sortedList.setComparator(selected);
        // and set the name of the table header to the current comparator
        for(int c = 0; c < table.getColumnCount(); c++) {
            if(c == column) {
                table.getColumnModel().getColumn(column).setHeaderValue(sortedHeader);
            } else {
                String header = listTable.getTableFormat().getFieldName(c);
                table.getColumnModel().getColumn(c).setHeaderValue(header);
            }
        }
        // force the table header to redraw itself
        table.getTableHeader().revalidate();
        table.getTableHeader().repaint();
    }
}
