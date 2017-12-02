/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import javax.swing.SwingUtilities;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class SwingThreadProxyEventListTest {

    @Test
    @Ignore
    public void testExceptionInListenerCausesNextListEventToBeDeliveredOnWrongThread() throws Exception {
        final EventList<String> source = new BasicEventList<String>();
        final EventList<String> threadProxy = GlazedListsSwing.swingThreadProxyList(source);

        final ListEventRecorder recorder = new ListEventRecorder();
        threadProxy.addListEventListener(recorder);
        assertEquals(0, recorder.getCallbackCountAndReset());

        // verify that the recorder is working
        source.add("New");
        Thread.sleep(500);
        assertEquals(1, recorder.getCallbackCountAndReset());
        assertTrue(recorder.getLastCallbackThreadWasEDT());

        // configure the recorder to throw an exception during its next callback
        recorder.setThrowException(true);
        source.add("This callback throws the exception");
        Thread.sleep(500);
        assertEquals(1, recorder.getCallbackCountAndReset());
        assertTrue(recorder.getLastCallbackThreadWasEDT());

        // verify that the NEXT ListEvent *still* arrives on the EDT
        recorder.setThrowException(false);
        source.add("This used to arrive in the recorder on the wrong thread");
        Thread.sleep(500);
        assertEquals(1, recorder.getCallbackCountAndReset());
        assertTrue(recorder.getLastCallbackThreadWasEDT());
    }

    private static final class ListEventRecorder implements ListEventListener<String> {

        private int callbackCount = 0;
        private boolean lastCallbackThreadWasEDT = true;
        private boolean throwException = false;

        @Override
        public void listChanged(ListEvent<String> listChanges) {
            callbackCount++;
            lastCallbackThreadWasEDT = SwingUtilities.isEventDispatchThread();

            if (throwException)
                throw new RuntimeException("This tests the behaviour of ThreadProxyEventList when ListEventListeners throw RuntimeExceptions");
        }

        public boolean getLastCallbackThreadWasEDT() { return lastCallbackThreadWasEDT; }
        public void setThrowException(boolean throwException) { this.throwException = throwException; }
        public int getCallbackCountAndReset() {
            final int result = callbackCount;
            callbackCount = 0;
            return result;
        }
    }
}
