/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the core Glazed Lists package
import ca.odell.glazedlists.event.*;
// for access to the Collection interface
import java.util.*;
// for locking
import ca.odell.glazedlists.util.concurrent.*;

/**
 * An {@link EventList} that obtains a {@link ReadWriteLock} for all operations.
 *
 * <p>This provides some support for sharing {@link EventList}s between multiple
 * threads.
 *
 * <p>Using a {@link ThreadSafeList} for concurrent access to lists can be expensive
 * because a {@link ReadWriteLock} is aquired and released for every operation.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> Although this class
 * provides thread safe access, it does not provide any guarantees that changes
 * will not happen between method calls. For example, the following code is unsafe
 * because the source {@link EventList} may change between calls to {@link #size() size()}
 * and {@link #get(int) get()}:
 * <pre> EventList source = ...
 * ThreadSafeList myList = new ThreadSafeList(source);
 * if(myList.size() > 3) {
 *   System.out.println(myList.get(3));
 * }</pre>
 *
 * @see ca.odell.glazedlists.util.concurrent
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ThreadSafeList extends TransformedList implements ListEventListener {

    /**
     * Creates a {@link ThreadSafeList} that provides thread safe access to all
     * methods in the source {@link EventList}.
     */
    public ThreadSafeList(EventList source) {
        super(source);
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // just pass on the changes
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    public Object get(int index) {
        getReadWriteLock().readLock().lock();
        try {
            return source.get(index);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public int size() {
        getReadWriteLock().readLock().lock();
        try {
            return source.size();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    /*protected int getSourceIndex(int mutationIndex) {
        return mutationIndex;
    }*/

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public boolean contains(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.contains(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean containsAll(Collection collection) {
        getReadWriteLock().readLock().lock();
        try {
            return source.containsAll(collection);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean equals(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.equals(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public int hashCode() {
        getReadWriteLock().readLock().lock();
        try {
            return source.hashCode();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.indexOf(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.lastIndexOf(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        getReadWriteLock().readLock().lock();
        try {
            return source.isEmpty();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public Object[] toArray() {
        getReadWriteLock().readLock().lock();
        try {
            return source.toArray();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public Object[] toArray(Object[] array) {
        getReadWriteLock().readLock().lock();
        try {
            return source.toArray(array);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        getReadWriteLock().readLock().lock();
        try {
            StringBuffer result = new StringBuffer();
            result.append("[");
            for(int i = 0; i < size(); i++) {
                if(i != 0) result.append(", ");
                result.append(get(i));
            }
            result.append("]");
            return result.toString();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }
}
