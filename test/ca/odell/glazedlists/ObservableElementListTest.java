/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.beans.JavaBeanEventListConnector;
import junit.framework.TestCase;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ObservableElementListTest extends TestCase {

    private ObservableElementList labels;
    private ListEventCounter counter;

    public ObservableElementListTest() {
        super("Observable Elements - RFE 157");
    }

    public void setUp() {
        labels = new ObservableElementList(new BasicEventList(), GlazedLists.beanConnector(JLabel.class));
        counter = new ListEventCounter();
        labels.addListEventListener(counter);
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
            new ObservableElementList(new BasicEventList(), null);
            fail("Failed to receive a NullPointerException on null connector argument");
        } catch (NullPointerException npe) {}

        try {
            new ObservableElementList(null, GlazedLists.beanConnector(JLabel.class));
            fail("Failed to receive a NullPointerException on null source list");
        } catch (NullPointerException npe) {}

        new ObservableElementList(new BasicEventList(), GlazedLists.beanConnector(JLabel.class));

        // if source already has elements, listeners should be installed on them after the
        // ObservableElementList constructor has been called
        final JLabel listElement1 = new JLabel();
        final int initialListenerCount = listElement1.getPropertyChangeListeners().length;

        final BasicEventList source = new BasicEventList();
        source.add(listElement1);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);

        ObservableElementList list = new ObservableElementList(source, GlazedLists.beanConnector(JLabel.class));
        assertEquals(initialListenerCount + 1, listElement1.getPropertyChangeListeners().length);

        list.remove(listElement1);
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);
    }

    public void testDisposeSingleEventList() {
        this.runTestDispose(this.labels);
        this.runTestDispose(new ObservableElementList(new BasicEventList(), new MultiEventListJLabelConnector(false)));
    }

    private void runTestDispose(ObservableElementList labels) {
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
        this.runTestPickyConnector(new ObservableElementList(new BasicEventList(), new PickyConnector(JLabel.class)));
        this.runTestPickyConnector(new ObservableElementList(new BasicEventList(), new MultiEventListJLabelConnector(true)));
    }

    private void runTestPickyConnector(ObservableElementList list) {
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
        final ObservableElementList list = new ObservableElementList(new BasicEventList(), new LateBloomingMultiEventListJLabelConnector(bloomCount));

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
        // verify that listeners at indexes 1 through 1 + bloomCount are identical
        for (int i = 1; i < bloomCount; i++)
            assertTrue(propertyChangeListeners[i] == propertyChangeListeners[i+1]);

        // verify that listeners at indexes initialListenerCount1 + bloomCount through the end are NOT identical
        for (int i = bloomCount + initialListenerCount1; i < propertyChangeListeners.length - 1; i++)
            assertTrue(propertyChangeListeners[i] != propertyChangeListeners[i+1]);

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

        public EventListener installListener(Object element) {
            // install a common PropertyChangeListener until the bloomCount is reached
            // and then begin installing unique PropertyChangeListeners
            if (this.elementCount++ < this.bloomCount) {
                ((JLabel) element).addPropertyChangeListener(this.propertyChangeListener);
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
    private class MultiEventListJLabelConnector extends JavaBeanEventListConnector {
        private final boolean picky;
        private int elementCount = 0;

        public MultiEventListJLabelConnector(boolean picky) {
            super(JLabel.class);
            this.picky = picky;
        }

        public EventListener installListener(Object element) {
            if (this.picky && this.elementCount++ % 2 == 0)
                return null;

            final PropertyChangeListener propertyChangeHandler = new PropertyChangeHandler();
            ((JLabel) element).addPropertyChangeListener(propertyChangeHandler);
            return propertyChangeHandler;
        }

        public void uninstallListener(Object element, EventListener listener) {
            ((JLabel) element).removePropertyChangeListener((PropertyChangeListener) listener);
        }
    }

    /**
     * This connector only installs JavaBean listeners on every second element
     * that is added to the associated {@link ObservableElementList}.
     */
    private class PickyConnector extends JavaBeanEventListConnector {
        private int elementCount = 0;

        public PickyConnector(Class beanClass) {
            super(beanClass);
        }

        public EventListener installListener(Object element) {
            if (this.elementCount++ % 2 == 0)
                return null;

            return super.installListener(element);
        }
    }
}