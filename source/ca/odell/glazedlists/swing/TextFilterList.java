/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
// for automatically responding to changes in the filter field 
import javax.swing.text.Document;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
// regular expressions are used to match case insensitively
import java.util.regex.*;
// for recycling filter strings
import java.util.ArrayList;
import java.util.List;


/**
 * A filter list that shows only elements that contain the filter text. It also owns
 * a text box. When the text box contains tokens (Strings separated by spaces),
 * these are used as filters on the contents. The list elements that contain
 * all of the tokens are retained, while all others are (temporarily) removed
 * from the list. The list dynamically changes as its tokens are edited.
 *
 * <p>The filter list either requires that a <code>TextFilterator</code> be specified
 * in its constructor, or that every object in the source list implements
 * the <code>TextFilterable</code> interface. This can be compared to the sorted
 * collections (ie. TreeSet) and the Comparable/Comparator interfaces.
 *
 * <p>Refiltering the list can be triggered in two ways. They are when the user
 * explicitly refilters by triggering the refilterActionListener or when the
 * user implicitly refilters by editing the filter edit field. For a performance
 * boost, turn off "live" mode that filters automatically as the text field is
 * edited. To do this, call <code>setLive(false)</code>.
 *
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part2/index.html">Glazed
 * Lists Tutorial Part 2 - Text Filtering</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TextFilterList extends AbstractFilterList {

    /** the filters list is currently just a list of Substrings to include */
    private Matcher[] filters = new Matcher[0];
    private JTextField filterEdit = new JTextField("");
    
    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private TextFilterator filterator = null;
    
    /** the document listener responds to changes, it is null when we're not listening */
    private FilterEditListener filterEditListener = null;
    
    /** the action listener performs a refilter when fired */
    private FilterActionListener filterActionListener = null;
    
    /** a heavily recycled list of filter Strings, call clear() before use */
    private List filterStrings = new ArrayList();

    /**
     * Creates a new filter list that filters elements out of the
     * specified source list.
     */
    public TextFilterList(EventList source) {
        this(source, null);
    }

    /**
     * Creates a new filter list that uses a TextFilterator. A TextFilterator is something
     * that I made up. It is basically a class that knows how to take an arbitrary
     * object and get an array of strings for that object.
     */
    public TextFilterList(EventList source, TextFilterator filterator) {
        super(source);
        this.filterator = filterator;

        // listen to filter events
        filterActionListener = new FilterActionListener();
        filterEdit.addActionListener(filterActionListener);
        setLive(true);

        // set up the initial list
        reFilter();
    }

    /**
     * Gets the filter edit component for editing filters.
     */
    public JTextField getFilterEdit() {
        return filterEdit;
    }
    
    /**
     * Directs this filter to respond to changes to the FilterEdit as they are
     * made. This uses a DocumentListener and every time the FilterEdit is
     * modified, the list is refiltered.
     *
     * To avoid the processing overhead of filtering for each keystroke, use
     * a not-live filter edit and trigger the ActionListener using a Button
     * or by pressing <code>ENTER</code> in the filter edit field.
     */
    public void setLive(boolean live) {
        if(live) {
            if(filterEditListener == null) {
                filterEditListener = new FilterEditListener();
                filterEdit.getDocument().addDocumentListener(filterEditListener);
            }
        } else {
            if(filterEditListener != null) {
                filterEdit.getDocument().removeDocumentListener(filterEditListener);
                filterEditListener = null;
            }
        }
    }
    
    /**
     * Gets an ActionListener that refilters the list when it is fired. This
     * listener can be used to filter when the user presses a JButton.
     */
    public ActionListener getFilterActionListener() {
        return filterActionListener;
    }
    
    /**
     * Implement the DocumentListener interface for text filter updates. When
     * The user edits the filter text field, this updates the filter to reflect
     * the current value of that text field.
     */
    class FilterEditListener implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {
            reFilter();
        }
        public void insertUpdate(DocumentEvent e) {
            reFilter();
        }
        public void removeUpdate(DocumentEvent e) {
            reFilter();
        }
    }
    
    /**
     * Implement the ActionListener interface for text filter updates. When
     * the user clicks a button (supplied by external code), this
     * ActionListener can be used to update the filter in response.
     */
    class FilterActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            reFilter();
        }
    }
    
    /**
     * When the filter changes, first update the regex pattern used
     * to do filtering, then apply the filter on all elements.
     */
    private void reFilter() {
        ((InternalReadWriteLock)getReadWriteLock()).internalLock().lock();
        try {
            // build the regex pattern from the filter strings
            updateFilterPattern();
            // refilter the whole list
            handleFilterChanged();
        } finally {
            ((InternalReadWriteLock)getReadWriteLock()).internalLock().unlock();
        }
    }

    /**
     * Creates a matcher for the specified source. The matcher will match all
     * Strings containing the source and is case insensitive.
     */
    private Matcher getMatcher(String source) {
        // create a pattern for the source string
        StringBuffer pattern = new StringBuffer();
        for(int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);

            // if the current character is plain, append it
            if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                pattern.append(c);

            // if the current character is not plain, escape it first
            } else {
                pattern.append("\\");
                pattern.append(c);
            }
        }
        
        return Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE).matcher("");
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
                filters[i] = getMatcher(filterStrings[i]);
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
        filterStrings.clear();
        if(filterator == null) {
            TextFilterable item = (TextFilterable)element;
            item.getFilterStrings(filterStrings);
        } else {
            filterator.getFilterStrings(filterStrings, element);
        }
        // ensure each filter matches at least one field
        filters:
        for(int f = 0; f < filters.length; f++) {
            for(int c = 0; c < filterStrings.size(); c++) {
                if(filterStrings.get(c) != null) {
                    filters[f].reset((String)filterStrings.get(c));
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
