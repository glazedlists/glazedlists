/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 19, 2005 - 8:56:42 PM
 */
package ca.odell.glazedlists.matchers;

/**
 * Abstract base for matchers that combine multiple delegate matchers.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
//public class CompositeMatcherSource extends AbstractValueMatcherSource
//	implements MatcherSourceListener {
//
//    // Changes for CompositeLogic.changeType(...)
//    private static final int CHANGE_NONE = 0;
//    private static final int CHANGE_CONSTRAINED = 1;
//    private static final int CHANGE_RELAXED = 2;
//    private static final int CHANGE_UNKNOWN = 3;
//
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
//     * referenced outside of this class. The actual value
//	 * used for matching is managed by {@link AbstractValueMatcherSource} via
//	 * {@link #setValue(Object)} and {@link #getValue()}.
//     */
//    private EventList delegate_storage;
//
//
//    private final ListEventHandler EVENT_HANDLER = new ListEventHandler();
//
//
//    /**
//     * Create an instance that starts with two delegate Matchers. Neither Matcher can be
//     * null.
//     */
//    public CompositeMatcherSource(MatcherSource one, MatcherSource two) {
//		super(TrueMatcher.getInstance(), false, null);
//
//		if (one == null || two == null) {
//			throw new IllegalArgumentException("Matcher cannot be null");
//		}
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
//     * @param matcher_sources	The array of {@link MatcherSource MatcherSources}. Null and
//	 * 							an empty set are acceptable values.
//     */
//    public CompositeMatcherSource(MatcherSource[] matcher_sources) {
//		super(TrueMatcher.getInstance(), false, null);
//
//        EventList event_list = new BasicEventList();
//
//        if (matcher_sources != null) {
//            for (int i = 0; i < matcher_sources.length; i++) {
//                event_list.add(matcher_sources[ i ]);
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
//    public CompositeMatcherSource(EventList matcher_list) {
//		super(TrueMatcher.getInstance(), false, null);
//
//        if (matcher_list == null) matcher_list = new BasicEventList();
//
//        init(matcher_list);
//    }
//
//
//    /**
//     * Add a delegate matchersource.
//     */
//    public void add(MatcherSource matcher) {
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
//    public void remove(MatcherSource matcher) {
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
//	protected Matcher createMatcher(Object value) {
//		return null;  // TODO: implement
//	}
//
//	public void cleared(MatcherSource source) {
//		// TODO: implement
//	}
//
//	public void changed(Matcher new_matcher, MatcherSource source) {
//		// TODO: implement
//	}
//
//	public void constrained(Matcher new_matcher, MatcherSource source) {
//		// TODO: implement
//	}
//
//	public void relaxed(Matcher new_matcher, MatcherSource source) {
//		// TODO: implement
//	}
//
//
//	private void init(EventList delegate_list) {
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
//                MatcherSource matcher = (MatcherSource) it.next();
//                matcher.addMatcherSourceListener(this);
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
//     * Rebuild the value and set it via {@link #setValue(Object)}.
//     */
//    private void rebuildArray() {
//        MatcherSource[] delegate_array = (MatcherSource[]) delegate_storage.toArray(
//			new MatcherSource[ delegate_storage.size() ]);
//
//		setValue(delegate_array);
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
//			Object[] delegate_array = (Object[]) getValue();
//            List original_list;
//			if (delegate_array == null) {
//				original_list = new LinkedList();
//			} else {
//				original_list = new LinkedList(Arrays.asList(delegate_array));
//			}
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
//            // This is just a hint to the garabage collector
//            original_list.clear();
//        }
//
//
//        private void handleInsert(int index, List original_list) {
//            // Register as a listener
//            MatcherSource matcher = (MatcherSource) delegate_storage.get(index);
//            matcher.addMatcherSourceListener(CompositeMatcherSource.this);
//
//            // Put it in our view list
//            original_list.add(index, matcher);
//        }
//
//        private void handleDelete(int index, List original_list) {
//            // Remove ourselves as a listener and remove it from our view list
//            MatcherSource matcher = (MatcherSource) original_list.remove(index);
//            if (matcher != null) matcher.removeMatcherSourceListener(CompositeMatcherSource.this);
//        }
//    }
//
//
//	private static interface CompositeLogic {
//		Matcher matcher(MatcherSource[] sources);
//
//		int changeType(MatcherSource changed_source, int change_type, MatcherSource[] sources);
//	}
//
//
//	private static class AndLogic implements CompositeLogic {
//		public Matcher matcher(MatcherSource[] sources) {
//			return null;  // TODO: implement
//		}
//
//		public int changeType(MatcherSource changed_source, int change_type,
//														  MatcherSource[] sources) {
//			return 0;  // TODO: implement
//		}
//	}
//}
