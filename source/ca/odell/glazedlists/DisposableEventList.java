/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

/**
 * A special kind of EventList that may potentially live longer than its
 * source EventList. It defines a {@link #dispose()} method which should be
 * called when this EventList is no longer useful, but the underlying
 * {@link EventList} is still referenced and useful. It allows this EventList
 * to be garbage collected before its source {@link EventList}.
 *
 * @author James Lemieux
 */
public interface DisposableEventList<E> extends EventList<E> {

    /**
     * Releases the resources consumed by this {@link DisposableEventList} so
     * that it may eventually be garbage collected.
     *
     * <p>A {@link DisposableEventList} will be garbage collected without a
     * call to {@link #dispose()}, but not before its source {@link EventList}
     * is garbage collected. By calling {@link #dispose()}, you allow the
     * {@link DisposableEventList} to be garbage collected before its source
     * {@link EventList}. This is necessary for situations where a
     * {@link DisposableEventList} is short-lived but its source
     * {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link DisposableEventList} after it has been
     * disposed.
     */
    public void dispose();
}