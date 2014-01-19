package ca.odell.glazedlists.jfreechart;

import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.category.CategoryDataset;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;

import java.util.List;

/**
 * This class helps adapt a pair of {@link EventList}s to the
 * {@link CategoryDataset} interface which is the necessary model for
 * JFreeChart views such as
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
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan="2"><font size="+2"><b>Extension: JFreeChart</b></font></td></tr>
 * <tr><td  colspan="2">This Glazed Lists <i>extension</i> requires the third party library <b>JFreeChart</b>.</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Tested Version:</b></td><td>1.0.0</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Home page:</b></td><td><a href="http://www.jfree.org/jfreechart/">http://www.jfree.org/jfreechart/</a></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>License:</b></td><td><a href="http://www.jfree.org/lgpl.php">LGPL</a></td></tr>
 * </td></tr>
 * </table>
 *
 * {@link CategoryDataset} is essentially a data model for a large matrix where
 * each unique row and unique column is identified by a {@link Comparable} key.
 * {@link CategoryDataset} requires its implementer to do three distinct things:
 *
 * <ul>
 *   <li> describe the unique row keys and their ordering
 *   <li> describe the unique column keys and their ordering
 *   <li> produce a Number given a row key and column key
 * </ul>
 *
 * This implementation of CategoryDataset uses two distinct EventList<Comparable>
 * to model the rows keys column keys. Thus, to add, remove, or change rows or
 * columns you need only modify the corresponding EventList.
 *
 * Extracting a data value from a combination of a row key and column key is
 * done via a FunctionList.Function<Context, Number>. Specifically, this
 * implementation creates an instance of a {@link Context} with information
 * about which row key and column key to use when producing a data value for
 * that position in the matrix. The logic of exactly how to produce that data
 * value is left to the particular implementation of the function.
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
public final class SimpleEventCategoryDataset<R extends Comparable, C extends Comparable> extends AbstractDataset implements CategoryDataset, ListEventListener<Comparable> {

    /** The single immutable DatasetChangeEvent we fire each time this Dataset is changed. */
    private final DatasetChangeEvent immutableChangeEvent = new DatasetChangeEvent(this, this);

    /** An ordered list of keys identifying the chart's rows. */
    protected EventList<R> rowKeys;

    /** An ordered list of keys identifying the chart's columns. */
    protected EventList<C> columnKeys;

    /** A function for mapping a {rowKey, columnKey} -> Number which represents the data point */
    private final FunctionList.Function<Context<R, C>, Number> valueMaker;

    /** The single Context which is used to describe which numeric data point we are asking about. This object is reused a lot. */
    private final Context context;

    /**
     * The given <code>rowKeys</code> and <code>columnKeys</code> represent the
     * ordered lists of row and column keys presented by this
     * {@link CategoryDataset}. Any modifications to either of those
     * {@link EventList}s trigger this dataset to broadcast a change.
     *
     * <p>The given <code>valueMaker</code> is used to produce a {@link Number}
     * value for a given pair: {row key, column key}. This effectively creates
     * the data which is charted.
     *
     * @param rowKeys the keys identifying the unique rows
     * @param columnKeys the keys identifying the unique columns
     * @param valueMaker the function which maps {row key, column key} -> Number
     */
    public SimpleEventCategoryDataset(EventList<R> rowKeys, EventList<C> columnKeys, FunctionList.Function<Context<R, C>, Number> valueMaker) {
        this.rowKeys = rowKeys;
        this.columnKeys = columnKeys;
        this.valueMaker = valueMaker;
        this.context = new Context<R, C>(rowKeys, columnKeys);

        // listen for changes in the rows or columns and broadcast our own change events
        this.rowKeys.addListEventListener(this);
        this.columnKeys.addListEventListener(this);
    }

    /**
     * Returns the row key for a given index.
     *
     * @param row the row index (zero-based)
     * @return the row key
     *
     * @throws IndexOutOfBoundsException if <code>row</code> is out of bounds
     */
    @Override
    public Comparable getRowKey(int row) {
        return rowKeys.get(row);
    }

    /**
     * Returns the row index for a given key.
     *
     * @param key the row key
     * @return the row index, or <code>-1</code> if the key is unrecognized
     */
    @Override
    public int getRowIndex(Comparable key) {
        return rowKeys.indexOf(key);
    }

    /**
     * Returns the row keys.
     */
    @Override
    public List getRowKeys() {
        return rowKeys;
    }

    /**
     * Returns the number of rows in the table.
     */
    @Override
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
    @Override
    public Comparable getColumnKey(int column) {
        return columnKeys.get(column);
    }

    /**
     * Returns the column index for a given key.
     *
     * @param key the column key
     * @return the column index, or <code>-1</code> if the key is unrecognized
     */
    @Override
    public int getColumnIndex(Comparable key) {
        return columnKeys.indexOf(key);
    }

    /**
     * Returns the column keys.
     */
    @Override
    public List getColumnKeys() {
        return columnKeys;
    }

    /**
     * Returns the number of columns in the table.
     */
    @Override
    public int getColumnCount() {
        return columnKeys.size();
    }

    /**
     * Returns the value associated with the specified keys.
     *
     * @param rowKey the row key (<code>null</code> not permitted)
     * @param columnKey the column key (<code>null</code> not permitted)
     * @return the value
     *
     * @throws org.jfree.data.UnknownKeyException if either key is not recognized
     */
    @Override
    public Number getValue(Comparable rowKey, Comparable columnKey) {
        context.update(rowKey, columnKey);
        return valueMaker.evaluate(context);
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
    @Override
    public Number getValue(int row, int column) {
        context.update(row, column);
        return valueMaker.evaluate(context);
    }

    /**
     * We override this method for speed reasons, since the super needlessly
     * constructs a new DatasetChangeEvent each time this method is called.
     */
    @Override
    protected void fireDatasetChanged() {
        notifyListeners(immutableChangeEvent);
    }

    /**
     * Watch the row key and column key lists for changes and rebroadcast those
     * ListEvents as DatasetChangeEvents.
     */
    @Override
    public void listChanged(ListEvent<Comparable> listChanges) {
        fireDatasetChanged();
    }

    /**
     * Releases the resources consumed by this {@link SimpleEventCategoryDataset}
     * so that it may eventually be garbage collected.
     *
     * <p>A {@link SimpleEventCategoryDataset} will be garbage collected without
     * a call to {@link #dispose()}, but not before its row key and column key
     * {@link EventList}s are garbage collected. By calling {@link #dispose()},
     * you allow the {@link SimpleEventCategoryDataset} to be garbage collected
     * before its source {@link EventList}s. This is necessary for situations
     * where a {@link SimpleEventCategoryDataset} is short-lived but its source
     * {@link EventList}s are long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an {@link SimpleEventCategoryDataset} after it has
     * been disposed.
     */
    public void dispose() {
        rowKeys.removeListEventListener(this);
        columnKeys.removeListEventListener(this);
    }

    /**
     * This object describes a cell within the "matrix" that is defined by
     * {@link CategoryDataset}. Specifically, it does this by providing the
     * row key and column key of the cell in question.
     */
    public static final class Context<R, C> {
        private final EventList<R> rowKeys;
        private final EventList<C> columnKeys;

        private int rowIndex;
        private int columnIndex;
        private R rowKey;
        private C columnKey;

        public Context(EventList<R> rowKeys, EventList<C> columnKeys) {
            this.rowKeys = rowKeys;
            this.columnKeys = columnKeys;
        }

        /**
         * This method reconfigures this Context to describe the cell at the
         * intersection of the row and column with the given indices.
         *
         * @param rowIndex the index of the row
         * @param columnIndex the index of the column
         */
        private void update(int rowIndex, int columnIndex) {
            this.rowIndex = rowIndex;
            this.columnIndex = columnIndex;
            this.rowKey = null;
            this.columnKey = null;
        }

        /**
         * This method reconfigures this Context to describe the cell at the
         * intersection of the row and column with the given keys.
         *
         * @param rowKey the key of the row
         * @param columnKey the key of the column
         */
        private void update(R rowKey, C columnKey) {
            this.rowIndex = -1;
            this.columnIndex = -1;
            this.rowKey = rowKey;
            this.columnKey = columnKey;
        }

        public EventList<R> getRowKeys() { return rowKeys; }
        public EventList<C> getColumnKeys() { return columnKeys; }

        /**
         * Returns the row index for the row of the data value to retrieve.
         */
        public int getRowIndex() {
            if (rowIndex == -1)
                rowIndex = rowKeys.indexOf(rowKey);
            return rowIndex;
        }

        /**
         * Returns the column index for the column of the data value to retrieve.
         */
        public int getColumnIndex() {
            if (columnIndex == -1)
                columnIndex = columnKeys.indexOf(columnKey);
            return columnIndex;
        }

        /**
         * Returns the row key for the row of the data value to retrieve.
         */
        public R getRowKey() {
            if (rowKey == null)
                rowKey = rowKeys.get(rowIndex);
            return rowKey;
        }

        /**
         * Returns the column key for the column of the data value to retrieve.
         */
        public C getColumnKey() {
            if (columnKey == null)
                columnKey = columnKeys.get(columnIndex);
            return columnKey;
        }
    }
}