/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.jfreechart;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;

import java.util.*;

/**
 * This class helps adapt an {@link EventList} to the {@link CategoryDataset}
 * interface which is the necessary model for JFreeChart views such as
 *
 * <ul>
 *   <li> Bar Charts
 *   <li> Stacked Bar Charts
 *   <li> Area Charts
 *   <li> Stacked Area Charts
 *   <li> Line Charts
 *   <li> Waterfall Charts
 * </ul>
 *
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan="2"><font size="+2"><b>Extension: JFreeChart</b></font></td></tr>
 * <tr><td  colspan="2">This Glazed Lists <i>extension</i> requires the third party library <b>JFreeChart</b>.</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Tested Version:</b></td><td>1.0.0</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Home page:</b></td><td><a href="http://www.jfree.org/jfreechart/">http://www.jfree.org/jfreechart/</a></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>License:</b></td><td><a href="http://www.jfree.org/lgpl.php">LGPL</a></td></tr>
 * </td></tr>
 * </table>
 *
 * <p>If it is possible to create a pipeline of list transformations such that
 * your source is an {@link EventList} of {@link ValueSegment} objects then
 * this class can aid you in showing and maintaining that data within a
 * JFreeChart. It handles the maintenance of efficient data structures for
 * implementing {@link #getValue(int, int)} and leaves subclasses the task
 * of defining the logic for maintaining the lists of rowkeys and columnkeys.
 *
 * <p> If the rowkeys and columnkeys are statically determined, they can be set
 * like so:
 *
 * <pre>
 *   EventList myValueSegments = ...
 *   CategoryDataset dataset = new EventListCategoryDataset(myValueSegments);
 *   dataset.getColumnKeys().add(...);
 *   dataset.getRowKeys().add(...);
 * </pre>
 *
 * If the rowkeys and/or columnkeys are dynamically maintained as
 * {@link ValueSegment}s are added and removed from the {@link EventList}, then
 * two hooks have been given for the benefit of subclasses:
 *
 * <ul>
 *   <li> {@link #postInsert} is a hook to process the insertion of new source.
 *        This is commonly where rows or columns may be created by adding their
 *        keys to the rowKey or columnKey lists.
 *
 *   <li> {@link #postDelete} is a hook to process the deletion of existing
 *        source. This is commonly where rows or columns may be removed by
 *        removing their keys from the rowKey or columnKey lists.
 * </ul>
 *
 * <p><strong><font color="#FF0000">Note:</font></strong> If this
 * {@link EventListCategoryDataset} is being shown in a Swing User Interface,
 * and thus Dataset Changes should be broadcast on the Swing Event Dispatch
 * Thread, it is the responsibility of the <strong>caller</strong> to ensure
 * that {@link ListEvent}s arrive on the Swing EDT.
 *
 * @see ca.odell.glazedlists.swing.GlazedListsSwing#swingThreadProxyList
 *
 * @author James Lemieux
 */
public abstract class EventListCategoryDataset<R extends Comparable, C extends Comparable> extends AbstractDataset implements CategoryDataset {

    /** The single immutable DatasetChangeEvent we fire each time this Dataset is changed. */
    private final DatasetChangeEvent immutableChangeEvent = new DatasetChangeEvent(this, this);

    /** Keep a private copy of the contents of the source list in order to access deleted elements. */
    private final List<ValueSegment<C,R>> sourceCopy;

    /** The source of the data to be charted. */
    private final EventList<ValueSegment<C,R>> source;

    /** Listens to changes in the source EventList and rebroadcasts them as changes to this CategoryDataset */
    private final DatasetEventListener datasetEventListener = new DatasetEventListener();

    /** An ordered list of keys identifying the chart's rows. */
    protected List<? extends Comparable> rowKeys;

    /** An ordered list of keys identifying the chart's columns. */
    protected List<? extends Comparable> columnKeys;

    /**
     * This is the main data structure which organizes the data to make
     * calculating counts of items within a continuum a fast operation. It maps
     * each unique value to a {@link TreePair}. Each tree within the
     * {@link TreePair} orders the value according to the starting value of its
     * {@link ValueSegment} or the ending value of its {@link ValueSegment}.
     * The count of all such values within a range can thus be calculated.
     */
    private Map<R,TreePair<C>> valueToTreePairs = new HashMap<R,TreePair<C>>();

