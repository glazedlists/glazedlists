/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.GuiTestCase;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility class for running JUnit tests with Swing code.
 *
 * <p>This class has the following behaviour:
 *
 * <ul>
 *  <li>Extending classes must not define any other <code>testXXX()</code>
 *      methods. The should define only <code>guiTestXXX()</code> methods.</li>
 *
 *  <li>If one test fails, they all fail.</li>
 * </ul>
 *
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public abstract class SwingTestCase extends GuiTestCase {

    @Override
    protected final void executeOnGUIThread(Runnable runnable) {
        try {
            SwingUtilities.invokeAndWait(runnable);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        } catch (InvocationTargetException e) {
            Throwable rootCause = e;

            // unwrap all wrapper layers down to the root problem
            while (rootCause.getCause() != null)
                rootCause = rootCause.getCause();

            if (rootCause instanceof RuntimeException)
                throw (RuntimeException) rootCause;

            if (rootCause instanceof Error)
                throw (Error) rootCause;

            // embed anything else as a RuntimeException
            throw new RuntimeException(rootCause);
        }
    }
}