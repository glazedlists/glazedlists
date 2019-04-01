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

import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.collection.CollectionPersister;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Hibernate persistent bag wrapper for an {@link EventList}.
 * <p>
 * Underlying collection implementation is {@link BasicEventList}.
 *
 * @author Holger Brands
 * @author Julian Aliaj
 */
public final class PersistentBagEventList extends PersistentBag implements EventList, ListEventListener {

    private static final long serialVersionUID = 0L;

    /** the change event and notification system */
    protected transient ListEventAssembler updates;

    /**
     * Constructor with session.
     *
     * @param session the session
     * @param listFactory factory for EventLists
     */
    public PersistentBagEventList(SharedSessionContractImplementor session, EventListFactory listFactory) {
        super(session);

        final EventList delegate = listFactory.createEventList();

        // instantiate list here to avoid NullPointerExceptions with lazy loading
        updates = new ListEventAssembler(this, delegate.getPublisher());
        delegate.addListEventListener(this);
        bag = delegate;
    }

    /**
     * Constructor with session and EventList.
     *
     * @param session the session
     * @param newList the EventList
     */
    public PersistentBagEventList(SharedSessionContractImplementor session, EventList newList) {
        super(session, newList);
        if (newList == null) {
            throw new IllegalArgumentException("EventList parameter may not be null");
        }

        updates = new ListEventAssembler(this, newList.getPublisher());
        newList.addListEventListener(this);
    }

    /** Kept for compatibility with older Hibernate versions. */
    public void beforeInitialize(CollectionPersister persister) {
        beforeInitialize();
    }

    /** {@inheritDoc} */
    @Override
    public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
        beforeInitialize();
    }

    /**
     * Helper method to prepare initialization of EventList, e.g. disable event notification.
     */
    private void beforeInitialize() {
        assert !wasInitialized() : "PersistentEventList is already initialized";
        if (this.bag == null) {
            throw new IllegalStateException("'list' member is undefined");
        }
    }

    /** {@inheritDoc} */
    @Override
    public ListEventPublisher getPublisher() {
        return ((EventList) bag).getPublisher();
    }

    /** {@inheritDoc} */
    @Override
    public ReadWriteLock getReadWriteLock() {
        return ((EventList) bag).getReadWriteLock();
    }

    /** {@inheritDoc} */
    @Override
    public void addListEventListener(ListEventListener listChangeListener) {
        updates.addListEventListener(listChangeListener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeListEventListener(ListEventListener listChangeListener) {
        updates.removeListEventListener(listChangeListener);
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent listChanges) {
        // ignore ListEvents during Hibernate's initialization
        // (initialization should always appear to be transparent and thus should not produce ListEvents)
        if (wasInitialized()) {
            updates.forwardEvent(listChanges);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        // TODO Holger please implement me!
    }

    /**
     * Serializes this list and all serializable listeners
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        // write out all serializable listeners
        List<ListEventListener> serializableListeners = new ArrayList<>();
        for (Iterator<ListEventListener> i = updates.getListEventListeners().iterator(); i.hasNext(); ) {
            ListEventListener listener = i.next();
            if (!(listener instanceof Serializable)) {
                continue;
            }
            serializableListeners.add(listener);
        }
        ListEventListener[] listeners = serializableListeners.toArray(new ListEventListener[serializableListeners.size()]);
        out.writeObject(listeners);
    }

    /**
     * Deserializes this list and all serializable listeners.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        assert (bag instanceof EventList) : "'bag' member type unknown";
        updates = new ListEventAssembler(this, ((EventList) bag).getPublisher());

        // read in the listeners
        final ListEventListener[] listeners = (ListEventListener[]) in.readObject();
        for (int i = 0; i < listeners.length; i++) {
            updates.addListEventListener(listeners[i]);
        }
    }
}
