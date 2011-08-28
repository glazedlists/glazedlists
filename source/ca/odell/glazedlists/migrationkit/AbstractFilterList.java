/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.migrationkit;

// the core Glazed Lists packages
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * An {@link EventList} that shows a subset of the elements of a source
 * {@link EventList}. This subset is composed of all elements of the source
 * {@link EventList} that match the filter.
 *
 * <p>The filter can be static or dynamic. Changing the behaviour of the filter
 * will change which elements of the source list are included.
 *
 * <p>Extending classes define the filter by implementing the method
 * {@link #filterMatches(Object)}.
 *
 * <p>Extending classes must call {@link #handleFilterChanged()} when the filter
 * has changed in order to update the subset of included elements. This method
 * must also be called at the end of the extending class's constructor.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N), filter changes O(N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>0 to 26 bytes per element</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @deprecated This class uses inheritance when composition is preferrable. By replacing
 *      the overriding method {@link #filterMatches(Object)} with a {@link Matcher} or
 *      {@link MatcherEditor}, logic can be reused. That approach is far more flexible
 *      and powerful than the static filtering required by AbstractFilterList.
 *
 * @since 2004
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public abstract class AbstractFilterList extends TransformedList {

    /** implement Matcher's requirements in one quick inner class */
    private PrivateMatcherEditor editor = null;
    
    /**
     * Creates a {@link AbstractFilterList} that includes a subset of the specified
     * source {@link EventList}.
     *
     * <p>Extending classes must call handleFilterChanged().
     */
    protected AbstractFilterList(EventList source) {
        super(new FilterList(source));
        
        // listen for changes to the source list
        this.source.addListEventListener(this);
    }

    /**
     * Handles a clearing of the filter. That is, the filter list will act as
     * a passthrough and not discriminate any of the elements of the wrapped
     * source list.
     */
    protected void handleFilterCleared() {
        if(editor == null) {
            editor = new PrivateMatcherEditor();
            FilterList filterList = (FilterList)super.source;
            filterList.setMatcherEditor(editor);
        } else {
            editor.fireCleared();
        }
    }

    /**
     * Handles a relaxing or widening of the filter. This may change the
     * contents of this {@link EventList} as filtered elements are unfiltered
     * due to the relaxation of the filter.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    protected final void handleFilterRelaxed() {
        editor.fireRelaxed();
    }

    /**
     * Handles a constraining or narrowing of the filter. This may change the
     * contents of this {@link EventList} as elements are further filtered due
     * to the constraining of the filter.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    protected final void handleFilterConstrained() {
        editor.fireConstrained();
    }

    /**
     * Handles changes to the behavior of the filter. This may change the contents
     * of this {@link EventList} as elements are filtered and unfiltered.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    protected final void handleFilterChanged() {
        if(editor == null) {
            editor = new PrivateMatcherEditor();
            FilterList filterList = (FilterList)super.source;
            filterList.setMatcherEditor(editor);
        } else {
            editor.fireChanged();
        }
    }

    /**
     * Tests if the specified item from the source {@link EventList} is matched by
     * the current filter.
     *
     * @return <tt>true</tt> for elements that match the filter and shall be
     *      included in this {@link EventList} or <tt>false</tt> for elements that
     *      shall not be included in this {@link EventList}.
     */
    public abstract boolean filterMatches(Object element);

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent listChanges) {
        // just pass on the changes
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isWritable() {
        return true;
    }
    
    /**
     * The MatcherEditor within the {@link AbstractFilterList} is a simple way for
     * it to fit into the new {@link Matcher}s micro framework.
     */
    private class PrivateMatcherEditor extends AbstractMatcherEditor implements Matcher {
        /** 
         * This MatcherEditor's Matcher is itself.
         */
        public PrivateMatcherEditor() {
            fireChanged();
        }
    
        /** {@inheritDoc} */
        public boolean matches(Object item) {
            return filterMatches(item);
        }
        public void fireCleared() { fireMatchAll(); }
        public void fireRelaxed() { fireRelaxed(this); }
        public void fireConstrained() { fireConstrained(this); }
        public void fireChanged() { fireChanged(this); }
    }


    /** {@inheritDoc} */
    @Override
    public void dispose() {
        FilterList filteredSource = (FilterList)source;
        super.dispose();
        filteredSource.dispose();
    }
}
