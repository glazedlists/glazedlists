/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists;

// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
// For calling methods on the event dispacher thread
import javax.swing.SwingUtilities;
// for automatically responding to changes in the filter field 
import javax.swing.text.Document;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
// regular expressions are used to match case insensitively
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * A filter list is a pseudo-list that owns another list. It also owns
 * a text box. When the text box contains tokens (Strings separated by spaces),
 * these are used as filters on the contents. The list elements that contain
 * all of the tokens are retained, while all others are (temporarily) removed
 * from the list. The list dynamically changes as its tokens are edited.
 *
 * The filter list either requires that a <code>Filterator</code> be specified
 * in its constructor, or that every object in the source list implements
 * the <code>filterable</code> interface. This can be compared to the sorted
 * collections (ie. TreeSet) and the Comparable/Comparator interfaces.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CaseInsensitiveFilterList extends AbstractFilterList implements DocumentListener {

    /** the filters list is currently just a list of Substrings to include */
    private Matcher[] filters = new Matcher[0];
    private JTextField filterEdit = new JTextField("");
    
    /** the filterator is used as an alternative to implementing the Filterable interface */
    private Filterator filterator = null;
    
    /**
     * Creates a new filter list that filters elements out of the
     * specified source list.
     */
    public CaseInsensitiveFilterList(EventList source) {
        super(source);
        // construct the filter editor
        filterEdit.getDocument().addDocumentListener(this);
        // set up the initial list
        handleFilterChanged();
    }
    /**
     * Creates a new filter list that uses a Filterator. A Filterator is something
     * that I made up. It is basically a class that knows how to take an arbitrary
     * object and get an array of strings for that object.
     */
    public CaseInsensitiveFilterList(EventList source, Filterator filterator) {
        super(source);
        this.filterator = filterator;
        // construct the filter editor
        filterEdit.getDocument().addDocumentListener(this);
        // set up the initial list
        handleFilterChanged();
    }

    /**
     * Gets the filter edit component for editing filters.
     */
    public JTextField getFilterEdit() {
        return filterEdit;
    }
    
    
    /**
     * Implement the document listener interface to create a text filter.
     */
    public void changedUpdate(DocumentEvent e) {
        // build the regex pattern from the filter strings
        updateFilterPattern();
        // refilter the whole list
        handleFilterChanged();
    }
    public void insertUpdate(DocumentEvent e) {
        // build the regex pattern from the filter strings
        updateFilterPattern();
        // refilter the whole list
        handleFilterChanged();
    }
    public void removeUpdate(DocumentEvent e) {
        // build the regex pattern from the filter strings
        updateFilterPattern();
        // refilter the whole list
        handleFilterChanged();
    }

    /**
     * Recompiles the filter regular expression patterns. When the user enters
     * a regular expression that is not recognized, the error is silently ignored
     * and no filters apply.
     */
    private void updateFilterPattern() {
        try {
            String[] filterStrings = filterEdit.getText().toLowerCase().split("[ \t]");
            filters = new Matcher[filterStrings.length];
            for(int i = 0; i < filterStrings.length; i++) {
                filters[i] = Pattern.compile(filterStrings[i], Pattern.CASE_INSENSITIVE).matcher("");
            }
        } catch(PatternSyntaxException e) {
            filters = new Matcher[0];
        }
    }

    /**
     * Tests if the specified item matches the current set of filters. This
     * class uses a user-editable text field of strings that must be in the
     * element to appear in the filtered list.
     */
    public boolean filterMatches(Object element) {
        // populate the strings for this object
        String values[] = null;
        if(filterator == null) {
            Filterable item = (Filterable)element;
            values = item.getFilterStrings();
        } else {
            values = filterator.getFilterStrings(element);
        }
        // ensure each filter matches at least one field
        filters:
        for(int f = 0; f < filters.length; f++) {
            for(int c = 0; c < values.length; c++) {
                if(values[c] != null) {
                    filters[f].reset(values[c]);
                    if(filters[f].find()) continue filters;
                }
            }
            // no field matched this filter 
            return false;
        }
        // all filters have been matched
        return true;
    }
}
