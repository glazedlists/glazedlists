/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.MatcherListener;


/**
 * An {@link EventList} that shows a subset of the elements of a source {@link EventList}.
 * This subset is composed of all elements of the source {@link EventList} that match the
 * filter see on a user-defined {@link ca.odell.glazedlists.Matcher}. <tt>Matcher<tt>s can
 * be static or dynamic. Changing the behavior of the <tt>Matcher</tt> will chnage which
 * elements of the source list are included.
 * <p/>
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class breaks the
 * contract required by {@link java.util.List}. See {@link EventList} for an example.
 * <p/>
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"> <tr
 * class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList
 * Overview</b></font></td></tr> <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not
 * thread safe</td></tr> <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads:
 * O(log N), writes O(log N), filter changes O(N)</td></tr> <tr><td
 * class="tablesubheadingcolor"><b>Memory:</b></td><td>O(N)</td></tr> <tr><td
 * class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr> <tr><td
 * class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr> </table>
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class FilterList extends AbstractFilterList implements MatcherListener {
    private volatile Matcher matcher;


    /**
     * Create a new list that filters a given source list using a given Matcher.
     *
     * @param source The source list that provides the raw data.
     */
    public FilterList(EventList source, Matcher matcher) {
        super(source);

        setMatcher(matcher);
    }


    /**
     * Update the matcher used by the list.
     */
    public synchronized void setMatcher(Matcher matcher) {
        Matcher old_matcher = this.matcher;
        if (old_matcher != null) old_matcher.removeMatcherListener(this);

        this.matcher = matcher;
        if (matcher != null) {
            matcher.addMatcherListener(this);
            handleFilterChanged();
        } else
            handleFilterCleared();
    }


    /**
     * {@inheritDoc}
     */
    public final boolean filterMatches(Object element) {
        Matcher matcher = this.matcher;
        return matcher == null ? true : matcher.matches(element);
    }


    /**
     * {@inheritDoc}
     */
    public final void cleared(Matcher source) {
        handleFilterCleared();
    }

    /**
     * {@inheritDoc}
     */
    public final void changed(Matcher source) {
        handleFilterChanged();
    }

    /**
     * {@inheritDoc}
     */
    public final void constrained(Matcher source) {
        handleFilterConstrained();
    }

    /**
     * {@inheritDoc}
     */
    public final void relaxed(Matcher source) {
        handleFilterRelaxed();
    }
}
