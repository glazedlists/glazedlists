/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.MatcherSourceListener;
import ca.odell.glazedlists.util.concurrent.InternalReadWriteLock;
import ca.odell.glazedlists.util.concurrent.J2SE12ReadWriteLock;


/**
 * // TODO: update comments
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
public class FilterList extends AbstractFilterList implements MatcherSourceListener {
    private MatcherSource matcher_source;

	private volatile Matcher active_matcher = null;


    /**
     * Create a new list that filters a given source list using a given Matcher.
     *
     * @param source The source list that provides the raw data.
     */
    public FilterList(EventList source, MatcherSource matcher_source) {
        super(source);

		readWriteLock = new InternalReadWriteLock(source.getReadWriteLock(),
			new J2SE12ReadWriteLock());

        setMatcherSource(matcher_source);
    }


    /**
     * Update the {@link MatcherSource} used by the list.
     */
    public synchronized void setMatcherSource(MatcherSource matcher_source) {
        MatcherSource old_matcher_source = this.matcher_source;
        if (old_matcher_source != null) old_matcher_source.removeMatcherSourceListener(this);

        this.matcher_source = matcher_source;
        if (matcher_source != null) {
            matcher_source.addMatcherSourceListener(this);
			this.active_matcher = matcher_source.getCurrentMatcher();
            handleFilterChanged();
        } else {
            handleFilterCleared();
		}
    }

    /**
     * @see #setMatcherSource(MatcherSource)
     */
    public MatcherSource getMatcherSource() {
        return matcher_source;
    }


    /**
     * {@inheritDoc}
     */
    public final boolean filterMatches(Object element) {
        Matcher matcher = this.active_matcher;
        return matcher == null ? true : matcher.matches(element);
    }


    /**
     * {@inheritDoc}
     */
    public final void cleared(MatcherSource source) {
		readWriteLock.writeLock().lock();

		try {
			// TODO: remove debugging
			System.out.println("Cleared");
			this.active_matcher = null;
			handleFilterCleared();
		}
		finally {
			readWriteLock.writeLock().unlock();
		}
    }

    /**
     * {@inheritDoc}
     */
    public final void changed(Matcher matcher, MatcherSource source) {
		readWriteLock.writeLock().lock();

		try {
			System.out.println("Changed: " + matcher);
			this.active_matcher = matcher;
			handleFilterChanged();
		}
		finally {
			readWriteLock.writeLock().unlock();
		}
    }

    /**
     * {@inheritDoc}
     */
    public final void constrained(Matcher matcher, MatcherSource source) {
		readWriteLock.writeLock().lock();

		try {
			System.out.println("Constrained: " + matcher);
			this.active_matcher = matcher;
			handleFilterConstrained();
		}
		finally {
			readWriteLock.writeLock().unlock();
		}
    }

    /**
     * {@inheritDoc}
     */
    public final void relaxed(Matcher matcher, MatcherSource source) {
		readWriteLock.writeLock().lock();

		try {
			System.out.println("Relaxed: " + matcher);
			this.active_matcher = matcher;
			handleFilterRelaxed();
		}
		finally {
			readWriteLock.writeLock().unlock();
		}
    }
}
