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
// for iterators and sublists
import com.odellengineeringltd.glazedlists.util.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import com.odellengineeringltd.glazedlists.util.concurrent.*;

/**
 * A MutationList is an altered view on an EventList. This may be a sorted
 * view of the list data or perhaps a filtering. All requests on a mutation
 * list are mapped to appropriate requests on a source list. 
 *
 * <p>Because there may be many mutations of a single source list, the
 * MutationLists are not directy modifiable. In order to modify a
 * MutationList, you must modify its underlying source, the root list,
 * which can be retrieved using getRootList. Mutation lists should be
 * thread-safe. 
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class MutationList extends AbstractList implements EventList, ListChangeListener {

    /** the underlying event list */
    protected EventList source;
    
    /** the change event and notification system */
    protected ListChangeSequence updates = new ListChangeSequence();
    
    /** the read/write lock provides mutual exclusion to access */
    private ReadWriteLock readWriteLock;

    /**
     * Creates a new Mutation list that uses the specified source list.
     */
    protected MutationList(EventList source) {
        this.source = source;
        readWriteLock = source.getReadWriteLock();
    }

    /**
     * Creates a new Mutation list that provides no underlying source list.
     * Extending classes must override the get() and size() methods.  
     *
     * @deprecated As of March 15, 2004, there is no need to create a
     *      MutationList with no source.
     */
    protected MutationList() {
        source = this;
        readWriteLock = new J2SE12ReadWriteLock();
    }
    
    /**
     * Returns the element at the specified position in this list.
     */
    public abstract Object get(int index);

    /**
     * Returns the number of elements in this list. 
     */
    public abstract int size();

    /**
     * Notifies this MutationList about changes to its underlying list store.
     * Overriding classes will want to override this method to respond to
     * changes and to send those changes to listening classes.
     */
    public abstract void notifyListChanges(ListChangeEvent listChanges);

    /**
     * Registers the specified listener to receive notification of changes
     * to this list.
     */
    public void addListChangeListener(ListChangeListener listChangeListener) {
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
        return source.getRootList();
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
        return new SubEventList(this, fromIndex, toIndex, true);
    }

    /**
     * Gets this list in String form for debug or display.
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
