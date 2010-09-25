/* Glazed Lists                                                 (c) 2003-2010 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.impl.swt.SWTThreadProxyCalculation;

import org.eclipse.swt.widgets.Display;

/**
 * A factory for creating SWT related calculations.
 *
 * @author Holger Brands
 */
public final class CalculationsSWT {

    private CalculationsSWT() {}

    /**
     * Wraps the source in a {@link Calculation} that fires all of its update
     * events from the SWT event dispatch thread.
     */
    public static <E> Calculation<E> swtThreadProxyCalculation(Calculation<? extends E> source, Display display) {
        return new SWTThreadProxyCalculation<E>(source, display);
    }

    /**
     * Returns <code>true</code> if <code>calc</code> is a {@link Calculation} that fires
     * all of its update events from the SWT event dispatch thread.
     */
    public static boolean isSWTThreadProxyCalculation(Calculation calc) {
        return calc instanceof SWTThreadProxyCalculation;
    }
}
