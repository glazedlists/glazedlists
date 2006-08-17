/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * A Hibernate custom collection type for mapping and persisting a {@link BasicEventList} with
 * the help of a {@link PersistentEventList}.
 * 
 * @author Bruce Alspaugh
 * @author Holger Brands
 */
public class EventListType implements UserCollectionType {
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(Object collection, Object entity) {
        return ((EventList) collection).contains(entity);
    }
    
    /**
     * {@inheritDoc}
     */
    public Iterator getElementsIterator(Object collection) {
        return ((EventList) collection).iterator();
    }
    
    /**
     * {@inheritDoc}
     */    
    public Object indexOf(Object collection, Object obj) {
        return new Integer(((EventList) collection).indexOf(obj));
    }
    
    /**
     * {@inheritDoc}
     */
    public Object instantiate() {
        return new BasicEventList();
    }
    
    /**
     * {@inheritDoc}
     */
    public PersistentCollection instantiate(SessionImplementor session,
            CollectionPersister persister) throws HibernateException {
        return new PersistentEventList(session);
    }
    
    /**
     * {@inheritDoc}
     */
    public Object replaceElements(Object original, Object target, CollectionPersister persister,
            Object owner, Map copyCache, SessionImplementor session) throws HibernateException {
        final EventList result = (EventList) target;
        final EventList source = (EventList) original;
        result.clear();
        result.addAll(source);
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public PersistentCollection wrap(SessionImplementor session, Object collection) {
        return new PersistentEventList(session, (EventList) collection);
    }
    
}
