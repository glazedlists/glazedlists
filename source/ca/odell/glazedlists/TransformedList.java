/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// for access to iterators and the Collection interface
import java.util.*;

/**
 * A convenience class for {@link EventList}s that decorate other {@link EventList}s.
 * Extending classes transform their source {@link EventList} by modifying the
 * order, visibility and value of its elements.
 *
 * <p>Extending classes may implement the method {@link #getSourceIndex(int)} to
 * translate between indices of this and indices of the source.
 *
 * <p>Extending classes may implement the method {@link #isWritable()} to make the
 * source writable via this API.
 *
 * <p>Extending classes must explicitly call {@link #addListEventListener(ListEventListener)}
 * to receive for change notification from the source {@link EventList}.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class TransformedList extends AbstractEventList implements ListEventListener {

    /** the event list to transform */
    protected EventList source;

    /**
     * Creates a {@link TransformedList} to transform the specified {@link EventList}.
     *
     * @param source the {@link EventList} to transform..
     */
    protected TransformedList(EventList source) {
        super(source.getPublisher());
        this.source = source;
        readWriteLock = source.getReadWriteLock();
    }

    /**
     * Gets the index in the source {@link EventList} that corresponds to the
     * specified index. More formally, returns the index such that
     * <br><code>this.get(i) == source.get(getSourceIndex(i))</code> for all
     * legal values of <code>i</code>.
     */
    protected int getSourceIndex(int mutationIndex) {
        return mutationIndex;
    }

    /**
     * Gets whether the source {@link EventList} is writable via this API.
     * 
     * <p>Extending classes must override this method in order to make themselves
     * writable.
     */
    protected boolean isWritable() {
        return false;
    }

    /**
     * Respond to a change in the source {@link EventList} by updating the internal
     * state of this {@link TransformedList}. If the state of this {@link TransformedList}
     * changes as a consequence, all interested {@link ListEventListener}s will
     * be notified in turn.
     */
    public abstract void listChanged(ListEvent listChanges);

    /** {@inheritDoc} */
    public boolean add(Object value) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            return source.add(value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public void add(int index, Object value) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            if(index < 0 || index > size()) throw new IndexOutOfBoundsException("Cannot add at " + index + " on list of size " + size());
            int sourceIndex = 0;
            if(index < size()) sourceIndex = getSourceIndex(index);
            else sourceIndex = source.size();
            source.add(sourceIndex, value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean addAll(int index, Collection values) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            if(index < 0 || index > size()) throw new IndexOutOfBoundsException("Cannot add at " + index + " on list of size " + size());
            int sourceIndex = 0;
            if(index < size()) sourceIndex = getSourceIndex(index);
            else sourceIndex = source.size();
            return source.addAll(sourceIndex, values);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean addAll(Collection values) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            return source.addAll(values);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public void clear() {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            // nest changes and let the other methods compose the event
            updates.beginEvent(true);
            while(!isEmpty()) {
                remove(0);
            }
            updates.commitEvent();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public Object get(int index) {
        if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot get at " + index + " on list of size " + size());
        return source.get(getSourceIndex(index));
    }

    /** {@inheritDoc} */
    public Object remove(int index) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot remove at " + index + " on list of size " + size());
            return source.remove(getSourceIndex(index));
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean remove(Object toRemove) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            int index = indexOf(toRemove);
            if(index == -1) return false;
            source.remove(getSourceIndex(index));
            return true;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean removeAll(Collection values) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            // nest changes and let the other methods compose the event
            updates.beginEvent(true);
            boolean overallChanged = false;
            for(Iterator i = values.iterator(); i.hasNext(); ) {
                boolean removeChanged = remove(i.next());
                if(removeChanged) overallChanged = true;
            }
            updates.commitEvent();
            return overallChanged;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public boolean retainAll(Collection values) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            // nest changes and let the other methods compose the event
            updates.beginEvent(true);
            boolean changed = false;
            for(int i = 0; i < size(); ) {
                if(!values.contains(get(i))) {
                    remove(i);
                    changed = true;
                } else {
                    i++;
                }
            }
            updates.commitEvent();
            return changed;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public Object set(int index, Object value) {
        getReadWriteLock().writeLock().lock();
        try {
            if(!isWritable()) throw new IllegalStateException("List cannot be modified in the current state");
            if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot set at " + index + " on list of size " + size());
            return source.set(getSourceIndex(index), value);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** {@inheritDoc} */
    public int size() {
        return source.size();
    }

    /**
     * Releases the resources consumed by this {@link TransformedList} so that it
     * may eventually be garbage collected.
     *
     * <p>A {@link TransformedList} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link TransformedList}
     * to be garbage collected before its source {@link EventList}. This is 
     * necessary for situations where a {@link TransformedList} is short-lived but
     * its source {@link EventList} is long-lived.
     * 
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link TransformedList} after it has been disposed.
     */
    public void dispose() {
        source.removeListEventListener(this);
        source = null;
        readWriteLock = null;
    }
}
