/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ThresholdList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.swt.BeanItemFormat;
import ca.odell.glazedlists.impl.swt.BoundedRangeControlFactory;
import ca.odell.glazedlists.impl.swt.LowerThresholdViewer;
import ca.odell.glazedlists.impl.swt.SWTThreadProxyEventList;
import ca.odell.glazedlists.impl.swt.UpperThresholdViewer;

import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;


/**
 * A factory for creating all sorts of objects to be used with Glazed Lists.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class GlazedListsSWT {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedListsSWT() {
        throw new UnsupportedOperationException();
    }

    // EventLists // // // // // // // // // // // // // // // // // // // // //

    /**
     * Wraps the source in an {@link EventList} that fires all of its update events
     * from the SWT user interface thread.
     */
    public static <E> TransformedList<E, E> swtThreadProxyList(EventList<E> source, Display display) {
        return new SWTThreadProxyEventList<E>(source, display);
    }

    /**
     * Returns true if <code>list</code> is an {@link EventList} that fires
     * all of its update events from the SWT event dispatch thread.
     */
    public static boolean isSWTThreadProxyList(EventList list) {
        return list instanceof SWTThreadProxyEventList;
    }

    /**
     * while holding a read lock, this method wraps the given source list with a SWT thread
     * proxy list if necessary.
     */
    static <E> EventList<E> createProxyListIfNecessary(EventList<E> source, Display display) {
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventTableModel
        EventList<E> result = source;
        source.getReadWriteLock().readLock().lock();
        try {
            final TransformedList<E,E> decorated = createSwtThreadProxyListIfNecessary(source, display);

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

    /** wraps the given source list with a SWT thread proxy list, if necessary */
    private static <E> TransformedList<E,E> createSwtThreadProxyListIfNecessary(EventList<E> source, Display display) {
        return GlazedListsSWT.isSWTThreadProxyList(source) ? null : GlazedListsSWT.swtThreadProxyList(source, display);
    }


    // ItemFormats // // // // // // // // // // // // // // // // // // //

    /**
	 * Creates a new {@link ItemFormat} that uses the string value of a JavaBean
	 * property as the formatted value of a list element. If the list element or
	 * the propery value is <code>null</code>, the emtpy string is returned.
	 *
	 * @param property the JavaBean property name
     */
    public static <E> ItemFormat<E> beanItemFormat(String property) {
        return new BeanItemFormat<E>(property);
    }

    /**
	 * Creates a new {@link ItemFormat} that uses the string value of a JavaBean
	 * property as the formatted value of a list element. If the list element or
	 * the propery value is <code>null</code>, the given value is returned.
	 *
	 * @param property the JavaBean property name
	 * @param valueForNullElement
	 *            string value to be used for a <code>null</code> element or
	 *            property value
     */
    public static <E> ItemFormat<E> beanItemFormat(String property, String valueForNullElement) {
        return new BeanItemFormat<E>(property, valueForNullElement);
    }

    // ThresholdViewers // // // // // // // // // // // // // // // // // // //

    /**
     * Creates a viewer that manipulates the lower bound of the specified
     * ThresholdList based on changes to the Slider.  The ThresholdList linked
     * to this viewer type will contain a range of Objects between the results
     * of getSelection() and getMaximum() on the Slider.
     */
    public static SelectionListener lowerThresholdViewer(ThresholdList thresholdList, Slider slider) {
        return new LowerThresholdViewer(thresholdList, BoundedRangeControlFactory.slider(slider));
    }

    /**
     * Creates a viewer that manipulates the lower bound of the specified
     * ThresholdList based on changes to Scale selection.  The ThresholdList
     * linked to this viewer type will contain a range of Objects between the
     * results of getSelection() and getMaximum() on the Scale.
     */
    public static SelectionListener lowerThresholdViewer(ThresholdList thresholdList, Scale scale) {
        return new LowerThresholdViewer(thresholdList, BoundedRangeControlFactory.scale(scale));
    }

    /**
     * Creates a viewer that manipulates the upper bound of the specified
     * ThresholdList based on changes to the Slider.  The ThresholdList linked
     * to this model type will contain a range of Objects between the results
     * of getMinimum() and getSelection() on the Slider.
     */
    public static SelectionListener upperThresholdViewer(ThresholdList thresholdList, Slider slider) {
        return new UpperThresholdViewer(thresholdList, BoundedRangeControlFactory.slider(slider));
    }

    /**
     * Creates a viewer that manipulates the upper bound of the specified
     * ThresholdList based on changes to Scale selection.  The ThresholdList
     * linked to this viewer type will contain a range of Objects between the
     * results of getMinimum() and getSelection() on the Scale.
     */
    public static SelectionListener upperThresholdViewer(ThresholdList thresholdList, Scale scale) {
        return new UpperThresholdViewer(thresholdList, BoundedRangeControlFactory.scale(scale));
    }

    // Viewer convenience factory methods

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * To do this programmatically, use {@link Display#syncExec(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     * </p>
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     */
    public static <E> DefaultEventTableViewer<E> eventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat) {
        return new DefaultEventTableViewer<E>(source, table, tableFormat, TableItemConfigurer.DEFAULT);
    }

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * To do this programmatically, use {@link Display#syncExec(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     * </p>
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     * @param tableItemConfigurer responsible for configuring table items
     */
    public static <E> DefaultEventTableViewer<E> eventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat,
            TableItemConfigurer<? super E> tableItemConfigurer) {
    	return new DefaultEventTableViewer<E>(source, table, tableFormat, tableItemConfigurer);
    }

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     * While holding a read lock, this method wraps the source list using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * </p>
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     */
    public static <E> DefaultEventTableViewer<E> eventTableViewerWithThreadProxyList(EventList<E> source, Table table, TableFormat<? super E> tableFormat) {
    	final EventList<E> proxySource = createSwtThreadProxyListWithLock(source, table.getDisplay());
        return new DefaultEventTableViewer<E>(proxySource, table, tableFormat, TableItemConfigurer.DEFAULT, true);
    }

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     * While holding a read lock, this method wraps the source list using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * </p>
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     * @param tableItemConfigurer responsible for configuring table items
     */
    public static <E> DefaultEventTableViewer<E> eventTableViewerWithThreadProxyList(EventList<E> source, Table table, TableFormat<? super E> tableFormat,
            TableItemConfigurer<? super E> tableItemConfigurer) {
    	final EventList<E> proxySource = createSwtThreadProxyListWithLock(source, table.getDisplay());
    	return new DefaultEventTableViewer<E>(proxySource, table, tableFormat, tableItemConfigurer, true);
    }

    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements will simply be displayed as the result of calling
     * toString() on the contents of the source list.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * To do this programmatically, use {@link Display#syncExec(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     * </p>
     *
     * @param source the EventList that provides the elements
     * @param list the list
     */
    public static <E> DefaultEventListViewer<E> eventListViewer(EventList<E> source, List list) {
    	return new DefaultEventListViewer<E>(source, list);
    }

    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements are formatted using the provided {@link ItemFormat}.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * To do this programmatically, use {@link Display#syncExec(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     * </p>
     *
     * @param source the EventList that provides the elements
     * @param list the list
     * @param itemFormat an {@link ItemFormat} for formatting the displayed values
     */
    public static <E> DefaultEventListViewer<E> eventListViewer(EventList<E> source, List list, ItemFormat<? super E> itemFormat) {
    	return new DefaultEventListViewer<E>(source, list, itemFormat);
    }

    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements will simply be displayed as the result of calling
     * toString() on the contents of the source list.
     * While holding a read lock, this method wraps the source list using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * </p>
     *
     * @param source the EventList that provides the elements
     * @param list the list
     */
    public static <E> DefaultEventListViewer<E> eventListViewerWithThreadProxyList(EventList<E> source, List list) {
    	final EventList<E> proxySource = createSwtThreadProxyListWithLock(source, list.getDisplay());
    	return new DefaultEventListViewer<E>(proxySource, list, new DefaultItemFormat<E>(), true);
    }

    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements are formatted using the provided {@link ItemFormat}.
     * While holding a read lock, this method wraps the source list using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * </p>
     *
     * @param source the EventList that provides the elements
     * @param list the list
     * @param itemFormat an {@link ItemFormat} for formatting the displayed values
     */
    public static <E> DefaultEventListViewer<E> eventListViewerWithThreadProxyList(EventList<E> source, List list, ItemFormat<? super E> itemFormat) {
    	final EventList<E> proxySource = createSwtThreadProxyListWithLock(source, list.getDisplay());
    	return new DefaultEventListViewer<E>(proxySource, list, itemFormat, true);
    }

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} compoment will be
     * the result of calling toString() on the Objects found in source.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * To do this programmatically, use {@link Display#syncExec(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     * </p>
     *
     * @param source the EventList that provides the elements
     * @param combo the combo box
     */
    public static <E> DefaultEventComboViewer<E> eventComboViewer(EventList<E> source, Combo combo) {
    	return new DefaultEventComboViewer<E>(source, combo);
    }

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} component will be
     * formatted using the provided {@link ItemFormat}.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * To do this programmatically, use {@link Display#syncExec(Runnable)} and
     * wrap the source list (or some part of the source list's pipeline) using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     * </p>
     *
     * @param source the EventList that provides the elements
     * @param combo the combo box
     * @param itemFormat an {@link ItemFormat} for formatting the displayed values
     */
    public static <E> DefaultEventComboViewer<E> eventComboViewer(EventList<E> source, Combo combo, ItemFormat<? super E> itemFormat) {
    	return new DefaultEventComboViewer<E>(source, combo, itemFormat);
    }

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} compoment will be
     * the result of calling toString() on the Objects found in source.
     * While holding a read lock, this method wraps the source list using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * </p>
     *
     * @param source the EventList that provides the elements
     * @param combo the combo box
     */
    public static <E> DefaultEventComboViewer<E> eventComboViewerWithThreadProxyList(EventList<E> source, Combo combo) {
    	final EventList<E> proxySource = createSwtThreadProxyListWithLock(source, combo.getDisplay());
    	return new DefaultEventComboViewer<E>(proxySource, combo, new DefaultItemFormat<E>(), true);
    }

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}. The
     * {@link String} values displayed in the {@link Combo} component will be
     * formatted using the provided {@link ItemFormat}.
     * While holding a read lock, this method wraps the source list using
     * {@link GlazedListsSWT#swtThreadProxyList(EventList, Display)}.
     *
     * <p>The returned viewer is <strong>not thread-safe</strong>. Unless otherwise
     * noted, all methods are only safe to be called from the SWT event handler thread.
     * </p>
     *
     * @param source the EventList that provides the elements
     * @param combo the combo box
     * @param itemFormat an {@link ItemFormat} for formatting the displayed values
     */
    public static <E> DefaultEventComboViewer<E> eventComboViewerWithThreadProxyList(EventList<E> source, Combo combo, ItemFormat<? super E> itemFormat) {
    	final EventList<E> proxySource = createSwtThreadProxyListWithLock(source, combo.getDisplay());
    	return new DefaultEventComboViewer<E>(proxySource, combo, itemFormat, true);
    }

    /** Helper method to create a SwtThreadProxyList with read locks. */
    static <E> EventList<E> createSwtThreadProxyListWithLock(EventList<E> source, Display display) {
        final EventList<E> result;
        source.getReadWriteLock().readLock().lock();
        try {
            result = GlazedListsSWT.swtThreadProxyList(source, display);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
        return result;
    }
}