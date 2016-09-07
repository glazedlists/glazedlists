/* Glazed Lists                                                 (c) 2003-2010 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swing;

import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.impl.gui.ThreadProxyCalculation;

import java.awt.EventQueue;

/**
 * {@link Calculation} that proxies all change events from the wrapped source calculation to the
 * Swing event dispatch thread.
 *
 * @author Holger Brands
 */
public class SwingThreadProxyCalculation<E> extends ThreadProxyCalculation<E> {

    /**
     * Create a {@link SwingThreadProxyCalculation} which delivers changes to the given
     * <code>source</code> on the Swing event dispatch thread.
     *
     * @param source the {@link Calculation} for which to proxy events
     */
    public SwingThreadProxyCalculation(Calculation<? extends E> source) {
        super(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void schedule(Runnable runnable) {
        if(EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            EventQueue.invokeLater(runnable);
        }
    }
}
