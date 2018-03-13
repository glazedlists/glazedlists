package ca.odell.glazedlists.impl.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.calculation.Calculations;
import ca.odell.glazedlists.swing.CalculationsSwing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <code>SwingThreadProxyCalculationTest</code> tests the bahaviour of
 * {@link SwingThreadProxyCalculation}.
 *
 * @author Holger Brands
 */
public class SwingThreadProxyCalculationTest {

    private EventList<String> source;
    private Calculation<Integer> countCalc;
    private Calculation<Integer> countProxyCalc;

    @Before
    public void setUp() throws Exception {
        source = new BasicEventList<>();
        countCalc = Calculations.count(source);
        countProxyCalc = CalculationsSwing.swingThreadProxyCalculation(countCalc);
    }

    @Test
    public void testIsProxy() {
        assertTrue(CalculationsSwing.isSwingThreadProxyCalculation(countProxyCalc));
        assertFalse(CalculationsSwing.isSwingThreadProxyCalculation(countCalc));
    }

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

    @Test
    public void testPropertyChangeListener() throws Exception {
        final PropertyChangeEventRecorder recorder = new PropertyChangeEventRecorder();
        countProxyCalc.addPropertyChangeListener(recorder);
        assertEquals(0, recorder.getCallbackCount());
        assertNull(recorder.getLastCallbackOldValue());
        assertNull(recorder.getLastCallbackNewValue());
        source.add("one");
        Thread.sleep(300);
        assertEquals(1, recorder.getCallbackCount());
        assertTrue(recorder.getLastCallbackThreadWasEDT());
        assertEquals(Integer.valueOf(0), recorder.getLastCallbackOldValue());
        assertEquals(Integer.valueOf(1), recorder.getLastCallbackNewValue());
        recorder.reset();
        source.addAll(Arrays.asList("two", "three"));
        Thread.sleep(300);
        assertEquals(1, recorder.getCallbackCount());
        assertTrue(recorder.getLastCallbackThreadWasEDT());
        assertEquals(Integer.valueOf(1), recorder.getLastCallbackOldValue());
        assertEquals(Integer.valueOf(3), recorder.getLastCallbackNewValue());
        recorder.reset();
        source.remove(1);
        Thread.sleep(300);
        assertEquals(1, recorder.getCallbackCount());
        assertTrue(recorder.getLastCallbackThreadWasEDT());
        assertEquals(Integer.valueOf(3), recorder.getLastCallbackOldValue());
        assertEquals(Integer.valueOf(2), recorder.getLastCallbackNewValue());
        recorder.reset();
        source.clear();
        Thread.sleep(300);
        assertEquals(1, recorder.getCallbackCount());
        assertTrue(recorder.getLastCallbackThreadWasEDT());
        assertEquals(Integer.valueOf(2), recorder.getLastCallbackOldValue());
        assertEquals(Integer.valueOf(0), recorder.getLastCallbackNewValue());
        recorder.reset();
    }

    private static final class PropertyChangeEventRecorder implements PropertyChangeListener {
        private int callbackCount;
        private boolean lastCallbackThreadWasEDT;
        private Object lastCallbackOldValue;
        private Object lastCallbackNewValue;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            callbackCount++;
            lastCallbackThreadWasEDT = SwingUtilities.isEventDispatchThread();
            lastCallbackOldValue = evt.getOldValue();
            lastCallbackNewValue = evt.getNewValue();
        }

        public boolean getLastCallbackThreadWasEDT() { return lastCallbackThreadWasEDT; }
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
            lastCallbackThreadWasEDT = false;
            lastCallbackOldValue = null;
            lastCallbackNewValue = null;
        }
    }
}
