/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// volatile implementation support
import ca.odell.glazedlists.util.impl.*;
// Java collections are used for underlying data storage
import java.util.*;
// For calling methods on the event dispacher thread
import javax.swing.SwingUtilities;
// for iterators and sublists
import ca.odell.glazedlists.util.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;

/**
 * An TransformedList is a good place to get started in implementing
 * an EventList that provides a transformed view of a source EventList.
 *
 * <p>Because it is a decorator, the user should carefully implement the method
 * <code>getSourceIndex()</code> which is used to translate between the indicies
 * of this decorator and the indicies of the source list that it decorates.
 *
 * <p>TransformedLists can be made mutable via the API by overriding the
 * method <code>isWritable()</code>.
 *
 * <p>Although this class implements the <code>ListChangeListener</code> interface,
 * users must explicitly call addListEventListener() to register themselves as
 * listeners to the source list.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=18">Bug 18</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class TransformedList extends AbstractEventList implements ListEventListener {

    /** the underlying list */
    protected EventList source;

    /**
     * Creates an TransformedList that provides a transformed view of the
     * specified source list.
     *
     * @param source the list to use as the source of this event list. If source
     *      implements EventList, the read write lock of that list will be used
     *      as the read write lock for this list.
     */
    protected TransformedList(EventList source) {
        this.source = source;
        readWriteLock = source.getReadWriteLock();
    }

    /**
     * Gets the index into the source list for the object with the specified
     * index in this list. This is the index such that the following works:
     * <br><code>this.get(i) == source.get(getSourceIndex(i))</code> for all
     * values.
     */
    protected int getSourceIndex(int mutationIndex) {
        return mutationIndex;
    }
    
    /**
     * Tests if this mutation shall accept calls to <code>add()</code>,
     * <code>remove()</code>, <code>set()</code> etc. This method is called
     * before every modification to verify that the modification is allowed
     * in the current state of this mutation.
     */
    protected boolean isWritable() {
        return false;
    }

    /**
     * For implementing the ListEventListener interface. Extending classes should
     * adjust in response to this change and forward notifications about that
     * adjustment to downstream listeners.
     */
    public abstract void listChanged(ListEvent listChanges);
    
    /**
     * Adds the specified element into this list. The added value may not
     * be visible in this mutated view.
     *
     * <p>Like all modifying methods, this method manages its own thread
     * safety via the <code>getReadWriteLock().writeLock()</code>. To perform
     * several changes atomically, obtain the write lock before the first change
     * and release it after the last change.
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
     * Adds the specified element into the source list. The added value may
     * not be visible in this mutated view. It may not be added at the specified
     * index as a consequence of reordering by this view or a parent view.
     *
     * <p>Like all modifying methods, this method manages its own thread
     * safety via the <code>getReadWriteLock().writeLock()</code>. To perform
     * several changes atomically, obtain the write lock before the first change
     * and release it after the last change.
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
     * Adds all of the elements of the specified collection to this list. Some
     * values may not be visible in this mutated view. It may not be added at
     * the specified index as a consequence of reordering by this view or a
     * parent view.
     *
     * <p>Like all modifying methods, this method manages its own thread
     * safety via the <code>getReadWriteLock().writeLock()</code>. To perform
     * several changes atomically, obtain the write lock before the first change
     * and release it after the last change.
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
     * <p>Like all modifying methods, this method manages its own thread
     * safety via the <code>getReadWriteLock().writeLock()</code>. To perform
     * several changes atomically, obtain the write lock before the first change
     * and release it after the last change.
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
     * <p>Like all modifying methods, this method manages its own thread
     * safety via the <code>getReadWriteLock().writeLock()</code>. To perform
     * several changes atomically, obtain the write lock before the first change
     * and release it after the last change.
     *
     * <p><strong>Warning:</strong> Because this method is implemented
     * using <i>multiple</i> modifying calls to the source list, <i>multiple</i>
     * events will be propogated. Therefore this method has been carefully
     * implemented to keep this list in a consistent state for each such modifying
     * operation.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public void clear() {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            // nest changes and let the other methods compose the event
            updates.beginEvent(true);
            while(!isEmpty()) {
                remove(0);
            }
            updates.commitEvent();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
    
    /**
     * Returns the element at the specified position in this list.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public Object get(int index) {
        if(index < 0 || index >= size()) throw new ArrayIndexOutOfBoundsException("Cannot get at " + index + " on list of size " + size());
        return source.get(getSourceIndex(index));
    }

    /**
     * Removes the element at the specified index from the source list.
     *
     * <p>Like all modifying methods, this method manages its own thread
     * safety via the <code>getReadWriteLock().writeLock()</code>. To perform
     * several changes atomically, obtain the write lock before the first change
     * and release it after the last change.
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
     * <p>Like all modifying methods, this method manages its own thread
     * safety via the <code>getReadWriteLock().writeLock()</code>. To perform
     * several changes atomically, obtain the write lock before the first change
     * and release it after the last change.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public boolean remove(Object toRemove) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            int index = indexOf(toRemove);
            if(index == -1) return false;
            source.remove(getSourceIndex(index));
            return true;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Removes the specified elements from the source list.
     *
     * <p>Like all modifying methods, this method manages its own thread
     * safety via the <code>getReadWriteLock().writeLock()</code>. To perform
     * several changes atomically, obtain the write lock before the first change
     * and release it after the last change.
     *
     * <p><strong>Warning:</strong> Because this method is implemented
     * using <i>multiple</i> modifying calls to the source list, <i>multiple</i>
     * events will be propogated. Therefore this method has been carefully
     * implemented to keep this list in a consistent state for each such modifying
     * operation.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public boolean removeAll(Collection values) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            // nest changes and let the other methods compose the event  
            updates.beginEvent(true);
            boolean overallChanged = false;
            for(Iterator i = values.iterator(); i.hasNext(); ) {
                boolean removeChanged = remove(i.next());
                if(removeChanged) overallChanged = true;
            }
            updates.commitEvent();
            return overallChanged;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Retains only the values from the source list that are in the
     * specified collection.
     *
     * <p>Like all modifying methods, this method manages its own thread
     * safety via the <code>getReadWriteLock().writeLock()</code>. To perform
     * several changes atomically, obtain the write lock before the first change
     * and release it after the last change.
     *
     * <p><strong>Warning:</strong> Because this method is implemented
     * using <i>multiple</i> modifying calls to the source list, <i>multiple</i>
     * events will be propogated. Therefore this method has been carefully
     * implemented to keep this list in a consistent state for each such modifying
     * operation.
     *
     * @throws IllegalStateException if this mutation cannot be modified in its
     *      current state.
     */
    public boolean retainAll(Collection values) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            // nest changes and let the other methods compose the event  
            updates.beginEvent(true);
            boolean changed = false;
            for(int i = 0; i < size(); ) {
                if(!values.contains(get(i))) {
                    remove(i);
                    changed = true;
                } else {
                    i++;
                }
            }
            updates.commitEvent();
            return changed;
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
     * <p>Like all modifying methods, this method manages its own thread
     * safety via the <code>getReadWriteLock().writeLock()</code>. To perform
     * several changes atomically, obtain the write lock before the first change
     * and release it after the last change.
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

    /**
     * Returns the number of elements in this list. 
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int size() {
        return source.size();
    }
}
