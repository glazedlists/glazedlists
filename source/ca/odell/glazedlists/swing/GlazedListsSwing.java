/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import javax.swing.BoundedRangeModel;
import javax.swing.table.TableModel;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ThresholdList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.swing.LowerThresholdRangeModel;
import ca.odell.glazedlists.impl.swing.SwingThreadProxyEventList;
import ca.odell.glazedlists.impl.swing.UpperThresholdRangeModel;

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
        return new SwingThreadProxyEventList<E>(source);
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
     * Creates a new table model that renders the specified list with an automatically
     * generated {@link TableFormat}. It uses JavaBeans and reflection to create
     * a {@link TableFormat} as specified.
     *
     * <p>Note that classes that will be obfuscated may not work with
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
     */
    public static <E> TableModel createEventTableModel(EventList<E> source, String[] propertyNames, String[] columnLabels, boolean[] writable) {
        return new DefaultEventTableModel<E>(source, GlazedLists.tableFormat(propertyNames, columnLabels, writable));
    }

}