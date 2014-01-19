/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * A factory for EventLists, that is used by instances of {@link EventListType} and
 * {@link PersistentEventList} to instantiate EventLists.
 * 
 * @author Holger Brands
 */
public interface EventListFactory {

    /**
     * Default implementation that always creates new EventLists with different 
     * ReadWriteLocks and ListEventPublishers.
     */
    EventListFactory DEFAULT = new DefaultFactory();
    
    /**
     * Creates a new EventList.
     */
    EventList createEventList();

    /**
     * Create a new EventList with an initial capacity.
     */
    EventList createEventList(int initalCapacity);    
}

/**
 * EventListFactory implementation that always creates new EventLists with different 
 * ReadWriteLocks and ListEventPublishers.
 * 
 * @author Holger Brands
 */
final class DefaultFactory implements EventListFactory {

    /** {@inheritDoc} */
    @Override
    public EventList createEventList() {
        return new BasicEventList();
    }

    /** {@inheritDoc} */
    @Override
    public EventList createEventList(int initalCapacity) {
        return new BasicEventList(initalCapacity);
    }
}