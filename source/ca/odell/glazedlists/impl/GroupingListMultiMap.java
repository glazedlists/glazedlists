/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.*;

/**
 * This multimap implementation sits atop an {@link EventList} and makes it
 * accessible via the convenient {@link Map} interface. It is constructed with
 * a {@link FunctionList.Function} which is used to create the keys of the map.
 * The values of the map are the lists of values from the {@link EventList}
 * which all map to a common key.
 *
 * <p>For example, an {@link EventList} containing
 *
 * <pre>
 * {Cherry, Plum, Cranberry, Pineapple, Banana, Prune}
 * </pre>
 *
 * paired with a Function that returns the first letter of the fruit name
 * produces the multi map:
 *
 * <pre>
 * "B" -> {Banana}
 * "C" -> {Cherry, Cranberry}
 * "P" -> {Plum, Pineapple, Prune}
 * </pre>
 *
 * @author James Lemieux
 */
public class GroupingListMultiMap<K, V> implements DisposableMap<K, List<V>>, ListEventListener<List<V>> {

    /** The raw values of this Map in an {@link EventList}. */
    private final GroupingList<V> groupingList;

    /** The values of this Map in an {@link EventList}. */
    private final FunctionList<List<V>, List<V>> valueList;

    /** The keys of this Map (used to remove entries from the {@link #delegate}) */
    private final List<K> keyList;

    /** The keys of this Map made to look like a Set (it is built lazily in {@link #keySet()}) */
    private Set<K> keySet;

    /** The function which produces keys for this multimap. */
    private final FunctionList.Function<V, ? extends K> keyFunction;

    /** The delegate Map which is kept in synch with {@link #groupingList} changes. */
    private final Map<K, List<V>> delegate;

    /** The set of Map.Entry objects in this Map (it is built lazily in {@link #entrySet()}) */
    private Set<Map.Entry<K, List<V>>> entrySet;

