package ca.odell.glazedlists.impl.beans;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import junit.framework.TestCase;

import javax.swing.*;

public class BeanConnectorTest extends TestCase {

    public void testBeanClassConstructor() {
        try {
            new BeanConnector(null);
            fail("Failed to receive NullPointerException with null beanClass");
        } catch (NullPointerException npe) { }

        try {
            new BeanConnector(Object.class);
            fail("Failed to receive RuntimeException for beanClass without add/remove PropertyChangeListener methods");
        } catch (IllegalArgumentException iae) { }
        try {
            new BeanConnector(JLabel.class, null);
            fail("Failed to receive IllegalArgumentException for null event matcher");
        } catch (IllegalArgumentException iae) { }

        new BeanConnector(JComponent.class);
    }

    public void testBeanClassAndMethodNamesConstructor() {
        try {
            new BeanConnector(null, "addPropertyChangeListener", "removePropertyChangeListener");
            fail("Failed to receive NullPointerException with null beanClass");
        } catch (NullPointerException npe) { }

        try {
            new BeanConnector(JComponent.class, null, "removePropertyChangeListener");
            fail("Failed to receive NullPointerException with null add method name");
        } catch (NullPointerException npe) { }

        try {
            new BeanConnector(JComponent.class, "addPropertyChangeListener", null);
            fail("Failed to receive NullPointerException with null remove method name");
        } catch (NullPointerException npe) { }

        try {
            new BeanConnector(Object.class, "addPropertyChangeListener", "removePropertyChangeListener");
            fail("Failed to receive IllegalArgumentException with invalid beanClass");
        } catch (IllegalArgumentException iae) { }

        try {
            new BeanConnector(JComponent.class, "addBOOBLAH", "removePropertyChangeListener");
            fail("Failed to receive IllegalArgumentException with invalid add method name");
        } catch (IllegalArgumentException iae) { }

        try {
            new BeanConnector(JComponent.class, "addPropertyChangeListener", "removeBOOLBLAH");
            fail("Failed to receive IllegalArgumentException with invalid remove method name");
        } catch (IllegalArgumentException iae) { }

        try {
            new BeanConnector(JComponent.class, "addPropertyChangeListener", "removePropertyChangeListener", null);
            fail("Failed to receive IllegalArgumentException for null event matcher");
        } catch (IllegalArgumentException iae) { }

        new BeanConnector(JComponent.class, "addPropertyChangeListener", "removePropertyChangeListener");
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

        @Override
        public void elementChanged(Object element) {
            this.elementChangeCount++;
            super.elementChanged(element);
        }
    }
}