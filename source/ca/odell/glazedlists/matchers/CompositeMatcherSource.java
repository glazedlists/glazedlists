/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 19, 2005 - 8:56:42 PM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.MatcherSource;
import ca.odell.glazedlists.event.MatcherSourceListener;


/**
 * Abstract base for matchers that combine multiple delegate matchers.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public abstract class CompositeMatcherSource extends AbstractMatcherSource
	implements MatcherSourceListener {
	protected CompositeMatcherSource(Matcher initial_matcher) {
		super(initial_matcher);	// TODO: implement
	}

	public void cleared(MatcherSource source) {
		// TODO: implement
	}

	public void changed(Matcher new_matcher, MatcherSource source) {
		// TODO: implement
	}

	public void constrained(Matcher new_matcher, MatcherSource source) {
		// TODO: implement
	}

	public void relaxed(Matcher new_matcher, MatcherSource source) {
		// TODO: implement
	}
	//
//    // Implementation note: this is built around the premise that the number of times
//    // the matchers being used are changed is vastly smaller than the number of times
//    // we need to iterate over those matchers. So, there will be excess time taken so
//    // that the setting of the delegates is always atomic. That way, no locking is
//    // necessary when iterating over them.
//
//    /**
//     * The "main" storage for delegates. This allows callers to have dynamic sets of
//     * matchers and for us to receive notifications when they change. This object is never
//     * referenced outside of this class (see {@link #delegate_array}).
//     */
//    private EventList delegate_storage;
//
//    /**
//     * Array of delegates that is returned to extending classes. This is never null and is
//     * returned directly to extending classes via the {@link #delegates()} method. Callers
//     * can use the array directly because it is never modified, only replaced when it is
//     * out of date.
//     */
//    private volatile Matcher[] delegate_array;
//
//
//    private final ListEventHandler EVENT_HANDLER = new ListEventHandler();
//
//
//    /**
//     * Create an instance that starts with two delegate Matchers. Neither Matcher can be
//     * null.
//     */
//    protected CompositeMatcherSource(MatcherSource one, MatcherSource two) {
//        if (one == null || two == null)
//            throw new IllegalArgumentException("Matcher cannot be null");
//
//        EventList event_list = new BasicEventList();
//        event_list.add(one);
//        event_list.add(two);
//
//        init(event_list);
//    }
//
//    /**
//     * Create an instance that starts with a given array of delegate Matchers.
//     *
//     * @param matchers The array of matchers. Null and an empty set are acceptable
//     *                 values.
//     */
//    protected CompositeMatcherSource(Matcher[] matchers) {
//        EventList event_list = new BasicEventList();
//
//        if (matchers != null) {
//            for (int i = 0; i < matchers.length; i++) {
//                event_list.add(matchers[ i ]);
//            }
//        }
//
//        init(event_list);
//    }
//
//
//    /**
//     * Create an instance that uses the given {@link EventList} to store delegate
//     * Matchers. Every element in the list must implement Matcher.
//     */
//    protected CompositeMatcherSource(EventList matcher_list) {
//        if (matcher_list == null) matcher_list = new BasicEventList();
//
//        init(matcher_list);
//    }
//
//
//    /**
//     * Add a delegate matchersource.
//     */
//    public void add(Matcher matcher) {
//        if (matcher == null) throw new IllegalArgumentException("Matcher cannot be null");
//
//        ReadWriteLock lock = delegate_storage.getReadWriteLock();
//        lock.writeLock().lock();
//        try {
//            delegate_storage.add(matcher);
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    /**
//     * Remove a delegate matchersource.
//     */
//    public void remove(Matcher matcher) {
//        if (matcher == null) throw new IllegalArgumentException("Matcher cannot be null");
//
//        ReadWriteLock lock = delegate_storage.getReadWriteLock();
//        lock.writeLock().lock();
//        try {
//            delegate_storage.remove(matcher);
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    /**
//     * Clear all matchers
//     */
//    public void clear() {
//        ReadWriteLock lock = delegate_storage.getReadWriteLock();
//        lock.writeLock().lock();
//        try {
//            delegate_storage.clear();
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//
//    /**
//     * Provides access to the delegates contained in this matchersource. Extending classes can
//     * use the returned array without worrying about threading access. The returned array
//     * <strong>must not</strong> be modified in any way.
//     * <p/>
//     * If there are no delegates, this will return an empty array (null will never be
//     * returned).
//     */
//    protected Matcher[] delegates() {
//        return delegate_array;
//    }
//
//
//    private void init(EventList delegate_list) {
//        if (delegate_list == null) {
//            throw new IllegalArgumentException("List cannot be null");
//        }
//
//        delegate_storage = delegate_list;
//
//        // Acquire lock
//        ReadWriteLock lock = delegate_list.getReadWriteLock();
//        lock.readLock().lock();
//
//        try {
//            // Register for matchersource events
//            Iterator it = delegate_list.iterator();
//            while (it.hasNext()) {
//                Matcher matcher = (Matcher) it.next();
//                matcher.addMatcherListener(this);
//            }
//
//            // Register for list update events
//            delegate_list.addListEventListener(EVENT_HANDLER);
//
//            // Build the array
//            rebuildArray();
//        } finally {
//            // Release lock
//            lock.readLock().unlock();
//        }
//    }
//
//
//    /**
//     * Rebuild the {@link #delegate_array}.
//     */
//    private void rebuildArray() {
//        delegate_array = (Matcher[]) delegate_storage.toArray(new Matcher[ delegate_storage.size() ]);
//    }
//
//
//    /**
//     * Inner listener handles update events from {@link CompositeMatcherSource#delegate_storage}.
//     * An inner class is being used (rather than the main class directly implementing
//     * ListEventListener because extending Matchers may want to listen for their own
//     * events and this makes that easier. Otherwise, they would have to correctly absorb
//     * and pass on events. Doing it this way prevents doing the wrong thing.
//     */
//    private class ListEventHandler implements ListEventListener {
//        public void listChanged(ListEvent listChanges) {
//            // Need to make a clone of the list before updates so that our indecies
//            // can stay in sync
//            List original_list = new LinkedList(Arrays.asList(delegate_array));
//
//            while (listChanges.hasNext()) {
//                int index = listChanges.getIndex();
//                int type = listChanges.getType();
//
//                switch(type) {
//                    case ListEvent.INSERT:
//                        handleInsert(index, original_list);
//                        break;
//                    case ListEvent.DELETE:
//                        handleDelete(index, original_list);
//                        break;
//                    case ListEvent.UPDATE:
//                        handleDelete(index, original_list);
//                        handleInsert(index, original_list);
//                        break;
//                }
//            }
//
//            // After processing all the updates, rebuild the delegate array
//            rebuildArray();
//
//            // Assertion: the "original_list" and the "delegate_array" (built off the
//            // "delegate_storage") should be the same
//            assert Arrays.equals(original_list.toArray(new Matcher[ original_list.size() ]),
//                delegate_array);
//
//            // This is just a hint to the garabage collector
//            original_list.clear();
//        }
//
//
//        private void handleInsert(int index, List original_list) {
//            // Register as a listener
//            Matcher matcher = (Matcher) delegate_storage.get(index);
//            matcher.addMatcherListener(CompositeMatcherSource.this);
//
//            // Put it in our view list
//            original_list.add(index, matcher);
//        }
//
//        private void handleDelete(int index, List original_list) {
//            // Remove ourselves as a listener and remove it from our view list
//            Matcher matcher = (Matcher) original_list.remove(index);
//            if (matcher != null) matcher.removeMatcherListener(CompositeMatcherSource.this);
//        }
//    }
}
