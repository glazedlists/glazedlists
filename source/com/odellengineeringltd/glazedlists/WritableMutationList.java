/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists;

// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;
// For calling methods on the event dispacher thread
import javax.swing.SwingUtilities;
// for iterating over a mutation list the lazy way
import com.odellengineeringltd.glazedlists.util.EventListIterator;

/**
 * A WritableMutationList is a MutationList that supports change operations
 * such as <code>add()</code>, <code>set()</code>, <code>remove()</code>,
 * etc.
 *
 * <p>This list operates on the assumption that there is a 1:1 relationship
 * between elements in the source list and elements in the muated list.
 * This is appropriate for mutation lists that mutate the ordering or
 * inclusion of values such as filtering lists and sorting lists. It may not
 * appropriate for mutation lists that mutate the elements of the list.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=18">Bug 18</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class WritableMutationList extends MutationList implements ListChangeListener, EventList {

    /**
     * Creates a new WritableMutationList that uses the specified source list.
     */
    protected WritableMutationList(EventList source) {
        super(source);
    }
    
    /**
     * Gets the index into the source list for the object with the specified
     * index in this list. This is the index such that the following works:
     * <br><code>this.get(i) == source.get(getSourceIndex(i))</code> for all
     * values.
     */
    protected abstract int getSourceIndex(int mutationIndex);
    
    /**
     * Tests if this mutation shall accept calls to <code>add()</code>,
     * <code>remove()</code>, <code>set()</code> etc. This method is called
     * before every modification to verify that the modification is allowed
     * in the current state of this mutation.
     */
    protected abstract boolean isWritable();
    
    /**
     * Adds the specified element into the source list. The added value may
     * not be visible in this mutated view. It may not be added at the specified
     * index as a consequence of reordering by this view or a parent view.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public void add(int index, Object value) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            if(index < 0 || index > size()) throw new ArrayIndexOutOfBoundsException("Cannot add at " + index + " on list of size " + size());
            int sourceIndex = 0;
            if(index < size()) sourceIndex = getSourceIndex(index);
            else sourceIndex = source.size();
            source.add(sourceIndex, value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Adds the specified element into this list. The added value may not
     * be visible in this mutated view.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public boolean add(Object value) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            return source.add(value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Adds all of the elements of the specified collection to this list. Some
     * values may not be visible in this mutated view. It may not be added at
     * the specified index as a consequence of reordering by this view or a
     * parent view.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public boolean addAll(int index, Collection values) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            if(index < 0 || index > size()) throw new ArrayIndexOutOfBoundsException("Cannot add at " + index + " on list of size " + size());
            int sourceIndex = 0;
            if(index < size()) sourceIndex = getSourceIndex(index);
            else sourceIndex = source.size();
            return source.addAll(sourceIndex, values);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Add all elements of the specified collection to this list. Some values
     * may not be visible in this mutated view.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public boolean addAll(Collection values) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            return source.addAll(values);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Removes <strong>all</strong> elements from the source list, even
     * those that are not displayed from this view. In order to remove only the
     * elements displayed by this view, iterate through this list to remove its
     * elements one at a time.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public void clear() {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            source.clear();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Removes the element at the specified index from the source list.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public Object remove(int index) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            if(index < 0 || index >= size()) throw new ArrayIndexOutOfBoundsException("Cannot remove at " + index + " on list of size " + size());
            return source.remove(getSourceIndex(index));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Removes the specified element from the source list.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public boolean remove(Object toRemove) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            return source.remove(toRemove);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Removes the specified elements from the source list.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public boolean removeAll(Collection values) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            return source.removeAll(values);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Retains only the values from the source list that are in the
     * specified collection.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public boolean retainAll(Collection values) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            return source.retainAll(values);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Replaces the object at the specified index in the source list with
     * the specified value. Note that the replacement value may not be
     * visible in this mutated view. The replaced value may not be at
     * the specified index as a consequence of reordering by this view or
     * the parent view.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public Object set(int index, Object value) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            if(index < 0 || index >= size()) throw new ArrayIndexOutOfBoundsException("Cannot set at " + index + " on list of size " + size());
            return source.set(getSourceIndex(index), value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
}
