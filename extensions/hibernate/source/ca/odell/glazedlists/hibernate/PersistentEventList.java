/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;
import org.hibernate.collection.PersistentList;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

/**
 * A Hibernate persistent wrapper for an {@link EventList}. Underlying
 * collection implementation is {@link BasicEventList}.
 * 
 * @author Bruce Alspaugh
 * @author Holger Brands
 */
public final class PersistentEventList extends PersistentList implements EventList {

    private static final long serialVersionUID = 0L;

    /**
     * Constructor with session.
     * 
     * @param session the session
     */
    public PersistentEventList(SessionImplementor session) {
        super(session);
        // instantiate list here to avoid NullPointerExceptions with lazy loading
        list = new BasicEventList();
    }

    /**
     * Constructor with session and EventList.
     * 
     * @param session the session
     * @param newList the EventList
     */
    public PersistentEventList(SessionImplementor session, EventList newList) {
        super(session, newList);
        if (newList == null) throw new IllegalArgumentException("EventList parameter must not be null");
    }

    /** {@inheritDoc} */
    public void beforeInitialize(CollectionPersister persister) {
        if (this.list == null) throw new IllegalStateException("'list' member is undefined");
    }

    /** {@inheritDoc} */
    public void addListEventListener(ListEventListener listChangeListener) {
        ((EventList) list).addListEventListener(listChangeListener);
    }

    /** {@inheritDoc} */
    public ListEventPublisher getPublisher() {
        return ((EventList) list).getPublisher();
    }

    /** {@inheritDoc} */
    public ReadWriteLock getReadWriteLock() {
        return ((EventList) list).getReadWriteLock();
    }

    /** {@inheritDoc} */
    public void removeListEventListener(ListEventListener listChangeListener) {
        ((EventList) list).removeListEventListener(listChangeListener);
    }
}