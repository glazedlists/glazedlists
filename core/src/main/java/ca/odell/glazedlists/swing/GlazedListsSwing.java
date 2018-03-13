/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ThresholdList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.swing.DefaultTableModelEventAdapterFactory;
import ca.odell.glazedlists.impl.swing.LowerThresholdRangeModel;
import ca.odell.glazedlists.impl.swing.ManyToOneTableModelEventAdapterFactory;
import ca.odell.glazedlists.impl.swing.SwingThreadProxyEventList;
import ca.odell.glazedlists.impl.swing.UpperThresholdRangeModel;
import ca.odell.glazedlists.swing.TableModelEventAdapter.Factory;

import javax.swing.BoundedRangeModel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

/**
 * A factory for creating all sorts of objects to be used with Glazed Lists.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class GlazedListsSwing {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedListsSwing() {
        throw new UnsupportedOperationException();
    }

    // EventLists // // // // // // // // // // // // // // // // // // // // //

    /**
     * Wraps the source in an {@link EventList} that fires all of its update
     * events from the Swing event dispatch thread.
     */
    public static <E> TransformedList<E, E> swingThreadProxyList(EventList<E> source) {
        return new SwingThreadProxyEventList<>(source);
    }

    /**
     * Returns true iff <code>list</code> is an {@link EventList} that fires
     * all of its update events from the Swing event dispatch thread.
     */
    public static boolean isSwingThreadProxyList(EventList list) {
        return list instanceof SwingThreadProxyEventList;
    }

    // ThresholdRangeModels // // // // // // // // // // // // // // // // //

    /**
     * Creates a model that manipulates the lower bound of the specified
     * ThresholdList.  The ThresholdList linked to this model type will contain
     * a range of Objects between the results of getValue() and getMaximum()
     * on the BoundedRangeModel.
     */
    public static BoundedRangeModel lowerRangeModel(ThresholdList target) {
        return new LowerThresholdRangeModel(target);
    }

    /**
     * Creates a model that manipulates the upper bound of the specified
     * ThresholdList.  The ThresholdList linked to this model type will contain
     * a range of Objects between the results of getMinimum() and getValue()
     * on the BoundedRangeModel.
     */
    public static BoundedRangeModel upperRangeModel(ThresholdList target) {
        return new UpperThresholdRangeModel(target);
    }

    // TableModel convenience creators

    /**
     * Creates a new table model that extracts column data from the given
     * <code>source</code> using the the given <code>tableFormat</code>.
     *
     * <p>The returned table model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the event dispatch thread.
     * To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.</p>
     *
     * @param source the EventList that provides the row objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     */
    public static <E> AdvancedTableModel<E> eventTableModel(EventList<E> source, TableFormat<? super E> tableFormat) {
        return new DefaultEventTableModel<>(source, tableFormat);
    }

    /**
     * Creates a new table model that extracts column data from the given <code>source</code>
     * using the the given <code>tableFormat</code>. While holding a read lock,
     * this method wraps the source list using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.
     * <p>
     * The returned table model is <strong>not thread-safe</strong>. Unless otherwise noted, all
     * methods are only safe to be called from the event dispatch thread.
     * </p>
     *
     * @param source the EventList that provides the row objects
     * @param tableFormat the object responsible for extracting column data from the row objects
     */
    public static <E> AdvancedTableModel<E> eventTableModelWithThreadProxyList(EventList<E> source, TableFormat<? super E> tableFormat) {
        final EventList<E> proxySource = createSwingThreadProxyList(source);
        return new DefaultEventTableModel<>(proxySource, true, tableFormat);
    }

    /**
     * Creates a new table model that extracts column data from the given
     * <code>source</code> using the the given <code>tableFormat</code>.
     *
     * <p>The <code>eventAdapterFactory</code> is used to create a {@link TableModelEventAdapter},
     * which is then used by the created table model to convert list events to table model events.</p>
     *
     * <p>The returned table model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the event dispatch thread.
     * To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.</p>
     *
     * @param source the EventList that provides the row objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     * @param eventAdapterFactory factory for creating a {@link TableModelEventAdapter}
     */
    public static <E> AdvancedTableModel<E> eventTableModel(EventList<E> source, TableFormat<? super E> tableFormat, TableModelEventAdapter.Factory<E> eventAdapterFactory) {
        final DefaultEventTableModel<E> result = new DefaultEventTableModel<>(source, tableFormat);
        final TableModelEventAdapter<E> eventAdapter = eventAdapterFactory.create(result);
        result.setEventAdapter(eventAdapter);
        return result;
    }

    /**
     * Creates a new table model that extracts column data from the given <code>source</code>
     * using the the given <code>tableFormat</code>.
     *
     * <p>While holding a read lock, this method wraps the source list using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.</p>
     *
     * <p>The <code>eventAdapterFactory</code> is used to create a {@link TableModelEventAdapter},
     * which is then used by the created table model to convert list events to table model events.</p>
     *
     * <p>
     * The returned table model is <strong>not thread-safe</strong>. Unless otherwise noted, all
     * methods are only safe to be called from the event dispatch thread.
     * </p>
     *
     * @param source the EventList that provides the row objects
     * @param tableFormat the object responsible for extracting column data from the row objects
     * @param eventAdapterFactory factory for creating a {@link TableModelEventAdapter}
     */
    public static <E> AdvancedTableModel<E> eventTableModelWithThreadProxyList(EventList<E> source, TableFormat<? super E> tableFormat, TableModelEventAdapter.Factory<E> eventAdapterFactory) {
        final EventList<E> proxySource = createSwingThreadProxyList(source);
        final DefaultEventTableModel<E> result = new DefaultEventTableModel<>(proxySource, true, tableFormat);
        final TableModelEventAdapter<E> eventAdapter = eventAdapterFactory.create(result);
        result.setEventAdapter(eventAdapter);
        return result;
    }

    /**
     * Creates a new table model that renders the specified list with an automatically
     * generated {@link TableFormat}. It uses JavaBeans and reflection to create
     * a {@link TableFormat} as specified.
     *
     * <p>Note that classes that will be obfuscated may not work with
     * reflection. In this case, implement a {@link TableFormat} manually.</p>
     *
     * <p>The returned table model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the event dispatch thread.
     * To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.</p>
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
     */
    public static <E> AdvancedTableModel<E> eventTableModel(EventList<E> source, String[] propertyNames, String[] columnLabels, boolean[] writable) {
        return eventTableModel(source, GlazedLists.tableFormat(propertyNames, columnLabels, writable));
    }

    /**
     * Creates a new table model that renders the specified list with an automatically
     * generated {@link TableFormat}. It uses JavaBeans and reflection to create
     * a {@link TableFormat} as specified.  While holding a read lock,
     * this method wraps the source list using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.
     *
     * <p>Note that classes that will be obfuscated may not work with
     * reflection. In this case, implement a {@link TableFormat} manually.</p>
     *
     * <p>The returned table model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the event dispatch thread.<p>
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
     */
    public static <E> AdvancedTableModel<E> eventTableModelWithThreadProxyList(EventList<E> source, String[] propertyNames, String[] columnLabels, boolean[] writable) {
        return eventTableModelWithThreadProxyList(source, GlazedLists.tableFormat(propertyNames, columnLabels, writable));
    }

    // event adapter factories

    /**
     * Gets a factory for creating a default {@link TableModelEventAdapter}.
     * <p>The default strategy to convert list events to table model events is to be as accurate as possible.
     * In particular, each list event block as converted to and fired as a separate {@link TableModelEvent}.
     * So, one list event can cause multiple table model events.
     * </p>
     * <p>In some cases, this conversion strategy can lead to undesirable effects, such as table repainting issues.
     * One known case is when the table property {@link JTable#getFillsViewportHeight() fillsViewportHeight} is <code>true</code>.
     * Using the {@link #manyToOneEventAdapterFactory() other standard factory} is then recommended.
     *
     * @return the factory for creating a default {@link TableModelEventAdapter}
     *
     * @see #manyToOneEventAdapterFactory()
     * @see Factory
     * @see ListEvent
     * @see TableModelEvent
     */
    public static <E> Factory<E> defaultEventAdapterFactory() {
        return DefaultTableModelEventAdapterFactory.getInstance();
    }

    /**
     * Gets a factory for creating a non-default {@link TableModelEventAdapter}.
     * <p>
     * Whereas the default TableModelEventAdapter converts each ListEvent block to a
     * TableModelEvent, this strategy tries to create only one TableModelEvent
     * for a ListEvent, if it does not represent a reorder. If the ListEvent
     * contains multiple blocks, a special <em>data changed</em> TableModelEvent
     * will be fired, indicating that all row data has changed. Note, that such
     * a <em>data changed</em> TableModelEvent can lead to a loss of the table
     * selection.
     * </p>
     * <p>
     * Therefore you should use this strategy only, when the
     * {@link #defaultEventAdapterFactory() default strategy} doesn't fit your
     * needs or causes undesirable effects/behaviour.
     * </p>
     *
     * @return the factory for creating a non-default {@link TableModelEventAdapter}
     *
     * @see #defaultEventAdapterFactory()
     * @see Factory
     * @see ListEvent
     * @see TableModelEvent
     */
    public static <E> Factory<E> manyToOneEventAdapterFactory() {
        return ManyToOneTableModelEventAdapterFactory.getInstance();
    }

    // ListSelectionModel convenience creators

    /**
     * Creates a new selection model that also presents a list of the selection.
     *
     * The {@link AdvancedListSelectionModel} listens to this {@link EventList} in order
     * to adjust selection when the {@link EventList} is modified. For example,
     * when an element is added to the {@link EventList}, this may offset the
     * selection of the following elements.
     *
     * <p>The returned selection model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the event dispatch thread.
     * To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.</p>
     *
     * @param source the {@link EventList} whose selection will be managed. This should
     *      be the same {@link EventList} passed to the constructor of your
     *      {@link AdvancedTableModel} or {@link EventListModel}.
     */
    public static <E> AdvancedListSelectionModel<E> eventSelectionModel(EventList<E> source) {
        return new DefaultEventSelectionModel<>(source);
    }

    /**
     * Creates a new selection model that also presents a list of the selection.
     * While holding a read lock, it wraps the source list using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}. The
     * {@link AdvancedListSelectionModel} listens to this {@link EventList} in order to adjust
     * selection when the {@link EventList} is modified. For example, when an element is added to
     * the {@link EventList}, this may offset the selection of the following elements.
     * <p>
     * The returned selection model is <strong>not thread-safe</strong>. Unless otherwise noted,
     * all methods are only safe to be called from the event dispatch thread.
     * </p>
     *
     * @param source the {@link EventList} whose selection will be managed. This should be the
     *            same {@link EventList} passed to the constructor of your
     *            {@link AdvancedTableModel} or {@link EventListModel}.
     */
    public static <E> AdvancedListSelectionModel<E> eventSelectionModelWithThreadProxyList(EventList<E> source) {
        final EventList<E> proxySource = createSwingThreadProxyList(source);
        return new DefaultEventSelectionModel<>(proxySource, true);
    }

    // EventListModel convenience creators

    /**
     * Creates a new list model that contains all objects located in the given
     * <code>source</code> and reacts to any changes in the given <code>source</code>.
     *
     * <p>The returned selection model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the event dispatch thread.
     * To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.
     * </p>
     *
     * @param source the EventList that provides the elements
     */
    public static <E> DefaultEventListModel<E> eventListModel(EventList<E> source) {
        return new DefaultEventListModel<>(source);
    }

    /**
     * Creates a new list model that contains all objects located in the given
     * <code>source</code> and reacts to any changes in the given <code>source</code>.
     * While holding a read lock, it wraps the source list using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.
     *
     * <p>The returned selection model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the event dispatch thread.
     * </p>
     *
     * @param source the EventList that provides the elements
     */
    public static <E> DefaultEventListModel<E> eventListModelWithThreadProxyList(EventList<E> source) {
        final EventList<E> proxySource = createSwingThreadProxyList(source);
        return new DefaultEventListModel<>(proxySource, true);
    }

    // EventComboBoxModel convenience creators

    /**
     * Creates a new combobox model that contains all objects located in the given
     * <code>source</code> and reacts to any changes in the given <code>source</code>.
     *
     * <p>The returned combobox model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the event dispatch thread.
     * To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.
     * </p>
     *
     * @param source the EventList that provides the elements
     */
    public static <E> DefaultEventComboBoxModel<E> eventComboBoxModel(EventList<E> source) {
        return new DefaultEventComboBoxModel<>(source);
    }

    /**
     * Creates a new combobox model that contains all objects located in the given
     * <code>source</code> and reacts to any changes in the given <code>source</code>.
     * While holding a read lock, it wraps the source list using
     * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.
     *
     * <p>The returned combobox model is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the event dispatch thread.
     * </p>
     *
     * @param source the EventList that provides the elements
     */
    public static <E> DefaultEventComboBoxModel<E> eventComboBoxModelWithThreadProxyList(EventList<E> source) {
        final EventList<E> proxySource = createSwingThreadProxyList(source);
        return new DefaultEventComboBoxModel<>(proxySource, true);
    }

    /** Helper method to create a SwingThreadProxyList with read locks. */
    private static <E> EventList<E> createSwingThreadProxyList(EventList<E> source) {
        final EventList<E> result;
        source.getReadWriteLock().readLock().lock();
        try {
            result = GlazedListsSwing.swingThreadProxyList(source);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
        return result;
    }


}