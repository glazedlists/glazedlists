/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.*;

/**
 * This map implementation sits atop an {@link EventList} and makes it
 * accessible via the convenient {@link Map} interface. It is constructed with
 * a {@link FunctionList.Function} which is used to create the keys of the map.
 * The values of the map are the lists of values from the {@link EventList}.
 *
 * <p>For example, an {@link EventList} containing
 *
 * <pre>
 * {Cherry, Orange, Apple, Pineapple, Banana}
 * </pre>
 *
 * paired with a Function that returns the first letter of the fruit name
 * produces the map:
 *
 * <pre>
 * "C" -> "Cherry"
 * "O" -> "Orange"
 * "A" -> "Apple"
 * "P" -> "Pinapple"
 * "B" -> "Banana"
 * </pre>
 *
 * Note: all values <strong>MUST</strong> map to unique keys in order to use
 * this class. If that constraint is violated at any time, an
 * {@link IllegalStateException} will be thrown to indicate the violation to
 * the programmer.
 *
 * @author James Lemieux
 */
public class FunctionListMap<K, V> implements Map<K, V>, ListEventListener<V> {

    /** The keys of this Map (used to remove entries from the {@link #delegate}) */
    private List<K> keyList;

    /** The keyList of this Map made to look like a Set (it is build lazily in {@link #keySet()}) */
    private Set<K> keySet;

    /** The values of this Map in an {@link EventList}. */
    private final EventList<V> valueList;

    /** The set of Map.Entry objects in this Map (it is build lazily in {@link #entrySet()}) */
    private Set<Map.Entry<K, V>> entrySet;

    /** The function which produces keyList for this multimap. */
    private final FunctionList.Function<V, K> keyFunction;

    /** The delegate Map which is kept in synch with changes. */
    private final Map<K, V> delegate;

    /**
     * Construct a map which maps the keys produced by the
     * <code>keyFunction</code>, to corresponding entries from the
     * <code>source</code>.
     *
     * @param source the raw data which has not yet been grouped
     * @param keyFunction the function capable of producing the key of this
     *      {@link Map} for each value
     */
    public FunctionListMap(EventList<V> source, FunctionList.Function<V, K> keyFunction) {
        if (keyFunction == null)
            throw new IllegalArgumentException("keyFunction may not be null");

        // the source is the list of values
        this.valueList = source;
        this.valueList.addListEventListener(this);
        this.keyFunction = keyFunction;

        // it is important that the keyList is a BasicEventList since we use its ListIterator, which remains
        // consistent with changes to its underlying data (any other Iterator would throw a ConcurrentModificationException)
        this.keyList = new BasicEventList<K>(source.size());
        this.delegate = new HashMap<K, V>(source.size());

        // populate the keyList and the delegate Map
        for (int i = 0, n = source.size(); i < n; i++)
            elementAdded(i);
    }

    /** @inheritDoc */
    public int size() {
        return delegate.size();
    }

    /** @inheritDoc */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /** @inheritDoc */
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    /** @inheritDoc */
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /** @inheritDoc */
    public V get(Object key) {
        return delegate.get(key);
    }

    /** @inheritDoc */
    public V put(K key, V value) {
        checkKeyValueAgreement(key, value);

        final V removed = remove(key);
        valueList.add(value);

        return removed;
    }

