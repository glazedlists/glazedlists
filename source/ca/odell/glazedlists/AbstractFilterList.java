/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the core Glazed Lists packages
import ca.odell.glazedlists.event.*;
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
 * <p>Extending classes define the filter by implementing the method
 * {@link #filterMatches(Object)}.
 *
 * <p>Extending classes must call {@link #handleFilterChanged()} when the filter
 * has changed in order to update the subset of included elements. This method
 * must also be called at the end of the extending class's constructor.
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
public abstract class AbstractFilterList extends TransformedList {

    /** the flag list contains Barcode.BLACK for items that match the current filter and Barcode.WHITE for others */
    private Barcode flagList = new Barcode();

    /**
     * Creates a {@link AbstractFilterList} that includes a subset of the specified
     * source {@link EventList}.
     *
     * <p>Extending classes must call handleFilterChanged().
     */
    protected AbstractFilterList(EventList source) {
        super(source);

        // use an Internal Lock to avoid locking the source list during a sort
        readWriteLock = new InternalReadWriteLock(source.getReadWriteLock(), new J2SE12ReadWriteLock());

        // build a list of what is filtered and what's not
        flagList.addBlack(0, source.size());
        // listen for changes to the source list
        source.addListEventListener(this);
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
                    boolean include = filterMatches(source.get(sourceIndex));

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
                    boolean include = filterMatches(source.get(sourceIndex));

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
     * Handles a clearing of the filter. That is, the filter list will act as
     * a passthrough and not discriminate any of the elements of the wrapped
     * source list.
     */
    protected void handleFilterCleared() {
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
        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // for all filtered items, see what the change is
        for(int i = 0; i < flagList.whiteSize(); ) {
            int sourceIndex = flagList.getIndex(i, Barcode.WHITE);
            if(filterMatches(source.get(sourceIndex))) {
                flagList.setBlack(sourceIndex, 1);
                updates.addInsert(flagList.getBlackIndex(sourceIndex));
            } else {
                i++;
            }
        }

        // commit the changes and notify listeners
        updates.commitEvent();
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
        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // for all unfiltered items, see what the change is
        for(int i = 0; i < flagList.blackSize(); ) {
            int sourceIndex = flagList.getIndex(i, Barcode.BLACK);
            if(!filterMatches(source.get(sourceIndex))) {
                flagList.setWhite(sourceIndex, 1);
                updates.addDelete(i);
            } else {
                i++;
            }
        }

        // commit the changes and notify listeners
        updates.commitEvent();
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
        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // for all source items, see what the change is
        for(int i = 0; i < source.size(); i++) {

            // determine if this value was already filtered out or not
            int filteredIndex = flagList.getBlackIndex(i);
            boolean wasIncluded = filteredIndex != -1;
            // whether we should add this item
            boolean include = filterMatches(source.get(i));

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

    /*
     * Returns <tt>true</tt> if <code>filter1</code> is the same logical filter
     * as <code>filter2</code>, meaning <code>filter1</code> would discriminate
     * the source list elements precisely the same way as <code>filter2</code>.
     * The default implementation simply returns
     * <code>filter1.equals(filter2)</code> if filter1 is non-null, however,
     * subclasses may have further strategies for determining
     *
     * @param filter1 a filter value
     * @param filter2 another filter value
     * @return <tt>true</tt> if <code>filter1</code> is the same logical filter
     *      as <code>filter2</code>; <tt>false</tt> otherwise
     */
    /*public boolean isFilterEqual(Object filter1, Object filter2) {
        return filter1 == null ? filter2 == null : filter1.equals(filter2);
    }*/

    /*
     * Tests if the <code>newFilter</code> represents a relaxed version of the
     * <code>oldFilter</code>. If <code>newFilter</code> is a relaxed version
     * of <code>oldFilter</code> then it is guaranteed to accept the same
     * source list items as <code>oldFilter</code> and possibly more. By
     * default, this method returns <tt>false</tt>, but subclasses may override
     * it if filter relaxation can be detected.
     *
     * @param oldFilter the current value filtering the source list
     * @param newFilter the next value to filter the source list
     * @return <tt>true</tt> if <code>newFilter</code> is a relaxed version of
     *      <code>oldFilter</code>; <tt>false</tt> otherwise
     */
    /*public boolean isFilterRelaxed(Object oldFilter, Object newFilter) {
        return false;
    }*/

    /*
     * Tests if the <code>newFilter</code> represents a constrained version of
     * the <code>oldFilter</code>. If <code>newFilter</code> is a constrained
     * version of <code>oldFilter</code> then it is guaranteed to reject the
     * same source list items as <code>oldFilter</code> and possibly more. By
     * default, this method returns <tt>false</tt>, but subclasses may override
     * it if filter constrainment can be detected.
     *
     * @param oldFilter the current value filtering the source list
     * @param newFilter the next value to filter the source list
     * @return <tt>true</tt> if <code>newFilter</code> is a constrained version
     *      of <code>oldFilter</code>; <tt>false</tt> otherwise
     */
    /*public boolean isFilterConstrained(Object oldFilter, Object newFilter) {
        return false;
    }*/

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
