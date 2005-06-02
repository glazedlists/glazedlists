/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import javax.swing.*;
import java.util.EventListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import ca.odell.glazedlists.impl.beans.JavaBeanEventListConnector;

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

}
