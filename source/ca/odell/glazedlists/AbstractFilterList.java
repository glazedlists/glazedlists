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
import ca.odell.glazedlists.util.impl.*;
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
public abstract class AbstractFilterList extends TransformedList implements ListEventListener {

    /** the flag list contains Boolean.TRUE for selected items and null or others */
    private SparseList flagList = new SparseList();

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

        // load the initial data
        getReadWriteLock().readLock().lock();
        try {
            // build a list of what is filtered and what's not
            prepareFlagList();
            // listen for changes to the source list
            source.addListEventListener(this);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }


    /**
     * Prepares the flagList by populating it with information from the source
     * {@link EventList}. Initially all elements are filtered out.
     */
    private void prepareFlagList() {
        for(int i = 0; i < source.size(); i++) {
            flagList.add(null);
        }
    }

    /** {@inheritDoc} */
    public final void listChanged(ListEvent listChanges) {
        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // handle reordering events
        if(listChanges.isReordering()) {
            int[] sourceReorderMap = listChanges.getReorderMap();
            int[] filterReorderMap = new int[flagList.getCompressedList().size()];

            // adjust the flaglist & construct a reorder map to propagate
            SparseList previousFlagList = flagList;
            flagList = new SparseList();
            for(int i = 0; i < sourceReorderMap.length; i++) {
                Object flag = previousFlagList.get(sourceReorderMap[i]);
                flagList.add(flag);
                if(flag != null) filterReorderMap[flagList.getCompressedIndex(i)] = previousFlagList.getCompressedIndex(sourceReorderMap[i]);
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
                    // test if this value was already not filtered out
                    boolean wasIncluded = flagList.get(sourceIndex) != null;

                    // if this value was not filtered out, it is now so add a change
                    if(wasIncluded) {
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.addDelete(filteredIndex);
                    }

                    // remove this entry from the flag list
                    flagList.remove(sourceIndex);

                // handle insert events
                } else if(changeType == ListEvent.INSERT) {

                    // whether we should add this item
                    boolean include = filterMatches(source.get(sourceIndex));

                    // if this value should be included, add a change and add the item
                    if(include) {
                        flagList.add(sourceIndex, Boolean.TRUE);
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.addInsert(filteredIndex);

                    // if this value should not be included, just add the item
                    } else {
                        flagList.add(sourceIndex, null);
                    }

                // handle update events
                } else if(changeType == ListEvent.UPDATE) {
                    // test if this value was already not filtered out
                    boolean wasIncluded = flagList.get(sourceIndex) != null;
                    // whether we should add this item
                    boolean include = filterMatches(source.get(sourceIndex));

                    // if this element is being removed as a result of the change
                    if(wasIncluded && !include) {
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        flagList.set(sourceIndex, null);
                        updates.addDelete(filteredIndex);

                    // if this element is being added as a result of the change
                    } else if(!wasIncluded && include) {
                        flagList.set(sourceIndex, Boolean.TRUE);
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.addInsert(filteredIndex);

                    // this element is still here
                    } else if(wasIncluded && include) {
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.addUpdate(filteredIndex);

                    }
                }
            }
        }

        // commit the changes and notify listeners
        updates.commitEvent();
    }

    /**
     * Handles changes to the behavior of the filter. This may change the contents
     * of this {@link EventList} as elements are filtered and unfiltered.
     */
    protected final void handleFilterChanged() {
        ((InternalReadWriteLock)getReadWriteLock()).internalLock().lock();
        try {
            // all of these changes to this list happen "atomically"
            updates.beginEvent();

            // for all source items, see what the change is
            for(int i = 0; i < source.size(); i++) {

                // test if this value was already not filtered out
                boolean wasIncluded = flagList.get(i) != null;
                // whether we should add this item
                boolean include = filterMatches(source.get(i));

                // if this element is being removed as a result of the change
                if(wasIncluded && !include) {
                    int filteredIndex = flagList.getCompressedIndex(i);
                    flagList.set(i, null);
                    updates.addDelete(filteredIndex);

                // if this element is being added as a result of the change
                } else if(!wasIncluded && include) {
                    flagList.set(i, Boolean.TRUE);
                    int filteredIndex = flagList.getCompressedIndex(i);
                    updates.addInsert(filteredIndex);
                }
            }

            // commit the changes and notify listeners
            updates.commitEvent();
        } finally {
            ((InternalReadWriteLock)getReadWriteLock()).internalLock().unlock();
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
    public final int size() {
        return flagList.getCompressedList().size();
    }

    /** {@inheritDoc} */
    protected final int getSourceIndex(int mutationIndex) {
        return flagList.getIndex(mutationIndex);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }
}
