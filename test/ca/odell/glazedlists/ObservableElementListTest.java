/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import javax.swing.*;

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

    public void testDispose() {
        final ObservableElementList list = new ObservableElementList(new BasicEventList(), GlazedLists.beanConnector(JLabel.class));
        final JLabel listElement1 = new JLabel();
        final int initialListenerCount = listElement1.getPropertyChangeListeners().length;

        list.add(listElement1);
        assertEquals(initialListenerCount + 1, listElement1.getPropertyChangeListeners().length);

        list.dispose();
        assertEquals(initialListenerCount, listElement1.getPropertyChangeListeners().length);

        assertTrue(listElement1 == list.get(0));
        list.remove(0);
        assertTrue(list.isEmpty());
    }
}