/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;
import ca.odell.glazedlists.util.concurrent.SerializedReadWriteLock;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;

/**
 * An {@link EventList} that wraps any simple {@link List}, such as {@link ArrayList}
 * or {@link LinkedList}.
 *
 * <p>Unlike most {@link EventList}s, this class is {@link Serializable}. When
 * {@link BasicEventList} is serialized, all of its elements are serialized
 * <i>and</i> all of its listeners that implement {@link Serializable}. Upon
 * deserialization, the new copy uses a different {@link #getReadWriteLock() lock}
 * than its source {@link BasicEventList}.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(1), writes O(1) amortized</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>O(N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class BasicEventList<E> extends AbstractEventList<E> implements Serializable, RandomAccess {

    /** For versioning as a {@link Serializable} */
    private static final long serialVersionUID = 4883958173323072345L;

    /** the underlying data list */
    private List<E> data;

    /**
     * Creates a {@link BasicEventList}.
     */
    public BasicEventList() {
        this(LockFactory.DEFAULT.createReadWriteLock());
    }

    /**
     * Creates a {@link BasicEventList} that uses the specified {@link ReadWriteLock}
     * for concurrent access.
     */
    public BasicEventList(ReadWriteLock readWriteLock) {
        this(null, readWriteLock);
    }

    /**
     * Creates an empty {@link BasicEventList} with the given
     * <code>initialCapacity</code>.
     */
    public BasicEventList(int initalCapacity) {
        this(initalCapacity, null, LockFactory.DEFAULT.createReadWriteLock());
    }

    /**
     * Creates a {@link BasicEventList} using the specified
     * {@link ListEventPublisher} and {@link ReadWriteLock}.
     *
     * @since 2006-June-12
     */
    public BasicEventList(ListEventPublisher publisher, ReadWriteLock readWriteLock) {
        this(10, publisher, readWriteLock);
    }

    /**
     * Creates a {@link BasicEventList} using the specified initial capacity,
     * {@link ListEventPublisher} and {@link ReadWriteLock}.
     *
     * @since 2007-April-19
     */
    public BasicEventList(int initialCapacity, ListEventPublisher publisher, ReadWriteLock readWriteLock) {
        super(publisher);
        this.data = new ArrayList<E>(initialCapacity);
        this.readWriteLock = (readWriteLock == null) ? LockFactory.DEFAULT.createReadWriteLock() : readWriteLock;
    }

    /**
     * Creates a {@link BasicEventList} that uses the specified {@link List} as
     * the underlying implementation.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> all editing to
     * the specified {@link List} <strong>must</strong> be done through via this
     * {@link BasicEventList} interface. Otherwise this {@link BasicEventList} will
     * become out of sync and operations will fail.
     *
     * @deprecated As of 2005/03/06, this constructor has been declared unsafe
     *     because the source list is exposed. This allows it to be modified without
     *     the required events being fired. This constructor has been replaced by
     *     the factory method {@link GlazedLists#eventList(Collection)}.
     */
    @Deprecated
    public BasicEventList(List<E> list) {
        super(null);
        this.data = list;
        this.readWriteLock = LockFactory.DEFAULT.createReadWriteLock();
    }

    /** {@inheritDoc} */
    @Override
    public void add(int index, E element) {
        // create the change event
        updates.beginEvent();
        updates.elementInserted(index, element);
        // do the actual add
        data.add(index, element);
        // fire the event
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(E element) {
        // create the change event
        updates.beginEvent();
        updates.elementInserted(size(), element);
        // do the actual add
        boolean result = data.add(element);
        // fire the event
        updates.commitEvent();
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return addAll(size(), collection);
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        // don't do an add of an empty set
        if(collection.size() == 0) return false;

        // create the change event
        updates.beginEvent();
        for(Iterator<? extends E> i = collection.iterator(); i.hasNext(); ) {
            E value = i.next();
            updates.elementInserted(index, value);
            data.add(index, value);
            index++;
        }
        // fire the event
        updates.commitEvent();
        return !collection.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public E remove(int index) {
        // create the change event
        updates.beginEvent();
        // do the actual remove
        E removed = data.remove(index);
        // fire the event
        updates.elementDeleted(index, removed);
        updates.commitEvent();
        return removed;
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(Object element) {
        int index = data.indexOf(element);
        if(index == -1) return false;
        remove(index);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        // don't do a clear on an empty set
        if(isEmpty()) return;
        // create the change event
        updates.beginEvent();
        for(int i = 0, size = size(); i < size; i++) {
            updates.elementDeleted(0, get(i));
        }
        // do the actual clear
        data.clear();
        // fire the event
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    @Override
    public E set(int index, E element) {
        // create the change event
        updates.beginEvent();
        // do the actual set
        E previous = data.set(index, element);
        // fire the event
        updates.elementUpdated(index, previous);
        updates.commitEvent();
        return previous;
    }

    /** {@inheritDoc} */
    @Override
    public E get(int index) {
        return data.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return data.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean changed = false;
        updates.beginEvent();
        for(Iterator i = collection.iterator(); i.hasNext(); ) {
            Object value = i.next();
            int index = -1;
            while((index = indexOf(value)) != -1) {
                E removed = data.remove(index);
                updates.elementDeleted(index, removed);
                changed = true;
            }
        }
        updates.commitEvent();
        return changed;
    }

    /** {@inheritDoc} */
    @Override
    public boolean retainAll(Collection<?> collection) {
        boolean changed = false;
        updates.beginEvent();
        int index = 0;
        while(index < data.size()) {
            if(collection.contains(data.get(index))) {
                index++;
            } else {
                E removed = data.remove(index);
                updates.elementDeleted(index, removed);
                changed = true;
            }
        }
        updates.commitEvent();
        return changed;
    }

    /**
     * This method does nothing. It is not necessary to dispose a BasicEventList.
     */
    @Override
    public void dispose() { }

    /**
     * Although {@link EventList}s are not in general, {@link BasicEventList} is
     * {@link Serializable}. All of the {@link ListEventListener}s that are themselves
     * {@link Serializable} will be serialized, but others will not. Note that there
     * is <strong>no</strong> easy way to access the {@link ListEventListener}s of
     * an {@link EventList}, particularly after it has been serialized.
     *
     * <p>As of October 3, 2005, this is the wire format of serialized
     * {@link BasicEventList}s:
     * <li>An <code>Object[]</code> containing each of the list's elements
     * <li>A <code>ListEventListener[]</code> containing <strong>only</strong> the
     *     listeners that themselves implement {@link Serializable}. Those that
     *     do not will not be serialized. Note that {@link TransformedList}s
     *     such as {@link FilterList} are not {@link Serializable} and will not
     *     be serialized.
     *
     * <p>As of March 4, 2007, the wire format was extended to include:
     * <li>the ListEventPublisher
     * <li>the ReadWriteLock represented as a {@link SerializedReadWriteLock}
     * <p>The motivation for this is documented <a
     * href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=398">here</a>.
     * Serialization streams with the old format are still readable. Serialization streams with
     * the new format are not downwards-compatible.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // 1. The elements to write
        E[] elements = (E[])data.toArray(new Object[data.size()]);

        // 2. The Listeners to write
        List<ListEventListener<E>> serializableListeners = new ArrayList<ListEventListener<E>>(1);
        for(Iterator<ListEventListener<E>> i = updates.getListEventListeners().iterator(); i.hasNext(); ) {
            ListEventListener<E> listener = i.next();
            if(!(listener instanceof Serializable)) continue;
            serializableListeners.add(listener);
        }
        ListEventListener[] listeners = serializableListeners.toArray(new ListEventListener[serializableListeners.size()]);

        // 3. Write the elements, listeners, publisher and lock
        out.writeObject(elements);
        out.writeObject(listeners);
        out.writeObject(getPublisher());
        out.writeObject(getReadWriteLock());
    }

    /**
     * Peer method to {@link #writeObject(ObjectOutputStream)}. Note that this
     * is functionally equivalent to a constructor and should validate that
     * everything is in place including locks, etc.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // 1. Read in the elements
        final E[] elements = (E[]) in.readObject();
        // 2. Read in the listeners
        final ListEventListener<E>[] listeners = (ListEventListener<E>[]) in.readObject();

        // 3. Try to read the ListEventPublisher and ReadWriteLock according to the new wire format
        try {
            this.publisher = (ListEventPublisher) in.readObject();
            this.updates = new ListEventAssembler<E>(this, publisher);
            this.readWriteLock = (ReadWriteLock) in.readObject();
        } catch (OptionalDataException e) {
            if (e.eof)
                // reading old serialization stream without publisher and lock
                this.readWriteLock = LockFactory.DEFAULT.createReadWriteLock();
            else throw e;
        }
        // 4. Populate the EventList data
        this.data = new ArrayList<E>(elements.length);
        this.data.addAll(Arrays.asList(elements));

        // 5. Populate the listeners
        for(int i = 0; i < listeners.length; i++) {
            this.updates.addListEventListener(listeners[i]);
        }
    }
}