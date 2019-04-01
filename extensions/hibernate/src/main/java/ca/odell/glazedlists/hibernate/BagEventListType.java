/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.collection.CollectionPersister;

/**
 * A Hibernate custom collection type for mapping and persisting a {@link BasicEventList} with the
 * help of a {@link PersistentBagEventList}.
 * <p>
 * To create the EventLists, an {@link EventListFactory} is used. The default factory simply
 * instantiates new {@link BasicEventList}s with unshared {@link ReadWriteLock}s and
 * {@link ListEventPublisher}s. If that doesn't suite your needs, you can either implement and set
 * your own {@link EventListFactory} implementation. Or you can use a so called <em>list
 * category</em>. By setting a list category on the EventListType instance, a different list factory
 * will be used which uses the category to determine the publisher and lock to use for all
 * EventLists it creates. This way, all EventListType instances which use the same list category
 * will produce EventLists with the same shared lock and publisher. The desired list category can be
 * set programmatically by subclassing. When Hibernate bug
 * <a href="http://opensource.atlassian.com/projects/hibernate/browse/HHH-2336">HHH-2336</a> is
 * fixed, you will be able to specify the category as collection type parameter in your Hibernate
 * mapping file.
 *
 * @see #setListFactory(EventListFactory)
 * @see #useListCategory(String)
 * @see #PROPERTYNAME_EVENTLIST_CATEGORY
 * 
 * @author Holger Brands
 * @author Julian Aliaj
 */
public class BagEventListType extends AbstractEventListType {

    /** {@inheritDoc} */
    @Override
    public PersistentCollection instantiate(SharedSessionContractImplementor session, CollectionPersister persister)
            throws HibernateException {
        return new PersistentBagEventList(session, getListFactory());
    }

    /** {@inheritDoc} */
    @Override
    public PersistentCollection wrap(SharedSessionContractImplementor session, Object collection) {
        return new PersistentBagEventList(session, (EventList) collection);
    }
}