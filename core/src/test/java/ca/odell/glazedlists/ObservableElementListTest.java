/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.beans.BeanConnector;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ObservableElementListTest {

    private ObservableElementList<JLabel> labels;
    private ListConsistencyListener<JLabel> counter;

    @Before
    public void setUp() {
        labels = new ObservableElementList<>(new BasicEventList<JLabel>(), GlazedLists.beanConnector(JLabel.class));
        counter = ListConsistencyListener.install(labels);
        assertEquals(0, counter.getEventCount());
    }

    @After
    public void tearDown() {
        labels = null;
        counter = null;
    }

    /**
     * Tests ObservableEleemntLists' handling of JavaBean PropertyChangeEvents
     */
    @Test
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

    @Test
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

    @Test
    public void testConstructor() {
        try {
            new ObservableElementList<>(new BasicEventList<JLabel>(), null);
            fail("Failed to receive a NullPointerException on null connector argument");
        } catch (NullPointerException npe) {}

        try {
            new ObservableElementList<>(null, GlazedLists.beanConnector(JLabel.class));
            fail("Failed to receive a NullPointerException on null source list");
        } catch (NullPointerException npe) {}

        new ObservableElementList<>(new BasicEventList<JLabel>(), GlazedLists.beanConnector(JLabel.class));

        // if source already has elements, listeners should be installed on them after the
        // ObservableElementList constructor has been called
        final JLabel listElement1 = new JLabel();
        final int initialListenerCount = listElement1.getPropertyChangeListeners().length;

        final BasicEventList<JLabel> source = new BasicEventList<>();
        source.add(listElement1);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);

        ObservableElementList<JLabel> list = new ObservableElementList<>(source, GlazedLists.beanConnector(JLabel.class));
        assertEquals(initialListenerCount + 1, listElement1.getPropertyChangeListeners().length);

        list.remove(listElement1);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);
    }

    @Test
    public void testDisposeSingleEventList() {
        this.runTestDispose(this.labels);
        this.runTestDispose(new ObservableElementList<>(new BasicEventList<JLabel>(), new MultiEventListJLabelConnector(false)));
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

    @Test
    public void testEventMatcher() {
        // match no property change event
        final Matcher<PropertyChangeEvent> falseMatcher = Matchers.falseMatcher();
        ObservableElementList<JLabel> labelList =
            new ObservableElementList<>(new BasicEventList<JLabel>(), GlazedLists.beanConnector(JLabel.class, falseMatcher));
        final JLabel listElement1 = new JLabel();
        final JLabel listElement2 = new JLabel();
        labelList.add(listElement1);
        labelList.add(listElement2);
        ListConsistencyListener listener = ListConsistencyListener.install(labelList);
        assertEquals(0, listener.getEventCount());
        listElement1.setText("Item 1");
        listElement2.setText("Item 2");
        listElement1.setEnabled(false);
        listElement2.setBackground(Color.RED);
        assertEquals(0, listener.getEventCount());

        // match only property change events for properties 'text and 'enabled'
        Matcher<PropertyChangeEvent> byNameMatcher = Matchers.propertyEventNameMatcher(true, "text", "enabled");
        labelList = new ObservableElementList<>(new BasicEventList<JLabel>(), GlazedLists
                .beanConnector(JLabel.class, byNameMatcher));
        labelList.add(listElement1);
        labelList.add(listElement2);
        listener = ListConsistencyListener.install(labelList);
        assertEquals(0, listener.getEventCount());
        listElement1.setText("Text 1");
        assertEquals(1, listener.getEventCount());
        listElement2.setText("Text 2");
        assertEquals(2, listener.getEventCount());
        listElement1.setBackground(Color.RED);
        assertEquals(2, listener.getEventCount());
        listElement2.setForeground(Color.GRAY);
        assertEquals(2, listener.getEventCount());
        listElement1.setEnabled(true);
        assertEquals(3, listener.getEventCount());

        // match only property change events for properties 'text and 'enabled'
        labelList = new ObservableElementList<>(new BasicEventList<JLabel>(), GlazedLists
                .beanConnector(JLabel.class, true, "text", "enabled"));
        labelList.add(listElement1);
        labelList.add(listElement2);
        listener = ListConsistencyListener.install(labelList);
        assertEquals(0, listener.getEventCount());
        listElement1.setText("Item 1");
        assertEquals(1, listener.getEventCount());
        listElement2.setText("Item 2");
        assertEquals(2, listener.getEventCount());
        listElement1.setBackground(Color.GREEN);
        assertEquals(2, listener.getEventCount());
        listElement2.setForeground(Color.WHITE);
        assertEquals(2, listener.getEventCount());
        listElement1.setEnabled(false);
        assertEquals(3, listener.getEventCount());

        // match all property change events excluding properties 'text' and 'enabled'
        labelList = new ObservableElementList<>(new BasicEventList<JLabel>(), GlazedLists
                .beanConnector(JLabel.class, false, "text", "enabled"));
        labelList.add(listElement1);
        labelList.add(listElement2);
        listener = ListConsistencyListener.install(labelList);
        assertEquals(0, listener.getEventCount());
        listElement1.setText("Text 1");
        assertEquals(0, listener.getEventCount());
        listElement2.setText("Text 2");
        assertEquals(0, listener.getEventCount());
        listElement1.setBackground(Color.BLACK);
        assertEquals(1, listener.getEventCount());
        listElement2.setForeground(Color.YELLOW);
        assertEquals(2, listener.getEventCount());
        listElement1.setEnabled(true);
        assertEquals(2, listener.getEventCount());
    }

    @Test
    public void testPickyConnector() {
        this.runTestPickyConnector(new ObservableElementList<>(new BasicEventList<JLabel>(), new PickyJLabelConnector()));
        this.runTestPickyConnector(new ObservableElementList<>(new BasicEventList<JLabel>(), new MultiEventListJLabelConnector(true)));
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

    @Test
    public void testLateBloomingMultiEventListConnector() {
        final int bloomCount = 5;
        final ObservableElementList<JLabel> list = new ObservableElementList<>(new BasicEventList<JLabel>(), new LateBloomingMultiEventListJLabelConnector(bloomCount));

        final JLabel listElement1 = new JLabel();
        final int initialListenerCount1 = listElement1.getPropertyChangeListeners().length;

        // install a common listener for the first bloomCount insertions
        for (int i = 0; i < bloomCount; i++) {
            list.add(listElement1);
        }
        assertEquals(initialListenerCount1 + bloomCount, listElement1.getPropertyChangeListeners().length);

        // install new listeners for the next bloomCount
        for (int i = 0; i < bloomCount; i++) {
            list.add(listElement1);
        }
        assertEquals(initialListenerCount1 + 2 * bloomCount, listElement1.getPropertyChangeListeners().length);

        final PropertyChangeListener[] propertyChangeListeners = listElement1.getPropertyChangeListeners();

        // verify we have 2xbloomcount listeners
        List<EventListener> listeners = new ArrayList<>();
        Set<EventListener> uniqueListeners = new HashSet<>();
        for (int i = 0; i < propertyChangeListeners.length; i++) {
            if(!(propertyChangeListeners[i] instanceof BeanConnector.PropertyChangeHandler)) {
                continue;
            }
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

    @Test
    public void testMultithreadedUpdate() throws InterruptedException {
        final LazyThreadedConnector connector = new LazyThreadedConnector();
        final ObservableElementList<JLabel> list = new ObservableElementList<>(new BasicEventList<JLabel>(), connector);

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

    @Test
    public void testGenerics() {
        // should be able to use an EventList<JLabel> and a Connector<Component>, for example
        new ObservableElementList<>(new BasicEventList<JLabel>(), new ComponentConnector());
    }

	@Test
	public void testSwitchToMultiListenerMode() {
        final ObservableElementList<JLabel> list = new ObservableElementList<>(new BasicEventList<JLabel>(), new BurstOfThreeJLabelConnector());

        // pattern of installed PropertyChangeListeners: _ _ _ A A A _ _ _ B B B
        for (int i = 0; i < 12; i++) {
            list.add(new JLabel());
        }

        assertEquals(list.get(0).getPropertyChangeListeners().length, list.get(1).getPropertyChangeListeners().length);
        assertEquals(list.get(1).getPropertyChangeListeners().length, list.get(2).getPropertyChangeListeners().length);
        assertEquals(list.get(2).getPropertyChangeListeners().length+1, list.get(3).getPropertyChangeListeners().length);

        assertEquals(list.get(3).getPropertyChangeListeners().length, list.get(4).getPropertyChangeListeners().length);
        assertEquals(list.get(4).getPropertyChangeListeners().length, list.get(5).getPropertyChangeListeners().length);
        assertEquals(list.get(5).getPropertyChangeListeners().length-1, list.get(6).getPropertyChangeListeners().length);

        assertEquals(list.get(6).getPropertyChangeListeners().length, list.get(7).getPropertyChangeListeners().length);
        assertEquals(list.get(7).getPropertyChangeListeners().length, list.get(8).getPropertyChangeListeners().length);
        assertEquals(list.get(8).getPropertyChangeListeners().length+1, list.get(9).getPropertyChangeListeners().length);

        assertEquals(list.get(9).getPropertyChangeListeners().length, list.get(10).getPropertyChangeListeners().length);
        assertEquals(list.get(10).getPropertyChangeListeners().length, list.get(11).getPropertyChangeListeners().length);

        list.dispose();

        // all Labels should now have the same number of listeners
        for (Iterator<JLabel> i = list.iterator(); i.hasNext();) {
            assertEquals(list.get(0).getPropertyChangeListeners().length, i.next().getPropertyChangeListeners().length);
        }
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

        @Override
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
    private class MultiEventListJLabelConnector extends BeanConnector<JLabel> {
        private final boolean picky;
        private int elementCount = 0;

        public MultiEventListJLabelConnector(boolean picky) {
            super(JLabel.class);
            this.picky = picky;
        }

        @Override
        public EventListener installListener(JLabel element) {
            if (this.picky && this.elementCount++ % 2 == 0) {
                return null;
            }

            final PropertyChangeListener propertyChangeHandler = new PropertyChangeHandler();
            element.addPropertyChangeListener(propertyChangeHandler);
            return propertyChangeHandler;
        }

        @Override
        public void uninstallListener(JLabel element, EventListener listener) {
            element.removePropertyChangeListener((PropertyChangeListener) listener);
        }
    }

    /**
     * This connector installs a "pattern of PropertyChangeListeners" like this:
     *
     * _ _ _ X X X _ _ _ Y Y Y _ _ _ Z Z Z....
     *
     * where "_" denotes NO LISTENER INSTALLED, and X, Y, and Z denote different
     * instances of PropertyChangeListeners.
     *
     * https://glazedlists.dev.java.net/issues/show_bug.cgi?id=452
     */
    private class BurstOfThreeJLabelConnector extends BeanConnector<JLabel> {
        private final int groupSize = 3;
        private int currentGroupSize;
        private PropertyChangeListener currentGroupListener = new PropertyChangeHandler();

        public BurstOfThreeJLabelConnector() {
            super(JLabel.class);
            this.currentGroupSize = -groupSize;
        }

        @Override
        public EventListener installListener(JLabel element) {
            currentGroupSize++;

            if (currentGroupSize > groupSize) {
                currentGroupSize = -groupSize+1;
                currentGroupListener = new PropertyChangeHandler();
            }

            if (currentGroupSize > 0 && currentGroupSize <= groupSize) {
                element.addPropertyChangeListener(currentGroupListener);
                return currentGroupListener;
            }

            return null;
        }

        @Override
        public void uninstallListener(JLabel element, EventListener listener) {
            element.removePropertyChangeListener((PropertyChangeListener) listener);
        }
    }

    private static class ComponentConnector extends BeanConnector<Component> {
        public ComponentConnector() {
            super(Component.class);
        }
    }

    /**
     * This connector only installs JavaBean listeners on every second element
     * that is added to the associated {@link ObservableElementList}.
     */
    private static class PickyJLabelConnector extends BeanConnector<JLabel> {
        private int elementCount = 0;

        public PickyJLabelConnector() {
            super(JLabel.class);
        }

        @Override
        public EventListener installListener(JLabel element) {
            if (this.elementCount++ % 2 == 0) {
                return null;
            }

            return super.installListener(element);
        }
    }

    /**
     * This connector only installs JavaBean listeners on every second element
     * that is added to the associated {@link ObservableElementList}.
     */
    private static class LazyThreadedConnector extends BeanConnector<JLabel> {
        private Thread lastUpdateThread;
        private boolean exitDueToException = false;

        public LazyThreadedConnector() {
            super(JLabel.class);
        }

        @Override
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
            @Override
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

            @Override
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
