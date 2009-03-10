/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.gui.TableFormat;

/**
 * A {@link DefaultEventTableModel} that silently wraps it's source list in a
 * SwingThreadProxyEventList to ensure that events that arrive at the TableModel do so on
 * the EDT. A {@link TableModel} that holds an {@link EventList}. Each element of the list
 * corresponds to a row in the {@link TableModel}. The columns of the table are specified using a
 * {@link TableFormat}.
 * <p>
 * The EventTableModel class is <strong>not thread-safe</strong>. Unless otherwise noted, all
 * methods are only safe to be called from the event dispatch thread. To do this
 * programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)}.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial/">Glazed Lists Tutorial</a>
 * @see SwingUtilities#invokeAndWait(Runnable)
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=112">Bug 112</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=146">Bug 146</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=177">Bug 177</a>
 *
 * @deprecated Use {@link DefaultEventTableModel} instead. This class will be removed in the GL
 *             2.0 release. The wrapping of the source list with an EDT safe list has been
 *             determined to be undesirable (it is better for the user to provide their own EDT
 *             safe list).
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventTableModel<E> extends DefaultEventTableModel<E> {

    /** the proxy moves events to the Swing Event Dispatch thread */
    protected TransformedList<E,E> swingThreadSource;

    /**
     * Creates a new table model that extracts column data from the given <code>source</code>
     * using the the given <code>tableFormat</code>.
     *
     * @param source the EventList that provides the row objects
     * @param tableFormat the object responsible for extracting column data from the row objects
     *
     * @deprecated Use {@link DefaultEventTableModel} and
     *             {@link GlazedListsSwing#swingThreadProxyList(EventList)} instead
     */
    public EventTableModel(EventList<E> source, TableFormat<? super E> tableFormat) {
        super(source, tableFormat);
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableModel

        // KD 20090306 - Not crazy about these locks, but it was in the original
        // EventTableModel implementation, so we need to leave it.  DefaultTableModel
        // should be considered to be non-Thread Safe, so source lists should be locked
        // before constructing a DefaultEventTableModel if there are any potential
        // race conditions
        source.getReadWriteLock().readLock().lock();
        try {
            final TransformedList<E,E> decorated = createSwingThreadProxyList(source);

            if (decorated != null && decorated != source){
                //we need to switch the configuration of the DefaultEventTableModel so it
                // uses the swingThreadProxyList instead
                this.source.removeListEventListener(this);
                this.source = swingThreadSource = decorated;
                this.source.addListEventListener(this);
            }
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }

    }

    /**
     * Creates a new table that renders the specified list with an automatically
     * generated {@link TableFormat}. It uses JavaBeans and reflection to create
     * a {@link TableFormat} as specified.
     *
     * <p>Note that the classes which will be obfuscated may not work with
     * reflection. In this case, implement a {@link TableFormat} manually.
     *
     * @param source the EventList that provides the row objects
     * @param propertyNames an array of property names in the JavaBeans format.
     *      For example, if your list contains Objects with the methods getFirstName(),
     *      setFirstName(String), getAge(), setAge(Integer), then this array should
     *      contain the two strings "firstName" and "age". This format is specified
     *      by the JavaBeans {@link java.beans.PropertyDescriptor}.
     * @param columnLabels the corresponding column names for the listed property
     *      names. For example, if your columns are "firstName" and "age", then
     *      your labels might be "First Name" and "Age".
     * @param writable an array of booleans specifying which of the columns in
     *      your table are writable.
     *
     * @deprecated Use {@link GlazedListsSwing#createEventTableModel(EventList, String[], String[], boolean[])}
     * and {@link GlazedListsSwing#swingThreadProxyList(EventList)} instead
     */
    public EventTableModel(EventList<E> source, String[] propertyNames, String[] columnLabels, boolean[] writable) {
        this(source, GlazedLists.tableFormat(propertyNames, columnLabels, writable));
    }

    /**
     * This method exists as a hook for subclasses that may have custom
     * threading needs within their EventTableModels. By default, this method
     * will wrap the given <code>source</code> in a SwingThreadProxyList if it
     * is not already a SwingThreadProxyList. Subclasses may replace this logic
     * and return either a custom ThreadProxyEventList of their choosing, or
     * return <code>null</code> or the <code>source</code> unchanged in order
     * to indicate that <strong>NO</strong> ThreadProxyEventList is desired.
     * In these cases it is expected that some external mechanism will ensure
     * that threading is handled correctly.
     *
     * @param source the EventList that provides the row objects
     * @return the source wrapped in some sort of ThreadProxyEventList if
     *      Thread-proxying is desired, or either <code>null</code> or the
     *      <code>source</code> unchanged to indicate that <strong>NO</strong>
     *      Thread-proxying is desired
     */
    protected TransformedList<E,E> createSwingThreadProxyList(EventList<E> source) {
        return GlazedListsSwing.isSwingThreadProxyList(source) ? null : GlazedListsSwing.swingThreadProxyList(source);
    }

    /**
     * Releases the resources consumed by this {@link EventTableModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventTableModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventTableModel}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link EventTableModel} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an {@link EventTableModel} after it has been disposed.
     * As such, this {@link EventTableModel} should be detached from its
     * corresponding Component <strong>before</strong> it is disposed.
     */
    public void dispose() {
        // if we created the swingThreadSource then we must also dispose it
        if (swingThreadSource != null)
            swingThreadSource.dispose();

        swingThreadSource = null;
        super.dispose();
    }
}