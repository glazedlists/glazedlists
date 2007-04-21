/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserCollectionType;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * A Hibernate custom collection type for mapping and persisting a {@link BasicEventList} with the
 * help of a {@link PersistentEventList}.
 * <p>
 * To create the EventLists, an {@link EventListFactory} is used. The default factory simply
 * instantiates new {@link BasicEventList}s with unshared {@link ReadWriteLock}s and
 * {@link ListEventPublisher}s. If that doesn't suite your needs, you can either implement and set
 * your own {@link EventListFactory} implementation. Or you can use a so called
 * <em>list category<em>. By setting a list category on the 
 * EventListType instance, a different list factory will be used which uses the category to determine
 * the publisher and lock to use for all EventLists it creates. This way, all EventListType instances
 * which use the same list category will produce EventLists with the same shared lock and publisher.  
 * The desired list category can be set programmatically by subclassing. When Hibernate bug
 * <a href="http://opensource.atlassian.com/projects/hibernate/browse/HHH-2336">HHH-2336</a> 
 * is fixed, you will be able to specify the category as collection type parameter in your 
 * Hibernate mapping file.
 * 
 * @see #setListFactory(EventListFactory)
 * @see #useListCategory(String)
 * @see #PROPERTYNAME_EVENTLIST_CATEGORY
 * 
 * @author Bruce Alspaugh
 * @author Holger Brands
 */
public class EventListType implements UserCollectionType, ParameterizedType {

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
        if (newListFactory == null) 
            throw new IllegalArgumentException("EventListFactory must not be null");
        listFactory = newListFactory;
    }
    
    /**
     * When Hibernate bug <a href="http://opensource.atlassian.com/projects/hibernate/browse/HHH-2336">HHH-2336</a>
     * is fixed, this method will be called by Hibernate when reading its mapping files.
     */
    public final void setParameterValues(Properties parameters) {
        if (parameters == null) return;
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
    public boolean contains(Object collection, Object entity) {
        return ((EventList) collection).contains(entity);
    }
    
    /** {@inheritDoc} */
    public Iterator getElementsIterator(Object collection) {
        return ((EventList) collection).iterator();
    }
    
    /** {@inheritDoc} */
    public Object indexOf(Object collection, Object obj) {
        final int index = ((EventList) collection).indexOf(obj);
        return (index < 0) ? null : new Integer(index); 
    }
    
    /** {@inheritDoc} */
    public Object instantiate() {
        return getListFactory().createEventList();
    }

    /** {@inheritDoc} */
    public Object instantiate(int anticipatedSize) {
        final EventListFactory fac = getListFactory();
        return anticipatedSize < 0 ? fac.createEventList() : fac.createEventList(anticipatedSize);
    }
    
    /** {@inheritDoc} */
    public PersistentCollection instantiate(SessionImplementor session,
            CollectionPersister persister) throws HibernateException {
        return new PersistentEventList(session, getListFactory());
    }
    
    /** {@inheritDoc} */
    public Object replaceElements(Object original, Object target, CollectionPersister persister,
            Object owner, Map copyCache, SessionImplementor session) throws HibernateException {
        final EventList result = (EventList) target;
        final EventList source = (EventList) original;
        result.clear();
        result.addAll(source);
        return result;
    }
    
    /** {@inheritDoc} */
    public PersistentCollection wrap(SessionImplementor session, Object collection) {
        return new PersistentEventList(session, (EventList) collection);
    }
}