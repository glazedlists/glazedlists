/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserCollectionType;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Base class for {@link EventListType} and {@link BagEventListType} with common methods.
 */
public abstract class AbstractEventListType implements UserCollectionType, ParameterizedType {

    /** Name of property for specifying an EventList category in the Hibernate mapping file. */
    public static final String PROPERTYNAME_EVENTLIST_CATEGORY = "EventList.category";

    /** Factory for EventLists. */
    private EventListFactory listFactory = EventListFactory.DEFAULT;

    /**
     * Gets the used EventListFactory.
     */
    public final EventListFactory getListFactory() {
        return listFactory;
    }

    /**
     * Sets a new EventListFactory.
     */
    public final void setListFactory(EventListFactory newListFactory) {
        if (newListFactory == null) {
            throw new IllegalArgumentException("EventListFactory must not be null");
        }
        listFactory = newListFactory;
    }

    /**
     * When Hibernate bug <a href="http://opensource.atlassian.com/projects/hibernate/browse/HHH-2336">HHH-2336</a>
     * is fixed, this method will be called by Hibernate when reading its mapping files.
     */
    @Override
    public final void setParameterValues(Properties parameters) {
        if (parameters == null) {
            return;
        }
        final String category = parameters.getProperty(PROPERTYNAME_EVENTLIST_CATEGORY);
        if (category != null) {
            useListCategory(category);
        }
    }

    /**
     * Convenience method to specify the used list category.
     */
    protected final void useListCategory(String category) {
        setListFactory(new CategoryEventListFactory(category));
    }

    /**
     * Convenience method to specify the used list category and the associated ReadWriteLock and
     * ListEventPublisher
     */
    protected final void useListCategory(String category, ReadWriteLock lock, ListEventPublisher publisher) {
        setListFactory(new CategoryEventListFactory(category, lock, publisher));
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object collection, Object entity) {
        return ((EventList) collection).contains(entity);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator getElementsIterator(Object collection) {
        return ((EventList) collection).iterator();
    }

    /** {@inheritDoc} */
    @Override
    public Object indexOf(Object collection, Object obj) {
        final int index = ((EventList) collection).indexOf(obj);
        return (index < 0) ? null : Integer.valueOf(index);
    }

    /**
     * Instantiate an empty instance of the "underlying" collection (not a wrapper).
     * Kept for compatibility with older Hibernate versions.
     */
    public Object instantiate() {
        return getListFactory().createEventList();
    }

    /** {@inheritDoc} */
    @Override
    public Object instantiate(int anticipatedSize) {
        final EventListFactory fac = getListFactory();
        return anticipatedSize < 0 ? fac.createEventList() : fac.createEventList(anticipatedSize);
    }

    /** {@inheritDoc} */
    @Override
    public Object replaceElements(Object original, Object target, CollectionPersister persister, Object owner, Map copyCache,
            SharedSessionContractImplementor session) throws HibernateException {
        final EventList result = (EventList) target;
        final EventList source = (EventList) original;
        result.clear();
        result.addAll(source);
        return result;
    }

}
