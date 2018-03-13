/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.javafx;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This list serves as an adapter to allow GlazedList's
 * {@link ca.odell.glazedlists.EventList EventLists} to be used as the backing
 * implementations for JavaFX {@link ObservableList ObservableLists}.
 *
 * <p>All methods that read from or write to the wrapped source {@link EventList}
 * apply the corresponding locking of the list, because these operations may be called from
 * JavaFX runtime code.</p>
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class EventObservableList<E> extends AbstractList<E> implements ObservableList<E>,
        ListEventListener<E> {

    private static final int[] EMPTY_INT_ARRAY = new int[0];

    private final EventList<E> source;

    private final List<InvalidationListener> invalidation_listeners = new ArrayList<>();
    private final List<ListChangeListener> list_listeners = new ArrayList<>();

    private boolean disposed = false;

    /**
     * Creates an adapter for the specified {@link EventList}.
     *
     * @param source the {@link EventList} to adapt to a JavaFX {@link ObservableList}
     */
    public EventObservableList(EventList<E> source) {
        this.source = source;
        source.addListEventListener(this);
    }

    /**
     * Releases the resources consumed by this {@link EventObservableList} so
     * that it may eventually be garbage collected.
     * <p/>
     * <p>
     * A {@link EventObservableList} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is
     * garbage collected. By calling {@link #dispose()}, you allow the
     * {@link EventObservableList} to be garbage collected before its source
     * {@link EventList}. This is necessary for situations where a
     * {@link EventObservableList} is short-lived but its source
     * {@link EventList} is long-lived.
     * <p/>
     * <p>
     * <strong><font color="#FF0000">Warning:</font></strong> It is an error to
     * call any method on a {@link EventObservableList} after it has been
     * disposed.
     */
    public void dispose() {
        if (disposed) {
            return;
        } else {
            disposed = true;
        }

        source.removeListEventListener(this);

        // Clear all listeners
        synchronized (list_listeners) {
            list_listeners.clear();
        }
        synchronized (invalidation_listeners) {
            invalidation_listeners.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(ListChangeListener<? super E> listener) {
        synchronized (list_listeners) {
            list_listeners.add(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(ListChangeListener<? super E> listener) {
        synchronized (list_listeners) {
            list_listeners.remove(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(InvalidationListener listener) {
        synchronized (invalidation_listeners) {
            invalidation_listeners.add(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(InvalidationListener listener) {
        synchronized (invalidation_listeners) {
            invalidation_listeners.remove(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(E... es) {
        source.getReadWriteLock().writeLock().lock();
        try {
            return source.addAll(Arrays.asList(es));
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setAll(E... es) {
        // Locking done within...
        return setAll(Arrays.asList(es));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setAll(Collection<? extends E> es) {
        source.getReadWriteLock().writeLock().lock();
        try {
            clear();
            addAll(es);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(E... es) {
        // Locking done within...
        return removeAll(Arrays.asList(es));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(E... es) {
        // Locking done within...
        return retainAll(Arrays.asList(es));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(int from, int to) {
        source.getReadWriteLock().writeLock().lock();
        try {
            if ((to - from) <= 0) {
                return;
            }
            for (int i = (to - 1); i >= from; i--) {
                remove(i);
            }
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.size();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.isEmpty();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.contains(o);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator() {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.iterator();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.toArray();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T[] a) {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.toArray(a);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(E e) {
        source.getReadWriteLock().writeLock().lock();
        try {
            return source.add(e);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        source.getReadWriteLock().writeLock().lock();
        try {
            return source.remove(o);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.containsAll(c);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        source.getReadWriteLock().writeLock().lock();
        try {
            return source.addAll(c);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        source.getReadWriteLock().writeLock().lock();
        try {
            return source.addAll(index, c);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        source.getReadWriteLock().writeLock().lock();
        try {
            return source.removeAll(c);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        source.getReadWriteLock().writeLock().lock();
        try {
            return source.retainAll(c);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        source.getReadWriteLock().writeLock().lock();
        try {
            source.clear();
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(int index) {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.get(index);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E set(int index, E element) {
        source.getReadWriteLock().writeLock().lock();
        try {
            return source.set(index, element);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int index, E element) {
        source.getReadWriteLock().writeLock().lock();
        try {
            source.add(index, element);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E remove(int index) {
        source.getReadWriteLock().writeLock().lock();
        try {
            return source.remove(index);
        } finally {
            source.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Object o) {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.indexOf(o);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(Object o) {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.lastIndexOf(o);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator() {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.listIterator();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.listIterator(index);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        source.getReadWriteLock().readLock().lock();
        try {
            return source.subList(fromIndex, toIndex);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    // ////////////////////////////////////////////
    // From ListEventListener

    @Override
    public void listChanged(ListEvent<E> source_changes) {
	    synchronized (invalidation_listeners) {
		    for (int i = invalidation_listeners.size() - 1; i >= 0; i--) {
			    invalidation_listeners.get(i).invalidated(this);
		    }
	    }

        synchronized (list_listeners) {
            for (int i = list_listeners.size() - 1; i >= 0; i--) {
                ListEvent<E> changes_to_distribute = source_changes.copy();

                ListChangeListener.Change<E> change;
                if (source_changes.isReordering()) {
                    change = new GLReorderChangeWrapper(this, changes_to_distribute);
                } else {
                    change = new GLChangeWrapper(this, changes_to_distribute);
                }

                // noinspection unchecked
                list_listeners.get(i).onChanged(change);
            }
        }
    }

    class GLReorderChangeWrapper extends ListChangeListener.Change<E> {
        private final int[] reorder_map;
        private boolean processed = false;

        public GLReorderChangeWrapper(ObservableList<E> es, ListEvent<E> changes) {
            super(es);

            // NOTE: logic of GL and JFX reorder maps are flipped. In GL the
            // index is the
            // new index, where it's the old index in JFX.
            int[] gl_reorder_map = changes.getReorderMap();
            reorder_map = new int[gl_reorder_map.length];
            for (int i = 0; i < gl_reorder_map.length; i++) {
                reorder_map[gl_reorder_map[i]] = i;
            }
        }

        @Override
        public boolean next() {
            if (processed) {
                return false;
            } else {
                processed = true;
                return true;
            }
        }

        @Override
        public void reset() {
            processed = false;
        }

        @Override
        public int getFrom() {
            return 0;
        }

        @Override
        public int getTo() {
            return reorder_map.length;
        }

        @Override
        public List<E> getRemoved() {
            return Collections.emptyList();
        }

        @Override
        protected int[] getPermutation() {
            return reorder_map;
        }

        @Override
        public String toString() {
            return "GLReorderChangeWrapper{" + "reorder_map=" + Arrays.toString(reorder_map)
                    + ", processed=" + processed + '}';
        }
    }

    class GLChangeWrapper extends ListChangeListener.Change<E> {
        private final ListEvent<E> changes;

        public GLChangeWrapper(ObservableList<E> es, ListEvent<E> changes) {
            super(es);
            this.changes = changes;
        }

        @Override
        public boolean next() {
            return changes.nextBlock();
        }

        @Override
        public void reset() {
            changes.reset();
        }

        @Override
        public int getFrom() {
            return changes.getBlockStartIndex();
        }

        @Override
        public int getTo() {
            // NOTE: GL is inclusive, JavaFX is exclusive
            return changes.getBlockEndIndex() + 1;
        }

        @Override
        public List<E> getRemoved() {
            return Collections.singletonList(changes.getOldValue());
        }

        // @Override
        // public List<E> getAddedSubList() {
        // return Collections.singletonList( changes.getNewValue() );
        // }

        @Override
        public boolean wasReplaced() {
            return changes.getType() == ListEvent.UPDATE;
        }

        @Override
        public boolean wasRemoved() {
            return changes.getType() == ListEvent.DELETE;
        }

        @Override
        public boolean wasAdded() {
            return changes.getType() == ListEvent.INSERT;
        }

        @Override
        protected int[] getPermutation() {
            return EMPTY_INT_ARRAY;
        }

        @Override
        public String toString() {
            return "GLChangeWrapper{" + "changes=" + changes + " ("
                    + System.identityHashCode(changes) + ")}";
        }
    }
}
