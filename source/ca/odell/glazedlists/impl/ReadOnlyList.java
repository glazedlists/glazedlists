/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// standard collections
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;

import java.util.Collection;
import java.util.List;

/**
 * An {@link EventList} that does not allow writing operations.
 *
 * <p>The {@link ReadOnlyList} is useful for programming defensively. A
 * {@link ReadOnlyList} is useful to supply an unknown class read-only access
 * to your {@link EventList}. 
 *
 * <p>The {@link ReadOnlyList} provides an up-to-date view of its source
 * {@link EventList} so changes to the source {@link EventList} will still be
 * reflected. For a static copy of any {@link EventList} it is necessary to copy
 * the contents of that {@link EventList} into any {@link List}.
 *
 * <p>{@link ReadOnlyList} does not alter the runtime performance
 * characteristics of the source {@link EventList}.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * @see TransformedList
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class ReadOnlyList<E> extends TransformedList<E, E> {

    /**
     * Creates a {@link ReadOnlyList} to provide a view of an {@link EventList}
     * that does not allow write operations.
     */
    public ReadOnlyList(EventList<E> source) {
        super(source);
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        // just pass on the changes
        updates.forwardEvent(listChanges);
    }

    //
    // All accessor methods must call to the source list so as not to
    // disturb the performance characteristics of the source list algorithms.
    //

    /** {@inheritDoc} */
    public boolean contains(Object object) {
        return source.contains(object);
    }

    /** {@inheritDoc} */
    public Object[] toArray() {
        return source.toArray();
    }

    /** {@inheritDoc} */
    public <T>T[] toArray(T[] array) {
        return source.toArray(array);
    }

    /** {@inheritDoc} */
    public boolean containsAll(Collection<?> values) {
        return source.containsAll(values);
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        return source.indexOf(object);
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object object) {
        return source.lastIndexOf(object);
    }

    /** {@inheritDoc} */
    public boolean equals(Object object) {
        return source.equals(object);
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return source.hashCode();
    }

    //
    // All mutator methods should throw an UnsupportedOperationException with
    // a descriptive message explaining why mutations are disallowed.
    //

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    public boolean add(E value) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    public void add(int index, E value) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    public boolean addAll(Collection<? extends E> values) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    public boolean addAll(int index, Collection<? extends E> values) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    public void clear() {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    public boolean remove(Object toRemove) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    public E remove(int index) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    public boolean retainAll(Collection<?> values) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    public E set(int index, E value) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }
}