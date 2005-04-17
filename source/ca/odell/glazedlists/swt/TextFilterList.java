/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// access to the volatile implementation pacakge
import ca.odell.glazedlists.impl.filter.*;
// for working with SWT Text widgets
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

/**
 * An {@link EventList} that shows only elements that contain a filter text
 * string.  The {@link TextFilterList} uses a Text to allow the user to
 * edit the filter text. As this filter text is edited, the contents of the
 * {@link TextFilterList} are changed to reflect the elements that match
 * the text.
 *
 * <p>The {@link TextFilterList} either requires that a {@link TextFilterator}
 * be specified in its constructor, or that every object in the source
 * list implements the {@link TextFilterable} interface.  These are used to
 * specify the {@link String}s to search for each element.
 *
 * <p>The {@link TextFilterList} initially refilters the list after each
 * change made to the Text. If this live filtering does not have adequate
 * performance, it can be turned off. In this case, the list will be refiltered
 * by pressing <tt>ENTER</tt> in the Text and on every SelectionEvent
 * received by the SelectionListener.  This SelectionListener is available via
 * the method {@link #getFilterSelectionListener()} and can be used to refilter
 * in response to a Button click.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N), filter changes O(N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>O(N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TextFilterList extends TransformedList {

    /** the text matcher editor does all the real work */
    private TextMatcherEditor textMatcherEditor;
    
    /** the filter edit text field */
    private Text filterEdit;

    /** the document listener responds to changes, it is null when we're not listening */
    private FilterModifyListener filterModifyListener = null;

    ///** the selection listener performs a refilter when fired */
    private FilterSelectionListener filterSelectionListener = new FilterSelectionListener();

    /**
     * Creates a new filter list that filters elements out of the
     * specified source list.  Elements in the source list must implement
     * the TextFilterable interface.
     */
    public TextFilterList(EventList source) {
        this(source, (Text)null, (TextFilterator)null);
    }

    /**
     * Creates a new filter list that filters elements out of the
     * specified source list.  Elements in the source list must implement
     * the TextFilterable interface.
     */
    public TextFilterList(EventList source, Text filterEdit) {
        this(source, filterEdit, (TextFilterator)null);
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
        this(source, null, GlazedLists.textFilterator(propertyNames));
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
    public TextFilterList(EventList source, Text filterEdit, String[] propertyNames) {
        this(source, filterEdit, GlazedLists.textFilterator(propertyNames));
    }

    /**
     * Creates a new filter list that uses a TextFilterator. A TextFilterator is something
     * that I made up. It is basically a class that knows how to take an arbitrary
     * object and get an array of strings for that object.
     *
     * @param filterEdit a text field for typing in the filter text.
     */
    public TextFilterList(EventList source, Text filterEdit, TextFilterator filterator) {
        super(new FilterList(source));
        textMatcherEditor = new TextMatcherEditor(filterator);
        ((FilterList)this.source).setMatcherEditor(textMatcherEditor);

        // listen to filter events
        if(filterEdit != null) this.setFilterEdit(filterEdit);
        
        // handle changes
        this.source.addListEventListener(this);
    }

    /**
     * Gets the filter edit component for editing filters.
     */
    public Text getFilterEdit() {
        return filterEdit;
    }

    /**
     * Sets the Text used to edit the filter search {@link String}.
     *
     * <p><strong>Warning:</strong> It is an error to call this method
     * with a null value for filterEdit.
     */
    public void setFilterEdit(Text filterEdit) {
        boolean live = true;

        // stop listening on filter events from the old filter edit
        if(this.filterEdit != null) {
            this.filterEdit.removeSelectionListener(filterSelectionListener);
            live = (filterModifyListener != null);
            setLive(false);
        }

        // start listening for filter events from the new filter edit
        this.filterEdit = filterEdit;
        filterEdit.addSelectionListener(filterSelectionListener);
        setLive(live);

        // filter with the new filter edit
        reFilter();
    }

    /**
     * Directs this filter to respond to changes to the Text as they are
     * made. This uses a ModifyListener and every time the Text is
     * modified, the list is refiltered.
     *
     * <p>To avoid the processing overhead of filtering for each keystroke, use
     * a not-live filter edit and trigger the SelectionListener using a Button
     * or by pressing <code>ENTER</code> in the filter edit Text field.
     *
     * <p><strong>Warning:</strong> This method affects listeners on the Text
     * field that you have specified.  It is an error to call this method before
     * you set a valid Text field.
     */
    public void setLive(boolean live) {
        if(live) {
            if(filterModifyListener == null) {
                filterModifyListener = new FilterModifyListener();
                filterEdit.addModifyListener(filterModifyListener);
            }
        } else {
            if(filterModifyListener != null) {
                filterEdit.removeModifyListener(filterModifyListener);
                filterModifyListener = null;
            }
        }
    }

    /**
     * Gets a SelectionListener that refilters the list when it is fired. This
     * listener can be used to filter when the user presses a 'Search' button.
     */
    public SelectionListener getFilterSelectionListener() {
        return filterSelectionListener;
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
     * Implements the SelectionListener interface for text filter updates. When
     * the user clicks a button (supplied by external code), this
     * SelectionListener can be used to update the filter in response.
     */
    private class FilterSelectionListener implements SelectionListener {
        public void widgetSelected(SelectionEvent e) {
            reFilter();
        }
        public void widgetDefaultSelected(SelectionEvent e) {
            reFilter();
        }
    }

    /**
     * When the filter changes, first update the filter values used
     * to do filtering, then apply the filter on all list elements.
     */
    private void reFilter() {
        textMatcherEditor.setFilterText(filterEdit.getText().split("[ \t]"));
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // just pass on the changes
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void dispose() {
        FilterList filteredSource = (FilterList)source;
        super.dispose();
        filteredSource.dispose();
    }
}