    /**
     * Constructs an implementation of {@link CategoryDataset} which presents
     * the data contained in the given <code>source</code>.
     *
     * @param source the {@link EventList} of data to be charted
     */
    public EventListCategoryDataset(EventList<ValueSegment<C,R>> source) {
        this.source = source;

        // make a copy of the source list's content
        this.sourceCopy = new ArrayList<ValueSegment<C,R>>(source.size());
        this.sourceCopy.addAll(source);

        this.rebuildRowAndColumnKeyList();

        // begin listening to changes in the source list
        this.source.addListEventListener(this.datasetEventListener);
    }

    /**
     * A convenience method to rebuild the lists of rowKeys and columnKeys.
     */
    private void rebuildRowAndColumnKeyList() {
        this.columnKeys = createColumnKeyList();
        this.rowKeys = createRowKeyList();
    }

    /**
     * A local factory method for creating the list containing the row keys.
     */
    protected List<? extends Comparable> createRowKeyList() {
        return new ArrayList<Comparable>();
    }

    /**
     * A local factory method for creating the list containing the column keys.
     */
    protected List<? extends Comparable> createColumnKeyList() {
        return new ArrayList<Comparable>();
    }

    /**
     * Returns the row key for a given index.
     *
     * @param row the row index (zero-based)
     * @return the row key
     *
     * @throws IndexOutOfBoundsException if <code>row</code> is out of bounds
     */
    public Comparable getRowKey(int row) {
        return rowKeys.get(row);
    }

    /**
     * Returns the row index for a given key.
     *
     * @param key the row key
     * @return the row index, or <code>-1</code> if the key is unrecognized
     */
    public int getRowIndex(Comparable key) {
        return rowKeys.indexOf(key);
    }

    /**
     * Returns the row keys.
     */
    public List getRowKeys() {
        return rowKeys;
    }

    /**
     * Returns the number of rows in the table.
     */
    public int getRowCount() {
        return rowKeys.size();
    }

    /**
     * Returns the column key for a given index.
     *
     * @param column the column index (zero-based)
     * @return the column key
     *
     * @throws IndexOutOfBoundsException if <code>column</code> is out of bounds
     */
    public Comparable getColumnKey(int column) {
        return columnKeys.get(column);
    }

    /**
     * Returns the column index for a given key.
     *
     * @param key the column key
     * @return the column index, or <code>-1</code> if the key is unrecognized
     */
    public int getColumnIndex(Comparable key) {
        return columnKeys.indexOf(key);
    }

    /**
     * Returns the column keys.
     */
    public List getColumnKeys() {
        return columnKeys;
    }

    /**
     * Returns the number of columns in the table.
     */
    public int getColumnCount() {
        return columnKeys.size();
    }

    /**
     * Returns a value from the table.
     *
     * @param row the row index (zero-based)
     * @param column the column index (zero-based)
     * @return the value (possibly <code>null</code>)
     *
     * @throws IndexOutOfBoundsException if the <code>row</code>
     *      or <code>column</code> is out of bounds
     */
    public Number getValue(int row, int column) {
        return getValue(getRowKey(row), getColumnKey(column));
    }

    /**
     * Returns the value associated with the specified keys.
     *
     * @param rowKey the row key (<code>null</code> not permitted)
     * @param columnKey the column key (<code>null</code> not permitted)
     * @return the value
     *
     * @throws UnknownKeyException if either key is not recognized
     */
    public abstract Number getValue(Comparable rowKey, Comparable columnKey);

    /**
     * Releases the resources consumed by this {@link EventListCategoryDataset}
     * so that it may eventually be garbage collected.
     *
     * <p>A {@link EventListCategoryDataset} will be garbage collected without
     * a call to {@link #dispose()}, but not before its source {@link EventList}
     * is garbage collected. By calling {@link #dispose()}, you allow the
     * {@link EventListCategoryDataset} to be garbage collected before its
     * source {@link EventList}. This is necessary for situations where a
     * {@link EventListCategoryDataset} is short-lived but its source
     * {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an {@link EventListCategoryDataset} after it has
     * been disposed.
     */
    public void dispose() {
        this.source.removeListEventListener(this.datasetEventListener);
    }

    /**
     * This convenience method clears all value, rowkey, and columnkey data
     * structures.
     */
    private void clear() {
        sourceCopy.clear();
        valueToTreePairs.clear();
        rowKeys.clear();
        rebuildRowAndColumnKeyList();
    }

    /**
     * Returns the {@link TreePair} associated with the given
     * <code>rowKey</code>.
     */
    private TreePair<C> getTreePair(R rowKey) {
        return valueToTreePairs.get(rowKey);
    }

