/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;

import de.kupzog.ktable.KTable;

import org.eclipse.swt.widgets.Display;


/**
 * A factory for creating all sorts of objects relevant to the Glazed Lists {@link KTable} extension.
 *
 * @author Holger Brands
 */
public final class GlazedListsKTable {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedListsKTable() {
        throw new UnsupportedOperationException();
    }

    // Viewer convenience factory methods

    /**
     * Create a new {@link DefaultEventKTableModel} that uses elements from the
     * specified {@link EventList} as rows, and the specified {@link TableFormat}
     * to divide row objects across columns.
     *
     * <p>The returned model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * To do this programmatically, use {@link Display#syncExec(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     * </p>
     *
     * @param table the KTable the model is created for
     * @param source the {@link EventList}
     * @param tableFormat provides logic to divide row objects across columns.
     *      If the value implements the {@link KTableFormat} interface, those
     *      methods will be used to provide further details such as cell renderers,
     *      cell editors and row heights.
     */
    public static DefaultEventKTableModel eventKTableModel(KTable table, EventList source, TableFormat tableFormat) {
    	return new DefaultEventKTableModel(table, source, tableFormat);
    }

    /**
     * Create a new {@link DefaultEventKTableModel} that uses elements from the
     * specified {@link EventList} as rows, and the specified {@link TableFormat}
     * to divide row objects across columns.
     *
     * <p>The returned model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * </p>
     *
     * @param table the KTable the model is created for
     * @param source the {@link EventList}
     * @param tableFormat provides logic to divide row objects across columns.
     *      If the value implements the {@link KTableFormat} interface, those
     *      methods will be used to provide further details such as cell renderers,
     *      cell editors and row heights.
     */
    public static DefaultEventKTableModel eventKTableModelWithThreadProxyList(KTable table, EventList source, TableFormat tableFormat) {
    	final EventList proxySource = GlazedListsSWT.createSwtThreadProxyListWithLock(source, table.getDisplay());
    	return new DefaultEventKTableModel(table, proxySource, tableFormat, true);
    }
}