    /** @inheritDoc */
    public void putAll(Map<? extends K, ? extends V> m) {
        // verify the contents of the given Map and ensure all key/value pairs agree with the keyFunction
        for (Iterator<? extends Entry<? extends K, ? extends V>> i = m.entrySet().iterator(); i.hasNext();) {
            final Entry<? extends K, ? extends V> entry = i.next();
            checkKeyValueAgreement(entry.getKey(), entry.getValue());
        }

        // remove all valueList currently associated with the keyList
        for (Iterator<? extends K> i = m.keySet().iterator(); i.hasNext();)
            remove(i.next());

        // add all new valueList into this Map
        valueList.addAll(m.values());
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

    /** @inheritDoc */
    public void clear() {
        valueList.clear();
    }

    /** @inheritDoc */
    public V remove(Object key) {
        if (!containsKey(key))
            return null;

        final V value = get(key);
        valueList.remove(value);
        return value;
    }

    /** @inheritDoc */
    public Collection<V> values() {
        return valueList;
    }

    /** @inheritDoc */
    public Set<K> keySet() {
        if (this.keySet == null)
            this.keySet = new KeySet();

        return this.keySet;
    }

    /** @inheritDoc */
    public Set<Entry<K, V>> entrySet() {
        if (this.entrySet == null)
            this.entrySet = new EntrySet();

        return this.entrySet;
    }

    /** @inheritDoc */
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    /** @inheritDoc */
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Updates this Map datastructure to reflect changes in the underlying
     * {@link EventList}. Specifically, new entries are added and existing
     * entries are updated in this Map by calculating a key using the key
     * function of this Map.
     *
     * The algorithm in this method operates in 2 passes. The reason for this
     * is that {@link #putInDelegate} contains a sanity check that ensures we
     * never enter an illegal state for the map (where a single key maps to two
     * distinct values). But, complex but valid <code>listChanges</code> may
     * temporarily break that invariant only to rectify the state at a later
     * index. (e.g. insert a duplicate value at i and then delete the original
     * value at i+1)
     *
     * By performing all remove operations first in pass 1, we preserve the
     * ability to check the invariant in pass 2 when additions are processed.
     * Thus, FunctionListMap remains proactive in locating values which break
     * the invariant.
     *
     * @param listChanges an event describing the changes in the FunctionList
     */
    public void listChanged(ListEvent<V> listChanges) {
        int offset = 0;

        // pass 1: do all remove work (this includes deletes and the front half of updates)
        while (listChanges.next()) {
            switch (listChanges.getType()) {
                case ListEvent.DELETE: elementRemoved(listChanges.getIndex() + offset); break;
                case ListEvent.UPDATE: elementRemoved(listChanges.getIndex() + offset); offset--; break;
                case ListEvent.INSERT: offset--; break;
            }
        }

        listChanges.reset();

        // pass 2: do all add work (this includes inserts and the back half of updates)
        while (listChanges.next()) {
            switch (listChanges.getType()) {
                case ListEvent.UPDATE: elementAdded(listChanges.getIndex()); break;
                case ListEvent.INSERT: elementAdded(listChanges.getIndex()); break;
            }
        }
    }

    /**
     * Updates the internal data structures to reflect the addition of a new
     * element at the given <code>index</code>.
     */
    private void elementAdded(int index) {
        final V value = valueList.get(index);
        final K key = key(value);

        keyList.add(index, key);
        putInDelegate(key, value);
    }

    /**
     * Updates the internal data structures to reflect the removal of an
     * element at the given <code>index</code>.
     */
    private void elementRemoved(int index) {
        final K key = keyList.remove(index);
        delegate.remove(key);
    }

    /**
     * This method puts an entry into the delegate Map after first verifying
     * that the delegate Map does not contain an entry for the given
     * <code>key</code>.
     */
    private void putInDelegate(K key, V value) {
        if (delegate.containsKey(key))
            throw new IllegalStateException("Detected duplicate key->value mapping: attempted to put '" + key + "' -> '" + value + "' in the map, but found '" + key + "' -> '" + delegate.get(key) + "' already existed.");

        delegate.put(key, value);
    }

    /**
     * Uses the key function to return the key for a given value.
     *
     * @param value a single value from the valueList list
     * @return the key which maps to the given value
     */
    private K key(V value) {
        return keyFunction.evaluate(value);
    }

    /**
     * This private {@link Set} implementation represents the {@link Map.Entry}
     * objects within this Map. All mutating methods are implemented to
     * "write through" to the backing {@link EventList} which ensures that both
     * the {@link EventList} and this Map always remain in sync.
     */
    private class EntrySet extends AbstractSet<Entry<K, V>> {
        /** {@inheritDoc} */
        public int size() {
            return keyList.size();
        }

        /** {@inheritDoc} */
        public Iterator<Entry<K, V>> iterator() {
            return new EntrySetIterator(keyList.listIterator());
        }

        /** {@inheritDoc} */
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;

            final Entry<K, V> e = (Entry<K, V>) o;
            final K key = e.getKey();
            final V value = e.getValue();

            final V mapValue = FunctionListMap.this.get(key);

            return GlazedListsImpl.equal(value, mapValue);
        }

        /** {@inheritDoc} */
        public boolean remove(Object o) {
            if (!contains(o)) return false;
            FunctionListMap.this.remove(((Map.Entry) o).getKey());
            return true;
        }

        /** {@inheritDoc} */
        public void clear() {
            FunctionListMap.this.clear();
        }
    }

    /**
     * This private {@link Iterator} implementation iterates the {@link Set} of
     * {@link Map.Entry} objects within this Map. All mutating methods are
     * implemented to "write through" to the backing {@link EventList} which
     * ensures that both the {@link EventList} and this Map always remain
     * in sync.
     *
     * <p>Note: This implementation returns a <strong>new</strong>
     * {@link Map.Entry} object each time {@link #next} is called. Identity is
     * not preserved.
     */
    private class EntrySetIterator implements Iterator<Entry<K, V>> {

        /** The delegate Iterator walks a List of keys for the Map. */
        private final ListIterator<K> keyIter;

        /**
         * Construct a new EntrySetIterator using a delegate Iterator that
         * walks the keys of the MultMap.
         *
         * @param keyIter a {@link ListIterator} that walks the keys of the Map
         */
        EntrySetIterator(ListIterator<K> keyIter) {
            this.keyIter = keyIter;
        }

        /** {@inheritDoc} */
        public boolean hasNext() {
            return keyIter.hasNext();
        }

        /**
         * Returns a new {@link Map.Entry} each time this method is called.
         */
        public Entry<K, V> next() {
            final K key = keyIter.next();
            return new MapEntry(key, get(key));
        }

        /** {@inheritDoc} */
        public void remove() {
            final int index = keyIter.previousIndex();
            if (index == -1) throw new IllegalStateException("Cannot remove() without a prior call to next()");
            valueList.remove(index);
        }
    }

    /**
     * This is an implementation of the {@link Map.Entry} interface that is
     * appropriate for this Map. All mutating methods are implemented to
     * "write through" to the backing {@link EventList} which ensures that
     * both the {@link EventList} and this Map always remain in sync.
     */
    private class MapEntry implements Map.Entry<K, V> {

        /** The Map key for this Entry object. */
        private final K key;

        /** The Map value for this Entry object. */
        private V value;

        /**
         * Constructs a new MapEntry with the given <code>key</code> and
         * initial <code>value</code>.
         */
        MapEntry(K key, V value) {
            if (value == null) throw new IllegalArgumentException("value cannot be null");

            this.value = value;
            this.key = key;
        }

        /** {@inheritDoc} */
        public K getKey() {
            return key;
        }

        /** {@inheritDoc} */
        public V getValue() {
            return value;
        }

        /** {@inheritDoc} */
        public V setValue(V newValue) {
            // ensure the newValue element agrees with the key of this Entry
            checkKeyValueAgreement(key, newValue);

            this.value = newValue;

            return FunctionListMap.this.put(key, newValue);
        }

        /**
         * Two MapEntry entry objects are equal iff their keys and values
         * are equal.
         */
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry) o;

            final boolean keysEqual = GlazedListsImpl.equal(getKey(), e.getKey());
            return keysEqual && GlazedListsImpl.equal(getValue(), e.getValue());
        }

