/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.MatcherSourceListener;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.util.concurrent.InternalReadWriteLock;
import ca.odell.glazedlists.util.concurrent.J2SE12ReadWriteLock;
import ca.odell.glazedlists.impl.adt.Barcode;
import ca.odell.glazedlists.matchers.TrueMatcher;


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
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class FilterList extends TransformedList implements MatcherSourceListener {

    /** the flag list contains Barcode.BLACK for items that match the current filter and Barcode.WHITE for others */
    private Barcode flagList = new Barcode();


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

        // build a list of what is filtered and what's not
        flagList.addBlack(0, source.size());
        // listen for changes to the source list
        source.addListEventListener(this);

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

			Matcher matcher = matcher_source.getCurrentMatcher();
            this.active_matcher = matcher;

            changed(matcher, matcher_source);
        } else {
            cleared(matcher_source);
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
    public final void cleared(MatcherSource matcher_source) {
        // TODO: remove debugging
//        System.out.println("Cleared");
		((InternalReadWriteLock)getReadWriteLock()).internalLock().lock();

        try {
            this.active_matcher = null;

            // all of these changes to this list happen "atomically"
            updates.beginEvent();

            // ensure all flags are set in the flagList indicating all
            // source list elements are matched with no filter
            for(int i = 0; i < flagList.whiteSize(); i++) {
                int sourceIndex = flagList.getIndex(i, Barcode.WHITE);
                updates.addInsert(sourceIndex);
            }
            flagList.clear();
            flagList.addBlack(0, source.size());

            // commit the changes and notify listeners
            updates.commitEvent();
        }
        finally {
		    ((InternalReadWriteLock)getReadWriteLock()).internalLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void changed(Matcher matcher, MatcherSource matcher_source) {
//        System.out.println("Changed: " + matcher);
		((InternalReadWriteLock)getReadWriteLock()).internalLock().lock();

		try {
			this.active_matcher = matcher;

            // all of these changes to this list happen "atomically"
            updates.beginEvent();

            // for all source items, see what the change is
            for(int i = 0; i < source.size(); i++) {

                // determine if this value was already filtered out or not
                int filteredIndex = flagList.getBlackIndex(i);
                boolean wasIncluded = filteredIndex != -1;
                // whether we should add this item
                boolean include = matcher.matches(source.get(i));

                // if this element is being removed as a result of the change
                if(wasIncluded && !include) {
                    flagList.setWhite(i, 1);
                    updates.addDelete(filteredIndex);

                // if this element is being added as a result of the change
                } else if(!wasIncluded && include) {
                    flagList.setBlack(i, 1);
                    updates.addInsert(flagList.getBlackIndex(i));
                }
            }

            // commit the changes and notify listeners
            updates.commitEvent();
		}
		finally {
			((InternalReadWriteLock)getReadWriteLock()).internalLock().unlock();
		}
    }

    /**
     * {@inheritDoc}
     */
    public final void constrained(Matcher matcher, MatcherSource matcher_source) {
//        System.out.println("Constrained: " + matcher);
		((InternalReadWriteLock)getReadWriteLock()).internalLock().lock();

		try {
			this.active_matcher = matcher;

            // all of these changes to this list happen "atomically"
            updates.beginEvent();

            // for all unfiltered items, see what the change is
            for(int i = 0; i < flagList.blackSize(); ) {
                int sourceIndex = flagList.getIndex(i, Barcode.BLACK);
                if(!matcher.matches(source.get(sourceIndex))) {
                    flagList.setWhite(sourceIndex, 1);
                    updates.addDelete(i);
                } else {
                    i++;
                }
            }

            // commit the changes and notify listeners
            updates.commitEvent();
		}
		finally {
			((InternalReadWriteLock)getReadWriteLock()).internalLock().unlock();
		}
    }

    /**
     * {@inheritDoc}
     */
    public final void relaxed(Matcher matcher, MatcherSource matcher_source) {
//        System.out.println("Relaxed: " + matcher);
		((InternalReadWriteLock)getReadWriteLock()).internalLock().lock();

		try {
			this.active_matcher = matcher;

            // all of these changes to this list happen "atomically"
            updates.beginEvent();

            // for all filtered items, see what the change is
            for(int i = 0; i < flagList.whiteSize(); ) {
                int sourceIndex = flagList.getIndex(i, Barcode.WHITE);
                if(matcher.matches(source.get(sourceIndex))) {
                    flagList.setBlack(sourceIndex, 1);
                    updates.addInsert(flagList.getBlackIndex(sourceIndex));
                } else {
                    i++;
                }
            }

            // commit the changes and notify listeners
            updates.commitEvent();
		}
		finally {
			((InternalReadWriteLock)getReadWriteLock()).internalLock().unlock();
		}
    }


    /**
     * {@inheritDoc}
     */
    public void listChanged(ListEvent listChanges) {        // all of these changes to this list happen "atomically"
        Matcher matcher = activeMatcher();

        updates.beginEvent();

        // handle reordering events
        if(listChanges.isReordering()) {
            int[] sourceReorderMap = listChanges.getReorderMap();
            int[] filterReorderMap = new int[flagList.blackSize()];

            // adjust the flaglist & construct a reorder map to propagate
            Barcode previousFlagList = flagList;
            flagList = new Barcode();
            for(int i = 0; i < sourceReorderMap.length; i++) {
                Object flag = previousFlagList.get(sourceReorderMap[i]);
                flagList.add(i, flag, 1);
                if(flag != Barcode.WHITE) filterReorderMap[flagList.getBlackIndex(i)] = previousFlagList.getBlackIndex(sourceReorderMap[i]);
            }

            // fire the reorder
            updates.reorder(filterReorderMap);

        // handle non-reordering events
        } else {

            // for all changes, one index at a time
            while(listChanges.next()) {

                // get the current change info
                int sourceIndex = listChanges.getIndex();
                int changeType = listChanges.getType();

                // handle delete events
                if(changeType == ListEvent.DELETE) {
                    // determine if this value was already filtered out or not
                    int filteredIndex = flagList.getBlackIndex(sourceIndex);

                    // if this value was not filtered out, it is now so add a change
                    if(filteredIndex != -1) {
                        updates.addDelete(filteredIndex);
                    }

                    // remove this entry from the flag list
                    flagList.remove(sourceIndex, 1);

                // handle insert events
                } else if(changeType == ListEvent.INSERT) {

                    // whether we should add this item
                    boolean include = matcher.matches(source.get(sourceIndex));

                    // if this value should be included, add a change and add the item
                    if(include) {
                        flagList.addBlack(sourceIndex, 1);
                        int filteredIndex = flagList.getBlackIndex(sourceIndex);
                        updates.addInsert(filteredIndex);

                    // if this value should not be included, just add the item
                    } else {
                        flagList.addWhite(sourceIndex, 1);
                    }

                // handle update events
                } else if(changeType == ListEvent.UPDATE) {



                    // determine if this value was already filtered out or not
                    int filteredIndex = flagList.getBlackIndex(sourceIndex);
                    boolean wasIncluded = filteredIndex != -1;
                    // whether we should add this item
                    boolean include = matcher.matches(source.get(sourceIndex));

                    // if this element is being removed as a result of the change
                    if(wasIncluded && !include) {
                        flagList.setWhite(sourceIndex, 1);
                        updates.addDelete(filteredIndex);

                    // if this element is being added as a result of the change
                    } else if(!wasIncluded && include) {
                        flagList.setBlack(sourceIndex, 1);
                        updates.addInsert(flagList.getBlackIndex(sourceIndex));

                    // this element is still here
                    } else if(wasIncluded && include) {
                        updates.addUpdate(filteredIndex);

                    }
                }
            }
        }

        // commit the changes and notify listeners
        updates.commitEvent();
    }


    /**
     * {@inheritDoc}
     */
    public final int size() {
        return flagList.blackSize();
    }

    /**
     * {@inheritDoc}
     */
    protected final int getSourceIndex(int mutationIndex) {
        return flagList.getIndex(mutationIndex, Barcode.BLACK);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isWritable() {
        return true;
    }


    /**
     * Get the active matcher ensuring that null is never returned.
     */
    private Matcher activeMatcher() {
        Matcher matcher = active_matcher;
        if (matcher == null) return TrueMatcher.getInstance();
        else return matcher;
    }
}
