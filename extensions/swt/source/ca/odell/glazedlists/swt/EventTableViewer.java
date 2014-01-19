/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

/**
 * A view helper that displays an EventList in an SWT table.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * @deprecated Use {@link DefaultEventTableViewer} instead. This class will be removed in the GL
 *             2.0 release. The wrapping of the source list with an SWT-EDT safe list has been
 *             determined to be undesirable (it is better for the user to provide their own SWT-EDT
 *             safe list).
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author Holger Brands
 */
@Deprecated
public class EventTableViewer<E> extends DefaultEventTableViewer<E> {

    /** the ThreadProxyEventList to which this EventTableViewer is listening */
    protected EventList<E> swtThreadSource;

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with an automatically generated
     * {@link TableFormat}. It uses JavaBeans and Reflection to create a
     * {@link TableFormat} as specified.
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param propertyNames an array of property names in the JavaBeans format.
     *        For example, if your list contains Objects with the methods
     *        getFirstName(), setFirstName(String), getAge(), setAge(Integer),
     *        then this array should contain the two strings "firstName" and
     *        "age". This format is specified by the JavaBeans
     *        {@link java.beans.PropertyDescriptor}.
     * @param columnLabels the corresponding column names for the listed
     *        property names. For example, if your columns are "firstName" and
     *        "age", then your labels might be "First Name" and "Age".
     * @deprecated use a combination of
     *             {@link GlazedLists#tableFormat(String[], String[])} and
     *             {@link #EventTableViewer(EventList, Table, TableFormat)}
     *             instead
     */
    @Deprecated
    public EventTableViewer(EventList<E> source, Table table, String[] propertyNames, String[] columnLabels) {
        this(source, table, GlazedLists.tableFormat(propertyNames, columnLabels));
    }

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     */
    public EventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat) {
        this(source, table, tableFormat, TableItemConfigurer.DEFAULT);
    }

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     *
     * @param source the EventList that provides the row objects
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     * @param tableItemConfigurer responsible for configuring table items
     */
    public EventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat,
            TableItemConfigurer<? super E> tableItemConfigurer) {
    	this(source, createProxyList(source, table.getDisplay()), table, tableFormat, tableItemConfigurer);
    }

    /**
     * Creates a new viewer for the given {@link Table} that updates the table
     * contents in response to changes on the specified {@link EventList}. The
     * {@link Table} is formatted with the specified {@link TableFormat}.
     *
     * @param source the EventList that provides the row objects
     * @param swtProxySource the ThreadProxyEventList
     * @param table the Table viewing the source objects
     * @param tableFormat the object responsible for extracting column data
     *      from the row objects
     * @param tableItemConfigurer responsible for configuring table items
     */
    private EventTableViewer(EventList<E> source, EventList<E> swtProxySource, Table table, TableFormat<? super E> tableFormat,
            TableItemConfigurer<? super E> tableItemConfigurer) {
    	super(swtProxySource, table, tableFormat, tableItemConfigurer);
    	this.swtThreadSource = (swtProxySource == source) ? null : swtProxySource;
    }

    /**
     * Releases the resources consumed by this {@link EventTableViewer} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventTableViewer} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventTableViewer}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link EventTableViewer} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link EventTableViewer} after it has been disposed.
     */
    @Override
    public void dispose() {
        if (swtThreadSource != null) swtThreadSource.dispose();
        super.dispose();
        swtThreadSource = null;
    }

    /**
     * while holding a read lock, this method wraps the given source list with a SWT thread
     * proxy list.
     */
    private static <E> EventList<E> createProxyList(EventList<E> source, Display display) {
    	return GlazedListsSWT.createProxyListIfNecessary(source, display);
    }
}