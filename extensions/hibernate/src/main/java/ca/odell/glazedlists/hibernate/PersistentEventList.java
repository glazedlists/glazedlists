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

import org.hibernate.collection.internal.PersistentList;
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
    protected transient ListEventAssembler updates;

    /**
     * Constructor with session.
     *
     * @param session the session
     * @param listFactory factory for EventLists
     */
    public PersistentEventList(SharedSessionContractImplementor session, EventListFactory listFactory) {
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
    public PersistentEventList(SharedSessionContractImplementor session, EventList newList) {
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
        if (this.list == null) {
            throw new IllegalStateException("'list' member is undefined");
        }
    }

    /** {@inheritDoc} */
    @Override
    public ListEventPublisher getPublisher() {
        return ((EventList) list).getPublisher();
    }

    /** {@inheritDoc} */
    @Override
    public ReadWriteLock getReadWriteLock() {
        return ((EventList) list).getReadWriteLock();
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
        for(Iterator<ListEventListener> i = updates.getListEventListeners().iterator(); i.hasNext(); ) {
            ListEventListener listener = i.next();
            if(!(listener instanceof Serializable)) {
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
        assert (list instanceof EventList) : "'list' member type unknown";
        updates = new ListEventAssembler(this, ((EventList) list).getPublisher());

        // read in the listeners
        final ListEventListener[] listeners = (ListEventListener[]) in.readObject();
        for(int i = 0; i < listeners.length; i++) {
            updates.addListEventListener(listeners[i]);
        }
    }
}