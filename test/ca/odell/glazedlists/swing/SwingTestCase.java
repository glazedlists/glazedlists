/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.junit.Rule;

/**
 * Utility class for running JUnit tests with Swing code.
 * <p>It uses {@link SwingTestRule} to perfom each test method on the Swing-EDT thread.</p>
 *
 * @author Holger Brands
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public abstract class SwingTestCase {

    @Rule
    public SwingTestRule swingTestRule = new SwingTestRule();

    /**
     * Run the specified task on a background Thread. If <code>block</code> is
     * <tt>true</tt> this will method will pause until the task has completed;
     * otherwise it will return immediately.
     *
     * @param task the logic to be executed on a background Thread
     * @param block true to wait for the background task to complete
     * @return the thread the background task was started on
     */
    protected Thread doBackgroundTask(Runnable task, boolean block) {
        // start the background task
        final Thread background = new Thread(task);
        background.start();

        // wait for the task to complete
        if (block) {
            try {
                background.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return background;
    }

//    @Override
    protected final void executeOnGUIThread(Runnable runnable) {
        try {
            SwingUtilities.invokeAndWait(runnable);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        } catch (InvocationTargetException e) {
            Throwable rootCause = e;

            // unwrap all wrapper layers down to the root problem
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }

            if (rootCause instanceof RuntimeException) {
                throw (RuntimeException) rootCause;
            }

            if (rootCause instanceof Error) {
                throw (Error) rootCause;
            }

            // embed anything else as a RuntimeException
            throw new RuntimeException(rootCause);
        }
    }
}