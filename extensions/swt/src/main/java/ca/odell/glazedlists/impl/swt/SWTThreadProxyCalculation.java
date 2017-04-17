/* Glazed Lists                                                 (c) 2003-2010 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swt;

import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.impl.gui.ThreadProxyCalculation;

import org.eclipse.swt.widgets.Display;

/**
 * {@link Calculation} that proxies all change events from the wrapped source calculation to the
 * SWT event dispatch thread.
 *
 * @author Holger Brands
 */
public class SWTThreadProxyCalculation<E> extends ThreadProxyCalculation<E> {
    /** the display which owns the user interface thread */
    private final Display display;

    /**
     * Create a {@link SWTThreadProxyCalculation} which delivers changes to the given
     * <code>source</code> on the SWT event dispatch thread.
     *
     * @param source the {@link Calculation} for which to proxy events
     * @param display the display which owns the user interface thread
     */
    public SWTThreadProxyCalculation(Calculation<? extends E> source, Display display) {
        super(source);
        this.display = display;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void schedule(Runnable runnable) {
        if(display.getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            display.asyncExec(runnable);
        }
    }
}
