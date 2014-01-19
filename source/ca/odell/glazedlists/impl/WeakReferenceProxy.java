/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// the core Glazed Lists package
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.lang.ref.WeakReference;

/**
 * This class is a proxy to another ListEventListener that may go out of scope
 * without explicitly removing itself from the source list's set of listeners.
 *
 * <p>WeakReferenceProxy exists to solve a garbage collection problem. Suppose
 * there exists an EventList <i>L</i> and with an iterator <i>I</i>. <i>I</i>
 * must listen to <i>L</i> for change events in order to remain consistent.
 * Therefore <i>I</i> will register itself as a listener to <i>L</i>. When
 * <i>I</i> goes out of scope (as they typically do), it will remain registered
 * as a listener of <i>L</i>. This prevents <i>I</i> from ever being garbage
 * collected! But <i>I</i> can never used again. Because iterators are expected
 * to be used very frequently, this will cause an unacceptable memory leak.
 *
 * <p>This problem is solved by WeakReferenceProxy. Instead of adding <i>I</i>
 * as a direct listener of <i>L</i>, add a proxy instead. The proxy will retain
 * a <code>WeakReference</code> to <i>I</i> and forward events to <i>I</i> as
 * long as it is reachable. When <i>I</i> is no longer reachable, the proxy
 * will remove itself from the list of listeners for <i>L</i> and all garbage
 * is available for collection.
 *
 * <p>Specifically, the proxy stops listening to <i>L</i> the
 * <strong>next</strong> time any of the following occurs:
 *
 * <ul>
 *   <li> another ListEventListener is registered with the same EventList
 *   <li> another ListEventListener is deregistered with the same EventList
 *   <li> another ListEvent is broadcast for the same EventList
 * </ul>
 *
 * @see java.lang.ref.WeakReference
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=21">Bug 21</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author James Lemieux
 */
public final class WeakReferenceProxy<E> implements ListEventListener<E> {

    /** a weak reference the target ListEventListener */
    private final WeakReference<ListEventListener<E>> proxyTargetReference;

    /** the list to remove this listener from when done */
    private EventList<E> source;

    /**
     * Creates a new WeakReferenceProxy that listens for events from the
     * specified list and forwards them to the specified listener.
     */
    public WeakReferenceProxy(EventList<E> source, ListEventListener<E> proxyTarget) {
        if (source == null)
            throw new IllegalArgumentException("source may not be null");

        if (proxyTarget == null)
            throw new IllegalArgumentException("proxyTarget may not be null");

        this.source = source;
        this.proxyTargetReference = new WeakReference<ListEventListener<E>>(proxyTarget);
    }

    /**
     * Accepts notification for the changes and forwards them to the proxy
     * target if it has not yet been garbage collected.
     */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        // if this listener has already been cleaned up, ignore ListEvents
        if (source == null) return;

        // fetch the underlying ListEventListener
        final ListEventListener<E> proxyTarget = getReferent();

        // test to see if the underlying ListEventListener still exists
        if (proxyTarget == null) {
            // it doesn't so clean it up
            source.removeListEventListener(this);
            dispose();

        } else {
            // it does, so notify it of the ListEvent
            proxyTarget.listChanged(listChanges);
        }
    }

    /**
     * Returns the underlying ListEventListener or <code>null</code> if it has
     * been garbage collected.
     */
    public ListEventListener<E> getReferent() {
        return proxyTargetReference.get();
    }

    /**
     * A callback to notify this WeakReferenceProxy that it has been
     * unregistered from the EventList to which it was listening. The
     * WeakReferenceProxy responds by cleaning up internal references to the
     * EventList and ensuring that any future ListEvents it receives are
     * ignored.
     */
    public void dispose() {
        source = null;
    }
}