    /**
     * Returns the number of values associated with the given <code>rowKey</code>.
     */
    public int getCount(R rowKey) {
        final TreePair treePair = getTreePair(rowKey);
        return treePair == null ? 0 : treePair.size();
    }

    /**
     * Returns the number of values associated with the given <code>rowKey</code>
     * between the given <code>start</code> and <code>end</code> values.
     */
    public int getCount(R rowKey, C start, C end) {
        // fetch the relevant pair of trees
        final TreePair<C> treePair = getTreePair(rowKey);

        // ensure we found something
        if (treePair == null)
            throw new UnknownKeyException("unrecognized rowKey: " + rowKey);

        // return the number of values between start and end
        return treePair.getCount(start, end);
    }

    /**
     * This no-op method is left as a hook for subclasses. It is called after
     * an element has been inserted into the value data structure in this
     * dataset. Subclasses typically override this method to provide extra
     * logic for maintaining the lists of rowkeys and columnkeys dynamically.
     *
     * @param valueSegment the data element inserted into this data set
     */
    protected void postInsert(ValueSegment<C,R> valueSegment) { }

    /**
     * This no-op method is left as a hook for subclasses. It is called after
     * an element has been removed from the value data structure in this
     * dataset. Subclasses typically override this method to provide extra
     * logic for maintaining the lists of rowkeys and columnkeys dynamically.
     *
     * @param valueSegment the data element removed from this data set
     */
    protected void postDelete(ValueSegment<C,R> valueSegment) { }

    /**
     * We override this method for speed reasons, since the super needlessly
     * constructs a new DatasetChangedEvent each time this method is called.
     */
    protected void fireDatasetChanged() {
        notifyListeners(immutableChangeEvent);
    }

    /**
     * This listener maintains a fast set of TreePairs for each unique value
     * found in the ValueSegments of the source list. The TreePairs, in turn,
     * are efficient at answering questions about the number of values which
     * fall in the certain segment of the value's continuum.
     *
     * This listener also rebroadcasts ListEvents as DatasetChangeEvents.
     */
    private class DatasetEventListener implements ListEventListener<ValueSegment<C,R>> {
        public void listChanged(ListEvent<ValueSegment<C,R>> listChanges) {
            // speed up the case when listChanges describes a total clearing of
            // the data structures, (which occurs when switching between projects)
            if (listChanges.getSourceList().isEmpty()) {
                clear();

            } else {
                while (listChanges.next()) {
                    final int type = listChanges.getType();
                    final int index = listChanges.getIndex();

                    if (type == ListEvent.INSERT) {
                        // fetch the segment that was inserted
                        final ValueSegment<C,R> segment = listChanges.getSourceList().get(index);
                        // fetch the TreePair for the segment's value
                        TreePair<C> treePair = getTreePair(segment.getValue());

                        // if a TreePair has not been created for the segment's value, create one now
                        if (treePair == null) {
                            treePair = new TreePair<C>();
                            valueToTreePairs.put(segment.getValue(), treePair);
                        }

                        // insert the segment into the TreePair
                        treePair.insert(segment);
                        // maintain our copy of the source contents
                        sourceCopy.add(index, segment);
                        // call into a hook for custom handling of this insert by subclasses
                        postInsert(segment);

                    } else if (type == ListEvent.UPDATE) {
                        // fetch the segments involved
                        final ValueSegment<C,R> newSegment = listChanges.getSourceList().get(index);
                        final ValueSegment<C,R> oldSegment = sourceCopy.set(index, newSegment);

                        // fetch the TreePairs involved
                        final TreePair<C> oldTreePair = getTreePair(oldSegment.getValue());
                        final TreePair<C> newTreePair = getTreePair(newSegment.getValue());

                        // delete the segment from the oldTreePair
                        oldTreePair.delete(oldSegment);
                        postDelete(oldSegment);

                        // add the segment to the newTreePair
                        newTreePair.insert(newSegment);
                        postInsert(newSegment);

                    } else if (type == ListEvent.DELETE) {
                        // fetch the segment that was removed
                        final ValueSegment<C,R> segment = sourceCopy.remove(index);
                        // fetch the TreePair for the segment's value
                        final TreePair<C> treePair = getTreePair(segment.getValue());

                        // delete the segment from the TreePair
                        treePair.delete(segment);
                        // call into a hook for custom handling of this delete by subclasses
                        postDelete(segment);
                    }
                }
            }

            // indicate the dataset contents have changed
            fireDatasetChanged();
        }
    }
}