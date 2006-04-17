/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.jfreechart;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.general.DatasetChangeEvent;

import java.util.Comparator;
import java.util.List;

/**
 * This class adapts an {@link EventList} to the JFreeChart PieDataset
 * interface. Changes to the backing {@link EventList} are rebroadcast as
 * changes to this PieDataset.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan="2"><font size="+2"><b>Extension: JFreeChart</b></font></td></tr>
 * <tr><td  colspan="2">This Glazed Lists <i>extension</i> requires the third party library <b>JFreeChart</b>.</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Tested Version:</b></td><td>1.0.0</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Home page:</b></td><td><a href="http://www.jfree.org/jfreechart/">http://www.jfree.org/jfreechart/</a></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>License:</b></td><td><a href="http://www.jfree.org/lgpl.php">LGPL</a></td></tr>
 * </td></tr>
 * </table>
 *
 * <p> Note: The DataEvents broadcasted by this class occur on the Thread the
 * ListEvents arrive on. If this PieDataset is attached to a swing component,
 * it is the responsibility of the client to ensure that the ListEvents are
 * arriving on the Swing Event Dispatch Thread, perhaps by using the
 * {@link ca.odell.glazedlists.impl.swing.SwingThreadProxyEventList}.
 *
 * @see ca.odell.glazedlists.impl.swing.SwingThreadProxyEventList
 *
 * @author James Lemieux
 */
public class EventListPieDataset extends AbstractDataset implements PieDataset {

    /** The single immutable DatasetChangeEvent we fire each time this Dataset is changed. */
    private final DatasetChangeEvent immutableChangeEvent = new DatasetChangeEvent(this, this);

    // the list that groups the data into sections of the pie
    private final GroupingList groupingList;
    // a function that extract keys from the groups
    private final FunctionList<List, Comparable> functionList;

    // listen to changes in the {@link groupingList} and rebroadcast them as changes to this PieDataset
    private final ListEventListener datasetEventListener = new DatasetEventListener();

    /**
     * Adapts the given <code>source</code> to the PieDataset interface by
     * applying the <code>groupingComparator</code> to forms groups to be
     * represented in the pie chart. The given <code>keyFunction</code> is then
     * applied to produce the key for a group.
     *
     * @param source the {@link EventList} containg the data to chart
     * @param keyFunction produces the keys of the groups in the pie chart
     * @param groupingComparator produces the groups in the pie chart
     */
    public EventListPieDataset(EventList source, FunctionList.Function<List, Comparable> keyFunction, Comparator groupingComparator) {
        this.groupingList = new GroupingList(source, groupingComparator);
        this.functionList = new FunctionList<List, Comparable>(this.groupingList, keyFunction);

        this.groupingList.addListEventListener(this.datasetEventListener);
    }

    /**
     * Returns the key of the value at the given <code>index</code>.
     *
     * @param index the item index (zero-based)
     * @return the key
     *
     * @throws IndexOutOfBoundsException if <code>index</code> is out of bounds
     */
    public Comparable getKey(int index) {
        return this.functionList.get(index);
    }

    /**
     * Returns the index for a given key.
     *
     * @param key the key
     * @return the index, or <code>-1</code> if the key is unrecognised
     */
    public int getIndex(Comparable key) {
        return this.functionList.indexOf(key);
    }

    /**
     * Returns the keys for the values in this PieDataset. Note that you can
     * access the values in this PieDataset by key or by index. For this
     * reason, the key order is important - this method should return the keys
     * in order.  The returned list may be unmodifiable.
     *
     * @return the keys (never <code>null</code>).
     */
    public List getKeys() {
        return this.functionList;
    }

    /**
     * Returns the value for a given key.
     *
     * @param key the key
     * @return the value (possibly <code>null</code>)
     *
     * @throws org.jfree.data.UnknownKeyException if the key is not recognised
     */
    public Number getValue(Comparable key) {
        final List group = (List) this.groupingList.get(this.getIndex(key));
        return new Integer(group.size());
    }

    /**
     * Returns the number of items (values).
     */
    public int getItemCount() {
        return this.functionList.size();
    }

    /**
     * Returns the value at the given <code>index</code>.
     *
     * @param index the index of interest (zero-based index).
     * @return the value
     */
    public Number getValue(int index) {
        final List group = (List) this.groupingList.get(index);
        return new Integer(group.size());
    }

    /**
     * Releases the resources consumed by this EventListPieDataset so that it
     * may eventually be garbage collected. This is important when the
     * {@link EventList} that backs this EventListPieDataset should outlast
     * this EventListPieDataset. This method should be called as soon as this
     * EventListPieDataset is no longer useful.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an EventListPieDataset after it has been disposed.
     */
    public void dispose() {
        this.functionList.dispose();
        this.groupingList.removeListEventListener(this.datasetEventListener);
        this.groupingList.dispose();
    }

    /**
     * We override this method for speed reasons, since the super needlessly
     * constructs a new DatasetChangedEvent each time this method is called.
     */
    protected void fireDatasetChanged() {
        notifyListeners(immutableChangeEvent);
    }

    /**
     * This listener rebroadcasts ListEvents as DatasetChangeEvents.
     */
    private class DatasetEventListener implements ListEventListener {
        public void listChanged(ListEvent listChanges) {
            fireDatasetChanged();
        }
    }
}