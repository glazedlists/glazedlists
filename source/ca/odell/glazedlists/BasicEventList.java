/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the core Glazed Lists packages
import ca.odell.glazedlists.event.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * An {@link EventList} that wraps any simple {@link List}, such as {@link ArrayList}
 * or {@link LinkedList}.
 *
 * <p>This {@link List} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> {@link EventList}s
 * are thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial-0.9.1/">Glazed
 * Lists Tutorial</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class BasicEventList extends AbstractEventList {

    /** the underlying data list */
    private List data;

    /**
     * Creates a {@link BasicEventList} that uses a {@link ArrayList} as the
     * underlying list implementation.
     */
    public BasicEventList() {
        this(new ArrayList());
    }

    /**
     * Creates a {@link BasicEventList} that uses the specified {@link List} as
     * the underlying implementation.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> all editing to
     * the specified {@link List} <strong>must</strong> be done through via this
     * {@link BasicEventList} interface. Otherwise this {@link BasicEventList} will
     * become out of sync and operations will fail.
     */
    public BasicEventList(List list) {
        data = list;
        readWriteLock = new J2SE12ReadWriteLock();
    }

    /** {@inheritDoc} */
    public void add(int index, Object element) {
        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addInsert(index);
            // do the actual add
            data.add(index, element);
            // fire the event
            updates.commitEvent();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean add(Object element) {
        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addInsert(size());
            // do the actual add
            boolean result = data.add(element);
            // fire the event
            updates.commitEvent();
            return result;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean addAll(Collection collection) {
        return addAll(size(), collection);
    }

    /** {@inheritDoc} */
    public boolean addAll(int index, Collection collection) {
        // don't do an add of an empty set
        if(collection.size() == 0) return false;

        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addInsert(index, index + collection.size() - 1);
            // do the actual add
            boolean result = data.addAll(index, collection);
            // fire the event
            updates.commitEvent();
            return result;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Appends all of the elements in the specified array to the end of this list.
     *
     * @param objects list of elements that are to be added to this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * 
     * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
     *         not supported by this list.
     * @throws ClassCastException if the class of an element in the specified
     * 	       array prevents it from being added to this list.
     * @throws NullPointerException if the specified array contains one
     *         or more null elements and this list does not support null
     *         elements, or if the specified array is <tt>null</tt>.
     * @throws IllegalArgumentException if some aspect of an element in the
     *         specified array prevents it from being added to this
     *         list.
     * @see #add(Object)
     */
    public boolean addAll(Object[] objects) {
        return addAll(size(), objects);
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list at the specified position. Shifts the element currently at that
     * position (if any) and any subsequent elements to the right (increases
     * their indices).
     *
     * @param objects list of elements that are to be added to this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * 
     * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
     *         not supported by this list.
     * @throws ClassCastException if the class of an element in the specified
     * 	       array prevents it from being added to this list.
     * @throws NullPointerException if the specified array contains one
     *         or more null elements and this list does not support null
     *         elements, or if the specified array is <tt>null</tt>.
     * @throws IllegalArgumentException if some aspect of an element in the
     *         specified array prevents it from being added to this
     *         list.
     * @see #add(Object)
     */
    public boolean addAll(int index, Object[] objects) {
        // don't do an add of an empty set
        if(objects.length == 0) return false;

        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addInsert(index, index + objects.length - 1);
            // do the actual add
            boolean overallResult = true;
            boolean elementResult = true;
            for(int i = 0; i < objects.length; i++) {
                elementResult = data.add(objects[i]);
                overallResult = (overallResult && elementResult);
            }
            // fire the event
            updates.commitEvent();
            return overallResult;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public Object remove(int index) {
        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addDelete(index);
            // do the actual remove
            Object removed = data.remove(index);
            // fire the event
            updates.commitEvent();
            return removed;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean remove(Object element) {
        getReadWriteLock().writeLock().lock();
        try {
            int index = data.indexOf(element);
            if(index == -1) return false;
            remove(index);
            return true;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public void clear() {
        getReadWriteLock().writeLock().lock();
        try {
            // don't do a clear on an empty set
            if(size() == 0) return;
            // create the change event
            updates.beginEvent();
            updates.addDelete(0, size() - 1);
            // do the actual clear
            data.clear();
            // fire the event
            updates.commitEvent();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public Object set(int index, Object element) {
        getReadWriteLock().writeLock().lock();
        try {
            // create the change event
            updates.beginEvent();
            updates.addUpdate(index);
            // do the actual set
            Object previous = data.set(index, element);
            // fire the event
            updates.commitEvent();
            return previous;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public Object get(int index) {
        return data.get(index);
    }

    /** {@inheritDoc} */
    public int size() {
        return data.size();
    }

    /** {@inheritDoc} */
    public boolean removeAll(Collection collection) {
        getReadWriteLock().writeLock().lock();
        try {
            boolean changed = false;
            updates.beginEvent();
            for(Iterator i = collection.iterator(); i.hasNext(); ) {
                int index = -1;
                if((index = data.indexOf(i.next())) != -1) {
                    updates.addDelete(index);
                    data.remove(index);
                    changed = true;
                }
            }
            updates.commitEvent();
            return changed;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean retainAll(Collection collection) {
        getReadWriteLock().writeLock().lock();
        try {
            boolean changed = false;
            updates.beginEvent();
            int index = 0;
            while(index < data.size()) {
                if(collection.contains(data.get(index))) {
                    index++;
                } else {
                    updates.addDelete(index);
                    data.remove(index);
                    changed = true;
                }
            }
            updates.commitEvent();
            return changed;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
}
