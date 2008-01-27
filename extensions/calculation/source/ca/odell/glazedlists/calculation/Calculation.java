/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import java.beans.PropertyChangeListener;

/**
 * It is sometimes desirable to compute single values from an entire List of
 * objects. For example, an EventList<Integer> may represent the ages of all
 * customers and it is useful to display the average age of all customers. The
 * average can be modeled as a <code>Calculation</code> which is updated as the
 * contents of the EventList change and in turn broadcast PropertyChangeEvents
 * to registered listeners describing a change in the calculated value.
 *
 * <p>In this way, Calculation object represent a continuation of data
 * transformation from the domain of Lists to the domain of simple Number
 * objects calculated from the elements of those Lists.
 *
 * @author James Lemieux
 */
public interface Calculation<N> {

    /**
     * @param pcl a PropertyChangeListener to notify any time the
     *      {@link #getValue value} changes
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl);

    /**
     * @param pcl a PropertyChangeListener which should no longer be notified
     *      of {@link #getValue value} changes
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl);

    /**
     * @return the latest calculated value
     */
    public N getValue();

    /**
     * Cease the updating of this Calculation from its data sources and free
     * them for garbage collection.
     */
    public void dispose();
}