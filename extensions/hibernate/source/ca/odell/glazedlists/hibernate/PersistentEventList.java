/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventAssembler;
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
 * @author James Lemieux
 */
public final class PersistentEventList extends PersistentList implements EventList, ListEventListener {

    private static final long serialVersionUID = 0L;

    /** the change event and notification system */
    protected final ListEventAssembler updates;

    /**
     * Constructor with session.
     *
     * @param session the session
     * @param listFactory factory for EventLists
     */
    public PersistentEventList(SessionImplementor session, EventListFactory listFactory) {
        super(session);

        final EventList delegate = listFactory.createEventList();

        // instantiate list here to avoid NullPointerExceptions with lazy loading
        updates = new ListEventAssembler(this, delegate.getPublisher());
        delegate.addListEventListener(this);
        list = delegate;
    }

    /**
     * Constructor with session and EventList.
     *
     * @param session the session
     * @param newList the EventList
     */
    public PersistentEventList(SessionImplementor session, EventList newList) {
        super(session, newList);
        if (newList == null) throw new IllegalArgumentException("EventList parameter may not be null");

        updates = new ListEventAssembler(this, newList.getPublisher());
        newList.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public void beforeInitialize(CollectionPersister persister) {
        beforeInitialize();
    }

    /** {@inheritDoc} */
    public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
        beforeInitialize();
    }

    /**
     * Helper method to prepare initialization of EventList, e.g. disable event notification.
     */
    private void beforeInitialize() {
        assert !wasInitialized() : "PersistentEventList is already initialized";
        if (this.list == null) throw new IllegalStateException("'list' member is undefined");
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
        updates.addListEventListener(listChangeListener);
    }

    /** {@inheritDoc} */
    public void removeListEventListener(ListEventListener listChangeListener) {
        updates.removeListEventListener(listChangeListener);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // ignore ListEvents during Hibernate's initialization
        // (initialization should always appear to be transparent and thus should not produce ListEvents)
        if (wasInitialized())
            updates.forwardEvent(listChanges);
    }
}