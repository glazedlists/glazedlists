/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.impl.filter.*;
import ca.odell.glazedlists.util.concurrent.InternalReadWriteLock;
// for recycling filter strings
import java.util.*;

/**
 * An {@link EventList} that shows only elements that contain a filter text
 * string. The {@link DefaultTextFilterList} is not coupled with any UI
 * component to allow the user to edit the filter text. That job is left to
 * subclasses. This list is fully concrete, and may be used directly by
 * headless applications.
 *
 * <p>The {@link DefaultTextFilterList} requires that either a
 * {@link TextFilterator} be specified in its constructor, or that every object
 * in the source list implements the {@link TextFilterable} interface. These
 * are used to specify the {@link String}s to search for each element.
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
 * @author James Lemieux
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class DefaultTextFilterList extends TransformedList {
    
    /** the text matcher editor does all the real work */
    private TextMatcherEditor textMatcherEditor;
    
    /**
     * Creates a {@link DefaultTextFilterList} that filters the specified
     * {@link EventList} of elements, all of which implement the
     * {@link TextFilterable} interface.
     */
    public DefaultTextFilterList(EventList source) {
        this(source, null);
    }

    /**
     * Creates a {@link DefaultTextFilterList} that filters the specified
     * {@link EventList} of elements using the specified {@link TextFilterator}
     * to get the {@link String}s to search.
     *
     * @param source the {@link EventList} to wrap with text filtering
     * @param filterator the object that will extract filter Strings from each
     *      object in the <code>source</code>; <code>null</code> indicates the
     *      list elements implement {@link TextFilterable}
     */
    public DefaultTextFilterList(EventList source, TextFilterator filterator) {
        super(new FilterList(source));
        textMatcherEditor = new TextMatcherEditor(filterator);
        ((FilterList)this.source).setMatcherEditor(textMatcherEditor);

        // handle changes
        this.source.addListEventListener(this);
    }

    /**
     * Adjusts the filters of this {@link DefaultTextFilterList} and then
     * applies the new filters to this list. This method is thread safe. It
     * delegates to {@link #updateFilter(String[])} to perform the actual logic
     * of updating the filter value and applying the new filters to this list.
     * Subclasses should override that method to alter the filtering process,
     * but clients should always use this method when changing the filter.
     *
     * @param newFilter the {@link String}s representing all of the filter values
     */
    public final void setFilterText(final String[] newFilter) {
        textMatcherEditor.setFilterText(newFilter);
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