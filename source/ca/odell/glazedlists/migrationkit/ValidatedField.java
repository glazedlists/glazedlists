/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.migrationkit;

// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import java.awt.GridBagLayout;
// for responding to user actions
import java.awt.event.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
// used to lookup valid values for this field
import java.util.SortedMap;
import java.util.TreeMap;
// for keeping a list of change listeners
import java.util.ArrayList;
// for drawing attention to invalid values
import java.awt.Color;

/**
 * A field that has a preset map of valid values, but that will also accept
 * a user-typed value.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ValidatedField extends CompletedField implements StringCompleter, DocumentListener {

    /** The map of valid values */
    private SortedMap validValues;
    private Object selectedValue;
    
    /** Colours used to indicate errors and success */
    private ComponentCaution componentCaution;
    
    /** Listens to when a value is selected */
    private ArrayList changeListeners = new ArrayList();

    /**
     * Create a new product editor/viewer.
     */
    public ValidatedField(String text, int columns) {
        super(text, columns);
        componentCaution = new ComponentCaution(this);
        validValues = new TreeMap();
        setStringCompleter(this);
        getDocument().addDocumentListener(this);
        setObject(text, null);
    }
    
    /**
     * Setting a validated field to edit mode prevents it from changing
     * the current object or completing fields.
     */
    public void setEditMode() {
        getDocument().removeDocumentListener(this);
        setStringCompleter(null);
    }
    
    
    /**
     * This class implements its own interface for finding completions. It looks
     * in the Map for a valid value and returns that if found.
     */
    public String getCompleted(String prefix) {
        SortedMap tailMap = validValues.tailMap(prefix);
        if(tailMap.isEmpty()) return prefix;
        String bestGuess = (String)tailMap.firstKey();
        if(bestGuess.indexOf(prefix) == 0) return bestGuess;
        return prefix;
    }
    
    /**
     * Gets the company data object from this form. This throws an exception
     * if no company has been selected, and logs a warning if the company has
     * not yet been seen.
     */
    public Object getObject() {
        return selectedValue;
    }
    /**
     * Sets the object and text of this field.
     *
     * @param text The text to display.
     * @param value The value that this text represents.
     */
    public void setObject(String text, Object value) {
        setText(text);
        selectedValue = value;
        objectChanged();
    }

    /**
     * Sets who listens to changes in the selected object. Change events generally
     * occur whenever the colour of the text field changes. That is, change events
     * occur when the selection becomes valid and when it becomes invalid.
     */
    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }
    /**
     * Whenever the selected object changes, this method is called to update
     * field colors and notify listeners.
     */
    private void objectChanged() {
        componentCaution.setCaution(selectedValue == null);
        for(int c = 0; c < changeListeners.size(); c++) {
            ChangeListener changeListener = (ChangeListener)changeListeners.get(c);
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }
    
    
    /**
     * Sets the field to use a new set of valid values. A map is used
     * in place of a sorted list as a convenience because this field will
     * commonly be used to select a named value from a set. The method
     * getObject() is the same as calling map.get(field.getText()).
     * If no objects back the set, use 'null' as the values. The set
     * must use Strings for keys.
     */
    public void setValidValues(SortedMap validValues) {
        this.validValues = validValues;
        changedUpdate(null);
    }

    
    /**
     * For implementing the document listener interface, this gives notification
     * that an attribute or set of attributes changed. 
     */
    public void changedUpdate(DocumentEvent e) {
        Object oldSelectedValue = selectedValue;
        selectedValue = validValues.get(getText());
        if(oldSelectedValue != selectedValue) {
            objectChanged();
        }
    }
    public void insertUpdate(DocumentEvent e) {
        // just get changed update to do the work
        changedUpdate(e);
    }
    public void removeUpdate(DocumentEvent e) { 
        // just get changed update to do the work
        changedUpdate(e);
    }
}
