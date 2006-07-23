/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import junit.framework.TestCase;

/**
 * Make sure that the {@link SequenceDependenciesEventPublisher} class fires
 * events properly.
 *
 * @author jessewilson
 */
public class ListEventPublisher2Test extends TestCase {

    /**
     * Make sure that all events get fired to all listeners.
     */
    public void testVeryBasicChain() {
        SequenceDependenciesEventPublisher publisher = new SequenceDependenciesEventPublisher();
        SimpleSubjectListener a = new SimpleSubjectListener("A", publisher);
        SimpleSubjectListener b = new SimpleSubjectListener("B", publisher);
        SimpleSubjectListener c = new SimpleSubjectListener("C", publisher);

        a.setValue("apple");
        assertEquals("apple", a.getValue());
        assertEquals(null, b.getValue());
        assertEquals(null, c.getValue());

        a.addListener(b);
        a.setValue("banana");
        assertEquals("banana", a.getValue());
        assertEquals("[A:banana]", b.getValue());
        assertEquals(null, c.getValue());

        a.addListener(c);
        a.setValue("chocolate");
        assertEquals("chocolate", a.getValue());
        assertEquals("[A:chocolate]", b.getValue());
        assertEquals("[A:chocolate]", c.getValue());

        a.removeListener(b);
        a.setValue("ducks");
        assertEquals("ducks", a.getValue());
        assertEquals("[A:chocolate]", b.getValue());
        assertEquals("[A:ducks]", c.getValue());

        c.addListener(b);
        a.setValue("elephants");
        assertEquals("elephants", a.getValue());
        assertEquals("[C:[A:elephants]]", b.getValue());
        assertEquals("[A:elephants]", c.getValue());

        c.setValue("foxes");
        assertEquals("elephants", a.getValue());
        assertEquals("[C:foxes]", b.getValue());
        assertEquals("foxes", c.getValue());

        a.removeListener(c);
        a.setValue("gorillas");
        assertEquals("gorillas", a.getValue());
        assertEquals("[C:foxes]", b.getValue());
        assertEquals("foxes", c.getValue());

        a.addListener(c);
        a.setValue("horses");
        assertEquals("horses", a.getValue());
        assertEquals("[C:[A:horses]]", b.getValue());
        assertEquals("[A:horses]", c.getValue());
    }

    /**
     * A simple class that is both the listener and the subject, useful only
     * for testing.
     */
    private static class SimpleSubjectListener {
        private final String name;
        private final SequenceDependenciesEventPublisher publisher;

        /** a string, automatically set when events are received */
        private String value = null;

        public SimpleSubjectListener(String name, SequenceDependenciesEventPublisher publisher) {
            this.name = name;
            this.publisher = publisher;
        }
        public void addListener(SimpleSubjectListener listener) {
            publisher.addListener(SimpleSubjectListener.this, listener, SimpleSubjectListenerEventformat.INSTANCE);
        }
        public void removeListener(SimpleSubjectListener listener) {
            publisher.removeListener(SimpleSubjectListener.this, listener);
        }
        public void handleChange(String value) {
            setValue(value);
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
            publisher.fireEvent(this, "[" + name + ":" + this.value + "]", SimpleSubjectListenerEventformat.INSTANCE);
        }
        public String toString() {
            return name;
        }
    }

    /**
     * Adapt {@link SimpleSubjectListener} for firing events.
     */
    private static class SimpleSubjectListenerEventformat implements SequenceDependenciesEventPublisher.EventFormat<SimpleSubjectListener,SimpleSubjectListener,String> {
        public static final SimpleSubjectListenerEventformat INSTANCE = new SimpleSubjectListenerEventformat();
        public void fire(SimpleSubjectListener subject, String event, SimpleSubjectListener listener) {
            listener.handleChange(event);
        }
        public void postEvent(SimpleSubjectListener subject) {
            // do nothing
        }
        public boolean isStale(SimpleSubjectListener subject, SimpleSubjectListener listener) {
            return false;
        }
    }
}
