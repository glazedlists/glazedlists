/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * An {@link EventList} that obtains a {@link ReadWriteLock} for all operations.
 * <p>
 * This provides some support for sharing {@link EventList}s between multiple threads.
 * <p>
 * Using a {@link ThreadSafeList} for concurrent access to lists can be expensive because a
 * {@link ReadWriteLock} is aquired and released for every operation.
 * <p>
 * <strong><font color="#FF0000">Warning:</font></strong> Although this class provides thread safe
 * access, it does not provide any guarantees that changes will not happen between method calls. For
 * example, the following code is unsafe because the source {@link EventList} may change between
 * calls to {@link #size() size()} and {@link #get(int) get()}:
 * 
 * <pre>
 *  EventList source = ...
 * ThreadSafeList myList = new ThreadSafeList(source);
 * if(myList.size() > 3) {
 *   System.out.println(myList.get(3));
 * }
 * </pre>
 * <p>
 * <strong><font color="#FF0000">Warning:</font></strong> The objects returned by {@link #iterator()
 * iterator()}, {@link #subList(int,int) subList()}, {@link #stream()}, {@link #parallelStream()},
 * {@link #spliterator()} etc. are not thread safe.
 *
 * @see ca.odell.glazedlists.util.concurrent
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ThreadSafeList<E> extends TransformedList<E, E> {

    /**
     * Creates a {@link ThreadSafeList} that provides thread safe access to all
     * methods in the source {@link EventList}.
     */
    public ThreadSafeList(EventList<E> source) {
        super(source);
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        // just pass on the changes
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    @Override
    public E get(int index) {
        getReadWriteLock().readLock().lock();
        try {
            return source.get(index);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        getReadWriteLock().readLock().lock();
        try {
            return source.size();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.contains(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(Collection<?> collection) {
        getReadWriteLock().readLock().lock();
        try {
            return source.containsAll(collection);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.equals(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        getReadWriteLock().readLock().lock();
        try {
            return source.hashCode();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int indexOf(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.indexOf(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int lastIndexOf(Object object) {
        getReadWriteLock().readLock().lock();
        try {
            return source.lastIndexOf(object);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        getReadWriteLock().readLock().lock();
        try {
            return source.isEmpty();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        getReadWriteLock().readLock().lock();
        try {
            source.forEach(action);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray() {
        getReadWriteLock().readLock().lock();
        try {
            return source.toArray();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T> T[] toArray(T[] array) {
        getReadWriteLock().readLock().lock();
        try {
            return source.toArray(array);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(E value) {
        getReadWriteLock().writeLock().lock();
        try {
            return source.add(value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(Object toRemove) {
        getReadWriteLock().writeLock().lock();
        try {
            return source.remove(toRemove);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(Collection<? extends E> values) {
        getReadWriteLock().writeLock().lock();
        try {
            return source.addAll(values);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(int index, Collection<? extends E> values) {
        getReadWriteLock().writeLock().lock();
        try {
            return source.addAll(index, values);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeAll(Collection<?> values) {
        getReadWriteLock().writeLock().lock();
        try {
            return source.removeAll(values);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean retainAll(Collection<?> values) {
        getReadWriteLock().writeLock().lock();
        try {
            return source.retainAll(values);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        getReadWriteLock().writeLock().lock();
        try {
            source.clear();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public E set(int index, E value) {
        getReadWriteLock().writeLock().lock();
        try {
            return source.set(index, value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void add(int index, E value) {
        getReadWriteLock().writeLock().lock();
        try {
            source.add(index, value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public E remove(int index) {
        getReadWriteLock().writeLock().lock();
        try {
            return source.remove(index);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        getReadWriteLock().writeLock().lock();
        try {
            return source.removeIf(filter);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        getReadWriteLock().writeLock().lock();
        try {
            source.replaceAll(operator);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public void sort(Comparator<? super E> c) {
        getReadWriteLock().writeLock().lock();
        try {
            source.sort(c);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        getReadWriteLock().readLock().lock();
        try {
            return source.toString();
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }
}