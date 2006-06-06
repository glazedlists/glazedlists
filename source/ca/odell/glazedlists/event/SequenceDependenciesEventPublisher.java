/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.EventList;

import java.util.*;

/**
 * Manage listeners, firing events, and making sure that events arrive in order.
 *
 * <p>All SubjectAndListener objects where A is a listener must be notified
 * before any SubjectAndListener pair where A is a subject. This allows us to
 * guarantee that A has been notified completely before its listeners are
 * notified.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
final class SequenceDependenciesEventPublisher extends ListEventPublisher {

    /** keep track of how many times the fireEvent() method is on the stack */
    private int reentrantFireEventCount = 0;
    /** subject to cleanup when this event is completely distributed */
    private Map<Object,EventFormat> subjectsToCleanUp = new IdentityHashMap<Object,EventFormat>();

    /** a mix of different subjects and listeners pairs in a deliberate order */
    private List<SubjectAndListener> subjectAndListeners = new ArrayList<SubjectAndListener>(5);

    /**
     * Register the specified listener to receive events from the specified
     * subject whenever they are fired.
     */
    public <Subject,Listener,Event> void addListener(Subject subject, Listener listener, EventFormat<Subject,Listener,Event> format) {
        // find the latest occurrence where our intended subject acts as a listener
        int latestIndexOfSubjectAsListener = -1;
        for(int i = subjectAndListeners.size() - 1; i >= 0; i--) {
            SubjectAndListener anotherSubjectAndListener = subjectAndListeners.get(i);
            if(anotherSubjectAndListener.listener == subject) {
                latestIndexOfSubjectAsListener = i;
                break;
            }
        }

        // find the earliest occurence where our intended listener acts as a subject
        int earliestIndexOfListenerAsSubject = subjectAndListeners.size();
        for(int i = 0; i < subjectAndListeners.size(); i++) {
            SubjectAndListener anotherSubjectAndListener = subjectAndListeners.get(i);
            if(anotherSubjectAndListener.subject == listener) {
                earliestIndexOfListenerAsSubject = i;
                break;
            }
        }

        // fail if this is a cycle that we should be fixing, in a future rev
        // this is where we should be rearranging the graph dramatically
        if(earliestIndexOfListenerAsSubject < latestIndexOfSubjectAsListener) {
            throw new IllegalStateException("Cannot register " + listener + " as a listener of " +
                subject + ", due to unsupported case in listener dependency graph");
        }

        // otherwise we can just insert immediately before the 'earliest' point,
        // where we know both the listener and subject were notified without
        // destroying our invariants
        subjectAndListeners.add(earliestIndexOfListenerAsSubject, new SubjectAndListener<Subject,Listener,Event>(subject, listener, format));
    }

    /**
     * Deregister the specified listener from recieving events from the specified
     * subject.
     */
    public void removeListener(Object subject, Object listener) {
        // remove by identity (==), not equals()
        for(Iterator<SubjectAndListener> i = subjectAndListeners.iterator(); i.hasNext(); ) {
            SubjectAndListener subjectAndListener = i.next();
            if(subjectAndListener.subject != subject) continue;
            if(subjectAndListener.listener != listener) continue;
            i.remove();
            break;
        }
    }

    /** {@inheritDoc} */
    public void addDependency(EventList dependency, ListEventListener listener) {
        // unsupported, do nothing
        // todo: resolve
    }

    /** {@inheritDoc} */
    public void removeDependency(EventList dependency, ListEventListener listener) {
        // unsupported, do nothing
        // todo: resolve
    }

    /**
     * Get all listeners of the specified object.
     */
    public <Listener> List<Listener> getListeners(Object subject) {
        List<Listener> result = new ArrayList<Listener>();
        for (Iterator<SubjectAndListener> i = subjectAndListeners.iterator(); i.hasNext();) {
            SubjectAndListener<?,Listener,?> subjectAndListener = i.next();
            if(subjectAndListener.subject != subject) continue;
            result.add(subjectAndListener.listener);
        }
        return result;
    }

    /**
     * Notify all listeners of the specified subject of the specified event.
     *
     * @param subject the event's source
     * @param event the event to send to all listeners
     * @param eventFormat the mechanism to notify listeners of the event, also
     *     used for a callback when this event is complete
     */
    public <Subject,Listener,Event> void fireEvent(Subject subject, Event event, EventFormat<Subject,Listener,Event> eventFormat) {
        reentrantFireEventCount++;
        try {
            // this is where fancy reentrancy has to happen
            // IF NOT REENTRANT:
            //     1. Mark the listeners
            //     2. Notify the listeners in order
            // IF REENTRANT
            //     1. Mark the listeners
            //     2. Pop

            // record this subject as firing an event, so we can clean up later
            EventFormat previous = subjectsToCleanUp.put(subject, eventFormat);
            if(previous != null) throw new IllegalStateException("Reentrant fireEvent() by \"" + subject + "\"");

            // Mark the listeners who need this event
            //for(SubjectAndListener subjectAndListener : subjectAndListeners) {
            int subjectAndListenersSize = subjectAndListeners.size();
            for(int i = 0; i < subjectAndListenersSize; i++) {
                SubjectAndListener subjectAndListener = subjectAndListeners.get(i);
                if(subjectAndListener.subject != subject) continue;
                subjectAndListener.addPendingEvent(event);
            }

            // If this method is reentrant, let someone higher up the stack handle this
            if(reentrantFireEventCount != 1) return;

            // remember any runtime exceptions thrown to rethrow later
            RuntimeException toRethrow = null;

            // fire events to listeners in order
            while(true) {
                SubjectAndListener nextToFire = null;

                // find the next listener still pending
//                for(SubjectAndListener subjectAndListener : subjectAndListeners) {
                for(int i = 0; i < subjectAndListenersSize; i++) {
                    SubjectAndListener subjectAndListener = subjectAndListeners.get(i);
                    if(subjectAndListener.hasPendingEvent()) {
                        nextToFire = subjectAndListener;
                        break;
                    }
                }

                // there's nobody to notify, we're done firing events
                if(nextToFire == null) break;

                // notify this listener
                try {
                    nextToFire.firePendingEvent();
                } catch(RuntimeException e) {
                    if(toRethrow == null) toRethrow = e;
                }
            }

            // clean up all the subjects now that we're done firing events
            for (Iterator<Map.Entry<Object, EventFormat>> i = subjectsToCleanUp.entrySet().iterator(); i.hasNext();) {
                Map.Entry<Object, EventFormat> subjectAndEventFormat = i.next();
                try {
                    subjectAndEventFormat.getValue().postEvent(subjectAndEventFormat.getKey());
                } catch(RuntimeException e) {
                    if(toRethrow == null) toRethrow = e;
                }
            }
            subjectsToCleanUp.clear();

            // rethrow any exceptions
            if(toRethrow != null) throw toRethrow;

        } finally {
            reentrantFireEventCount--;
        }
    }

    /**
     * Adapt any observer-style interface to a common format.
     */
    public interface EventFormat<Subject,Listener,Event> {

        /**
         * Fire the specified event to the specified listener.
         */
        void fire(Subject subject, Event event, Listener listener);

        /**
         * A callback made only after all listeners of the specified subject
         * have been notified of the specified event. This can be used as
         * a hook to clean up temporary datastructures for that event.
         */
        void postEvent(Subject subject);
    }

    /**
     * Manage a subject/listener pair, plus a possible event that is queued to
     * be fired to the listener from the subject.
     */
    private static class SubjectAndListener<Subject,Listener,Event> {
        private Subject subject;
        private Listener listener;
        private Event pendingEvent;
        private EventFormat<Subject,Listener,Event> eventFormat;

        public SubjectAndListener(Subject subject, Listener listener, EventFormat<Subject,Listener,Event> eventFormat) {
            this.subject = subject;
            this.listener = listener;
            this.eventFormat = eventFormat;
        }

        public boolean hasPendingEvent() {
            return pendingEvent != null;
        }

        public void addPendingEvent(Event pendingEvent) {
            if(this.pendingEvent != null) throw new IllegalStateException();
            this.pendingEvent = pendingEvent;
        }

        public void firePendingEvent() {
            assert(pendingEvent != null);
            try {
                eventFormat.fire(subject, pendingEvent, listener);
            } finally {
                pendingEvent = null;
            }
        }

        public String toString() {
            String separator = hasPendingEvent() ? ">>>" : "-->";
            return subject + separator + listener;
        }
    }
}