    /**
     * Construct a multimap which maps the keys produced by the
     * <code>keyFunction</code>, to groups of values from <code>source</code>
     * that agree on their keys.
     *
     * @param source the raw data which has not yet been grouped
     * @param keyFunction the function capable of producing the keys of this
     *      {@link Map} from each value
     * @param keyGrouper the comparator that groups together values which
     *      have the same key according to the given <code>keyFunction</code>
     */
    public GroupingListMultiMap(EventList<V> source, FunctionList.Function<V, ? extends K> keyFunction, Comparator<? super K> keyGrouper) {
        if (keyFunction == null)
            throw new IllegalArgumentException("keyFunction may not be null");
        if (keyGrouper == null)
            throw new IllegalArgumentException("keyGrouper may not be null");

        this.keyFunction = keyFunction;

        // construct a GroupingList which groups together the source elements for common keys
        this.groupingList = new GroupingList<V>(source, new FunctionComparator(keyFunction, keyGrouper));

        // wrap each List in the GroupingList in a layer that enforces the keyFunction constraints for writes
        this.valueList = new FunctionList<List<V>, List<V>>(this.groupingList, new ValueListFunction());
        this.valueList.addListEventListener(this);

        // it is important that the keyList is a BasicEventList since we use its ListIterator, which remains
        // consistent with changes to its underlying data (any other Iterator would throw a ConcurrentModificationException)
        this.keyList = new BasicEventList<K>(this.groupingList.size());
        this.delegate = new HashMap<K,List<V>>(this.groupingList.size());

        // initialize both the keyList and the delegate Map
        for (Iterator<List<V>> i = this.valueList.iterator(); i.hasNext();) {
            final List<V> value = i.next();
            final K key = key(value);
            this.keyList.add(key);
            this.delegate.put(key, value);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        valueList.removeListEventListener(this);
        valueList.dispose();
        groupingList.dispose();

        keySet = null;
        entrySet = null;
        keyList.clear();
        delegate.clear();
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return delegate.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public List<V> get(Object key) {
        return delegate.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public List<V> put(K key, List<V> value) {
        checkKeyValueAgreement(key, value);

        final List<V> removed = remove(key);
        groupingList.add(value);

        return removed;
    }

    /** {@inheritDoc} */

    @Override
    public void putAll(Map<? extends K, ? extends List<V>> m) {
        // verify the contents of the given Map and ensure all key/value pairs agree with the keyFunction
        for (Iterator<? extends Entry<? extends K, ? extends List<V>>> i = m.entrySet().iterator(); i.hasNext();) {
            final Entry<? extends K, ? extends List<V>> entry = i.next();
            final K key = entry.getKey();
            final List<V> value = entry.getValue();

            checkKeyValueAgreement(key, value);
        }

        // remove all values currently associated with the keys
        for (Iterator<? extends K> i = m.keySet().iterator(); i.hasNext();)
            remove(i.next());

        // add all new values into this Map
        groupingList.addAll(m.values());
    }

    /**
     * This convenience method ensures that the <code>key</code> matches the
     * key values produced by each of the <code>value</code> objects. If a
     * mismatch is found, an {@link IllegalArgumentException} is thrown.
     *
     * @param key the expected key value of each value object
     * @param value the value objects which should produce the given key when
     *      run through the key function
     */
    private void checkKeyValueAgreement(K key, Collection<? extends V> value) {
        for (Iterator<? extends V> i = value.iterator(); i.hasNext();)
            checkKeyValueAgreement(key, i.next());
    }

    /**
     * This convenience method ensures that the <code>key</code> matches the
     * key value produced for the <code>value</code> object. If a
     * mismatch is found, an {@link IllegalArgumentException} is thrown.
     *
     * @param key the expected key value of each value object
     * @param value the value object which should produce the given key when
     *      run through the key function
     */
    private void checkKeyValueAgreement(K key, V value) {
        final K k = key(value);

        if (!GlazedListsImpl.equal(key, k))
            throw new IllegalArgumentException("The calculated key for the given value (" + k + ") does not match the given key (" + key + ")");
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        groupingList.clear();
    }

    /** {@inheritDoc} */
    @Override
    public List<V> remove(Object key) {
        final int index = keyList.indexOf(key);
        return index == -1 ? null : groupingList.remove(index);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<List<V>> values() {
        return groupingList;
    }

    /** {@inheritDoc} */
    @Override
    public Set<K> keySet() {
        if (keySet == null)
            keySet = new KeySet();

        return keySet;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        if (entrySet == null)
            entrySet = new EntrySet();

        return entrySet;
    }

    /** @inheritDoc */
    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    /** @inheritDoc */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Updates this MultiMap datastructure to reflect changes in the underlying
     * {@link GroupingList}. Specifically, new entries are added to this
     * MultiMap by calculating a key using the key function of this MultiMap.
     *
     * Interestingly, we don't have to handle the UPDATE events here. The
     * entries in the delegate map are silenty updated in place since the List
     * we were given by the GroupingList is simply mutated. INSERTS and
     * DELETES, however, require actual changes to the delegate map, and thus
     * are processed here accordingly.
     *
     * @param listChanges an event describing the changes in the GroupingList
     */
    @Override
    public void listChanged(ListEvent<List<V>> listChanges) {
        while (listChanges.next()) {
            final int changeIndex = listChanges.getIndex();
            final int changeType = listChanges.getType();

            if (changeType == ListEvent.INSERT) {
                final List<V> inserted = listChanges.getSourceList().get(changeIndex);
                final K key = key(inserted);
                keyList.add(changeIndex, key);
                delegate.put(key, inserted);

            } else if (changeType == ListEvent.DELETE) {
                final K deleted = keyList.remove(changeIndex);
                delegate.remove(deleted);
            }
        }
    }

    /**
     * Uses the key function to return the key for a given list of values.
     *
     * @param values a non-empty list of values from the source
     *      {@link GroupingList} which share the same key value
     * @return the shared key which maps to each of the given values
     */
    private K key(List<V> values) {
        return key(values.get(0));
    }

    /**
     * Uses the key function to return the key for a given value.
     *
     * @param value a single value from the source list
     * @return the key which maps to the given value
     */
    private K key(V value) {
        return keyFunction.evaluate(value);
    }

    /**
     * This private {@link Set} implementation represents the {@link Map.Entry}
     * objects within this MultiMap. All mutating methods are implemented to
     * "write through" to the backing {@link EventList} which ensures that both
     * the {@link EventList} and this MultiMap always remain in sync.
     */
    private class EntrySet extends AbstractSet<Entry<K, List<V>>> {
        /** {@inheritDoc} */
        @Override
        public int size() {
            return keyList.size();
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<Entry<K, List<V>>> iterator() {
            return new EntrySetIterator(keyList.listIterator());
        }

        /** {@inheritDoc} */
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;

            final Entry<K, List<V>> e = (Entry<K, List<V>>) o;
            final K key = e.getKey();
            final List<V> value = e.getValue();

            final List<V> mapValue = GroupingListMultiMap.this.get(key);

            return GlazedListsImpl.equal(value, mapValue);
        }

        /** {@inheritDoc} */
        @Override
        public boolean remove(Object o) {
            if (!contains(o)) return false;
            GroupingListMultiMap.this.remove(((Map.Entry) o).getKey());
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public void clear() {
            GroupingListMultiMap.this.clear();
        }
    }

    /**
     * This private {@link Iterator} implementation iterates the {@link Set} of
     * {@link Map.Entry} objects within this MultiMap. All mutating methods are
     * implemented to "write through" to the backing {@link EventList} which
     * ensures that both the {@link EventList} and this MultiMap always remain
     * in sync.
     *
     * <p>Note: This implementation returns a <strong>new</strong>
     * {@link Map.Entry} object each time {@link #next} is called. Identity is
     * not preserved.
     */
    private class EntrySetIterator implements Iterator<Entry<K, List<V>>> {

        /** The delegate Iterator walks a List of keys for the MultiMap. */
        private final ListIterator<K> keyIter;

        /**
         * Construct a new EntrySetIterator using a delegate Iterator that
         * walks the keys of the MultMap.
         *
         * @param keyIter a {@link ListIterator} that walks the keys of the MultiMap
         */
        EntrySetIterator(ListIterator<K> keyIter) {
            this.keyIter = keyIter;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return keyIter.hasNext();
        }

        /**
         * Returns a new {@link Map.Entry} each time this method is called.
         */
        @Override
        public Entry<K, List<V>> next() {
            final K key = keyIter.next();
            return new MultiMapEntry(key, get(key));
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            final int index = keyIter.previousIndex();
            if (index == -1) throw new IllegalStateException("Cannot remove() without a prior call to next()");
            groupingList.remove(index);
        }
    }

    /**
     * This is an implementation of the {@link Map.Entry} interface that is
     * appropriate for this MultiMap. All mutating methods are implemented to
     * "write through" to the backing {@link EventList} which ensures that
     * both the {@link EventList} and this MultiMap always remain in sync.
     */
    private class MultiMapEntry implements Map.Entry<K, List<V>> {

        /** The MultiMap key for this Entry object. */
        private final K key;

        /** The MultiMap value for this Entry object. */
        private List<V> value;

        /**
         * Constructs a new MultiMapEntry with the given <code>key</code> and
         * initial <code>value</code>.
         */
        MultiMapEntry(K key, List<V> value) {
            if (value == null) throw new IllegalArgumentException("value cannot be null");

            this.value = value;
            this.key = key;
        }

        /** {@inheritDoc} */
        @Override
        public K getKey() {
            return key;
        }

        /** {@inheritDoc} */
        @Override
        public List<V> getValue() {
            return value;
        }

        /**
         * Since {@link GroupingList} is particular about the identity of the
         * Lists it contains, and this MultiMap uses those <strong>same</strong>
         * Lists as its values, this method is implemented to simply
         * <strong>replace</strong> the contents of the List with the contents
         * of the given <code>newValue</code>. So, the data is changed, but the
         * identity of the List in the MultiMap and {@link GroupingList} is not.
         *
         * @param newValue the new values use as elements of the value List
         * @return the old value List of this Entry
         */
        @Override
        public List<V> setValue(List<V> newValue) {
            // ensure all of the newValue elements agree with the key of this Entry
            checkKeyValueAgreement(getKey(), newValue);

            // record the old value List elements (to return)
            final List<V> oldValue = new ArrayList<V>(value);

            // replace all elements within the List
            //
            // (GroupingList actually removes Lists the moment they become *empty*
            // so we first insert the new values rather than removing the old values
            // to avoid the temporary existence of an empty List)
            value.addAll(newValue);
            value.removeAll(oldValue);

            return oldValue;
        }

        /**
         * Two MultiMapEntry entry objects are equal iff their keys and values
         * are equal.
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry) o;

            final boolean keysEqual = GlazedListsImpl.equal(getKey(), e.getKey());
            return keysEqual && GlazedListsImpl.equal(getValue(), e.getValue());
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^ value.hashCode();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    /**
     * This private {@link Set} implementation represents the keys within this
     * MultiMap. All mutating methods are implemented to "write through" to the
     * backing {@link EventList} which ensures that both the {@link EventList}
     * and this MultiMap always remain in sync.
     */
    private class KeySet extends AbstractSet<K> {
        /** {@inheritDoc} */
        @Override
        public int size() {
            return keyList.size();
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<K> iterator() {
            return new KeySetIterator(keyList.listIterator());
        }

        /** {@inheritDoc} */
        @Override
        public boolean contains(Object o) {
            return GroupingListMultiMap.this.containsKey(o);
        }

        /** {@inheritDoc} */
        @Override
        public boolean remove(Object o) {
            return GroupingListMultiMap.this.remove(o) != null;
        }

        /** {@inheritDoc} */
        @Override
        public void clear() {
            GroupingListMultiMap.this.clear();
        }
    }

    /**
     * This private {@link Iterator} implementation iterates the {@link Set} of
     * keys within this MultiMap. All mutating methods are implemented to
     * "write through" to the backing {@link EventList} which ensures that both
     * the {@link EventList} and this MultiMap always remain in sync.
     */
    private class KeySetIterator implements Iterator<K> {

        /** The delegate Iterator walks a List of keys for the MultiMap. */
        private final ListIterator<K> keyIter;

        /**
         * Construct a new KeySetIterator using a delegate Iterator that walks
         * the list of unique keys of the MultMap.
         *
         * @param keyIter a {@link ListIterator} that walks the keys of the MultiMap
         */
        KeySetIterator(ListIterator<K> keyIter) {
            this.keyIter = keyIter;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return keyIter.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public K next() {
            return keyIter.next();
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            final int index = keyIter.previousIndex();
            if (index == -1) throw new IllegalStateException("Cannot remove() without a prior call to next()");
            groupingList.remove(index);
        }
    }

    /**
     * This Comparator first runs each value through a
     * {@link FunctionList.Function} to produce key objects which are then
     * compared to determine a relative ordering using the given delegate
     * {@link Comparator}.
     */
    private final class FunctionComparator implements Comparator<V> {

        /** A Comparator that orders {@link Comparable} objects. */
        private final Comparator<? super K> delegate;

        /** A function that extracts {@link Comparable} values from given objects. */
        private final FunctionList.Function<V, ? extends K> function;

        /**
         * Construct a new FunctionComparator that uses the given
         * <code>function</code> to extract {@link Comparable} values from
         * given objects.
         */
        FunctionComparator(FunctionList.Function<V, ? extends K> function, Comparator<? super K> delegate) {
            this.function = function;
            this.delegate = delegate;
        }

        /** {@inheritDoc} */
        @Override
        public int compare(V o1, V o2) {
            final K k1 = function.evaluate(o1);
            final K k2 = function.evaluate(o2);
            return delegate.compare(k1, k2);
        }
    }

    /**
     * This Function wraps each List produced by the GroupingList with a layer
     * that ensures that mutations to it don't violate the keyFunction
     * constraints required by this MultiMap.
     */
    private final class ValueListFunction implements FunctionList.Function<List<V>, List<V>> {
        @Override
        public List<V> evaluate(List<V> sourceValue) {
            return new ValueList(sourceValue);
        }
    }

    /**
     * This class wraps each element of the GroupingList with a layer of
     * checking to ensure that mutations to it don't violate the keyFunction
     * constraints required by this MultiMap.
     */
    private final class ValueList implements List<V> {

        /** The List that actually implements the List operations */
        private final List<V> delegate;

        /** The key that all values in this List must share. */
        private final K key;

        public ValueList(List<V> delegate) {
            this.delegate = delegate;
            this.key = key(delegate.get(0));
        }

        @Override
        public int size() { return delegate.size(); }
        @Override
        public boolean isEmpty() { return delegate.isEmpty(); }
        @Override
        public boolean contains(Object o) { return delegate.contains(o); }
        @Override
        public Iterator<V> iterator() { return delegate.iterator(); }
        @Override
        public Object[] toArray() { return delegate.toArray(); }
        @Override
        public <T>T[] toArray(T[] a) { return delegate.toArray(a); }

        @Override
        public boolean add(V o) {
            checkKeyValueAgreement(key, o);
            return delegate.add(o);
        }

        @Override
        public boolean addAll(Collection<? extends V> c) {
            checkKeyValueAgreement(key, c);
            return delegate.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends V> c) {
            checkKeyValueAgreement(key, c);
            return delegate.addAll(index, c);
        }

        @Override
        public void add(int index, V element) {
            checkKeyValueAgreement(key, element);
            delegate.add(index, element);
        }

        @Override
        public V set(int index, V element) {
            checkKeyValueAgreement(key, element);
            return delegate.set(index, element);
        }

        @Override
        public List<V> subList(int fromIndex, int toIndex) {
            return new ValueList(delegate.subList(fromIndex, toIndex));
        }

        @Override
        public ListIterator<V> listIterator() {
            return new ValueListIterator(delegate.listIterator());
        }

        @Override
        public ListIterator<V> listIterator(int index) {
            return new ValueListIterator(delegate.listIterator(index));
        }

        @Override
        public boolean remove(Object o) { return delegate.remove(o); }
        @Override
        public boolean containsAll(Collection<?> c) { return delegate.containsAll(c); }
        @Override
        public boolean removeAll(Collection<?> c) { return delegate.removeAll(c); }
        @Override
        public boolean retainAll(Collection<?> c) { return delegate.retainAll(c); }
        @Override
        public void clear() { delegate.clear(); }
        @Override
        public boolean equals(Object o) { return delegate.equals(o); }
        @Override
        public int hashCode() { return delegate.hashCode(); }
        @Override
        public V get(int index) { return delegate.get(index); }
        @Override
        public V remove(int index) { return delegate.remove(index); }
        @Override
        public int indexOf(Object o) { return delegate.indexOf(o); }
        @Override
        public int lastIndexOf(Object o) { return delegate.lastIndexOf(o); }
        @Override
        public String toString() { return delegate.toString(); }

        /**
         * This class wraps the normal ListIterator returned by the GroupingList
         * elements with extra checking to ensure mutations to it don't violate
         * the keyFunction constraints required by this MultiMap.
         */
        private final class ValueListIterator implements ListIterator<V> {
            private final ListIterator<V> delegate;

            public ValueListIterator(ListIterator<V> delegate) {
                this.delegate = delegate;
            }

            @Override
            public void set(V o) {
                checkKeyValueAgreement(key, o);
                delegate.set(o);
            }

            @Override
            public void add(V o) {
                checkKeyValueAgreement(key, o);
                delegate.add(o);
            }

            @Override
            public boolean hasNext() { return delegate.hasNext(); }
            @Override
            public V next() { return delegate.next(); }
            @Override
            public boolean hasPrevious() { return delegate.hasPrevious(); }
            @Override
            public V previous() { return delegate.previous(); }
            @Override
            public int nextIndex() { return delegate.nextIndex(); }
            @Override
            public void remove() { delegate.remove(); }
            @Override
            public int previousIndex() { return delegate.previousIndex(); }
        }
    }
}