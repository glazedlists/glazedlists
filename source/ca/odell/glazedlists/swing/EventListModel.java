/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

/**
 * An EventListModel adapts an EventList to the ListModel interface making it
 * appropriate for use with a {@link JList}. Each element of the list
 * corresponds to an element in the {@link ListModel}.
 *
 * <p>The EventListModel class is <strong>not thread-safe</strong>. Unless
 * otherwise noted, all methods are only safe to be called from the event
 * dispatch thread. To do this programmatically, use
 * {@link SwingUtilities#invokeAndWait(Runnable)}.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=14">Bug 14</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=146">Bug 146</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=177">Bug 177</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=228">Bug 228</a>
 * @see SwingUtilities#invokeAndWait(Runnable)
 *
 * @deprecated Use {@link DefaultEventListModel} instead. This class will be removed in the GL
 *             2.0 release. The wrapping of the source list with an EDT safe list has been
 *             determined to be undesirable (it is better for the user to provide their own EDT
 *             safe list).
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author Holger Brands
 */
public class EventListModel<E> extends DefaultEventListModel<E> {
    /** indicates, if source list has to be disposed */
    private boolean disposeSource;

    /**
     * Creates a new model that contains all objects located in the given
     * <code>source</code> and reacts to any changes in the given
     * <code>source</code>.
     */
    public EventListModel(EventList<E> source) {
        super(createProxyList(source));
        disposeSource = (this.source != source);
    }

    /**
     * Releases the resources consumed by this {@link EventListModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventListModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventListModel}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link EventListModel} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an {@link EventListModel} after it has been disposed.
     * As such, this {@link EventListModel} should be detached from its
     * corresponding Component <strong>before</strong> it is disposed.
     */
    @Override
    public void dispose() {
        if (disposeSource) source.dispose();
        super.dispose();
    }

    /**
     * while holding a read lock, this method wraps the given source list with a swing thread
     * proxy list.
     */
    private static <E> EventList<E> createProxyList(EventList<E> source) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableModel
        EventList<E> result = source;
        source.getReadWriteLock().readLock().lock();
        try {
            final TransformedList<E,E> decorated = createSwingThreadProxyList(source);

            // if the create method actually returned a decorated form of the source,
            // record it so it may later be disposed
            if (decorated != null && decorated != source) {
                result = decorated;
            }
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
        return result;
    }

    /** wraps the given source list with a swing thread proxy list, if necessary */
    private static <E> TransformedList<E,E> createSwingThreadProxyList(EventList<E> source) {
        return GlazedListsSwing.isSwingThreadProxyList(source) ? null : GlazedListsSwing.swingThreadProxyList(source);
    }

}