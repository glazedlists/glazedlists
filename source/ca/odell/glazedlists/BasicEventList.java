/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * An {@link EventList} that wraps any simple {@link List}, such as {@link ArrayList}
 * or {@link LinkedList}.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(1), writes O(1) amortized</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>O(N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class BasicEventList<E> extends AbstractEventList<E> {

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
        super(null);
        this.data = new ArrayList<E>();
        this.readWriteLock = readWriteLock;
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
    public BasicEventList(List<E> list) {
        super(null);
        data = list;
        readWriteLock = LockFactory.DEFAULT.createReadWriteLock();
    }
    
    /** {@inheritDoc} */
    public void add(int index, E element) {
        // create the change event
        updates.beginEvent();
        updates.addInsert(index);
        // do the actual add
        data.add(index, element);
        // fire the event
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    public boolean add(E element) {
        // create the change event
        updates.beginEvent();
        updates.addInsert(size());
        // do the actual add
        boolean result = data.add(element);
        // fire the event
        updates.commitEvent();
        return result;
    }

    /** {@inheritDoc} */
    public boolean addAll(Collection<? extends E> collection) {
        return addAll(size(), collection);
    }

    /** {@inheritDoc} */
    public boolean addAll(int index, Collection<? extends E> collection) {
        // don't do an add of an empty set
        if(collection.size() == 0) return false;

        // create the change event
        updates.beginEvent();
        updates.addInsert(index, index + collection.size() - 1);
        // do the actual add
        boolean result = data.addAll(index, collection);
        // fire the event
        updates.commitEvent();
        return result;
    }

    /** {@inheritDoc} */
    public E remove(int index) {
        // create the change event
        updates.beginEvent();
        updates.addDelete(index);
        // do the actual remove
        E removed = data.remove(index);
        // fire the event
        updates.commitEvent();
        return removed;
    }

    /** {@inheritDoc} */
    public boolean remove(Object element) {
        int index = data.indexOf(element);
        if(index == -1) return false;
        remove(index);
        return true;
    }

    /** {@inheritDoc} */
    public void clear() {
        // don't do a clear on an empty set
        if(size() == 0) return;
        // create the change event
        updates.beginEvent();
        updates.addDelete(0, size() - 1);
        // do the actual clear
        data.clear();
        // fire the event
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    public E set(int index, E element) {
        // create the change event
        updates.beginEvent();
        updates.addUpdate(index);
        // do the actual set
        E previous = data.set(index, element);
        // fire the event
        updates.commitEvent();
        return previous;
    }

    /** {@inheritDoc} */
    public E get(int index) {
        return data.get(index);
    }

    /** {@inheritDoc} */
    public int size() {
        return data.size();
    }

    /** {@inheritDoc} */
    public boolean removeAll(Collection<?> collection) {
        boolean changed = false;
        updates.beginEvent();
        for(Iterator i = collection.iterator(); i.hasNext(); ) {
            Object value = i.next();
            int index = -1;
            while((index = indexOf(value)) != -1) {
                updates.addDelete(index);
                data.remove(index);
                changed = true;
            }
        }
        updates.commitEvent();
        return changed;
    }

    /** {@inheritDoc} */
    public boolean retainAll(Collection<?> collection) {
        boolean changed = false;
        updates.beginEvent();
        int index = 0;
        while(index < data.size()) {
            if(collection.contains(data.get(index))) {
                index++;
            } else {
                updates.addDelete(index);
                data.remove(index);
                changed = true;
            }
        }
        updates.commitEvent();
        return changed;
    }
}