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
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Collection;
// For calling methods on the event dispacher thread
import javax.swing.SwingUtilities;
// for iterating over a mutation list the lazy way
import com.odellengineeringltd.glazedlists.util.EventListIterator;

/**
 * A mutation list is an altered view on an Event List. This may be a sorted
 * view of the list data or perhaps a filtering. All requests on a mutation
 * list are mapped to appropriate requests on a source list. Because there may
 * be many mutations of a single source list, the mutation lists are not
 * directy modifiable. In order to modify a mutation list, you must modify
 * its underlying source, the root list, which can be retrieved using
 * getRootList. Mutation lists should be thread-safe. 
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class MutationList implements EventList, ListChangeListener {

    /** the underlying event list */
    protected EventList source;
    
    /** the change event and notification system */
    protected ListChangeSequence updates = new ListChangeSequence();

    /**
     * Creates a new Mutation list that uses the specified source list.
     */
    public MutationList(EventList source) {
        this.source = source;
    }
    /**
     * Creates a new Mutation list that provides no underlying source list.
     * Extending classes must override the get() and size() methods.  
     */
    protected MutationList() {
        source = this;
    }
    
    /**
     * Returns the element at the specified position in this list. Most
     * mutation lists will override the get method to use a mapping.
     */
    public Object get(int index) {
        if(source == this) throw new RuntimeException("Classes that extend mutation list must override the get() method"); 
        return source.get(index);
    }

    /**
     * Returns the number of elements in this list. Most mutation lists will
     * override the size() method.
     */
    public int size() {
        if(source == this) throw new RuntimeException("Classes that extend mutation list must override the size() method"); 
        return source.size();
    }

    /**
     * Returns true if this list contains no elements.
     *
     * This implementation uses the size() method, to allow classes that
     * extend MutationList from having to override both methods.
     */
    public boolean isEmpty() {
        return (size() == 0);
    }

    /**
     * Notifies this MutationList about changes to its underlying list store.
     * Overriding classes will want to override this method to respond to
     * changes and to send those changes to listening classes.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        // the basic mutation list just passes on the changes
        updates.beginAtomicChange();
        while(listChanges.next()) {
            updates.appendChange(listChanges.getIndex(), listChanges.getType());
        }
        updates.commitAtomicChange();
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
     * For implementing the EventList interface. This returns the root of the
     * source list, or <code>this</code> if this list has no source.
     */
    public EventList getRootList() {
        if(source == this) return this;
        return source.getRootList();
    }


    /**
     * Lookup methods are slow. They iterate through all elements in the source
     * list looking for the specified objects.
     */
    public boolean contains(Object object) {
        for(int i = 0; i < size(); i++) {
            if(get(i).equals(object)) return true;
        }
        return false;
    }
    public boolean containsAll(Collection collection) {
        for(Iterator i = collection.iterator(); i.hasNext(); ) {
            if(!contains(i.next())) return false;
        }
        return true;
    }
    

    /**
     * Compares this list item-by-item to see if they are equal.
     * This comparison can be extremely slow as it iterates through
     * all elements in this list once for every element in the
     * provided list. The total runtime is <code>O(N^2)</code>
     */
    public boolean equals(Object object) {
        if(!(object instanceof List)) return false;
        List otherList = (List)object;
        if(size() != otherList.size()) return false;
        return containsAll(otherList);
    }
    
    /**
     * Returns the hash code value for this list.
     */
    public int hashCode() {
        int hashCode = 37;
        for(int i = 0; i < size(); i++) {
            hashCode = (hashCode * 17) + get(i).hashCode();
        }
        return hashCode;
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int indexOf(Object object) {
        for(int i = 0; i < size(); i++) {
            if(get(i).equals(object)) return i;
        }
        return -1;
    }
    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(Object object) {
        for(int i = size() - 1; i >= 0; i--) {
            if(get(i).equals(object)) return i;
        }
        return -1;
    }


    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence.
     */
    public Object[] toArray() {
        Object[] array = new Object[size()];
        for(int i = 0; i < array.length; i++) {
            array[i] = get(i);
        }
        return array;
    }
    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the
     * specified array.
     *
     * This includes reflection code as prototyped by the Java ArrayList
     * implementation, to create an array of the runtime type as specified.
     */
    public Object[] toArray(Object[] array) {
        int size = size();
        if (array.length < size) {
            array = (Object[])java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
        }
        for(int i = 0; i < size; i++) {
            array[i] = get(i);
        }
        // set the element after last to null, as per the Collections.toArray contract
        if(size < array.length) array[size] = null;
        return array;
    }

    /**
     * Methods that would change this list directly throw exceptions when
     * called.
     */
    public void add(int index, Object element) {
        throw new RuntimeException("This list cannot be edited directly! modify the source list instead.");
    }
    public boolean add(Object element) {
        throw new RuntimeException("This list cannot be edited directly! modify on the source list instead.");
    }
    public boolean addAll(Collection collection) {
        throw new RuntimeException("This list cannot be edited directly! modify on the source list instead.");
    }
    public boolean addAll(int index, Collection collection) {
        throw new RuntimeException("This list cannot be edited directly! modify on the source list instead.");
    }
    public Object remove(int index) {
        throw new RuntimeException("This list cannot be edited directly! modify on the source list instead.");
    }
    public boolean remove(Object element) {
        throw new RuntimeException("This list cannot be edited directly! modify on the source list instead.");
    }
    public Object set(int index, Object element) {
        throw new RuntimeException("This list cannot be edited directly! modify on the source list instead.");
    }
    public void clear() {
        throw new RuntimeException("This list cannot be edited directly! modify on the source list instead.");
    }
    public boolean removeAll(Collection collection) {
        throw new RuntimeException("This list cannot be edited directly! modify on the source list instead.");
    }
    public boolean retainAll(Collection collection) {
        throw new RuntimeException("This list cannot be edited directly! modify on the source list instead.");
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
     * Returns a view of the portion of this list between the specified
     * fromIndex, inclusive, and toIndex, exclusive.
     */
    public List subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("The method is not implemented.");
    }
}
