/* Glazed Lists                                                 (c) 2003-2005 */
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

import java.util.Comparator;
import java.util.List;

/**
 * This class adapts an {@link EventList} to the JFreeChart PieDataset
 * interface. Changes to the backing {@link EventList} are rebroadcast as
 * changes to this PieDataset.
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
        final List group = this.groupingList.get(this.getIndex(key));
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
        final List group = this.groupingList.get(index);
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
        this.groupingList.dispose();
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