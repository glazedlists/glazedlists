/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the core Glazed Lists packages
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.matchers.*;
// volatile implementation support
import ca.odell.glazedlists.impl.adt.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;

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
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N), filter changes O(N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>0 to 26 bytes per element</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class FilterList extends TransformedList {

    /** the flag list contains Barcode.BLACK for items that match the current filter and Barcode.WHITE for others */
    private Barcode flagList = new Barcode();

    /** the matcher determines whether elements get filtered in or out */
    private Matcher currentMatcher = Matchers.trueMatcher();

    /** the editor changes the matcher and fires events */
    private MatcherEditor currentEditor = null;

    /** listener handles changes to the matcher */
    protected MatcherEditor.Listener listener = new PrivateMatcherEditorListener();

    /**
     * Creates a {@link FilterList} that includes a subset of the specified
     * source {@link EventList}.
     */
    public FilterList(EventList source) {
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
    public FilterList(EventList source, Matcher matcher) {
        this(source);
        setMatcher(matcher);
    }
    /**
     * Convenience constructor for creating a {@link FilterList} and setting its
     * {@link MatcherEditor}.
     */
    public FilterList(EventList source, MatcherEditor matcherEditor) {
        this(source);
        setMatcherEditor(matcherEditor);
    }

    /**
     * Set the {@link Matcher} which specifies which elements shall be filtered.
     *
     * <p>This will remove the current {@link Matcher} or {@link MatcherEditor}
     * and refilter the entire list.
     */
    public void setMatcher(Matcher matcher) {
        // cancel the previous editor
        if(currentEditor != null) {
            currentEditor.removeMatcherEditorListener(listener);
            currentEditor = null;
        }

        // refilter
        listener.changedMatcher(new MatcherEditor.Event(this, MatcherEditor.Event.CHANGED, matcher));
    }

    /**
     * Set the {@link MatcherEditor} which provides a dynamic {@link Matcher}
     * to determine which elements shall be filtered.
     *
     * <p>This will remove the current {@link Matcher} or {@link MatcherEditor}
     * and refilter the entire list.
     */
    public void setMatcherEditor(MatcherEditor editor) {
        // cancel the previous editor
        if(currentEditor != null) {
            currentEditor.removeMatcherEditorListener(listener);
        }

        // use the new editor
        this.currentEditor = editor;
        if(currentEditor != null) {
            currentEditor.addMatcherEditorListener(listener);
            currentMatcher = currentEditor.getMatcher();
            listener.changedMatcher(new MatcherEditor.Event(currentEditor, MatcherEditor.Event.CHANGED, currentMatcher));
        } else {
            currentMatcher = Matchers.trueMatcher();
            listener.changedMatcher(new MatcherEditor.Event(this, MatcherEditor.Event.CHANGED, currentMatcher));
        }
    }

    /** {@inheritDoc} */
    public final void listChanged(ListEvent listChanges) {
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
     * Listens to changes from the current {@link MatcherEditor} and handles them.
     */
    private class PrivateMatcherEditorListener implements MatcherEditor.Listener {

        /**
         * This implementation of this method simply delegates the handling of
         * the given <code>matcherEvent</code> to one of the protected methods
         * defined by this class. This clearly separates the logic for each
         * type of Matcher change.
         *
         * @param matcherEvent a MatcherEvent describing the change in the
         *      Matcher produced by the MatcherEditor
         */
        public void changedMatcher(MatcherEditor.Event matcherEvent) {
            final MatcherEditor matcherEditor = matcherEvent.getMatcherEditor();
            final Matcher matcher = matcherEvent.getMatcher();

            switch (matcherEvent.getType()) {
                case MatcherEditor.Event.CONSTRAINED: this.constrained(matcherEditor, matcher); break;
                case MatcherEditor.Event.RELAXED: this.relaxed(matcherEditor, matcher); break;
                case MatcherEditor.Event.CHANGED: this.changed(matcherEditor, matcher); break;
                case MatcherEditor.Event.MATCH_ALL: this.matchAll(matcherEditor); break;
                case MatcherEditor.Event.MATCH_NONE: this.matchNone(matcherEditor); break;
            }
        }

        /**
         * Handles a constraining of the filter to a degree that guarantees no
         * values can be matched. That is, the filter list will act as a total
         * filter and not match any of the elements of the wrapped source list.
         */
        private void matchNone(MatcherEditor editor) {
            getReadWriteLock().writeLock().lock();
            try {
                // update my matchers
                if(currentEditor != editor) throw new IllegalStateException();
                currentMatcher = Matchers.falseMatcher();

                // all of these changes to this list happen "atomically"
                updates.beginEvent();

                // filter out all remaining items in this list
                updates.addDelete(0, size() - 1);

                // reset the flaglist to all white (which matches nothing)
                flagList.clear();
                flagList.addWhite(0, source.size());

                // commit the changes and notify listeners
                updates.commitEvent();
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
        }

        /**
         * Handles a clearing of the filter. That is, the filter list will act as
         * a passthrough and not discriminate any of the elements of the wrapped
         * source list.
         */
        private void matchAll(MatcherEditor editor) {
            getReadWriteLock().writeLock().lock();
            try {
                // update my matchers
                if(currentEditor != editor) throw new IllegalStateException();
                currentMatcher = Matchers.trueMatcher();

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
                for(BarcodeIterator i = flagList.iterator(); i.hasNextWhite(); ) {
                    i.nextWhite();
                    updates.addInsert(i.getIndex());
                }
                flagList.clear();
                flagList.addBlack(0, source.size());

                // commit the changes and notify listeners
                updates.commitEvent();
            } finally {
                getReadWriteLock().writeLock().unlock();
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
        private void relaxed(MatcherEditor editor, Matcher matcher) {
            getReadWriteLock().writeLock().lock();
            try {
                // update my matchers
                if(currentEditor != editor) throw new IllegalStateException();
                currentMatcher = matcher;

                // all of these changes to this list happen "atomically"
                updates.beginEvent();

                // for all filtered items, see what the change is
                for(BarcodeIterator i = flagList.iterator(); i.hasNextWhite(); ) {
                    i.nextWhite();
                    if(currentMatcher.matches(source.get(i.getIndex()))) {
                        updates.addInsert(i.setBlack());
                    }
                }

                // commit the changes and notify listeners
                updates.commitEvent();
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
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
        private void constrained(MatcherEditor editor, Matcher matcher) {
            getReadWriteLock().writeLock().lock();
            try {
                // update my matchers
                if(currentEditor != editor) throw new IllegalStateException();
                currentMatcher = matcher;

                // all of these changes to this list happen "atomically"
                updates.beginEvent();

                // for all unfiltered items, see what the change is
                for(BarcodeIterator i = flagList.iterator(); i.hasNextBlack(); ) {
                    i.nextBlack();
                    if(!currentMatcher.matches(source.get(i.getIndex()))) {
                        int blackIndex = i.getBlackIndex();
                        i.setWhite();
                        updates.addDelete(blackIndex);
                    }
                }

                // commit the changes and notify listeners
                updates.commitEvent();
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
        }

        /**
         * Handles changes to the behavior of the filter. This may change the contents
         * of this {@link EventList} as elements are filtered and unfiltered.
         *
         * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
         * thread ready but not thread safe. See {@link EventList} for an example
         * of thread safe code.
         */
        private void changed(MatcherEditor editor, Matcher matcher) {
            getReadWriteLock().writeLock().lock();
            try {
                // update my matchers
                if(currentEditor != editor) throw new IllegalStateException();
                currentMatcher = matcher;

                // all of these changes to this list happen "atomically"
                updates.beginEvent();

                // for all source items, see what the change is
                for(BarcodeIterator i = flagList.iterator();i.hasNext(); ) {
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
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
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