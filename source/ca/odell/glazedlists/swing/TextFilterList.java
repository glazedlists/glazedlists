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
// for recycling filter strings
import java.util.ArrayList;
import java.util.List;


/**
 * A filter list that shows only elements that contain the filter text. It also owns
 * a {@link JTextField}. When the text field contains tokens (Strings separated by spaces),
 * these are used as filters on the contents. The list elements that contain
 * all of the tokens are retained, while all others are (temporarily) removed
 * from the list. The list dynamically changes as its tokens are edited.
 *
 * <p>The filter list either requires that a {@link TextFilterator} be specified
 * in its constructor, or that every object in the source list implements
 * the {@link TextFilterable} interface. This can be compared to the sorted
 * collections and the {@link Comparable}/{@link java.util.Comparator}
 * interfaces.
 *
 * <p>Refiltering the list can be triggered in two ways. They are when the user
 * explicitly refilters by triggering the refilterActionListener or when the
 * user implicitly refilters by editing the filter edit field. For a performance
 * boost, turn off "live" mode that filters automatically as the text field is
 * edited. To do this, call <code>setLive(false)</code>.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial-0.9.1/part3/index.html">Glazed
 * Lists Tutorial Part 3 - Text Filtering</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TextFilterList extends AbstractFilterList {

    /** the filters list is currently just a list of Substrings to include */
    private String[] filters = new String[0];
    
    /** the field where the filter strings are edited */
    private JTextField filterEdit = null;
    
    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private TextFilterator filterator = null;
    
    /** the document listener responds to changes, it is null when we're not listening */
    private FilterEditListener filterEditListener = null;
    
    /** the action listener performs a refilter when fired */
    private FilterActionListener filterActionListener = new FilterActionListener();
    
    /** a heavily recycled list of filter Strings, call clear() before use */
    private List filterStrings = new ArrayList();

    /**
     * Creates a new filter list that filters elements out of the
     * specified source list.
     */
    public TextFilterList(EventList source) {
        this(source, null, new JTextField(""));
    }

    /**
     * Creates a new filter list that filters elements out of the
     * specified source list.
     *
     * @param filterator a class that knows how to take a list element
     *      and get a filter strings for it. If this is null, the list elements
     *      must all implement {@link TextFilterable}.
     */
    public TextFilterList(EventList source, TextFilterator filterator) {
        this(source, filterator, new JTextField(""));
    }
    
    /**
     * Creates a new filter list that filters elements out of the
     * specified source list with an automatically generated {@link TextFilterator}.
     * It uses JavaBeans and reflection to create a {@link TextFilterator} as
     * specified.
     *
     * <p>Note that the classes which will be obfuscated may not work with
     * reflection. In this case, implement a {@link TextFilterator} manually.
     *
     * @param propertyNames an array of property names in the JavaBeans format.
     *      For example, if your list contains Objects with the methods getFirstName(),
     *      setFirstName(String), getAge(), setAge(Integer), then this array should
     *      contain the two strings "firstName" and "age". This format is specified
     *      by the JavaBeans {@link java.beans.PropertyDescriptor}.
     */
    public TextFilterList(EventList source, String[] propertyNames) {
        this(source, new BeanTextFilterator(propertyNames), new JTextField(""));
    }
    
    /**
     * Creates a new filter list that filters elements out of the
     * specified source list.
     *
     * @param filterator a class that knows how to take a list element
     *      and get a filter strings for it. If this is null, the list elements
     *      must all implement {@link TextFilterable}.
     * @param filterEdit a text field for typing in the filter text.
     */
    public TextFilterList(EventList source, TextFilterator filterator, JTextField filterEdit) {
        super(source);
        this.filterator = filterator;

        // listen to filter events
        setFilterEdit(filterEdit);
    }

    /**
     * Gets the filter edit component for editing filters.
     */
    public JTextField getFilterEdit() {
        return filterEdit;
    }
    
    /**
     * Sets the filter edit component for editing filters.
     */
    public void setFilterEdit(JTextField filterEdit) {
        boolean live = true;

        // stop listening on filter events from the old filter edit
        if(this.filterEdit != null) {
            this.filterEdit.removeActionListener(filterActionListener);
            live = (filterEditListener != null);
            setLive(false);
        }
        
        // start listening for filter events from the new filter edit
        this.filterEdit = filterEdit;
        filterEdit.addActionListener(filterActionListener);
        setLive(live);
        
        // filter with the new filter edit
        reFilter();
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
     * Recompiles the filter regular expression patterns. When the user enters
     * a regular expression that is not recognized, the error is silently ignored
     * and no filters apply.
     */
    private void updateFilterPattern() {
        filters = filterEdit.getText().toUpperCase().split("[ \t]");
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
                    if(caseInsensitiveIndexOf(filterStrings.get(c).toString(), filters[f]) != -1) continue filters;
                }
            }
            // no field matched this filter 
            return false;
        }
        // all filters have been matched
        return true;
    }

    /**
     * Tests if one String contains the other. Originally this task was performed
     * by Java's regular expressions library, but this is faster and less complex.
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=89">Bug 89</a>
     * @param host the string to search within
     * @param filter the string to search for, this must be upper case
     */
    public static int caseInsensitiveIndexOf(String host, String filter) {
        int lastFirst = host.length() - filter.length();
        sourceCharacter:
        for(int c = 0; c <= lastFirst; c++) {
            for(int f = 0; f < filter.length(); f++) {
                if(Character.toUpperCase(host.charAt(c+f)) != filter.charAt(f)) continue sourceCharacter;
            }
            return c;
        }
        return -1;
    }
}
