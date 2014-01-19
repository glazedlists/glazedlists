/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.util.HashMap;
import java.util.Map;

/**
 * An EventListFactory implementation that uses a list category as key to determine, which
 * {@link ListEventPublisher} and {@link ReadWriteLock} should be used to create EventLists.
 * All factory instances that use the same category produce EventLists wich share the same publisher
 * and lock.
 *
 * @author Holger Brands
 */
public final class CategoryEventListFactory implements EventListFactory {

    /** Map holding {@link ListInfo} per unique category. */
    private static final Map<String, ListInfo> CATEGORY_MAP = new HashMap<String, ListInfo>();

    /** List category used by this factory instance. */
    private final String category;

    /**
     * Constructor with list category to use. If the category is not registered yet, it will be
     * registered with a new ReadWriteLock and ListEventPublisher.
     */
    public CategoryEventListFactory(String category) {
        if (category == null) throw new IllegalArgumentException("Category must not be null");
        this.category = category;
        registerCategory(category);
    }

    /**
     * Constructor with list category, lock and publisher to use. If the category is not registered
     * yet, it will be registered with the given ReadWriteLock and ListEventPublisher.
     *
     * @throws IllegalStateException if the same category is already registered with different values
     */
    public CategoryEventListFactory(String category, ReadWriteLock lock, ListEventPublisher publisher) {
        if (category == null) throw new IllegalArgumentException("Category must not be null");
        if (lock == null) throw new IllegalArgumentException("ReadWriteLock must not be null");
        if (publisher == null) throw new IllegalArgumentException("ListEventPublisher must not be null");
        this.category = category;
        registerCategory(category, lock, publisher);
    }

    /**
     * Registers a new list category, if not already there.
     */
    private void registerCategory(String newCategory) {
        synchronized (CATEGORY_MAP) {
            if (!CATEGORY_MAP.containsKey(newCategory)) {
                CATEGORY_MAP.put(newCategory, new ListInfo());
            }
        }
    }

    /**
     * Registers a new list category with the given lock and publisher, if not already there.
     *
     * @throws IllegalStateException if the same category is already registered with different values
     */
    private void registerCategory(String newCategory, ReadWriteLock lock, ListEventPublisher publisher) {
        synchronized (CATEGORY_MAP) {
            if (CATEGORY_MAP.containsKey(newCategory)) {
                final ListInfo info = getListInfo();
                if (!lock.equals(info.lock) || !publisher.equals(info.publisher)) {
                    throw new IllegalStateException("List category " + newCategory
                            + " already in use with different lock or publisher");
                }
            } else {
                CATEGORY_MAP.put(newCategory, new ListInfo(lock, publisher));
            }
        }
    }

    /**
     * Gets the list category.
     */
    public String getCategory() {
        return category;
    }

    /** {@inheritDoc} */
    @Override
    public EventList createEventList() {
        final ListInfo info = getListInfo();
        return new BasicEventList(info.publisher, info.lock);
    }

    /** {@inheritDoc} */
    @Override
    public EventList createEventList(int initalCapacity) {
        final ListInfo info = getListInfo();
        return new BasicEventList(initalCapacity, info.publisher, info.lock);
    }

    /**
     * Gets the ListInfo for the category.
     */
    private ListInfo getListInfo() {
        synchronized (CATEGORY_MAP) {
            return CATEGORY_MAP.get(getCategory());
        }
    }

    /**
     * Helper method to clear the mapping of categories to publisher/lock pairs.
     */
    public static void clearCategoryMapping() {
        synchronized (CATEGORY_MAP) {
            CATEGORY_MAP.clear();
        }
    }

    /**
     * Helper class to hold a ReadWriteLock and a ListEventPublisher.
     *
     * @author Holger Brands
     */
    private static class ListInfo {
        public ReadWriteLock lock;
        public ListEventPublisher publisher;

        ListInfo() {
            lock = LockFactory.DEFAULT.createReadWriteLock();
            publisher = ListEventAssembler.createListEventPublisher();
        }
        ListInfo(ReadWriteLock lock, ListEventPublisher publisher) {
            this.lock = lock;
            this.publisher = publisher;
        }
    }
}