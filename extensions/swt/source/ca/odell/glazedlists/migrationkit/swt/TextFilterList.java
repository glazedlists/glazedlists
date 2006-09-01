/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.migrationkit.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;

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
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N), filter changes O(N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>O(N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @deprecated This class uses inheritance when composition is preferrable. Instead
 *      of TextFilterList, use {@link FilterList} and {@link TextWidgetMatcherEditor}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TextFilterList extends TransformedList {

    /** the text matcher editor does all the real work */
    private TextWidgetMatcherEditor matcherEditor;
    
    /** the filter edit text field */
    private Text filterEdit;

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
        this.filterEdit = filterEdit;
        matcherEditor = new TextWidgetMatcherEditor(filterEdit, filterator, false);
        ((FilterList)this.source).setMatcherEditor(matcherEditor);

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
        if(this.filterEdit == filterEdit) return;

        // clean up the old matcher editor
        boolean live = matcherEditor.isLive();
        TextFilterator textFilterator = matcherEditor.getFilterator();
        matcherEditor.dispose();

        // prepare the new matcher editor
        this.matcherEditor = new TextWidgetMatcherEditor(filterEdit, textFilterator, live);
        ((FilterList)source).setMatcherEditor(matcherEditor);
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
        matcherEditor.setLive(live);
    }

    /**
     * Gets a SelectionListener that refilters the list when it is fired. This
     * listener can be used to filter when the user presses a 'Search' button.
     */
    public SelectionListener getFilterSelectionListener() {
        return new FilterSelectionListener();
    }

    /**
     * Implement the {@link SelectionListener} interface for text filter updates. When
     * the user clicks a button (supplied by external code), this
     * {@link SelectionListener} can be used to update the filter in response.
     */
    private class FilterSelectionListener implements SelectionListener {
        public void widgetSelected(SelectionEvent selectionEvent) {
            matcherEditor.setFilterText(filterEdit.getText().split("[ \t]"));
        }
        public void widgetDefaultSelected(SelectionEvent selectionEvent) {
            matcherEditor.setFilterText(filterEdit.getText().split("[ \t]"));
        }
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
        matcherEditor.dispose();
    }
}