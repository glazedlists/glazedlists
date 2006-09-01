/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the core Glazed Lists packages
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.adt.Barcode;
import ca.odell.glazedlists.impl.adt.BarcodeIterator;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;

/**
 * An {@link EventList} that shows a subset of the elements of a source
 * {@link EventList}. This subset is composed of all elements of the source
 * {@link EventList} that match the filter.
 *
 * <p>The filter can be static or dynamic. Changing the behaviour of the filter
 * will change which elements of the source list are included.
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
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>0 to 26 bytes per element</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=1">1</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=2">2</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=7">7</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=46">46</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=187">187</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=254">254</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=312">312</a>
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author James Lemieux
 */
public final class FilterList<E> extends TransformedList<E,E> {

    /** the flag list contains Barcode.BLACK for items that match the current filter and Barcode.WHITE for others */
    private Barcode flagList = new Barcode();

    /** the matcher determines whether elements get filtered in or out */
    private Matcher<? super E> currentMatcher = Matchers.trueMatcher();

    /** the editor changes the matcher and fires events */
    private MatcherEditor<? super E> currentEditor = null;

    /** listener handles changes to the matcher */
    private final MatcherEditor.Listener listener = new PrivateMatcherEditorListener();

    /**
     * Creates a {@link FilterList} that includes a subset of the specified
     * source {@link EventList}.
     */
    public FilterList(EventList<E> source) {
        super(source);

        // build a list of what is filtered and what's not
        flagList.addBlack(0, source.size());

        // listen for changes to the source list
        source.addListEventListener(this);
    }

    /**
     * Convenience constructor for creating a {@link FilterList} and setting its
     * {@link Matcher}.
     */
    public FilterList(EventList<E> source, Matcher<? super E> matcher) {
        this(source);

        // if no matcher was given, we have no further initialization work
        if (matcher == null) return;

        currentMatcher = matcher;
        changed();
    }

    /**
     * Convenience constructor for creating a {@link FilterList} and setting its
     * {@link MatcherEditor}.
     */
    public FilterList(EventList<E> source, MatcherEditor<? super E> matcherEditor) {
        this(source);

        // if no matcherEditor was given, we have no further initialization work
        if (matcherEditor == null) return;

        currentEditor = matcherEditor;
        currentEditor.addMatcherEditorListener(listener);
        currentMatcher = currentEditor.getMatcher();
        changed();
    }

    /**
     * Set the {@link Matcher} which specifies which elements shall be filtered.
     *
     * <p>This will remove the current {@link Matcher} or {@link MatcherEditor}
     * and refilter the entire list.
     */
    public void setMatcher(Matcher<? super E> matcher) {
        // cancel the previous editor
        if(currentEditor != null) {
            currentEditor.removeMatcherEditorListener(listener);
            currentEditor = null;
        }

        if (matcher != null)
            changeMatcherWithLocks(currentEditor, matcher, MatcherEditor.Event.CHANGED);
        else
            changeMatcherWithLocks(currentEditor, null, MatcherEditor.Event.MATCH_ALL);
    }

    /**
     * Set the {@link MatcherEditor} which provides a dynamic {@link Matcher}
     * to determine which elements shall be filtered.
     *
     * <p>This will remove the current {@link Matcher} or {@link MatcherEditor}
     * and refilter the entire list.
     */
    public void setMatcherEditor(MatcherEditor<? super E> editor) {
        // cancel the previous editor
        if (currentEditor != null)
            currentEditor.removeMatcherEditorListener(listener);

        // use the new editor
        currentEditor = editor;

        if (currentEditor != null) {
            currentEditor.addMatcherEditorListener(listener);
            changeMatcherWithLocks(currentEditor, currentEditor.getMatcher(), MatcherEditor.Event.CHANGED);
        } else {
            changeMatcherWithLocks(currentEditor, null, MatcherEditor.Event.MATCH_ALL);
        }
    }

    /** @inheritDoc */
    public void dispose() {
        super.dispose();

        // stop listening to the MatcherEditor if one exists
        if (currentEditor != null) {
            currentEditor.removeMatcherEditorListener(listener);
            currentEditor = null;
        }

        currentMatcher = null;
    }

