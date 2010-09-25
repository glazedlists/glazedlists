/* Glazed Lists                                                 (c) 2003-2010 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.calculation.AbstractCalculation;
import ca.odell.glazedlists.calculation.Calculation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A {@link Calculation} that wraps another calculation to forward its events on a proxy thread,
 * regardless of the thread of their origin. Subclasses will determine the concrete proxy thread
 * on which the events will be delivered.
 * <p>
 * To do it's job, <code>ThreadProxyCalculation</code> will add a PropertyChangeListener to the
 * wrapped calculation, which will be removed again when {@link ThreadProxyCalculation#dispose()}
 * is called.
 * </p>
 * <p>
 * Note, that the wrapped source calculation will not be disposed by this calculation.
 * </p>
 *
 * @author Holger Brands
 */
public abstract class ThreadProxyCalculation<E> extends AbstractCalculation<E> {
    /** the wrapped source calculation. */
    private Calculation<? extends E> source;

    /** the listener for the source calculation. */
    private PCL pcl;

    /**
     * Create a {@link ThreadProxyCalculation} which delivers changes to the given
     * <code>source</code> on a particular {@link Thread}, called the proxy {@link Thread} of a
     * subclasses choosing. The {@link Thread} used depends on the implementation of
     * {@link #schedule(Runnable)}.
     *
     * @param source the {@link EventList} for which to proxy events
     */
    public ThreadProxyCalculation(Calculation<? extends E> source) {
        super(source.getValue());
        this.source = source;
        this.pcl = new PCL();
        source.addPropertyChangeListener(pcl);
    }

    /**
     * Releases the resources consumed by this {@link Calculation} so that it may eventually be
     * garbage collected.
     * <p>
     * Note, that the source calculation will not be disposed by this method.
     * </p>
     */
    public void dispose() {
        source.removePropertyChangeListener(pcl);
        source = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getValue() {
        return source.getValue();
    }

    /**
     * Schedule the specified runnable to be executed on the proxy thread.
     *
     * @param runnable a unit of work to be executed on the proxy thread
     */
    protected abstract void schedule(Runnable runnable);

    /**
     * PropertyChangeListener for the wrapped calculation, that refires the PropertyChangeEvent
     * on another thread, determined by the subclass.
     */
    private class PCL implements PropertyChangeListener {
        public void propertyChange(final PropertyChangeEvent evt) {
            schedule(new Runnable() {
                @SuppressWarnings("unchecked")
                public void run() {
                    fireValueChange((E) evt.getOldValue(), (E) evt.getNewValue());
                }
            });
        }
    }
}
