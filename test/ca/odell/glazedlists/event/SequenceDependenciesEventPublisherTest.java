/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

import ca.odell.glazedlists.*;

/**
 * Make sure that the {@link SequenceDependenciesEventPublisher} class fires events properly.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
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

        /** a log of events received */
        private List<String> receivedEvents = new ArrayList<String>();

        /** an arbitrary runnable to run when the next event is received */
        public Runnable runOnceRunnable;

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
            // remember the event
            receivedEvents.add(value);

            // execute any requested code
            try {
                if(runOnceRunnable != null) {
                    runOnceRunnable.run();
                }
            } finally {
                runOnceRunnable = null;
            }

            // apply this change
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
        public List<String> getReceivedEvents() {
            return receivedEvents;
        }
        public void setRunOnceRunnable(Runnable runnable) {
            this.runOnceRunnable = runnable;
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
        public boolean isStale(SimpleSubjectListener subject, SimpleSubjectListener listener) {
            return false;
        }
    }

    /**
     * Prepare the original diamond-dependency problem, where B depends on A
     * and C depends on both A and B. We need to guarantee that C can read
     * all of its dependencies only when they're in a 'consistent' state,
     * which is when they've received all events from their dependencies,
     * and their dependencies are in a consistent state.
     *
     *        A --> B --.    This diagram shows A --> B, B --> C and A --> C.
     *        |         |    The only safe notification order of events is
     *        |         V    for B to receive events from A before C receives
     *        '-------> C    those same events.
     */
    public void testDiamondDependency() {

        SequenceDependenciesEventPublisher publisher = new SequenceDependenciesEventPublisher();
        DependentSubjectListener a = new DependentSubjectListener("A", publisher);
        DependentSubjectListener b = new DependentSubjectListener("B", publisher);
        DependentSubjectListener c = new DependentSubjectListener("C", publisher);

        a.addListener(c);
        a.increment(10);
        assertEquals(10, a.latestRevision);
        assertEquals( 0, b.latestRevision);
        assertEquals(10, c.latestRevision);

        c.increment(5);
        assertEquals(10, a.latestRevision);
        assertEquals( 0, b.latestRevision);
        assertEquals(15, c.latestRevision);

        b.increment(20);
        assertEquals(10, a.latestRevision);
        assertEquals(20, b.latestRevision);
        assertEquals(15, c.latestRevision);

        b.addListener(c);
        assertEquals(10, a.latestRevision);
        assertEquals(20, b.latestRevision);
        assertEquals(20, c.latestRevision);

        b.increment(2);
        assertEquals(10, a.latestRevision);
        assertEquals(22, b.latestRevision);
        assertEquals(22, c.latestRevision);

        a.increment(15);
        assertEquals(25, a.latestRevision);
        assertEquals(22, b.latestRevision);
        assertEquals(25, c.latestRevision);

        a.addListener(b);
        assertEquals(25, a.latestRevision);
        assertEquals(25, b.latestRevision);
        assertEquals(25, c.latestRevision);

        a.increment(4);
        assertEquals(29, a.latestRevision);
        assertEquals(29, b.latestRevision);
        assertEquals(29, c.latestRevision);
    }



    /**
     * The publisher should throw an IllegalStateException when a cycle in the
     * listener graph is created.
     */
    public void testCycleThrows() {
        SequenceDependenciesEventPublisher publisher = new SequenceDependenciesEventPublisher();
        DependentSubjectListener a = new DependentSubjectListener("A", publisher);
        DependentSubjectListener b = new DependentSubjectListener("B", publisher);
        DependentSubjectListener c = new DependentSubjectListener("C", publisher);

        // simple cycle of three
        a.addListener(b);
        b.addListener(c);
        try {
            c.addListener(a);
            fail("Cycle not detected");
        } catch(IllegalStateException e) {
            // expected
        }

        // cycle of 10
        publisher = new SequenceDependenciesEventPublisher();
        DependentSubjectListener[] subjects = new DependentSubjectListener[10];
        for(int i = 0; i < 10; i++) {
            subjects[i] = new DependentSubjectListener("" + i, publisher);
            if(i > 0) subjects[i-1].addListener(subjects[i]);
        }
        try {
            subjects[9].addListener(subjects[0]);
            fail("Cycle not detected");
        } catch(IllegalStateException e) {
            // expected
        }

        // cycle of 1
        publisher = new SequenceDependenciesEventPublisher();
        a = new DependentSubjectListener("A", publisher);
        try {
            a.addListener(a);
            fail("Cycle not detected");
        } catch(IllegalStateException e) {
            // expected
        }
    }


    /**
     * An interesting subject that uses a single integer to maintain state. The
     * integer can increase at any subject, and  all downstream listeners must
     * be notified of this change. If ever a listener's integer is less than that
     * of its dependency, then it did not receive notification from that
     * dependency and we have a terrible problem!
     */
    public static class DependentSubjectListener {
        private SequenceDependenciesEventPublisher publisher;
        /** a monotonically increasing revision number */
        int latestRevision = 0;
        /** useful for debugging */
        private final String name;
        /** objects I depend on, must all be consistent for this to be consistent */
        List<DependentSubjectListener> upstreamSubjects = new ArrayList<DependentSubjectListener>();
        /** objects that depend on me */
        List<DependentSubjectListener> downstreamListeners = new ArrayList<DependentSubjectListener>();
        public DependentSubjectListener(String name, SequenceDependenciesEventPublisher publisher) {
            this.publisher = publisher;
            this.name = name;
        }
        public String toString() {
            return name + ":" + latestRevision;
        }
        public void increment(int amount) {
            this.latestRevision += amount;
            this.publisher.fireEvent(this, new Integer(this.latestRevision), DependentSubjectListenerEventFormat.INSTANCE);
        }
        /**
         * Register the listener as dependent on the subject.
         */
        public void addListener(DependentSubjectListener listener) {
            downstreamListeners.add(listener);
            listener.upstreamSubjects.add(this);
            listener.latestRevision = Math.max(listener.latestRevision, latestRevision);
            this.publisher.addListener(this, listener, DependentSubjectListenerEventFormat.INSTANCE);
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
            boolean incremented = listener.latestRevision < event.intValue();
            listener.latestRevision = event.intValue();
            // make sure the listener's dependencies were notified first
            listener.assertDependenciesSatisfiedRecursively(listener);
            // send the event forward
            if(incremented) {
                listener.increment(0);
            }
        }
        public void postEvent(DependentSubjectListener subject) {
            // do nothing
        }
        public boolean isStale(DependentSubjectListener subject, DependentSubjectListener listener) {
            return false;
        }
    }

    /**
     * Make sure that when the listener graph changes during an event, it gets
     * processed only at the conclusion of that event.
     */
    public void testRemovesAndAddsDuringEvent() {
        SequenceDependenciesEventPublisher publisher = new SequenceDependenciesEventPublisher();
        SimpleSubjectListener a = new SimpleSubjectListener("A", publisher);
        SimpleSubjectListener b = new SimpleSubjectListener("B", publisher);
        SimpleSubjectListener c = new SimpleSubjectListener("C", publisher);
        SimpleSubjectListener d = new SimpleSubjectListener("D", publisher);
        a.addListener(b);

        // add c as a listener to b, it shouldn't receive the first event
        b.setRunOnceRunnable(new AddListenerRunnable(b, c));
        a.setValue("Saskatchwan");
        assertEquals(1, b.getReceivedEvents().size());
        assertEquals(0, c.getReceivedEvents().size());

        a.setValue("Tiger-Cats");
        assertEquals(2, b.getReceivedEvents().size());
        assertEquals(1, c.getReceivedEvents().size());

        // remove c as a listener to b, it should still receive the event
        b.setRunOnceRunnable(new RemoveListenerRunnable(b, c));
        a.setValue("Blue Bombers");
        assertEquals(3, b.getReceivedEvents().size());
        assertEquals(2, c.getReceivedEvents().size());
        a.setValue("Stampeders");
        assertEquals(4, b.getReceivedEvents().size());
        assertEquals(2, c.getReceivedEvents().size());

        // add a completely unrelated listener b to do events for c and d
        a.addListener(c);
        b.setRunOnceRunnable(new AddListenerRunnable(c, d));
        a.setValue("Argonauts");
        assertEquals(3, c.getReceivedEvents().size());
        assertEquals(0, d.getReceivedEvents().size());

        a.setValue("Lions");
        assertEquals(4, c.getReceivedEvents().size());
        assertEquals(1, d.getReceivedEvents().size());

        // remove the unrelated listener b to do events for c and d
        b.setRunOnceRunnable(new RemoveListenerRunnable(c, d));
        a.setValue("Eskimos");
        assertEquals(5, c.getReceivedEvents().size());
        assertEquals(2, d.getReceivedEvents().size());

        a.setValue("Alouettes");
        assertEquals(6, c.getReceivedEvents().size());
        assertEquals(2, d.getReceivedEvents().size());
    }

    /**
     * Make sure we throw an exception when attempting to remove something that's
     * not a listener.
     */
    public void testUnknownRemoveThrowsException() {
        SequenceDependenciesEventPublisher publisher = new SequenceDependenciesEventPublisher();
        SimpleSubjectListener a = new SimpleSubjectListener("A", publisher);
        SimpleSubjectListener b = new SimpleSubjectListener("B", publisher);
        SimpleSubjectListener c = new SimpleSubjectListener("C", publisher);

        // add a listener
        a.addListener(b);
        try {
            a.removeListener(c);
            fail("No exception thrown when removing a non-existent listener");
        } catch(IllegalArgumentException e) {
            // expected
        }

        // remove the other listener, this shouldn't throw
        a.removeListener(b);
    }

    /**
     * Add a listener when executed.
     */
    private class AddListenerRunnable implements Runnable {
        private SimpleSubjectListener subject;
        private SimpleSubjectListener listener;
        public AddListenerRunnable(SimpleSubjectListener subject, SimpleSubjectListener listener) {
            this.subject = subject;
            this.listener = listener;
        }
        public void run() {
            subject.addListener(listener);
        }
    }

    /**
     * Remove a listener when executed.
     */
    private class RemoveListenerRunnable implements Runnable {
        private SimpleSubjectListener subject;
        private SimpleSubjectListener listener;
        public RemoveListenerRunnable(SimpleSubjectListener subject, SimpleSubjectListener listener) {
            this.subject = subject;
            this.listener = listener;
        }
        public void run() {
            subject.removeListener(listener);
        }
    }

    /**
     * This test currently fails in the graph publisher implementation because
     * we don't support merging events in GraphDependenciesListEventPublisher.
     */
    public void testMergingListEvents() {
        CompositeList<String> compositeList = new CompositeList<String>();
        ListConsistencyListener.install(compositeList);
        EventList<String> source = compositeList.createMemberList();
        source.add("C");
        source.add("A");
        source.add("B");

        SortedList<String> forwardSource = new SortedList<String>(source);
        SortedList<String> reverseSource = new SortedList<String>(source, GlazedLists.reverseComparator());
        compositeList.addMemberList(forwardSource);
        compositeList.addMemberList(source);
        compositeList.addMemberList(reverseSource);

        assertEquals(compositeList, GlazedListsTests.stringToList("ABCCABCBA"));

        source.add(1, "D");
        assertEquals(compositeList, GlazedListsTests.stringToList("ABCDCDABDCBA"));

        source.removeAll(GlazedListsTests.stringToList("AC"));
        assertEquals(compositeList, GlazedListsTests.stringToList("BDDBDB"));

        source.clear();
        assertEquals(compositeList, GlazedListsTests.stringToList(""));

        source.addAll(GlazedListsTests.stringToList("CADB"));
        assertEquals(compositeList, GlazedListsTests.stringToList("ABCDCADBDCBA"));
    }

    /**
     * Test that listeners and subjects do not have to use the same identity
     * so long as {@link ListEventPublisher#setRelatedSubject} is used.
     */
    public void testRelatedSubjects() {
        SequenceDependenciesEventPublisher publisher = new SequenceDependenciesEventPublisher();
        DetachedSubject a = new DetachedSubject("A", publisher);
        DetachedSubject b = new DetachedSubject("B", publisher);
        DetachedSubject c = new DetachedSubject("C", publisher);
        DetachedSubject d = new DetachedSubject("D", publisher);
        DetachedSubject e = new DetachedSubject("E", publisher);

        b.addListener(e);
        a.addListener(b);

        c.addListener(e);
        a.addListener(c);

        d.addListener(e);
        a.addListener(d);

        // changing a should impact e, but only after b, c, and d
        a.increment(10);
        assertEquals(10, a.latestRevision);
        assertEquals(10, b.latestRevision);
        assertEquals(10, c.latestRevision);
        assertEquals(10, d.latestRevision);
        assertEquals(10, e.latestRevision);
    }

    /**
     * A subject that listens to another subject via an inner listener class.
     * This is used to test that listener identity is not required.
     */
    private static class DetachedSubject {
        private SequenceDependenciesEventPublisher publisher;
        private String name;
        private int latestRevision = 0;
        private List<DetachedSubject> upstreamSubjects = new ArrayList<DetachedSubject>();

        public DetachedSubject(String name, SequenceDependenciesEventPublisher publisher) {
            this.name = name;
            this.publisher = publisher;
        }
        public String toString() {
            return name;
        }
        public void addListener(DetachedSubject listener) {
            listener.upstreamSubjects.add(this);
            Listener innerListener = new Listener(listener);
            publisher.setRelatedSubject(innerListener, innerListener.subject);
            publisher.addListener(this, innerListener, DetachedSubjectAndListenerEventFormat.INSTANCE);
        }
        public void increment(int amount) {
            this.latestRevision += amount;
            publisher.fireEvent(this, new Integer(this.latestRevision), DetachedSubjectAndListenerEventFormat.INSTANCE);
        }
        /**
         * Dependencies are satisfied if the latestRevision is at least the
         * latestRevision of all upstream {@link DependentSubjectListener}s.
         */
        public void assertDependenciesSatisfiedRecursively(DetachedSubject notified) {
            for(DetachedSubject upstream : upstreamSubjects) {
                upstream.assertDependenciesSatisfiedRecursively(notified);
                if(latestRevision < upstream.latestRevision) {
                    throw new IllegalStateException("Dependencies not satisfied for " + notified + ", dependency " + this + " not updated by " + upstream + "!");
                }
            }
        }
        private static class Listener {
            private DetachedSubject subject;
            public Listener(DetachedSubject subject) {
                this.subject = subject;
            }
            public String toString() {
                return "L(" + subject.name + ")";
            }
        }
    }
    /**
     * Adapt {@link DetachedSubject} and {@link DetachedSubject.Listener} for firing events.
     */
    private static class DetachedSubjectAndListenerEventFormat implements SequenceDependenciesEventPublisher.EventFormat<DetachedSubject,DetachedSubject.Listener,Integer> {
        public static final DetachedSubjectAndListenerEventFormat INSTANCE = new DetachedSubjectAndListenerEventFormat();
        public void fire(DetachedSubject subject, Integer event, DetachedSubject.Listener listener) {
            boolean changed = listener.subject.latestRevision < event.intValue();
            // update the listener
            listener.subject.latestRevision = event.intValue();
            // make sure the listener's dependencies were notified first
            listener.subject.assertDependenciesSatisfiedRecursively(listener.subject);
            // send the event forward
            if(changed) listener.subject.increment(0);
        }
        public void postEvent(DetachedSubject subject) {
            // do nothing
        }
        public boolean isStale(DetachedSubject subject, DetachedSubject.Listener listener) {
            return false;
        }
    }

    /**
     * Test that the ListEvent iterator isn't adjusted by calling
     * {@link ListEventAssembler#forwardEvent}.
     */
    public void testEventStateAfterForwardEvent() {
        EventList<String> source = new BasicEventList<String>();
        CountingList<String> counting = new CountingList<String>(source);
        source.add("Hello");
        assertEquals(1, counting.changeCount);
    }
    public class CountingList<T> extends TransformedList<T,T> {
        public int changeCount = 0;
        protected CountingList(EventList<T> source) {
            super(source);
            source.addListEventListener(this);
        }
        public void listChanged(ListEvent<T> listChanges) {
            updates.forwardEvent(listChanges);
            while(listChanges.next()) {
                changeCount++;
            }
        }
    }
}