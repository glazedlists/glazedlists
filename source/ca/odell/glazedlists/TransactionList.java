package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class TransactionList<S> extends TransformedList<S,S> {

    private final BasicEventList<S> txSnapshot;

    private Thread txThread;

    private final ListEventListener<S> commitListener = new CommitListener();

    public TransactionList(EventList<S> source) {
        super(source);
        txSnapshot = new BasicEventList<S>(getPublisher(), getReadWriteLock());
    }

    public void begin() {
        if (txThread == Thread.currentThread())
            throw new IllegalStateException("Cannot begin() another transaction before committing or rolling back the previous transaction on Thread:" + Thread.currentThread());

        getReadWriteLock().writeLock().lock();
        txSnapshot.addAll(source);
        txSnapshot.updates.beginEvent(true);
        txThread = Thread.currentThread();
    }

    public void commit() {
        if (txThread != Thread.currentThread())
            throw new IllegalStateException("Cannot commit() a transaction that does not exist on Thread:" + Thread.currentThread());

        txThread = null;
        txSnapshot.addListEventListener(commitListener);
        try {
            txSnapshot.updates.commitEvent();
        } finally {
            txSnapshot.removeListEventListener(commitListener);
            txSnapshot.clear();
            getReadWriteLock().writeLock().unlock();
        }
    }

    public void rollback() {
        if (txThread == Thread.currentThread())
            throw new IllegalStateException("Cannot rollback() a transaction that does not exist on Thread:" + Thread.currentThread());

        try {
            txSnapshot.updates.commitEvent();
        } finally {
            txThread = null;
            txSnapshot.clear();
            getReadWriteLock().writeLock().unlock();
        }
    }

    protected boolean isWritable() {
        return true;
    }

    public void listChanged(ListEvent<S> listChanges) {
        updates.forwardEvent(listChanges);
    }

    private boolean isEventThread() {
        return txThread == Thread.currentThread();
    }

    private final class CommitListener implements ListEventListener<S> {
        public void listChanged(ListEvent<S> listChanges) {
            updates.beginEvent(true);

            final EventList<S> source = listChanges.getSourceList();

            while (listChanges.next()) {
                final int index = listChanges.getIndex();

                switch (listChanges.getType()) {
                    case ListEvent.INSERT: add(index, source.get(index)); break;
                    case ListEvent.DELETE: remove(index); break;
                    case ListEvent.UPDATE: set(index, source.get(index)); break;
                }
            }
            updates.commitEvent();
        }
    }

    //
    // List methods which defer to either {@link #txSnapshot} or the source
    // list depending on whether the calling Thread is currently involved in a
    // transaction at the moment. That is, views of transactions that are only
    // partially complete will always appear to be local to the Thread that
    // started them.
    //

    public void add(int index, S element) {
        if (isEventThread())
            txSnapshot.add(index, element);
        else
            super.add(index, element);
    }

    public boolean add(S element) {
        if (isEventThread())
            return txSnapshot.add(element);
        else
            return super.add(element);
    }

    public boolean addAll(Collection<? extends S> ses) {
        if (isEventThread())
            return txSnapshot.addAll(ses);
        else
            return super.addAll(ses);
    }

    public boolean addAll(int index, Collection<? extends S> ses) {
        if (isEventThread())
            return txSnapshot.addAll(index, ses);
        else
            return super.addAll(index, ses);
    }

    public S remove(int index) {
        if (isEventThread())
            return txSnapshot.remove(index);
        else
            return super.remove(index);
    }

    public boolean remove(Object element) {
        if (isEventThread())
            return txSnapshot.remove(element);
        else
            return super.remove(element);
    }

    public void clear() {
        if (isEventThread())
            txSnapshot.clear();
        else
            super.clear();
    }

    public S set(int index, S element) {
        if (isEventThread())
            return txSnapshot.set(index, element);
        else
            return super.set(index, element);
    }

    public S get(int index) {
        if (isEventThread())
            return txSnapshot.get(index);
        else
            return super.get(index);
    }

    public int size() {
        if (isEventThread())
            return txSnapshot.size();
        else
            return super.size();
    }

    public boolean removeAll(Collection<?> collection) {
        if (isEventThread())
            return txSnapshot.removeAll(collection);
        else
            return super.removeAll(collection);
    }

    public boolean retainAll(Collection<?> collection) {
        if (isEventThread())
            return txSnapshot.retainAll(collection);
        else
            return super.retainAll(collection);
    }

    public boolean isEmpty() {
        if (isEventThread())
            return txSnapshot.isEmpty();
        else
            return super.isEmpty();
    }

    public boolean contains(Object object) {
        if (isEventThread())
            return txSnapshot.contains(object);
        else
            return super.contains(object);
    }

    public Iterator<S> iterator() {
        if (isEventThread())
            return txSnapshot.iterator();
        else
            return super.iterator();
    }

    public Object[] toArray() {
        if (isEventThread())
            return txSnapshot.toArray();
        else
            return super.toArray();
    }

    public <T> T[] toArray(T[] array) {
        if (isEventThread())
            return txSnapshot.toArray(array);
        else
            return super.toArray(array);
    }

    public boolean containsAll(Collection<?> values) {
        if (isEventThread())
            return txSnapshot.containsAll(values);
        else
            return super.containsAll(values);
    }

    public int indexOf(Object object) {
        if (isEventThread())
            return txSnapshot.indexOf(object);
        else
            return super.indexOf(object);
    }

    public int lastIndexOf(Object object) {
        if (isEventThread())
            return txSnapshot.lastIndexOf(object);
        else
            return super.lastIndexOf(object);
    }

    public ListIterator<S> listIterator() {
        if (isEventThread())
            return txSnapshot.listIterator();
        else
            return super.listIterator();
    }

    public ListIterator<S> listIterator(int index) {
        if (isEventThread())
            return txSnapshot.listIterator(index);
        else
            return super.listIterator(index);
    }

    public List<S> subList(int fromIndex, int toIndex) {
        if (isEventThread())
            return txSnapshot.subList(fromIndex, toIndex);
        else
            return super.subList(fromIndex, toIndex);
    }

    public boolean equals(Object object) {
        if (isEventThread())
            return txSnapshot.equals(object);
        else
            return super.equals(object);
    }

    public int hashCode() {
        if (isEventThread())
            return txSnapshot.hashCode();
        else
            return super.hashCode();
    }

    public String toString() {
        if (isEventThread())
            return txSnapshot.toString();
        else
            return super.toString();
    }
}