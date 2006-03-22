/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// for being a list iterator
import java.util.*;

/**
 * The SimpleIterator is an iterator that allows the user to iterate
 * on a list.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class SimpleIterator<E> implements Iterator<E> {

    /** the list being iterated */
    private final List<E> source;

    /** the index of and reference to the next element to view */
    private int nextIndex = 0;

    /**
     * Create a new event list iterator that iterates over the specified
     * source list.
     *
     * @param source the list to iterate
     */
    public SimpleIterator(List<E> source) {
        this.source = source;
    }

    /**
     * Returns true if this iterator has more elements when traversing the
     * list in the forward direction.
     *
     */
    public boolean hasNext() {
        return nextIndex < source.size();
    }

    /**
     * Returns the next element in the list.
     */
    public E next() {
        // there are no more values
        if(nextIndex == source.size())
            throw new NoSuchElementException("Cannot retrieve element " + nextIndex + " on a list of size " + source.size());

        // a next value exists
        final E result = source.get(nextIndex);
        nextIndex++;
        return result;
    }

    /**
     * Removes from the list the last element that was returned by next
     * or previous.
     */
    public void remove() {
        if(nextIndex == 0) throw new IllegalStateException("Cannot remove() without a prior call to next() or previous()");
        source.remove(nextIndex - 1);
        nextIndex--;
    }
}