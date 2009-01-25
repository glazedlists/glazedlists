/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class for running JUnit tests with GUI code.
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
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public abstract class GuiTestCase extends TestCase {

    /** test only methods that start with this prefix */
    private static final String TEST_METHOD_PREFIX = "guiTest";
    private static final String SET_UP_METHOD = "guiSetUp";
    private static final String TEAR_DOWN_METHOD = "guiTearDown";
    private static final String JUNIT_BAD_PREFIX = "test";
    private static final String JUNIT_OK_METHOD = "testGui";
    private static final String JUNIT_OK_METHOD2 = "testOnMainThread";

    /** useful empty arrays */
    private static final Class[] DECLARE_NO_PARAMETERS = new Class[0];
    private static final Object[] SEND_NO_PARAMETERS = new Object[0];

    public void guiSetUp() {}
    public void guiTearDown() {}

    /**
     * Execute all methods from the specified GUI test class that start with
     * the prefix "guiTest" on the GUI Thread.
     */
    public final void testGui() {
        executeOnGUIThread(new ExecuteGuiTestsRunnable(this));
    }

    protected abstract void executeOnGUIThread(Runnable runnable);

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

    /**
     * This Runnable executes all public methods of the form guiTestXXX()
     * declared on the given test case or any of its super classes.
     */
    private class ExecuteGuiTestsRunnable implements Runnable {
        private final GuiTestCase testCase;

        public ExecuteGuiTestsRunnable(GuiTestCase testCase) {
            this.testCase = testCase;
        }

        public void run() {
            try {
                final Class guiTestClass = testCase.getClass();

                final Method setUp = guiTestClass.getMethod(SET_UP_METHOD, DECLARE_NO_PARAMETERS);
                final Method tearDown = guiTestClass.getMethod(TEAR_DOWN_METHOD, DECLARE_NO_PARAMETERS);

                // ensure no methods of the form testXXX() exist
                final Method[] allMethods = guiTestClass.getMethods();
                for (int i = 0; i < allMethods.length; i++) {
                    final String methodName = allMethods[i].getName();
                    if (methodName.startsWith(JUNIT_BAD_PREFIX) && !methodName.equals(JUNIT_OK_METHOD) && !methodName.startsWith(JUNIT_OK_METHOD2))
                        throw new IllegalStateException(methodName + "() must be renamed to guiT" + methodName.substring(1) +"()");
                }

                // run all test methods
                for (int i = 0; i < allMethods.length; i++) {
                    final String methodName = allMethods[i].getName();
                    if (!methodName.startsWith(TEST_METHOD_PREFIX))
                        continue;

                    System.out.println("Executing " + methodName);
                    setUp.invoke(testCase, SEND_NO_PARAMETERS);
                    allMethods[i].invoke(testCase, SEND_NO_PARAMETERS);
                    tearDown.invoke(testCase, SEND_NO_PARAMETERS);
                }
            }
            catch (InvocationTargetException e) { throw new RuntimeException(e.getCause()); }
            catch (NoSuchMethodException e) { throw new RuntimeException(e); }
            catch (IllegalAccessException e) { throw new RuntimeException(e); }
        }
    }
}