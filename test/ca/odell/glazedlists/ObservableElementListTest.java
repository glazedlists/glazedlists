/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.beans.JavaBeanEventListConnector;
import junit.framework.TestCase;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ObservableElementListTest extends TestCase {

    private ObservableElementList<JLabel> labels;
    private ListConsistencyListener<JLabel> counter;

    public ObservableElementListTest() {
        super("Observable Elements - RFE 157");
    }

    public void setUp() {
        labels = new ObservableElementList<JLabel>(new BasicEventList<JLabel>(), GlazedLists.beanConnector(JLabel.class));
        counter = ListConsistencyListener.install(labels);
        assertEquals(0, counter.getEventCount());
    }

    public void tearDown() {
        labels = null;
        counter = null;
    }

    /**
     * Tests ObservableEleemntLists' handling of JavaBean PropertyChangeEvents
     */
    public void testJavabeans() {
        JLabel ottawa = new JLabel("Rough Riders");
        JLabel wrestling = new JLabel("WWF");

        labels.add(ottawa);
        labels.add(wrestling);
        assertEquals(2, counter.getEventCount());

        ottawa.setText("Renegades");
        assertEquals(3, counter.getEventCount());
        assertEquals(1, counter.getChangeCount(2));

        wrestling.setText("WWE");
        assertEquals(4, counter.getEventCount());
        assertEquals(1, counter.getChangeCount(3));
    }

    public void testAddRemoveListeners() {
        final JLabel listElement1 = new JLabel();
        final JLabel listElement2 = new JLabel();
        final int initialListenerCount = listElement1.getPropertyChangeListeners().length;

        labels.add(listElement1);
        assertEquals(initialListenerCount + 1, listElement1.getPropertyChangeListeners().length);

        labels.remove(listElement1);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);

        labels.add(listElement1);
        assertEquals(initialListenerCount + 1, listElement1.getPropertyChangeListeners().length);

        labels.set(0, null);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);

        labels.set(0, listElement1);
        assertEquals(initialListenerCount + 1, listElement1.getPropertyChangeListeners().length);

        labels.set(0, listElement2);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);
        assertEquals(initialListenerCount + 1, listElement2.getPropertyChangeListeners().length);

        labels.add(listElement2);
        assertEquals(initialListenerCount + 2, listElement2.getPropertyChangeListeners().length);

        labels.remove(0);
        assertEquals(initialListenerCount + 1, listElement2.getPropertyChangeListeners().length);

        labels.remove(0);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);
        assertEquals(initialListenerCount, listElement2.getPropertyChangeListeners().length);
    }

    public void testConstructor() {
        try {
            new ObservableElementList<JLabel>(new BasicEventList<JLabel>(), null);
            fail("Failed to receive a NullPointerException on null connector argument");
        } catch (NullPointerException npe) {}

        try {
            new ObservableElementList<JLabel>(null, GlazedLists.beanConnector(JLabel.class));
            fail("Failed to receive a NullPointerException on null source list");
        } catch (NullPointerException npe) {}

        new ObservableElementList<JLabel>(new BasicEventList<JLabel>(), GlazedLists.beanConnector(JLabel.class));

        // if source already has elements, listeners should be installed on them after the
        // ObservableElementList constructor has been called
        final JLabel listElement1 = new JLabel();
        final int initialListenerCount = listElement1.getPropertyChangeListeners().length;

        final BasicEventList<JLabel> source = new BasicEventList<JLabel>();
        source.add(listElement1);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);

        ObservableElementList<JLabel> list = new ObservableElementList<JLabel>(source, GlazedLists.beanConnector(JLabel.class));
        assertEquals(initialListenerCount + 1, listElement1.getPropertyChangeListeners().length);

        list.remove(listElement1);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);
    }

    public void testDisposeSingleEventList() {
        this.runTestDispose(this.labels);
        this.runTestDispose(new ObservableElementList<JLabel>(new BasicEventList<JLabel>(), new MultiEventListJLabelConnector(false)));
    }

    private void runTestDispose(ObservableElementList<JLabel> labels) {
        final JLabel listElement1 = new JLabel();
        final int initialListenerCount = listElement1.getPropertyChangeListeners().length;

        labels.add(listElement1);
        assertEquals(initialListenerCount + 1, listElement1.getPropertyChangeListeners().length);

        labels.dispose();
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);

        assertTrue(listElement1 == labels.get(0));
        labels.remove(0);
        assertTrue(labels.isEmpty());
    }

    public void testPickyConnector() {
        this.runTestPickyConnector(new ObservableElementList<JLabel>(new BasicEventList<JLabel>(), new PickyJLabelConnector()));
        this.runTestPickyConnector(new ObservableElementList<JLabel>(new BasicEventList<JLabel>(), new MultiEventListJLabelConnector(true)));
    }

    private void runTestPickyConnector(ObservableElementList<JLabel> list) {
        final JLabel listElement1 = new JLabel();
        final int initialListenerCount = listElement1.getPropertyChangeListeners().length;

        list.add(listElement1);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);

        list.add(listElement1);
        assertEquals(initialListenerCount + 1, listElement1.getPropertyChangeListeners().length);

        list.remove(0);
        assertEquals(initialListenerCount + 1, listElement1.getPropertyChangeListeners().length);

        list.remove(0);
        assertTrue(list.isEmpty());
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);
    }

    public void testLateBloomingMultiEventListConnector() {
        final int bloomCount = 5;
        final ObservableElementList<JLabel> list = new ObservableElementList<JLabel>(new BasicEventList<JLabel>(), new LateBloomingMultiEventListJLabelConnector(bloomCount));

        final JLabel listElement1 = new JLabel();
        final int initialListenerCount1 = listElement1.getPropertyChangeListeners().length;

        // install a common listener for the first bloomCount insertions
        for (int i = 0; i < bloomCount; i++)
            list.add(listElement1);
        assertEquals(initialListenerCount1 + bloomCount, listElement1.getPropertyChangeListeners().length);

        // install new listeners for the next bloomCount
        for (int i = 0; i < bloomCount; i++)
            list.add(listElement1);
        assertEquals(initialListenerCount1 + 2 * bloomCount, listElement1.getPropertyChangeListeners().length);

        final PropertyChangeListener[] propertyChangeListeners = listElement1.getPropertyChangeListeners();

        // verify we have 2xbloomcount listeners
        List<EventListener> listeners = new ArrayList<EventListener>();
        Set<EventListener> uniqueListeners = new HashSet<EventListener>();
        for (int i = 0; i < propertyChangeListeners.length; i++) {
            if(!(propertyChangeListeners[i] instanceof JavaBeanEventListConnector.PropertyChangeHandler)) continue;
            listeners.add(propertyChangeListeners[i]);
            uniqueListeners.add(propertyChangeListeners[i]);
        }
        assertEquals(bloomCount * 2, listeners.size());
        assertEquals(bloomCount + 1, uniqueListeners.size());

        // install a completely new list element
        final JLabel listElement2 = new JLabel();
        final int initialListenerCount2 = listElement2.getPropertyChangeListeners().length;
        list.add(listElement2);
        assertEquals(initialListenerCount2 + 1, listElement2.getPropertyChangeListeners().length);

        list.remove(listElement2);
        assertEquals(initialListenerCount2, listElement2.getPropertyChangeListeners().length);

        for (int i = 1; i < 2*bloomCount+1; i++) {
            list.remove(0);
            assertEquals(initialListenerCount1 + 2*bloomCount - i, listElement1.getPropertyChangeListeners().length);
        }
    }

    public void testMultithreadedUpdate() throws InterruptedException {
        final LazyThreadedConnector connector = new LazyThreadedConnector();
        final ObservableElementList<JLabel> list = new ObservableElementList<JLabel>(new BasicEventList<JLabel>(), connector);

        final JLabel listElement1 = new JLabel();
        list.add(listElement1);

        // this will pause half a second before notifying the list of the change
        listElement1.setText("testing");

        // attempt to modify the list before the notification is received
        list.remove(0);

        // get the Thread that was started by the connector
        final Thread updateThread = connector.getLastUpdateThread();

        // ensure the update thread is still running and thus the test is valid
        assertTrue(updateThread.isAlive());

        // wait until the update occurs
        updateThread.join();

        // ensure the update thread finished with no exception
        assertEquals(false, connector.isExitDueToException());
    }

    /**
     * This connector installs a common JavaBean listener on every element
     * until bloomCount has been reached, when it begins installing unique
     * listeners on each additional element.
     */
    private class LateBloomingMultiEventListJLabelConnector extends MultiEventListJLabelConnector {
        private int elementCount = 0;
        private final int bloomCount;

        public LateBloomingMultiEventListJLabelConnector(int bloomCount) {
            super(false);
            this.bloomCount = bloomCount;
        }

        public EventListener installListener(JLabel element) {
            // install a common PropertyChangeListener until the bloomCount is reached
            // and then begin installing unique PropertyChangeListeners
            if (this.elementCount++ < this.bloomCount) {
                element.addPropertyChangeListener(this.propertyChangeListener);
                return this.propertyChangeListener;
            }

            return super.installListener(element);
        }
    }

    /**
     * This connector only installs JavaBean listeners on every second element
     * that is added to the associated {@link ObservableElementList} if the picky
     * flag is set to true.
     */
    private class MultiEventListJLabelConnector extends JavaBeanEventListConnector<JLabel> {
        private final boolean picky;
        private int elementCount = 0;

        public MultiEventListJLabelConnector(boolean picky) {
            super(JLabel.class);
            this.picky = picky;
        }

        public EventListener installListener(JLabel element) {
            if (this.picky && this.elementCount++ % 2 == 0)
                return null;

            final PropertyChangeListener propertyChangeHandler = new PropertyChangeHandler();
            element.addPropertyChangeListener(propertyChangeHandler);
            return propertyChangeHandler;
        }

        public void uninstallListener(JLabel element, EventListener listener) {
            element.removePropertyChangeListener((PropertyChangeListener) listener);
        }
    }

    /**
     * This connector only installs JavaBean listeners on every second element
     * that is added to the associated {@link ObservableElementList}.
     */
    private class PickyJLabelConnector extends JavaBeanEventListConnector<JLabel> {
        private int elementCount = 0;

        public PickyJLabelConnector() {
            super(JLabel.class);
        }

        public EventListener installListener(JLabel element) {
            if (this.elementCount++ % 2 == 0)
                return null;

            return super.installListener(element);
        }
    }

    /**
     * This connector only installs JavaBean listeners on every second element
     * that is added to the associated {@link ObservableElementList}.
     */
    private class LazyThreadedConnector extends JavaBeanEventListConnector<JLabel> {
        private Thread lastUpdateThread;
        private boolean exitDueToException = false;

        public LazyThreadedConnector() {
            super(JLabel.class);
        }

        protected PropertyChangeListener createPropertyChangeListener() {
            return new LazyThreadedPropertyChangeHandler();
        }

        public Thread getLastUpdateThread() {
            return lastUpdateThread;
        }

        public boolean isExitDueToException() {
            return exitDueToException;
        }

        /**
         * The PropertyChangeListener which notifies the
         * {@link ObservableElementList} within this Connector of changes to
         * list elements.
         */
        protected class LazyThreadedPropertyChangeHandler extends PropertyChangeHandler {
            public void propertyChange(final PropertyChangeEvent event) {
                // start another thread which, in half a second, will notify the list of the
                // update (which allows us time to modify the list in our TestCase thread)
                lastUpdateThread = new Thread(new DelayThenNotifyRunnable(event, this));
                lastUpdateThread.start();
            }
        }
        private class DelayThenNotifyRunnable implements Runnable {
            private PropertyChangeEvent event;
            private LazyThreadedPropertyChangeHandler handler;

            public DelayThenNotifyRunnable(PropertyChangeEvent event, LazyThreadedPropertyChangeHandler handler) {
                this.event = event;
                this.handler = handler;
            }

            public void run() {
                try {
                    // 1. delay
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {}

                    // 2. notify the list of the change
                    handler.propertyChange(event);
                } catch(Exception e) {
                    exitDueToException = true;
                }
            }
        }
    }
}