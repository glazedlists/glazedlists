/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ca.odell.glazedlists.GuiTestCase;

/**
 * Utility class for running JUnit tests with SWT code.
 *
 * <p>This class has the following behaviour:
 *
 * <ul>
 *  <li>Extending classes must not define any <code>testXXX()</code> methods.
 *      They should define only <code>guiTestXXX()</code> methods.</li>
 *
 *  <li>If one test fails, they all fail.</li>
 * </ul>
 *
 * This class provides both the SWT {@link Display} and {@link Shell} for the
 * test methods available via {@link #getDisplay()} and {@link #getShell()}.
 *
 * @author Holger Brands
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SwtTestCase extends GuiTestCase {

    private Display display;
    private Shell shell;

    protected final void executeOnGUIThread(Runnable runnable) {
        display = new Display();
        shell = new Shell(display);

        try {
            display.syncExec(runnable);
        } finally {
            display.dispose();
        }
    }

    protected Display getDisplay() { return display; }
    protected Shell getShell() { return shell; }
}