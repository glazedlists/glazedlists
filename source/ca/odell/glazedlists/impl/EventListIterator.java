/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl;

// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// for being a list iterator
import java.util.*;

/**
 * The EventListIterator is an iterator that allows the user to iterate
 * on a list <i>that may be changed while it is iterated</i>. This is
 * possible because the iterator is a listener for change events to the
 * source list.
 *
 * <p>This iterator simply keeps an index of where it is and what it last
 * saw. It knows nothing about the underlying storage performance of the List
 * that it iterates, and therefore provides generic, possibly slow access
 * to its elements.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventListIterator implements ListIterator, ListEventListener {

    /** the list being iterated */
    private EventList source;

    /** the index of the next element to view */
    private int nextIndex;

    /** the most recently accessed element */
    private int lastIndex = -1;

    /**
     * Create a new event list iterator that iterates over the specified
     * source list.
     *
     * @param source the list to iterate
     */
    public EventListIterator(EventList source) {
        this(source, 0, true);
    }

    /**
     * Creates a new iterator that starts at the specified index.
     *
     * @param source the list to iterate
     * @param nextIndex the starting point within the list
     */
    public EventListIterator(EventList source, int nextIndex) {
        this(source, nextIndex, true);
    }

    /**
     * Creates a new iterator that starts at the specified index.
     *
     * @param source the list to iterate
     * @param nextIndex the starting point within the list
     * @param automaticallyRemove true if this SubList should deregister itself
     *      from the ListEventListener list of the source list once it is
     *      otherwise out of scope.
     *
     * @see ca.odell.glazedlists.event.WeakReferenceProxy
     */
    public EventListIterator(EventList source, int nextIndex, boolean automaticallyRemove) {
        this.source = source;
        this.nextIndex = nextIndex;

        // listen directly or via a proxy that will do garbage collection
        if(automaticallyRemove) {
            WeakReferenceProxy gcProxy = new WeakReferenceProxy(source, this);
            source.addListEventListener(gcProxy);
            // do not manage dependencies for iterators, they never have multiple sources
            source.getPublisher().removeDependency(source, gcProxy);

        } else {
            source.addListEventListener(this);
            // do not manage dependencies for iterators, they never have multiple sources
            source.getPublisher().removeDependency(source, this);
        }
    }

    /**
     * Returns true if this list iterator has more elements when traversing the
     * list in the forward direction.
     */
    public boolean hasNext() {
        if(nextIndex < source.size()) return true;
        return false;
    }

    /**
     * Returns the next element in the list.
     */
    public Object next() {
        // next shouldn't have been called.
        if(nextIndex == source.size()) {
 	       throw new NoSuchElementException("Cannot retrieve element " + nextIndex + " on a list of size " + source.size());

        // when next has not been removed
        } else {
            lastIndex = nextIndex;
            nextIndex++;
            return source.get(lastIndex);
        }
    }

    /**
     * Returns the index of the element that would be returned by a subsequent call to next.
     */
    public int nextIndex() {
        return nextIndex;
    }

    /**
     * Returns true if this list iterator has more elements when traversing the
     * list in the reverse direction.
     */
    public boolean hasPrevious() {
        if(nextIndex > 0) return true;
        return false;
    }

    /**
     * Returns the previous element in the list.
     */
    public Object previous() {
        // previous shouldn't have been called
        if(nextIndex == 0) {
	        throw new NoSuchElementException("Cannot retrieve element " + nextIndex + " on a list of size " + source.size());

        // when previous has not been removed
        } else {
            nextIndex--;
            lastIndex = nextIndex;
            return source.get(nextIndex);
        }
    }

    /**
     * Returns the index of the element that would be returned by a subsequent call to previous.
     */
    public int previousIndex() {
        return nextIndex - 1;
    }

    /**
     * Inserts the specified element into the list (optional operation).
     */
    public void add(Object o) {
        source.add(nextIndex, o);
    }

    /**
     * Removes from the list the last element that was returned by next
     * or previous (optional operation).
     */
    public void remove() {
        if(lastIndex == -1) throw new IllegalStateException("Cannot remove() without a prior call to next() or previous()");
        source.remove(lastIndex);
    }

    /**
     * Replaces the last element returned by next or previous with the
     * specified element (optional operation).
     */
    public void set(Object o) {
        if(lastIndex == -1) throw new IllegalStateException("Cannot set() without a prior call to next() or previous()");
        source.set(lastIndex, o);
    }

    /**
     * When the list is changed, the iterator adjusts its index.
     */
    public void listChanged(ListEvent listChanges) {
        while(listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            // if it is an insert
            if(changeType == ListEvent.INSERT) {
                if(changeIndex <= nextIndex) nextIndex++;

                if(lastIndex != -1 && changeIndex <= lastIndex) lastIndex++;
            // if it is a delete
            } else if(changeType == ListEvent.DELETE) {
                if(changeIndex < nextIndex) nextIndex--;

                if(lastIndex != -1 && changeIndex < lastIndex) lastIndex--;
                else if(lastIndex != -1 && changeIndex == lastIndex) lastIndex = -1;
            }
        }
    }
}
