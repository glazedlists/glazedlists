/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// standard collections
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

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

    /**
     * @return <tt>false</tt>; ReadOnlyList is... ahem... readonly
     */
    @Override
    protected boolean isWritable() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        // just pass on the changes
        updates.forwardEvent(listChanges);
    }

    //
    // All accessor methods must call to the source list so as not to
    // disturb the performance characteristics of the source list algorithms.
    //

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object object) {
        return source.contains(object);
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray() {
        return source.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public <T>T[] toArray(T[] array) {
        return source.toArray(array);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(Collection<?> values) {
        return source.containsAll(values);
    }

    /** {@inheritDoc} */
    @Override
    public int indexOf(Object object) {
        return source.indexOf(object);
    }

    /** {@inheritDoc} */
    @Override
    public int lastIndexOf(Object object) {
        return source.lastIndexOf(object);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object) {
        return source.equals(object);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return source.hashCode();
    }

    //
    // All mutator methods should throw an UnsupportedOperationException with
    // a descriptive message explaining why mutations are disallowed.
    //

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public boolean add(E value) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public void add(int index, E value) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public boolean addAll(Collection<? extends E> values) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public boolean addAll(int index, Collection<? extends E> values) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public boolean remove(Object toRemove) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public boolean retainAll(Collection<?> values) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public E set(int index, E value) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }

    /** @throws UnsupportedOperationException since ReadOnlyList cannot be modified */
    @Override
    public void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException("ReadOnlyList cannot be modified");
    }
}