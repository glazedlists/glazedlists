package ca.odell.glazedlists.impl.beans;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import junit.framework.TestCase;

import javax.swing.*;

public class JavaBeanEventListConnectorTest extends TestCase {

    public void testBeanClassConstructor() {
        try {
            new JavaBeanEventListConnector(null);
            fail("Failed to receive NullPointerException with null beanClass");
        } catch (NullPointerException npe) { }

        try {
            new JavaBeanEventListConnector(Object.class);
            fail("Failed to receive RuntimeException for beanClass without add/remove PropertyChangeListener methods");
        } catch (IllegalArgumentException iae) { }

        new JavaBeanEventListConnector(JComponent.class);
    }

    public void testBeanClassAndMethodNamesConstructor() {
        try {
            new JavaBeanEventListConnector(null, "addPropertyChangeListener", "removePropertyChangeListener");
            fail("Failed to receive NullPointerException with null beanClass");
        } catch (NullPointerException npe) { }

        try {
            new JavaBeanEventListConnector(JComponent.class, null, "removePropertyChangeListener");
            fail("Failed to receive NullPointerException with null add method name");
        } catch (NullPointerException npe) { }

        try {
            new JavaBeanEventListConnector(JComponent.class, "addPropertyChangeListener", null);
            fail("Failed to receive NullPointerException with null remove method name");
        } catch (NullPointerException npe) { }

        try {
            new JavaBeanEventListConnector(Object.class, "addPropertyChangeListener", "removePropertyChangeListener");
            fail("Failed to receive IllegalArgumentException with invalid beanClass");
        } catch (IllegalArgumentException iae) { }

        try {
            new JavaBeanEventListConnector(JComponent.class, "addBOOBLAH", "removePropertyChangeListener");
            fail("Failed to receive IllegalArgumentException with invalid add method name");
        } catch (IllegalArgumentException iae) { }

        try {
            new JavaBeanEventListConnector(JComponent.class, "addPropertyChangeListener", "removeBOOLBLAH");
            fail("Failed to receive IllegalArgumentException with invalid remove method name");
        } catch (IllegalArgumentException iae) { }

        new JavaBeanEventListConnector(JComponent.class, "addPropertyChangeListener", "removePropertyChangeListener");
    }

    public void testListenerCascade() {
        final CountingObservableElementList list = new CountingObservableElementList(new BasicEventList(), GlazedLists.beanConnector(JComponent.class));
        final JLabel listElement = new JLabel();
        list.add(listElement);
        assertEquals(0, list.elementChangeCount);

        listElement.setText("booblah");
        assertEquals(1, list.elementChangeCount);

        listElement.setText("whippleback");
        assertEquals(2, list.elementChangeCount);

        list.clear();
        listElement.setText("booblah");
        assertEquals(2, list.elementChangeCount);
    }

    private static class CountingObservableElementList extends ObservableElementList {
        private int elementChangeCount = 0;

        public CountingObservableElementList(EventList source, Connector elementConnector) {
            super(source, elementConnector);
        }

        public void elementChanged(Object element) {
            this.elementChangeCount++;
            super.elementChanged(element);
        }
    }
}