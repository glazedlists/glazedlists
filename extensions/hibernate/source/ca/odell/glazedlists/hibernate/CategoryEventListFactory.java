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
final class CategoryEventListFactory implements EventListFactory {

    /** Map holding {@link ListInfo} per unique category. */
    private static final Map<String, ListInfo> CATEGORY_MAP = new HashMap<String, ListInfo>();
    
    /** List category used by this factory instance. */
    private String category;
    
    /**
     * Constructor with list category.
     */
    public CategoryEventListFactory(String category) {
        setCategory(category);
    }
    
    /**
     * Sets the list category.
     */
    private void setCategory(String newCategory) {
        if (newCategory == null) throw new IllegalArgumentException("Category must not be null");
        category = newCategory;
        synchronized (CATEGORY_MAP) {
            if (!CATEGORY_MAP.containsKey(category)) {
                CATEGORY_MAP.put(category, new ListInfo());
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
    public EventList createEventList() {
        final ListInfo info = getListInfo();
        return new BasicEventList(info.publisher, info.lock);
    }

    /** {@inheritDoc} */
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
    }
}