    /** {@inheritDoc} */
    public final void listChanged(ListEvent<E> listChanges) {
        // all of these changes to this list happen "atomically"
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
                    boolean include = currentMatcher.matches(source.get(sourceIndex));

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
                    boolean include = currentMatcher.matches(source.get(sourceIndex));

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
     * This method acquires the write lock for the FilterList and then selects
     * an appropriate delegate method to perform the correct work for each of
     * the possible <code>changeType</code>s.
     */
    private void changeMatcherWithLocks(MatcherEditor<? super E> matcherEditor, Matcher<? super E> matcher, int changeType) {
        getReadWriteLock().writeLock().lock();
        try {
            changeMatcher(matcherEditor, matcher, changeType);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * This method selects an appropriate delegate method to perform the
     * correct work for each of the possible <code>changeType</code>s. This
     * method does <strong>NOT</strong> acquire any locks and is thus used
     * during initialization of FilterList.
     */
    private void changeMatcher(MatcherEditor<? super E> matcherEditor, Matcher<? super E> matcher, int changeType) {
        // ensure the MatcherEvent is from OUR MatcherEditor
        if (currentEditor != matcherEditor) throw new IllegalStateException();

        switch (changeType) {
            case MatcherEditor.Event.CONSTRAINED: currentMatcher = matcher; this.constrained(); break;
            case MatcherEditor.Event.RELAXED: currentMatcher = matcher; this.relaxed(); break;
            case MatcherEditor.Event.CHANGED: currentMatcher = matcher; this.changed(); break;
            case MatcherEditor.Event.MATCH_ALL: currentMatcher = Matchers.trueMatcher(); this.matchAll(); break;
            case MatcherEditor.Event.MATCH_NONE: currentMatcher = Matchers.falseMatcher(); this.matchNone(); break;
        }
    }

    /**
     * Handles a constraining of the filter to a degree that guarantees no
     * values can be matched. That is, the filter list will act as a total
     * filter and not match any of the elements of the wrapped source list.
     */
    private void matchNone() {
        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // filter out all remaining items in this list
        if(size() > 0) updates.addDelete(0, size() - 1);

        // reset the flaglist to all white (which matches nothing)
        flagList.clear();
        flagList.addWhite(0, source.size());

        // commit the changes and notify listeners
        updates.commitEvent();
    }

    /**
     * Handles a clearing of the filter. That is, the filter list will act as
     * a passthrough and not discriminate any of the elements of the wrapped
     * source list.
     */
    private void matchAll() {
        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // for all filtered items, add them.
        // this code exploits the fact that all flags before
        // the current index are all conceptually black. we don't change
        // the flag to black immediately as a performance optimization
        // because the current implementation of barcode is faster for
        // batch operations. The call to i.getIndex() is exploiting the
        // fact that i.getIndex() == i.blackIndex() when all flags before
        // are conceptually black. Otherwise we would need to change flags
        // to black as we go so that flag offsets are correct
        for(BarcodeIterator i = flagList.iterator(); i.hasNextWhite();) {
            i.nextWhite();
            updates.addInsert(i.getIndex());
        }
        flagList.clear();
        flagList.addBlack(0, source.size());

        // commit the changes and notify listeners
        updates.commitEvent();
    }

    /**
     * Handles a relaxing or widening of the filter. This may change the
     * contents of this {@link EventList} as filtered elements are unfiltered
     * due to the relaxation of the filter.
     */
    private void relaxed() {
        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // for all filtered items, see what the change is
        for(BarcodeIterator i = flagList.iterator(); i.hasNextWhite();) {
            i.nextWhite();
            if(currentMatcher.matches(source.get(i.getIndex()))) {
                updates.addInsert(i.setBlack());
            }
        }

        // commit the changes and notify listeners
        updates.commitEvent();
    }

    /**
     * Handles a constraining or narrowing of the filter. This may change the
     * contents of this {@link EventList} as elements are further filtered due
     * to the constraining of the filter.
     */
    private void constrained() {
        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // for all unfiltered items, see what the change is
        for(BarcodeIterator i = flagList.iterator(); i.hasNextBlack();) {
            i.nextBlack();
            if(!currentMatcher.matches(source.get(i.getIndex()))) {
                int blackIndex = i.getBlackIndex();
                i.setWhite();
                updates.addDelete(blackIndex);
            }
        }

        // commit the changes and notify listeners
        updates.commitEvent();
    }

    /**
     * Handles changes to the behavior of the filter. This may change the contents
     * of this {@link EventList} as elements are filtered and unfiltered.
     */
    private void changed() {
        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // for all source items, see what the change is
        for(BarcodeIterator i = flagList.iterator();i.hasNext();) {
            i.next();

            // determine if this value was already filtered out or not
            int filteredIndex = i.getBlackIndex();
            boolean wasIncluded = filteredIndex != -1;
            // whether we should add this item
            boolean include = currentMatcher.matches(source.get(i.getIndex()));

            // this element is being removed as a result of the change
            if(wasIncluded && !include) {
                i.setWhite();
                updates.addDelete(filteredIndex);

            // this element is being added as a result of the change
            } else if(!wasIncluded && include) {
                updates.addInsert(i.setBlack());
            }
        }

        // commit the changes and notify listeners
        updates.commitEvent();
    }

    /**
     * Listens to changes from the current {@link MatcherEditor} and handles them.
     */
    private class PrivateMatcherEditorListener implements MatcherEditor.Listener<E> {
        /**
         * This method changes the current Matcher controlling the filtering on
         * the FilterList. It does so in a thread-safe manner by acquiring the
         * write lock.
         *
         * @param matcherEvent a MatcherEvent describing the change in the
         *      Matcher produced by the MatcherEditor
         */
        public void changedMatcher(MatcherEditor.Event<E> matcherEvent) {
            final MatcherEditor<? super E> matcherEditor = matcherEvent.getMatcherEditor();
            final Matcher<? super E> matcher = matcherEvent.getMatcher();
            final int changeType = matcherEvent.getType();

            changeMatcherWithLocks(matcherEditor, matcher, changeType);
        }
    }

    /** {@inheritDoc} */
    public final int size() {
        return flagList.blackSize();
    }

    /** {@inheritDoc} */
    protected final int getSourceIndex(int mutationIndex) {
        return flagList.getIndex(mutationIndex, Barcode.BLACK);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }
}