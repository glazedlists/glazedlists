/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.Map;

/**
 * A special kind of Map backed by an EventList that is expected to live longer
 * than this Map. It defines a {@link #dispose()} method which should be called
 * when the Map is no longer useful, but the underlying {@link EventList} is
 * still referenced and useful. It allows this Map to be garbage collected
 * before its source {@link EventList}.
 *
 * @author James Lemieux
 */
public interface DisposableMap<K, V> extends Map<K, V> {

    /**
     * Releases the resources consumed by this {@link DisposableMap} so that it
     * may eventually be garbage collected.
     *
     * <p>A {@link DisposableMap} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link DisposableMap}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where a {@link DisposableMap} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link DisposableMap} after it has been disposed.
     */
    public void dispose();
}