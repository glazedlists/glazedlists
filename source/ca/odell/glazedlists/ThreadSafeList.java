/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the core Glazed Lists package
import ca.odell.glazedlists.event.*;
// for event list utilities, iterators and comparators
import ca.odell.glazedlists.util.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * A ThreadSafeList is a mutation list that guarantees that access
 * to the source list is thread-safe.
 *
 * <p>Synchronizing method calls using <code>ReadWriteLock</code>s for
 * each method is expensive and adds needless complexity to a user's code.
 * As such, we provide this thread-safe Decorator to alleviate the need to
 * focus on underlying details so that users can focus on building their
 * business logic.
 *
 *<p>As a note, using this class blindly could result in a loss of performance
 * as a lock is aquired and released with each method call.  We provide this
 * class for convienience, however. If performance is a concern, feel free to implement
 * a similar class to better suit your needs.
 *
 * @see ca.odell.glazedlists.util.concurrent
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ThreadSafeList extends TransformedList implements ListEventListener {

    /**
     * Creates a new ThreadSafeList that is a thread-safe view of the
     * specified list.
     */
    public ThreadSafeList(EventList source) {
        super(source);
        source.addListEventListener(this);
    }
    
    /**
     * For implementing the ListEventListener interface. When the underlying list
     * changes, this sends notification to listening lists.
     */
    public void listChanged(ListEvent listChanges) {
        // just pass on the changes
        updates.forwardEvent(listChanges);
    }

    /**
     * Gets the specified element from the list.
     */
    public Object get(int index) {
        getReadWriteLock().readLock().lock();
        try {
            return source.get(index);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }
    
    /**
     * Gets the total number of elements in the list.
     */
    public int size() {
        getReadWriteLock().readLock().lock();
        try {
            return source.size();
        } finally {
            getReadWriteLock().readLock().unlock();
        }       
    }

    /**
     * Gets the index into the source list for the object with the specified
     * index in this list.
     */
    protected int getSourceIndex(int mutationIndex) {
        return mutationIndex;
    }
    
    /**
     * Tests if this mutation shall accept calls to <code>add()</code>,
     * <code>remove()</code>, <code>set()</code> etc.
     */
    protected boolean isWritable() {
        return true;
    }

    /**
     * Returns true if this list contains the specified element.
     */
    public boolean contains(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.contains(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns true if this list contains all of the elements of the specified collection.
     */
    public boolean containsAll(Collection collection) {
        getReadWriteLock().readLock().lock();
        try {
            return source.containsAll(collection);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }
    
    /**
     * Compares the specified object with this list for equality.
     */
    public boolean equals(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.equals(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns the hash code value for this list.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public int hashCode() {
        getReadWriteLock().readLock().lock();
        try {
            return source.hashCode();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int indexOf(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.indexOf(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.lastIndexOf(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns true if this list contains no elements.
     */
    public boolean isEmpty() {
        getReadWriteLock().readLock().lock();
        try {
            return source.isEmpty();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence.
     */
    public Object[] toArray() {
        getReadWriteLock().readLock().lock();
        try {
            return source.toArray();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the
     * specified array.
     */
    public Object[] toArray(Object[] array) {
        getReadWriteLock().readLock().lock();
        try {
            return source.toArray(array);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Gets this list in String form for debug or display.
     */
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
