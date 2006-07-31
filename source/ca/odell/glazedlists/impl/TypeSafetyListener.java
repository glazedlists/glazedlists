/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.Set;

/**
 * This {@link ListEventListener} is created with a Set of Classes which
 * represent the given element types that are allowed to be stored in the
 * {@link EventList} to which it listens. As elements are inserted and updated
 * within the {@link EventList} their types are checked against the set of
 * permitted types. If an element with an illegal type is added to the
 * {@link EventList} an {@link IllegalArgumentException} is thrown explaining
 * precisely where and what was the offending type.
 *
 * <p>Note: if the EventList is allowed to contain <tt>null</tt> values the
 * supplied Set of types must contain <tt>null</tt> as one of the supported
 * types.
 */
public class TypeSafetyListener<E> implements ListEventListener<E> {

    /** The element types supported by the EventList listened to */
    private final Class[] types;

    /**
     * Create a {@link TypeSafetyListener} that listens for changes on the
     * specified source {@link EventList} and verifies the added elements are
     * one of the allowed types.
     */
    public TypeSafetyListener(EventList<E> source, Set<Class> types) {
        // reserve a private copy of the supported Classes
        this.types = types.toArray(new Class[types.size()]);

        // begin listening to the source list
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        final EventList<E> source = listChanges.getSourceList();
        while (listChanges.next()) {
            final int type = listChanges.getType();

            // skip deletes as the type was validated on insertion
            if (type == ListEvent.DELETE) continue;

            // fetch the element in question
            final int index = listChanges.getIndex();
            final E e = source.get(index);

            if (type == ListEvent.INSERT && !checkType(e)) {
                final Class badType = e == null ? null : e.getClass();
                throw new IllegalArgumentException("Element with illegal type " + badType + " inserted at index " + index + ": " + e);

            } else if (type == ListEvent.UPDATE && !checkType(e)) {
                final Class badType = e == null ? null : e.getClass();
                throw new IllegalArgumentException("Element with illegal type " + badType + " updated at index " + index + ": " + e);
            }
        }
    }

    /**
     * Returns <tt>true</tt> if <code>e</code> is assignable to one of the
     * types accepted by the {@link EventList}; <tt>false</tt> otherwise.
     *
     * @param e the object to check for type safety
     * @return <tt>true</tt> if <code>e</code> is assignable to one of the
     *      types accepted by the {@link EventList}; <tt>false</tt> otherwise
     */
    private boolean checkType(E e) {
        for (int i = 0; i < types.length; i++) {
            // avoid a NullPointerException from isAssignableFrom(null)
            if (e == null && types[i] != null)
                continue;

            if (types[i] == null ? e == null : types[i].isAssignableFrom(e.getClass()))
                return true;
        }

        return false;
    }
}