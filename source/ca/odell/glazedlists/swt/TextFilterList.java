/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// regular expressions are used to match case insensitively
import java.util.regex.*;
// for recycling filter strings
import java.util.ArrayList;
import java.util.List;
// SWT toolkit stuff for displaying widgets
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;


/**
 * A filter list that shows only elements that contain the filter text.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * <p><strong>Warning:</strong> This class is a a developer preview and subject to
 * many bugs and API changes.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TextFilterList extends AbstractFilterList {

    /** the filters list is currently just a list of Substrings to include */
    private Matcher[] filters = new Matcher[0];
    
    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private TextFilterator filterator = null;
    
    /** the filter edit text field */
    private Text filterEdit;
    
    /** the document listener responds to changes, it is null when we're not listening */
    private FilterModifyListener filterModifyListener = null;
    
    ///** the action listener performs a refilter when fired */
    //private FilterActionListener filterActionListener = null;
    
    /** a heavily recycled list of filter Strings, call clear() before use */
    private List filterStrings = new ArrayList();

    /**
     * Creates a new filter list that filters elements out of the
     * specified source list.
     */
    public TextFilterList(EventList source, Text filterEdit) {
        this(source, filterEdit, null);
    }

    /**
     * Creates a new filter list that uses a TextFilterator. A TextFilterator is something
     * that I made up. It is basically a class that knows how to take an arbitrary
     * object and get an array of strings for that object.
     */
    public TextFilterList(EventList source, Text filterEdit, TextFilterator filterator) {
        super(source);
        this.filterEdit = filterEdit;
        this.filterator = filterator;

        // listen to filter events
        filterModifyListener = new FilterModifyListener();
        filterEdit.addModifyListener(filterModifyListener);
        //setLive(true);

        // set up the initial list
        reFilter();
    }

    /**
     * Gets the filter edit component for editing filters.
     */
    public Text getFilterEdit() {
        return filterEdit;
    }
    
    /**
     * Directs this filter to respond to changes to the FilterEdit as they are
     * made. This uses a DocumentListener and every time the FilterEdit is
     * modified, the list is refiltered.
     *
     * <p>To avoid the processing overhead of filtering for each keystroke, use
     * a not-live filter edit and trigger the ActionListener using a Button
     * or by pressing <code>ENTER</code> in the filter edit field.
     */
    public void setLive(boolean live) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Implement the ModifyListener interface for text filter updates. When the
     * user edits the filter text field, this updates the filter to reflect
     * the current value of that text field.
     */
    class FilterModifyListener implements ModifyListener { 
        public void modifyText(ModifyEvent e) {
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
