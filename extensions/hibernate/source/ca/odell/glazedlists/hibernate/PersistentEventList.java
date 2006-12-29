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

import java.util.ArrayList;
import java.util.List;


/**
 * A Hibernate persistent wrapper for an {@link EventList}. Underlying
 * collection implementation is {@link BasicEventList}.
 * 
 * @author Bruce Alspaugh
 * @author Holger Brands
 */
public final class PersistentEventList extends PersistentList implements EventList {

    private static final long serialVersionUID = 0L;

    /** Keep a redundant list of the ListEventListeners. */
    private List<ListEventListener> listenerList = new ArrayList<ListEventListener>(); 
    
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
        beforeInitialize();
    }
    
    /** {@inheritDoc} */
    public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
        beforeInitialize();
    }
    
    /** {@inheritDoc} */
    public boolean afterInitialize() {
        final boolean result = super.afterInitialize();
        // turn on event notification after initialization
        addAllListeners();
        return result;
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
    public void addListEventListener(ListEventListener listChangeListener) {        
        ((EventList) list).addListEventListener(listChangeListener);
        listenerList.add(listChangeListener);
    }
    
    /** {@inheritDoc} */
    public void removeListEventListener(ListEventListener listChangeListener) {        
        ((EventList) list).removeListEventListener(listChangeListener);
        listenerList.remove(listChangeListener);
    }

    /**
     * Helper method to prepare initialization of EventList, e.g. disable event notification. 
     */
    private void beforeInitialize() {
        assert !wasInitialized() : "PersistentEventList is already initialized";
        if (this.list == null) throw new IllegalStateException("'list' member is undefined");        
        // disable event notification during initialization
        removeAllListeners();        
    }

    /**
     * Removes all listeners from the wrapped EventList.
     */
    private void removeAllListeners() {
        for (int i = 0, n = listenerList.size(); i < n; i++) {
            ((EventList) list).removeListEventListener(listenerList.get(i));    
        }
    }
    
    /**
     * Adds all listeners to the wrapped EventList.
     */
    private void addAllListeners() {
        for (int i = 0, n = listenerList.size(); i < n; i++) {
            ((EventList) list).addListEventListener(listenerList.get(i));    
        }
    }
    
}