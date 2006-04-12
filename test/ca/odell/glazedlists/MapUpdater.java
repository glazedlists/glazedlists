/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;

import java.util.*;

/**
 * A helper class that updates a {@link Map} everytime an {@link EventList}
 * is modified. This requires a {@link FunctionList.Function} that provides
 * distinct keys for every element in the list.
 *
 * <p>It would be possible to enhance this class to keep track of duplicate
 * keys and have a policy when such is encountered:
 *   <li>the map values could be {@link List}s of values
 *   <li>keep the value with the lowest index in the list
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class MapUpdater<E,K,V> implements ListEventListener<E> {

    /** a basic helper function for general purpose */
    public static final FunctionList.Function IDENTITY_FUNCTION = new IdentityFunction<Object>();

    /** the map to keep up-to-date */
    private final Map<K,V> map;
    private final Map<K,V> mapReadOnly;

    /** the event list to watch */
    private final EventList<E> eventList;
    private final List<E> eventListValues;

    /** functions to pull keys and values from the map */
    private final FunctionList.Function<E,K> keyFunction;
    private final FunctionList.Function<E,V> valueFunction;

    private MapUpdater(EventList<E> eventList, FunctionList.Function<E, K> keyFunction, FunctionList.Function<E, V> valueFunction) {
        this.map = new HashMapPlusReference<E,K,V>(this);
        this.mapReadOnly = Collections.unmodifiableMap(map);

        this.eventList = eventList;
        this.keyFunction = keyFunction;
        this.valueFunction = valueFunction;

        eventList.getReadWriteLock().readLock().lock();
        try {
            // add all elements to the map
            eventListValues = new ArrayList<E>(eventList.size());
            for(int i = 0; i < eventList.size(); i++) {
                handleInsert(i);
            }

            // handle future changes to the list
            eventList.addListEventListener(this);
        } finally {
            eventList.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Create a new {@link MapUpdater} for the specified {@link EventList},
     * using the specified functions for keys and values.
     *
     * <p>Note that for consistent behaviour, all keys must be distinct.
     */
    public static final <E,K,V> Map<K,V> mapForEventList(EventList<E> eventList, FunctionList.Function<E,K> keyFunction, FunctionList.Function<E,V> valueFunction) {
        MapUpdater<E,K,V> mapUpdater = new MapUpdater<E,K,V>(eventList, keyFunction, valueFunction);
        return mapUpdater.mapReadOnly;
    }

    private void handleInsert(int index) {
        E element = eventList.get(index);
        eventListValues.add(index, element);
        map.put(keyFunction.evaluate(element), valueFunction.evaluate(element));
    }
    private void handleUpdate(int index) {
        E previous = eventListValues.get(index);
        E current = eventList.get(index);

        map.remove(keyFunction.evaluate(previous));
        eventListValues.set(index, current);
        map.put(keyFunction.evaluate(current), valueFunction.evaluate(current));
    }
    private void handleDelete(int index) {
        E previous = eventListValues.remove(index);
        map.remove(keyFunction.evaluate(previous));
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        while(listChanges.next()) {
            int index = listChanges.getIndex();
            int type = listChanges.getType();
            if(type == ListEvent.INSERT) {
                handleInsert(index);
            } else if(type == ListEvent.UPDATE) {
                handleUpdate(index);
            } else if(type == ListEvent.DELETE) {
                handleDelete(index);
            }
        }
    }


    /**
     * This class exists simply to add a hard reference to the listener that
     * maintains this map. As soon as this map is no longer needed, that reference
     * will go out of scope and the {@link MapUpdater} will get garbage collected.
     */
    private static final class HashMapPlusReference<E,K,V> extends HashMap<K,V> {

        /** maintain a strong reference to the listener as long as the map is referenced */
        private final ListEventListener<E> target;

        public HashMapPlusReference(ListEventListener<E> target) {
            this.target = target;
        }
    }

    /**
     * A no-op function.
     */
    private static final class IdentityFunction<A> implements FunctionList.Function<A,A> {
        public A evaluate(A sourceValue) {
            return sourceValue;
        }
    }
}