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
public abstract class TransformedList implements EventList, ListEventListener {

    /** the underlying list */
    protected EventList source;
    
    /** the change event and notification system */
    protected ListEventAssembler updates = new ListEventAssembler(this);
    
    /** the read/write lock provides mutual exclusion to access */
    protected ReadWriteLock readWriteLock;

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
     * Gets the lock object in order to access this list in a thread-safe manner.
     * This will return a <strong>re-entrant</strong> implementation of
     * ReadWriteLock which can be used to guarantee mutual exclusion on access.
     */
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
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
            for(ListIterator i = listIterator(); i.hasNext(); ) {
                i.next();
                i.remove();
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
    
    /**
     * Returns true if this list contains the specified element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean contains(Object object) {
        // for through this, looking for the lucky object
        for(Iterator i = iterator(); i.hasNext(); ) {
            Object a = i.next();
            if(a == null && object == null) return true;
            else if(a.equals(object)) return true;
        }
        // not found
        return false;
    }

    /**
     * Returns true if this list contains all of the elements of the specified collection.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean containsAll(Collection collection) {
        // look for something that is missing
        for(Iterator i = collection.iterator(); i.hasNext(); ) {
            Object a = i.next();
            if(!contains(a)) return false;
        }
        // contained everything we looked for
        return true;
    }

    /**
     * Compares the specified object with this list for equality.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean equals(Object object) {
        if(object == this) return true;
        if(object == null) return false;
        if(!(object instanceof List)) return false;
        
        // ensure the lists are the same size
        List otherList = (List)object;
        if(otherList.size() != size()) return false;
        
        // compare element wise, via iterators
        ListIterator iterA = listIterator();
        ListIterator iterB = otherList.listIterator();
        while(iterA.hasNext() && iterB.hasNext()) {
            // get the ith object from each list to compare
            Object a = iterA.next();
            Object b = iterB.next();
            
            // handle the both null case
            if(a == b) continue;
            // if one is null and the other is not, die
            if(a == null || b == null) return false;
            // if they are not equal die
            if(!a.equals(b)) return false;
        }
        
        // if we haven't failed yet, they match
        return true;
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
     * Returns the hash code value for this list.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int hashCode() {
        // stolen SHAMELESSLY from the JDK for guaranteed compatibility
        int hashCode = 1;
        for(Iterator i = iterator(); i.hasNext(); ) {
            Object a = i.next();
            hashCode = 31 * hashCode + (a == null ? 0 : a.hashCode());
        }
        return hashCode;
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int indexOf(Object object) {
        // for through this, looking for the lucky object
        int index = 0;
        for(Iterator i = iterator(); i.hasNext(); ) {
            Object a = i.next();
            if(a == null && object == null) return index;
            else if(a.equals(object)) return index;
            index++;
        }
        // not found
        return -1;
    }
    
    /**
     * Returns true if this list contains no elements.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean isEmpty() {
        return (size() == 0);
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
            boolean overallChanged = false;
            for(Iterator i = values.iterator(); i.hasNext(); ) {
                boolean removeChanged = remove(i.next());
                if(removeChanged) overallChanged = true;
            }
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
            boolean changed = false;
            for(ListIterator i = listIterator(); i.hasNext(); ) {
                if(!values.contains(i.next())) {
                    i.remove();
                    changed = true;
                }
            }
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

    /**
     * Returns a view of the portion of this list between the specified
     * fromIndex, inclusive, and toIndex, exclusive.
     */
    public List subList(int fromIndex, int toIndex) {
        return new SubEventList(this, fromIndex, toIndex, true);
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public Object[] toArray() {
        // copy values into the array
        Object[] array = new Object[size()];
        int index = 0;
        for(Iterator i = iterator(); i.hasNext(); ) {
            array[index] = i.next();
            index++;
        }
        return array;
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the
     * specified array.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public Object[] toArray(Object[] array) {
        // create an array of the same type as the array passed
        if (array.length < size()) {
            array = (Object[])java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size());
        } else if(array.length > size()) {
            array[size()] = null;
        }
        
        // copy values into the array
        int index = 0;
        for(Iterator i = iterator(); i.hasNext(); ) {
            array[index] = i.next();
            index++;
        }
        return array;
    }

    /**
     * Gets this list in String form for debug or display.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("[");
        for(Iterator i = iterator(); i.hasNext(); ) {
            result.append(i.next());
            if(i.hasNext()) result.append(", ");
        }
        result.append("]");
        return result.toString();
    }
    

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int lastIndexOf(Object object) {
        // for through this, looking for the lucky object
        int index = size() - 1;
        for(ListIterator i = listIterator(size()); i.hasPrevious(); ) {
            Object a = i.previous();
            if(a == null && object == null) return index;
            else if(a.equals(object)) return index;
            index--;
        }
        // not found
        return -1;
    }
}
