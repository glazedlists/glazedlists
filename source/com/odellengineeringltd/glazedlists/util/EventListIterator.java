/**
* Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// for being a list iterator
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;


/**
* A non-optimized list iterator for iterating event lists. This non-clever
 * iterator simply keeps an index of where it is and what it last saw. It
 * knows nothing about the underlying storage of the List that it iterates,
 * and therefore provides generic, possibly slow access to its elements.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventListIterator implements ListIterator {
    
    /** the list being iterated */
    private List target;
    
    /** the index of the next element to view */
    private int nextIndex;
    /** the most recently accessed element */
    private int lastIndex = -1;
    
    /**
     * Create a new event list iterator that iterates over the specified
     * target list.
     */
    public EventListIterator(List target) {
        this.target = target;
        nextIndex = 0;
    }

    /**
     * Creates a new iterator that starts at the specified index.
     */
    public EventListIterator(List target, int nextIndex) {
        this.target = target;
        this.nextIndex = nextIndex;
    }
    
    /**
     * Inserts the specified element into the list (optional operation).
     */
    public void add(Object o) {
        target.add(nextIndex, o);
        nextIndex++;
        lastIndex = -1;
    }
    
    /**
     * Returns true if this list iterator has more elements when traversing the list in the forward direction.
     */
    public boolean hasNext() {
        return nextIndex < target.size();
    }
    
    /**
     * Returns true if this list iterator has more elements when traversing the list in the reverse direction.
     */
    public boolean hasPrevious() {
        return nextIndex > 0;
    }
    
    /**
     * Returns the next element in the list.
     */
    public Object next() {
        if(nextIndex >= target.size()) throw new NoSuchElementException("Cannot retrieve element " + nextIndex + " on a list of size " + target.size());
        Object result = target.get(nextIndex);
        lastIndex = nextIndex;
        nextIndex++;
        return result;
    }
    
    /**
     * Returns the index of the element that would be returned by a subsequent call to next.
     */
    public int nextIndex() {
        return nextIndex;
    }
    
    /**
     * Returns the previous element in the list.
     */
    public Object previous() {
        nextIndex--;
        if(nextIndex < 0) throw new NoSuchElementException("Cannot retrieve element " + nextIndex + " on a list of size " + target.size());
        Object result = target.get(nextIndex);
        lastIndex = nextIndex;
        return result;
    }
    
    /**
     * Returns the index of the element that would be returned by a subsequent call to previous.
     */
    public int previousIndex() {
        return nextIndex - 1;
    }
    
    /**
     * Removes from the list the last element that was returned by next or previous (optional operation).
     */
    public void remove() {
        if(lastIndex == -1) throw new IllegalStateException("Cannot remove() without a prior call to next() or previous()");
        target.remove(lastIndex);
        lastIndex = -1;
    }
    
    /**
     * Replaces the last element returned by next or previous with the specified element (optional operation).
     */
    public void set(Object o) {
        if(lastIndex == -1) throw new IllegalStateException("Cannot set() without a prior call to next() or previous()");
        target.set(lastIndex, o);
    }
}
