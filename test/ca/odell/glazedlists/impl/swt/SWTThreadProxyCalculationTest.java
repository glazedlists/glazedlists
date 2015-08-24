/* Glazed Lists                                                 (c) 2003-2010 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swt;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ExecuteOnMainThread;
import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.calculation.Calculations;
import ca.odell.glazedlists.swt.CalculationsSWT;
import ca.odell.glazedlists.swt.SwtClassRule;
import ca.odell.glazedlists.swt.SwtTestRule;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * <code>SWTThreadProxyCalculationTest</code> tests the bahaviour of
 * {@link SWTThreadProxyCalculation}.
 *
 * @author Holger Brands
 */
public class SWTThreadProxyCalculationTest {

    private EventList<String> source;
    private Calculation<Integer> countCalc;
    private Calculation<Integer> countProxyCalc;

    @ClassRule
    public static SwtClassRule swtClassRule = new SwtClassRule();

    @Rule
    public SwtTestRule swtTestRule = new SwtTestRule(swtClassRule);

    @Before
    public void setUp() {
        // TODO: SWT tests only work reliably on Windows
        Assume.assumeTrue( "Test is only reliable on Windows",
            System.getProperty( "os.name" ).contains( "Windows" ) );

        source = new BasicEventList<String>();
        countCalc = Calculations.count(source);
        countProxyCalc = CalculationsSWT.swtThreadProxyCalculation(countCalc, swtClassRule.getDisplay());
    }

    @Test
    public void testIsProxy() {
        assertTrue(CalculationsSWT.isSWTThreadProxyCalculation(countProxyCalc));
        assertFalse(CalculationsSWT.isSWTThreadProxyCalculation(countCalc));
    }

    /** Tests values after Threadproxying. */
    @Test
    public void testValue() {
        assertEquals(Integer.valueOf(0), countCalc.getValue());
        assertEquals(Integer.valueOf(0), countProxyCalc.getValue());
        source.add("one");
        assertEquals(Integer.valueOf(1), countCalc.getValue());
        assertEquals(Integer.valueOf(1), countProxyCalc.getValue());
        source.addAll(Arrays.asList("two", "three"));
        assertEquals(Integer.valueOf(3), countCalc.getValue());
        assertEquals(Integer.valueOf(3), countProxyCalc.getValue());
        source.remove(1);
        assertEquals(Integer.valueOf(2), countCalc.getValue());
        assertEquals(Integer.valueOf(2), countProxyCalc.getValue());
        source.clear();
        assertEquals(Integer.valueOf(0), countCalc.getValue());
        assertEquals(Integer.valueOf(0), countProxyCalc.getValue());
    }

    /**
     * This tests creates the SWT display and runs an event loop in a different thread than the
     * main thread to test the thread proxy behaviour.
     */
    @Test
    @ExecuteOnMainThread
    public void testOnMainThreadPropertyChangeListener() {
        final DisplayRunner displayInit = new DisplayRunner();
        // start the background task to init the display
        final Thread background = new Thread(displayInit);
        background.start();
        sleep(1000);
        final PropertyChangeEventRecorder recorder = new PropertyChangeEventRecorder(displayInit.getDisplay());
        source = new BasicEventList<String>();
        countCalc = Calculations.count(source);
        countProxyCalc = CalculationsSWT.swtThreadProxyCalculation(countCalc, displayInit.getDisplay());
        countProxyCalc.addPropertyChangeListener(recorder);
        assertEquals(0, recorder.getCallbackCount());
        assertNull(recorder.getLastCallbackOldValue());
        assertNull(recorder.getLastCallbackNewValue());
        source.add("one");
        sleep(300);
        assertEquals(1, recorder.getCallbackCount());
        assertTrue(recorder.getLastCallbackThreadWasDisplayThread());
        assertEquals(Integer.valueOf(0), recorder.getLastCallbackOldValue());
        assertEquals(Integer.valueOf(1), recorder.getLastCallbackNewValue());
        recorder.reset();
        source.addAll(Arrays.asList("two", "three"));
        sleep(300);
        assertEquals(1, recorder.getCallbackCount());
        assertTrue(recorder.getLastCallbackThreadWasDisplayThread());
        assertEquals(Integer.valueOf(1), recorder.getLastCallbackOldValue());
        assertEquals(Integer.valueOf(3), recorder.getLastCallbackNewValue());
        recorder.reset();
        source.remove(1);
        sleep(300);
        assertEquals(1, recorder.getCallbackCount());
        assertTrue(recorder.getLastCallbackThreadWasDisplayThread());
        assertEquals(Integer.valueOf(3), recorder.getLastCallbackOldValue());
        assertEquals(Integer.valueOf(2), recorder.getLastCallbackNewValue());
        recorder.reset();
        source.clear();
        sleep(300);
        assertEquals(1, recorder.getCallbackCount());
        assertTrue(recorder.getLastCallbackThreadWasDisplayThread());
        assertEquals(Integer.valueOf(2), recorder.getLastCallbackOldValue());
        assertEquals(Integer.valueOf(0), recorder.getLastCallbackNewValue());
        recorder.reset();
        // stop the event loop and wait for completion
        try {
            displayInit.stop();
            background.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Thread was interrupted");
        }
    }

    /**
     * Initializes the SWT display and runs the event loop.
     *
     * @author Holger Brands
     */
    private static class DisplayRunner implements Runnable {
        private Display display;
        private Shell shell;
        private volatile boolean stop;
        @Override
        public void run() {
            display = new Display();
            shell = new Shell(display);
            while (!shell.isDisposed () && !stop) {
                if (!display.readAndDispatch ()) {
                    display.sleep ();
                }
            }
            display.dispose();
        }
        Display getDisplay() {
            return display;
        }
        void stop() {
            stop = true;
            display.wake();
        }
    }

    private static final class PropertyChangeEventRecorder implements PropertyChangeListener {
        private int callbackCount;
        private boolean lastCallbackThreadWasDisplayThread;
        private Object lastCallbackOldValue;
        private Object lastCallbackNewValue;
        private final Display display;

        PropertyChangeEventRecorder(Display display) {
            this.display = display;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            callbackCount++;
            lastCallbackThreadWasDisplayThread = (Thread.currentThread() == display.getThread());
            lastCallbackOldValue = evt.getOldValue();
            lastCallbackNewValue = evt.getNewValue();
        }
        public boolean getLastCallbackThreadWasDisplayThread() {
            return lastCallbackThreadWasDisplayThread;
        }
        public int getCallbackCount() {
            return callbackCount;
        }
        public Object getLastCallbackOldValue() {
            return lastCallbackOldValue;
        }
        public Object getLastCallbackNewValue() {
            return lastCallbackNewValue;
        }
        public void reset() {
            callbackCount = 0;
            lastCallbackThreadWasDisplayThread = false;
            lastCallbackOldValue = null;
            lastCallbackNewValue = null;
        }
    }
}
