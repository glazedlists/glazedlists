/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
// standard collections
import java.util.*;
// reflection for invoking methods by name
import java.lang.reflect.*;
// swing utilities for interacting with the event dispatch thread
import javax.swing.SwingUtilities;
import javax.swing.event.*;

/**
 * Utility class for running JUnit tests with Swing code.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SwingTestCase extends TestCase {
    
    /** test only methods that start with this prefix */
    public static final String TEST_METHOD_PREFIX = "guiTest";
    public static final String SET_UP_METHOD = "guiSetUp";
    public static final String TEAR_DOWN_METHOD = "guiTearDown";
    public static final String JUNIT_BAD_PREFIX = "test";
    public static final String JUNIT_OK_PREFIX = "test";
    
    /** useful empty arrays */
    private static final Class[] DECLARE_NO_PARAMETERS = new Class[0];
    private static final Object[] SEND_NO_PARAMETERS = new Class[0];
    
    /**
     * Prepare for the test.
     */
    public final void setUp() {
        // do nothing
    }

    /**
     * Clean up after the test.
     */
    public final void tearDown() {
        // do nothing
    }

    /**
     * Run all methods from the specified test class that start with the prefix
     * "guiTest". These test methods will be executed from the Swing event dispatch
     * thread.
     */
    public void testGui() {
        try {
            SwingUtilities.invokeAndWait(new TestOnSwingThread(getClass()));
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }
    private class TestOnSwingThread implements Runnable {
        private Class guiClass;
        public TestOnSwingThread(Class guiClass) {
            this.guiClass = guiClass;
        }
        public void run() {
            try {
                // create an instance with the appropriate methods
                Object instance = guiClass.getConstructor(DECLARE_NO_PARAMETERS).newInstance(new Object[0]);
                Method setUp = null;
                try {
                    setUp = guiClass.getMethod(SET_UP_METHOD, DECLARE_NO_PARAMETERS);
                } catch(NoSuchMethodException e) { 
                    // do nothing
                }
                Method tearDown = null;
                try { 
                    tearDown = guiClass.getMethod(TEAR_DOWN_METHOD, DECLARE_NO_PARAMETERS);
                } catch(NoSuchMethodException e) {
                    // do nothing
                }
                
                // run all test methods
                Method[] allMethods = guiClass.getDeclaredMethods();
                for(int m = 0; m < allMethods.length; m++) {
                    if(!allMethods[m].getName().startsWith(JUNIT_OK_PREFIX) && allMethods[m].getName().startsWith(JUNIT_BAD_PREFIX)) throw new IllegalStateException("No testXXX() methods allowed in decendents of SwingTestCase, use guiTestXXX() instead.");
                    if(!allMethods[m].getName().startsWith(TEST_METHOD_PREFIX)) continue;
                    
                    System.out.println("Running " + allMethods[m].getName());
                    if(setUp != null) setUp.invoke(instance, SEND_NO_PARAMETERS);
                    allMethods[m].invoke(instance, SEND_NO_PARAMETERS);
                    if(tearDown != null) tearDown.invoke(instance, SEND_NO_PARAMETERS);
                }
            } catch(InvocationTargetException e) {
                Assert.fail(e.getCause().getClass() + ": \"" + e.getCause().getMessage() + "\"");
            } catch(NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch(InstantiationException e) {
                throw new RuntimeException(e);
            } catch(IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Ensures that all waiting Swing events have been handled before proceeding.
     * This hack method can be used when unit testing classes that interact with
     * the Swing event dispatch thread.
     *
     * <p>This guarantees that all events in the event dispatch queue before this
     * method is called will have executed. It provides no guarantee that the event
     * dispatch thread will be empty upon return.
     */
    private void flushEventDispatchThread() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                }
            });
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        } catch(java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
