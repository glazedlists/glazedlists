/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.jfreechart;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;

/**
 * An implementation of a CategoryDataset that is backed by a List&lt;Calculation&gt;
 * where each Calculation reports a numeric value. Typical usage of this class
 * resembles:
 *
 * <pre>
 * // create the numeric Calculations we want to chart
 * final Calculation&lt;Number&gt; appleStockPrice = ...
 * final Calculation&lt;Number&gt; googleStockPrice = ...
 * final Calculation&lt;Number&gt; microsoftStockPrice = ...
 *
 * // create the dataset
 * final CalculationCategoryDataset dataset = new CalculationCategoryDataset();
 *
 * // add the numeric Calculations into the dataset; any change to the
 * // Calculations induces a redraw of the corresponding chart
 * dataset.getCalculations().add(appleStockPrice);
 * dataset.getCalculations().add(googleStockPrice);
 * dataset.getCalculations().add(microsoftStockPrice);
 *
 * ...
 *
 * // when the dataset is no longer needed, dispose() of it so it can be GC'd
 * dataset.dispose();
 * </pre>
 *
 * <p><strong><font color="#FF0000">Note:</font></strong> If this
 * {@link CalculationCategoryDataset} is being shown in a Swing User Interface,
 * and thus Dataset Changes should be broadcast on the Swing Event Dispatch
 * Thread, it is the responsibility of the <strong>caller</strong> to ensure
 * that {@link ListEvent}s arrive on the Swing EDT.
 *
 * @see ca.odell.glazedlists.swing.GlazedListsSwing#swingThreadProxyList
 *
 * @author James Lemieux
 */
public class CalculationCategoryDataset extends AbstractDataset implements CategoryDataset, ListEventListener<Calculation<? extends Number>> {

    /** used when extracting Names from Calculations to be used as row keys */
    private static final FunctionList.Function<Calculation<? extends Number>, String> NAME_FUNCTION = new NameFunction();

    /** The single immutable DatasetChangeEvent we fire each time this Dataset is changed. */
    private final DatasetChangeEvent immutableChangeEvent = new DatasetChangeEvent(this, this);

    /** a single column key for each and every row */
    private static final List<Integer> COLUMN_KEYS = Collections.singletonList(new Integer(0));

    /** the Calculations providing the values of this CategoryDataset */
    private final ObservableElementList<Calculation<? extends Number>> calculations = new ObservableElementList<>(new BasicEventList<Calculation<? extends Number>>(), GlazedLists.beanConnector(Calculation.class));

    /** the names of each of the {@link #calculations} reported as the row keys */
    private final FunctionList<Calculation<? extends Number>, String> rowKeys = new FunctionList<>(calculations, NAME_FUNCTION);

    public CalculationCategoryDataset() {
        calculations.addListEventListener(this);
    }

    /**
     * Constructs a CategoryDataset backed by the given <code>calculations</code>.
     * Each {@link Calculation} is a single-valued series in the CategoryDataset.
     *
     * @param calculations the calculations of the dataset
     */
    @SafeVarargs
    public CalculationCategoryDataset(Calculation<? extends Number>... calculations) {
        this();
        getCalculations().addAll(Arrays.asList(calculations));
    }

    /**
     * Returns the mutable List of Calculations that create the data values in
     * this CategoryDataset. Any Calculation objects added to / removed from
     * this List will be added to / removed from this CategoryDataset.
     *
     * @return the mutable List of Calculations backing this CategoryDataset
     */
    public List<Calculation<? extends Number>> getCalculations() { return calculations; }

    //
    // Methods for reporting row key information
    //
    @Override
    public Comparable getRowKey(int row) { return rowKeys.get(row); }
    @Override
    public int getRowIndex(Comparable key) { return rowKeys.indexOf(key); }
    @Override
    public List getRowKeys() { return rowKeys; }
    @Override
    public int getRowCount() { return rowKeys.size(); }

    //
    // Methods for reporting column key information
    //
    @Override
    public Comparable getColumnKey(int column) { return COLUMN_KEYS.get(column); }
    @Override
    public int getColumnIndex(Comparable key) { return COLUMN_KEYS.indexOf(key); }
    @Override
    public List getColumnKeys() { return COLUMN_KEYS; }
    @Override
    public int getColumnCount() { return COLUMN_KEYS.size(); }

    //
    // Methods for reporting value information
    //
    @Override
    public Number getValue(Comparable rowKey, Comparable columnKey) { return calculations.get(getRowIndex(rowKey)).getValue(); }
    @Override
    public Number getValue(int row, int column) { return calculations.get(row).getValue(); }

    /**
     * This listener rebroadcasts ListEvents as DatasetChangeEvents.
     */
    @Override
    public void listChanged(ListEvent<Calculation<? extends Number>> listChanges) {
        fireDatasetChanged();
    }

    /**
     * Releases the resources consumed by this {@link CalculationCategoryDataset}
     * so that it may eventually be garbage collected.
     *
     * <p>A {@link CalculationCategoryDataset} will be garbage collected without
     * a call to {@link #dispose()}, but not before its source {@link Calculation}s
     * are all garbage collected. By calling {@link #dispose()}, you allow the
     * {@link CalculationCategoryDataset} to be garbage collected before its
     * source {@link Calculation}s. This is necessary for situations where a
     * {@link CalculationCategoryDataset} is short-lived but its source
     * {@link Calculation}s are long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an {@link CalculationCategoryDataset} after it has
     * been disposed.
     */
    public void dispose() {
        calculations.removeListEventListener(this);
        calculations.dispose();
    }

    /**
     * We override this method for speed reasons, since the super needlessly
     * constructs a new DatasetChangeEvent each time this method is called.
     */
    @Override
    protected void fireDatasetChanged() {
        notifyListeners(immutableChangeEvent);
    }

    private static class NameFunction implements FunctionList.Function<Calculation<? extends Number>, String> {
        @Override
        public String evaluate(Calculation<? extends Number> sourceValue) {
            return sourceValue.getName();
        }
    }
}