        /** {@inheritDoc} */
        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^ value.hashCode();
        }

        /** {@inheritDoc} */
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    /**
     * This private {@link Set} implementation represents the keyList within this
     * Map. All mutating methods are implemented to "write through" to the
     * backing {@link EventList} which ensures that both the {@link EventList}
     * and this Map always remain in sync.
     */
    private class KeySet extends AbstractSet<K> {
        /** {@inheritDoc} */
        public int size() {
            return keyList.size();
        }

        /** {@inheritDoc} */
        public Iterator<K> iterator() {
            return new KeySetIterator(keyList.listIterator());
        }

        /** {@inheritDoc} */
        public boolean contains(Object o) {
            return FunctionListMap.this.containsKey(o);
        }

        /** {@inheritDoc} */
        public boolean remove(Object o) {
            return FunctionListMap.this.remove(o) != null;
        }

        /** {@inheritDoc} */
        public void clear() {
            FunctionListMap.this.clear();
        }
    }

    /**
     * This private {@link Iterator} implementation iterates the {@link Set} of
     * keyList within this Map. All mutating methods are implemented to
     * "write through" to the backing {@link EventList} which ensures that both
     * the {@link EventList} and this Map always remain in sync.
     */
    private class KeySetIterator implements Iterator<K> {

        /** The delegate Iterator walks a List of keyList for the Map. */
        private final ListIterator<K> keyIter;

        /**
         * Construct a new KeySetIterator using a delegate Iterator that walks
         * the list of unique keyList of the MultMap.
         *
         * @param keyIter a {@link ListIterator} that walks the keyList of the Map
         */
        KeySetIterator(ListIterator<K> keyIter) {
            this.keyIter = keyIter;
        }

        /** {@inheritDoc} */
        public boolean hasNext() {
            return keyIter.hasNext();
        }

        /** {@inheritDoc} */
        public K next() {
            return keyIter.next();
        }

        /** {@inheritDoc} */
        public void remove() {
            final int index = keyIter.previousIndex();
            if (index == -1) throw new IllegalStateException("Cannot remove() without a prior call to next()");
            valueList.remove(index);
        }
    }
}