/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// for iterators and sublists
import ca.odell.glazedlists.util.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// volatile implementation support
import ca.odell.glazedlists.util.impl.*;
// Java collections are used for underlying data storage
import java.util.*;
// For calling methods on the event dispacher thread
import javax.swing.SwingUtilities;
// for being serializable
import java.io.Serializable;

/**
 * An event list that wraps a Java Collections list. This list provides an
 * event notifying interface to a plain Java list. This may be useful to wrap
 * filtering or sorting on to an existing list, or to simply receive events
 * when a list changes.
 *
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part1/index.html#basiceventlist">Glazed
 * Lists Tutorial Part 1 - Basics</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class BasicEventList implements EventList, Serializable {

    /** the underlying data list */
    protected List data;

    /** the change event and notification system */
    protected ListEventAssembler updates = new ListEventAssembler(this);
    
    /** the read/write lock provides mutual exclusion to access */
    private ReadWriteLock readWriteLock = new J2SE12ReadWriteLock();

    /**
     * Creates a new EventArrayList that uses an ArrayList as the source list
     * implementation.
     */
    public BasicEventList() {
        data = new ArrayList();
    }
    
    /**
     * Creates a new EventArrayList that uses the specified list as the source
     * list. All editing to the specified source list <strong>must</strong> be
     * done through the BasicEventList interface. Otherwise the two lists will
     * become out of sync and the BasicEventList will fail.
     */
    public BasicEventList(List list) {
        data = list;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     */
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
            
    /**
     * Appends the specified element to the end of this list.
     */
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
    
    /**
     * Appends all of the elements in the specified Collection to the end of
     * this list, in the order that they are returned by the specified 
     * Collection's Iterator.
     */
    public boolean addAll(Collection collection) {
        return addAll(size(), collection);
    }
          
    /**
     * Inserts all of the elements in the specified Collection into this
     * list, starting at the specified position.
     */
    public boolean addAll(int index, Collection collection) {
        // don't do an add of an empty set
        if(collection.size() == 0) return true;

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
     * Appends all of the elements in the specified array to the end of
     * this list.
     */
    public boolean addAll(Object[] objects) {
        return addAll(size(), objects);
    }

    /**
     * Inserts all of the elements in the specified array into this
     * list, starting at the specified position.
     */
    public boolean addAll(int index, Object[] objects) {
        // don't do an add of an empty set
        if(objects.length == 0) return true;

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

    /**
     * Removes the element at the specified position in this list.
     */
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
    /**
     * Removes a single instance of the specified element from this 
     * collection, if it is present (optional operation).
     *
     * This uses indexOf and remove(index) to do the actual remove.
     */
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
          
    /**
     * Removes all of the elements from this list (optional operation).
     */
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

    /**
     * Replaces the element at the specified position in this list with the 
     * specified element.
     */
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
          

    /**
     * Returns true if this list contains the specified element.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean contains(Object object) {
        return data.contains(object);
    }

    /**
     * Returns true if this list contains all of the elements of the specified collection.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean containsAll(Collection collection) {
        return data.containsAll(collection);
    }
    
    /**
     * Compares the specified object with this list for equality.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean equals(Object object) {
        return data.equals(object);
    }
    
    /**
     * Returns the element at the specified position in this list.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public Object get(int index) {
        return data.get(index);
    }
    /**
     * Returns the hash code value for this list.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public int hashCode() {
        return data.hashCode();
    }
    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public int indexOf(Object object) {
        return data.indexOf(object);
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public int lastIndexOf(Object object) {
        return data.lastIndexOf(object);
    }

    /**
     * Returns the number of elements in this list.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public int size() {
        return data.size();
    }

    /**
     * Returns true if this list contains no elements.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Returns a view of the portion of this list between the specified
     * fromIndex, inclusive, and toIndex, exclusive.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public List subList(int fromIndex, int toIndex) {
        return new SubEventList(this, fromIndex, toIndex, true);
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public Object[] toArray() {
        return data.toArray();
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the
     * specified array.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public Object[] toArray(Object[] array) {
        return data.toArray(array);
    }


    /**
     * Removes from this collection all of its elements that are contained
     * in the specified collection (optional operation). This method has been
     * is available in this implementation, although the not particularly
     * high performance.
     */
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

    /**
     * Retains only the elements in this collection that are contained in 
     * the specified collection (optional operation). This method is available
     * in this implementation, although not particularly high performance.
     */
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

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     */
    public Iterator iterator() {
        return new EventListIterator(this);
    }
    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence).
     */
    public ListIterator listIterator() {
        return new EventListIterator(this);
    }
    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list.
     */
    public ListIterator listIterator(int index) {
        return new EventListIterator(this, index);
    }


    /**
     * Registers the specified listener to receive notification of changes
     * to this list.
     */
    public final void addListEventListener(ListEventListener listChangeListener) {
        updates.addListEventListener(listChangeListener);
    }
    /**
     * Removes the specified listener from receiving change updates for this list.
     */
    public void removeListEventListener(ListEventListener listChangeListener) {
        updates.removeListEventListener(listChangeListener);
    }

    /**
     * For implementing the EventList interface. This returns this list, which does
     * not depend on another list.
     */
    public EventList getRootList() {
        return this;
    }
    
    /**
     * Gets the lock object in order to access this list in a thread-safe manner.
     * This will return a <strong>re-entrant</strong> implementation of
     * ReadWriteLock which can be used to guarantee mutual exclusion on access.
     */
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    /**
     * Gets this list in String form for debug or display.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("[");
        for(int i = 0; i < size(); i++) {
            if(i != 0) result.append(", ");
            result.append(get(i));
        }
        result.append("]");
        return result.toString();
    }
}
