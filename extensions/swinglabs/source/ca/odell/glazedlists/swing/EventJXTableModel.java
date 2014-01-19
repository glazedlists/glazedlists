/* Glazed Lists                                                 (c) 2003-2008 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.gui.TableFormat;

import javax.swing.event.TableModelEvent;

/**
 * An extension of the {@link EventTableModel} for better integration with
 * JXTable.
 * <p>
 * In particular, this table model implements a different strategy to tranform
 * {@link ListEvent}s to {@link TableModelEvent}s. Whereas EventTableModel
 * converts each ListEvent block to a TableModelEvent, EventJXTableModel tries
 * to create only one TableModelEvent for a ListEvent, that does not represent a
 * reorder. If the ListEvent contains multiple blocks, a special
 * <em>data changed</em> TableModelEvent will be fired, indicating that all row
 * data has changed. Note, that such a <em>data changed</em> TableModelEvent can
 * lead to a loss of the table selection.
 * </p>
 *
 * @deprecated Use {@link DefaultEventTableModel} with a
 *             {@link GlazedListsSwing#manyToOneEventAdapterFactory()
 *             ManyToOneTableModelEventAdapter} instead. This class will be
 *             removed in the GL 2.0 release. The wrapping of the source list
 *             with an EDT safe list has been determined to be undesirable (it
 *             is better for the user to provide their own EDT safe list).
 *
 * @see GlazedListsSwing#eventTableModelWithThreadProxyList(EventList,
 *      TableFormat, ca.odell.glazedlists.swing.TableModelEventAdapter.Factory)
 * @see GlazedListsSwing#manyToOneEventAdapterFactory()
 * @author Holger Brands
 */
@Deprecated
public class EventJXTableModel<E> extends EventTableModel<E> {

    /**
     * Creates a new table that renders the specified list with an automatically
     * generated {@link TableFormat}. It uses JavaBeans and reflection to create
     * a {@link TableFormat} as specified.
     *
     * <p>Note that the classes which will be obfuscated may not work with
     * reflection. In this case, implement a {@link TableFormat} manually.
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
     * @deprecated Use {@link GlazedLists#tableFormat(String[], String[], boolean[])}
     * and {@link GlazedListsSwing#eventTableModelWithThreadProxyList(EventList, TableFormat, ca.odell.glazedlists.swing.TableModelEventAdapter.Factory)}
     * instead
     */
    @Deprecated
    public EventJXTableModel(EventList<E> source, String[] propertyNames, String[] columnLabels,
            boolean[] writable) {
        super(source, propertyNames, columnLabels, writable);
        setEventAdapter(GlazedListsSwing.<E>manyToOneEventAdapterFactory().create(this));
    }

    /**
     * Creates a new table model that extracts column data from the given <code>source</code>
     * using the the given <code>tableFormat</code>.
     *
     * @param source the EventList that provides the row objects
     * @param tableFormat the object responsible for extracting column data from the row objects
     *
     * @deprecated Use {@link GlazedListsSwing#eventTableModelWithThreadProxyList(EventList, TableFormat, ca.odell.glazedlists.swing.TableModelEventAdapter.Factory)} instead
     */
    @Deprecated
    public EventJXTableModel(EventList<E> source, TableFormat<? super E> tableFormat) {
        super(source, tableFormat);
        setEventAdapter(GlazedListsSwing.<E>manyToOneEventAdapterFactory().create(this));
    }
}
