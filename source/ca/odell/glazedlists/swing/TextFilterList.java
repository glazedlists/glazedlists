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
import ca.odell.glazedlists.util.impl.*;
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
import java.util.Map;
import java.util.HashMap;


/**
 * An {@link EventList} that shows only elements that contain a filter text string.
 * The {@link TextFilterList} uses a {@link JTextField} to allow the user to edit
 * the filter text. As this filter text is edited, the contents of the
 * {@link TextFilterList} are changed to reflect the elements that match the text.
 *
 * <p>The {@link TextFilterList} either requires that a {@link TextFilterator} be
 * specified in its constructor, or that every object in the source list implements
 * the {@link TextFilterable} interface. These are used to specify the {@link String}s
 * to search for each element.
 *
 * <p>The {@link TextFilterList} initially refilters the list after each change in
 * the {@link JTextField}. If this live filtering does not have adequate performance,
 * it can be turned off. In this case, the list will refiltered by pressing
 * <tt>ENTER</tt> in the {@link JTextField} and on every action to the {@link ActionListener}.
 * This {@link ActionListener} will be returned from the method {@link #getFilterActionListener()}
 * and can be used to refilter in response to a button click.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 * 
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class TextFilterList extends AbstractFilterList {

    /** the filters list is currently just a list of Substrings to include */
    private String[] filters = new String[0];

    /** a map from each filter to a Strategy for locating that filter in arbitrary text */
    private Map filterToTextContainmentStrategyMap = new HashMap();

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
     * Creates a {@link TextFilterList} that filters the specified {@link EventList}
     * of elements that implement the {@link TextFilterable} interface.
     */
    public TextFilterList(EventList source) {
        this(source, (TextFilterator)null, new JTextField(""));
    }

    /**
     * Creates a {@link TextFilterList} that filters the specified {@link EventList}
     * of elements using the specified {@link TextFilterator} to get the
     * {@link String}s to search.
     */
    public TextFilterList(EventList source, TextFilterator filterator) {
        this(source, filterator, new JTextField(""));
    }
    
    /**
     * Creates a {@link TextFilterList} that filters the specified {@link EventList}
     * of elements using the JavaBeans property names specified to get the
     * {@link String}s to search.
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
     * Creates a {@link TextFilterList} that filters the specified {@link EventList}
     * of elements using the JavaBeans property names specified to get the
     * {@link String}s to search.
     *
     * <p>Note that the classes which will be obfuscated may not work with
     * reflection. In this case, implement a {@link TextFilterator} manually.
     *
     * @param propertyNames an array of property names in the JavaBeans format.
     *      For example, if your list contains Objects with the methods getFirstName(),
     *      setFirstName(String), getAge(), setAge(Integer), then this array should
     *      contain the two strings "firstName" and "age". This format is specified
     *      by the JavaBeans {@link java.beans.PropertyDescriptor}.
     * @param filterEdit a text field for typing in the filter text.
     */
    public TextFilterList(EventList source, String[] propertyNames, JTextField filterEdit) {
        this(source, new BeanTextFilterator(propertyNames), filterEdit);
    }
    
    /**
     * Creates a {@link TextFilterList} that filters the specified {@link EventList}
     * of elements using the specified {@link TextFilterator} to get the
     * {@link String}s to search.
     *
     * @param filterEdit a text field for typing in the filter text.
     */
    public TextFilterList(EventList source, TextFilterator filterator, JTextField filterEdit) {
        super(source);
        this.filterator = filterator;

        // listen to filter events
        setFilterEdit(filterEdit);
    }
    
    /**
     * Gets the {@link JTextField} used to edit the filter search {@link String}.
     */
    public JTextField getFilterEdit() {
        return filterEdit;
    }
    
    /**
     * Sets the {@link JTextField} used to edit the filter search {@link String}.
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
     * Directs this filter to respond to changes to the {@link JTextField} as they are
     * made. This uses a {@link DocumentListener} and every time the
     * {@link JTextField} is modified, the list is refiltered.
     *
     * <p>To avoid the processing overhead of filtering for each keystroke, use
     * a not-live filter edit and trigger the {@link ActionListener} using a
     * button or by pressing <tt>ENTER</tt> in the {@link JTextField}.
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
     * Gets an {@link ActionListener} that refilters the list when it is fired. This
     * listener can be used to filter when the user presses a button.
     */
    public ActionListener getFilterActionListener() {
        return filterActionListener;
    }
    
    /**
     * Implement the {@link DocumentListener} interface for text filter updates. When
     * The user edits the filter {@link JTextField}, this updates the filter to reflect
     * the current value of that {@link JTextField}.
     */
    private class FilterEditListener implements DocumentListener {
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
     * Implement the {@link ActionListener} interface for text filter updates. When
     * the user clicks a button (supplied by external code), this
     * {@link ActionListener} can be used to update the filter in response.
     */
    private class FilterActionListener implements ActionListener {
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
        filters = filterEdit.getText().split("[ \t]");

        // rebuild the filter -> TextSearchStrategy map
        this.filterToTextContainmentStrategyMap.clear();
        for (int i = 0; i < this.filters.length; i++) {
            final String filter = this.filters[i];
            final TextSearchStrategy strategy = this.selectTextSearchStrategy(filter);
            strategy.setSubtext(filter);
            this.filterToTextContainmentStrategyMap.put(filter, strategy);
        }
    }

    /**
     * This local factory method allows fine grained control over the choice of
     * text search strategies for a given <code>filter</code>. Subclasses are
     * welcome to override this method to return any custom TextSearchStrategy
     * implementations which may exploit valid assumptions about the text being
     * searched or the subtext being found.
     *
     * @param filter the filter for which to locate a TextSearchStrategy
     * @return a TextSearchStrategy capable of location the given
     *      <code>filter</code> within arbitrary text
     */
    protected TextSearchStrategy selectTextSearchStrategy(String filter) {
        // uncomment me to test the old text search algorithm
        return new OldCaseInsensitiveTextSearchStrategy();

        // if the filter is only 1 character, use the optimized SingleCharacter strategy
//        if (filter.length() == 1)
//            return new SingleCharacterCaseInsensitiveTextSearchStrategy();

        // default the using the Boyer-Moore algorithm
//        return new BoyerMooreCaseInsensitiveTextSearchStrategy();
    }

    /** {@inheritDoc} */
    public boolean filterMatches(Object element) {
        // populate the strings for this object
        filterStrings.clear();
        if(filterator == null) {
            TextFilterable item = (TextFilterable)element;
            item.getFilterStrings(filterStrings);
        } else {
            filterator.getFilterStrings(filterStrings, element);
        }

        TextSearchStrategy textSearchStrategy;
        Object filterString;

        // ensure each filter matches at least one field
        filters:
        for (int f = 0; f < filters.length; f++) {
            // get the text search strategy for the current filter
            textSearchStrategy = (TextSearchStrategy) this.filterToTextContainmentStrategyMap.get(filters[f]);

            // search through all fields for the current filter
            for (int c = 0; c < filterStrings.size(); c++) {
                filterString = filterStrings.get(c);
                if (filterString != null && textSearchStrategy.indexOf(filterString.toString()) != -1)
                    continue filters;
            }
            // no field matched this filter 
            return false;
        }
        // all filters have been matched
        return true;
    }
}
