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
public class BasicEventList implements EventList, Serializable {

    /** the underlying data list */
    private List data;

    /** the change event and notification system */
    protected ListChangeSequence updates = new ListChangeSequence();
    
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
        // lock on this list
        synchronized(getRootList()) {
            // create the change event
            updates.beginAtomicChange();
            updates.appendChange(index, ListChangeBlock.INSERT);
            // do the actual add
            data.add(index, element);
            // fire the event
            updates.commitAtomicChange();
        }
    }
            
    /**
     * Appends the specified element to the end of this list.
     */
    public boolean add(Object element) {
        // lock on this list
        synchronized(getRootList()) {
            // create the change event
            updates.beginAtomicChange();
            updates.appendChange(size(), ListChangeBlock.INSERT);
            // do the actual add
            boolean result = data.add(element);
            // fire the event
            updates.commitAtomicChange();
            return result;
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
        // lock on this list
        synchronized(getRootList()) {
            // create the change event
            updates.beginAtomicChange();
            updates.appendChange(index, index + collection.size() - 1, ListChangeBlock.INSERT);
            // do the actual add
            boolean result = data.addAll(index, collection);
            // fire the event
            updates.commitAtomicChange();
            return result;
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
        // lock on this list
        synchronized(getRootList()) {
            // create the change event
            updates.beginAtomicChange();
            updates.appendChange(index, index + objects.length - 1, ListChangeBlock.INSERT);
            // do the actual add
            boolean overallResult = true;
            boolean elementResult = true;
            for(int i = 0; i < objects.length; i++) {
                elementResult = data.add(objects[i]);
                overallResult = (overallResult && elementResult);
            }
            // fire the event
            updates.commitAtomicChange();
            return overallResult;
        }
    }

    /**
     * Removes the element at the specified position in this list.
     */
    public Object remove(int index) {
        // lock on this list
        synchronized(getRootList()) {
            // create the change event
            updates.beginAtomicChange();
            updates.appendChange(index, ListChangeBlock.DELETE);
            // do the actual remove
            Object removed = data.remove(index);
            // fire the event
            updates.commitAtomicChange();
            return removed;
        }
    }
    /**
     * Removes a single instance of the specified element from this 
     * collection, if it is present (optional operation).
     *
     * This uses indexOf and remove(index) to do the actual remove.
     */
    public boolean remove(Object element) {
        int index = data.indexOf(element);
        if(index == -1) return false;
        remove(index);
        return true;
    }
          
    /**
     * Removes all of the elements from this list (optional operation).
     */
    public void clear() {
        // don't do a clear on an empty set
        if(size() == 0) return;
        // lock on this list
        synchronized(getRootList()) {
            // create the change event
            updates.beginAtomicChange();
            updates.appendChange(0, size() - 1, ListChangeBlock.DELETE);
            // do the actual clear
            data.clear();
            // fire the event
            updates.commitAtomicChange();
        }
    }

    /**
     * Replaces the element at the specified position in this list with the 
     * specified element.
     */
    public Object set(int index, Object element) {
        // lock on this list
        synchronized(getRootList()) {
            // create the change event
            updates.beginAtomicChange();
            updates.appendChange(index, ListChangeBlock.UPDATE);
            // do the actual set
            Object previous = data.set(index, element);
            // fire the event
            updates.commitAtomicChange();
            return previous;
        }
    }
          

    /**
     * Returns true if this list contains the specified element.
     */
    public boolean contains(Object object) {
        return data.contains(object);
    }
    /**
     * Returns true if this list contains all of the elements of the specified collection.
     */
    public boolean containsAll(Collection collection) {
        return data.containsAll(collection);
    }
    
    /**
     * Compares the specified object with this list for equality.
     */
    public boolean equals(Object object) {
        return data.equals(object);
    }
    
    /**
     * Returns the element at the specified position in this list.
     */
    public Object get(int index) {
        return data.get(index);
    }
    /**
     * Returns the hash code value for this list.
     */
    public int hashCode() {
        return data.hashCode();
    }
    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int indexOf(Object object) {
        return data.indexOf(object);
    }
    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(Object object) {
        return data.lastIndexOf(object);
    }
    /**
     * Returns the number of elements in this list.
     */
    public int size() {
        return data.size();
    }
    /**
     * Returns true if this list contains no elements.
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }
    /**
     * Returns a view of the portion of this list between the specified
     * fromIndex, inclusive, and toIndex, exclusive.
     */
    public List subList(int fromIndex, int toIndex) {
        return data.subList(fromIndex, toIndex);
    }
    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence.
     */
    public Object[] toArray() {
        return data.toArray();
    }
    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the
     * specified array.
     */
    public Object[] toArray(Object[] array) {
        return data.toArray(array);
    }


    /**
     * Removes from this collection all of its elements that are contained
     * in the specified collection (optional operation).
     */
    public boolean removeAll(Collection collection) {
        throw new RuntimeException("The removeAll() method has not been implemented in the glazedlists package!");
    }
    /**
     * Retains only the elements in this collection that are contained in 
     * the specified collection (optional operation).
     */
    public boolean retainAll(Collection collection) {
        throw new RuntimeException("The retainAll() method has not been implemented in the glazedlists package!");
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
    public final void addListChangeListener(ListChangeListener listChangeListener) {
        updates.addListChangeListener(listChangeListener);
    }
    /**
     * Removes the specified listener from receiving change updates for this list.
     */
    public void removeListChangeListener(ListChangeListener listChangeListener) {
        updates.removeListChangeListener(listChangeListener);
    }

    /**
     * For implementing the EventList interface. This returns this list, which does
     * not depend on another list.
     */
    public EventList getRootList() {
        return this;
    }
    
    /**
     * Gets this list in String form for debug or display.
     */
    public String toString() {
        synchronized(getRootList()) {
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
}
