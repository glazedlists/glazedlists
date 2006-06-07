/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

/**
 * Make sure that the {@link SequenceDependenciesEventPublisher} class fires events properly.
 *
 * @author jessewilson
 */
public class SequenceDependenciesEventPublisherTest extends TestCase {

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
            publisher.addListener(SimpleSubjectListener.this, listener, SimpleSubjectListenerEventFormat.INSTANCE);
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
            publisher.fireEvent(this, "[" + name + ":" + this.value + "]", SimpleSubjectListenerEventFormat.INSTANCE);
        }
        public String toString() {
            return name;
        }
    }

    /**
     * Adapt {@link SimpleSubjectListener} for firing events.
     */
    private static class SimpleSubjectListenerEventFormat implements SequenceDependenciesEventPublisher.EventFormat<SimpleSubjectListener,SimpleSubjectListener,String> {
        public static final SimpleSubjectListenerEventFormat INSTANCE = new SimpleSubjectListenerEventFormat();
        public void fire(SimpleSubjectListener subject, String event, SimpleSubjectListener listener) {
            listener.handleChange(event);
        }
        public void postEvent(SimpleSubjectListener subject) {
            // do nothing
        }
    }

    /**
     * Prepare the original diamond-dependency problem, where B depends on A
     * and C depends on both A and B. We need to guarantee that C can read
     * all of its dependencies only when they're in a 'consistent' state,
     * which is when they've received all events from their dependencies,
     * and their dependencies are in a consistent state.
     *
     *        A
     *        |\
     *        | B
     *        |/
     *        C
     */
    public void testDiamondDependency() {

        SequenceDependenciesEventPublisher publisher = new SequenceDependenciesEventPublisher();
        DependentSubjectListener a = new DependentSubjectListener("A");
        DependentSubjectListener b = new DependentSubjectListener("B");
        DependentSubjectListener c = new DependentSubjectListener("C");

        DependentSubjectListener.addDependency(publisher, a, c);
        a.increment(publisher, 10);
        assertEquals(10, a.latestRevision);
        assertEquals( 0, b.latestRevision);
        assertEquals(10, c.latestRevision);

        c.increment(publisher, 5);
        assertEquals(10, a.latestRevision);
        assertEquals( 0, b.latestRevision);
        assertEquals(15, c.latestRevision);

        b.increment(publisher, 20);
        assertEquals(10, a.latestRevision);
        assertEquals(20, b.latestRevision);
        assertEquals(15, c.latestRevision);

        DependentSubjectListener.addDependency(publisher, b, c);
        assertEquals(10, a.latestRevision);
        assertEquals(20, b.latestRevision);
        assertEquals(20, c.latestRevision);

        b.increment(publisher, 2);
        assertEquals(10, a.latestRevision);
        assertEquals(22, b.latestRevision);
        assertEquals(22, c.latestRevision);

        a.increment(publisher, 15);
        assertEquals(25, a.latestRevision);
        assertEquals(22, b.latestRevision);
        assertEquals(25, c.latestRevision);

        DependentSubjectListener.addDependency(publisher, a, b);
        assertEquals(25, a.latestRevision);
        assertEquals(25, b.latestRevision);
        assertEquals(25, c.latestRevision);

        a.increment(publisher, 4);
        assertEquals(29, a.latestRevision);
        assertEquals(29, b.latestRevision);
        assertEquals(29, c.latestRevision);
    }


    /**
     * An interesting subject that uses a single integer to maintain state. The
     * integer can increase at any subject, and  all downstream listeners must
     * be notified of this change. If ever a listener's integer is less than that
     * of its dependency, then it did not receive notification from that
     * dependency and we have a terrible problem!
     */
    public static class DependentSubjectListener {
        /** a monotonically increasing revision number */
        int latestRevision = 0;
        /** useful for debugging */
        private final String name;
        /** objects I depend on, must all be consistent for this to be consistent */
        List<DependentSubjectListener> upstreamSubjects = new ArrayList<DependentSubjectListener>();
        /** objects that depend on me */
        List<DependentSubjectListener> downstreamListeners = new ArrayList<DependentSubjectListener>();
        public DependentSubjectListener(String name) {
            this.name = name;
        }
        public String toString() {
            return name + ":" + latestRevision;
        }
        public void increment(SequenceDependenciesEventPublisher publisher, int amount) {
            this.latestRevision += amount;
            publisher.fireEvent(this, new Integer(this.latestRevision), DependentSubjectListenerEventFormat.INSTANCE);
        }
        /**
         * Register the listener as dependent on the subject.
         */
        public static void addDependency(SequenceDependenciesEventPublisher publisher, DependentSubjectListener subject, DependentSubjectListener listener) {
            subject.downstreamListeners.add(listener);
            listener.upstreamSubjects.add(subject);
            listener.latestRevision = Math.max(listener.latestRevision, subject.latestRevision);
            publisher.addListener(subject, listener, DependentSubjectListenerEventFormat.INSTANCE);
        }
        /**
         * Dependencies are satisfied if the latestRevision is at least the
         * latestRevision of all upstream {@link DependentSubjectListener}s.
         */
        public void assertDependenciesSatisfiedRecursively(DependentSubjectListener notified) {
            for(DependentSubjectListener upstream : upstreamSubjects) {
                upstream.assertDependenciesSatisfiedRecursively(notified);
                if(latestRevision < upstream.latestRevision)
                    throw new IllegalStateException("Dependencies not satisfied for " + notified + ", dependency " + this + " not updated by " + upstream + "!");
            }
        }
    }
    /**
     * Adapt {@link SimpleSubjectListener} for firing events.
     */
    private static class DependentSubjectListenerEventFormat implements SequenceDependenciesEventPublisher.EventFormat<DependentSubjectListener,DependentSubjectListener,Integer> {
        public static final DependentSubjectListenerEventFormat INSTANCE = new DependentSubjectListenerEventFormat();
        public void fire(DependentSubjectListener subject, Integer event, DependentSubjectListener listener) {
            // update the listener
            listener.latestRevision = event.intValue();
            // make sure the listener's dependencies were notified first
            listener.assertDependenciesSatisfiedRecursively(listener);
        }
        public void postEvent(DependentSubjectListener subject) {
            // do nothing
        }
    }

}
