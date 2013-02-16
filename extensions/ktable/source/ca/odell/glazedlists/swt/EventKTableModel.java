/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableModel;

import org.eclipse.swt.widgets.Display;

/**
 * A {@link KTableModel} that displays an {@link EventList}. Each element of the
 * {@link EventList} corresponds to a row in the {@link KTableModel}. The columns
 * of the table must be specified using a {@link TableFormat}.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan="2"><font size="+2"><b>Extension: KTable</b></font></td></tr>
 * <tr><td  colspan="2">This Glazed Lists <i>extension</i> requires the third party library <b>KTable</b>.</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Tested Version:</b></td><td>2.1.2</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Home page:</b></td><td><a href="http://ktable.sourceforge.net/">http://ktable.sourceforge.net/</a></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>License:</b></td><td><a href="http://www.eclipse.org/legal/epl-v10.html">Eclipse Public License</a></td></tr>
 * </td></tr>
 * </table>
 *
 * <p>The EventKTableModel class is <strong>not thread-safe</strong>. Unless otherwise
 * noted, all methods are only safe to be called from the SWT event dispatch thread.
 * To do this programmatically, use {@link org.eclipse.swt.widgets.Display#asyncExec(Runnable)}.
 *
 *
 * @deprecated Use {@link DefaultEventKTableModel} instead. This class will be removed in the GL
 *             2.0 release. The wrapping of the source list with an SWT-EDT safe list has been
 *             determined to be undesirable (it is better for the user to provide their own SWT-EDT
 *             safe list).
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author Holger Brands
 */
public class EventKTableModel extends DefaultEventKTableModel {

	/** indicates, if source list has to be disposed */
    private boolean disposeSource;

    /**
     * Create a new {@link EventKTableModel} that uses elements from the
     * specified {@link EventList} as rows, and the specified {@link TableFormat}
     * to divide row objects across columns.
     *
     * @param table the KTable the model is created for
     * @param source the {@link EventList}
     * @param tableFormat provides logic to divide row objects across columns.
     *      If the value implements the {@link KTableFormat} interface, those
     *      methods will be used to provide further details such as cell renderers,
     *      cell editors and row heights.
     */
    public EventKTableModel(KTable table, EventList source, TableFormat tableFormat) {
    	super(table, createProxyList(source, table.getDisplay()), tableFormat);
    	disposeSource = (this.source != source);
    }

    /**
     * Releases the resources consumed by this {@link EventKTableModel} so that it
     * may eventually be garbage collected.
     *
     * <p>A {@link EventKTableModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventKTableModel}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where a {@link EventKTableModel} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link EventKTableModel} after it has been disposed.
     */
    @Override
    public void dispose() {
        if (disposeSource) source.dispose();
        super.dispose();
    }

    /**
     * while holding a read lock, this method wraps the given source list with a SWT thread
     * proxy list.
     */
    private static EventList createProxyList(EventList source, Display display) {
    	return GlazedListsSWT.createProxyListIfNecessary(source, display);
    }
}