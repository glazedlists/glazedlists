/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// for managing weak references
import java.lang.ref.*;

/**
 * This class is a proxy to another ListEventListener that may go out of
 * scope without explicitly removing itself from the source list's set of
 * listeners.
 *
 * <p>WeakReferenceProxy exists to solve a garbage
 * collection problem. Suppose I have an EventList <i>L</i> and I request
 * an iterator for <i>L</i>. The iterator must listen for change events to
 * <i>L</i> in order to be consistent. Therefore such an iterator will register
 * itself as a listener for <i>L</i>. When the iterator goes out of scope (as
 * they usually do), it will remain as a listener for <i>L</i>. This prevents
 * the iterator object from ever being garbage collected! But the iterator is
 * never used again. Because iterators can be used very frequently, this will
 * cause an unacceptable memory leak.
 *
 * <p>This problem is solved by WeakReferenceProxy. Instead
 * of adding the iterator directly as a listener for <i>L</i>, add the proxy
 * instead. The proxy will retain a <code>WeakReference</code> to the iterator
 * and forward events to the iterator as long as it is reachable. When the
 * iterator is no longer reachable, the proxy will remove itself from the list
 * of listeners for <i>L</i>. All garbage is available for collection.
 *
 * @see java.lang.ref.WeakReference
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=21">Bug 21</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class WeakReferenceProxy<E> implements ListEventListener<E> {

    /** a weak reference the target ListEventListener */
    private WeakReference<ListEventListener<E>> proxyTargetReference;

    /** the list to remove this listener from when done */
    private EventList<E> source;

    /**
     * Creates a new WeakReferenceProxy that listens for
     * events from the specified list and forwards them to the specified
     * listener.
     */
    public WeakReferenceProxy(EventList<E> source, ListEventListener<E> proxyTarget) {
        if(source == null || proxyTarget == null) throw new NullPointerException();

        this.source = source;
        proxyTargetReference = new WeakReference<ListEventListener<E>>(proxyTarget);
    }

    /**
     * Accepts notification for the changes and forwards them to the proxy target
     * if it has not yet been garbage collected.
     */
    public void listChanged(ListEvent<E> listChanges) {
        ListEventListener<E> proxyTarget = proxyTargetReference.get();

        if(source != null && (proxyTarget == null || proxyTargetReference.isEnqueued())) {
            source.removeListEventListener(this);
            source = null;
        } else {
            proxyTarget.listChanged(listChanges);
        }
    }
}