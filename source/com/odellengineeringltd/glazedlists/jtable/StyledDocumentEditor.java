/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jtable;

// for editing this component inside of a table
import javax.swing.table.TableCellEditor;
import javax.swing.event.CellEditorListener;
import java.util.EventObject;
import javax.swing.event.ChangeEvent;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JComponent;
// for keeping a list of cell editor listeners
import java.util.ArrayList;

/**
 * Renders a table cell in a pretty way.
 *
 * The editor is currently broken because there has been no work on deciding
 * which focus events should be accepted. When this is fixed, the Editor will
 * be marked as fixed and it will be given a public constructor.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class StyledDocumentEditor extends StyledDocumentRenderer implements TableCellEditor {

    /** listeners paying attention to whether or not editing is taking place */
    private ArrayList cellEditorListeners;
    
    /** the current editing context */
    private JTable table;
    private Object value;
    private boolean isSelected;
    private int row;
    private int column;

    /**
     * Creates a message editor.
     */
    private StyledDocumentEditor(boolean controlHeight) {
        super(controlHeight);
        cellEditorListeners = new ArrayList();
        table = null;
        value = null;
        isSelected = false;
        row = -1;
        column = -1;
    }
    
    /**
     * Gets this Component for editing in a table. This sets the rendered width,
     * then calls editObject, and finally sets the table row height.
     */
    public final Component getTableCellEditorComponent(JTable table, Object value, 
        boolean isSelected, int row, int column) {
        // save the editing context
        this.table = table;
        this.value = value;
        this.row = row;
        this.isSelected = isSelected;
        this.row = row;
        this.column = column;
        // match the document width to the column width
        prepareRendered(table, value, isSelected, true, row, column);
        // append the actual contents of this cell
        editObject(table, value, isSelected, row, column, true);
        // match the row height to the document height
        getRendered(table, value, isSelected, true, row, column);
        // give back the rendered textpane
        return rendered;
    }
    
    /**
     * Implementing classes fill this method with a series of append() calls
     * to get the message editor in the textpane.
     */
    public abstract void editObject(JTable table, Object value, boolean isSelected,
        int row, int column, boolean edit);

    /**
     * To write an editable document, do edit with the edit parameter
     * as false.
     */
    public final void writeObject(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
        editObject(table, value, isSelected, row, column, false);
    }

    
    /**
     * Gets the Object after all changes from components have been
     * applied. The implementor should call getters and setters of the
     * object original with the values of the widgets used to perform
     * editing.
     */
    public abstract Object getEditedValue(Object original);
    /**
     * Returns the value edited in the editor.
     */
    public Object getCellEditorValue() {
        return getEditedValue(value);
    }


    /**
     * Asks the editor if it can start editing using anEvent.
     */
    public boolean isCellEditable(EventObject anEvent) { 
        return true;
    }
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    /**
     * Adds a listener to the list that's notified when the editor stops, 
     * or cancels editing.
     */
    public void addCellEditorListener(CellEditorListener cellEditorListener) {
        cellEditorListeners.add(cellEditorListener);
    }
    public void removeCellEditorListener(CellEditorListener cellEditorListener) {
        cellEditorListeners.remove(cellEditorListener);
    }
    /**
     * Tells the editor to stop editing and accept any partially edited value
     * as the value of the editor.
     */
    public boolean stopCellEditing() { 
        for(int c = 0; c < cellEditorListeners.size(); c++) {
            CellEditorListener cellEditorListener = (CellEditorListener)cellEditorListeners.get(c);
            cellEditorListener.editingStopped(new ChangeEvent(this));
        }
        return false;
    } 
    public void cancelCellEditing() { 
        for(int c = 0; c < cellEditorListeners.size(); c++) {
            CellEditorListener cellEditorListener = (CellEditorListener)cellEditorListeners.get(c);
            cellEditorListener.editingCanceled(new ChangeEvent(this));
        }
    }
}
