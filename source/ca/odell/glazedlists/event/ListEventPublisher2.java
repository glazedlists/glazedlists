/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

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
final class ListEventPublisher2 {

    /** keep track of how many times the fireEvent() method is on the stack */
    private int reentrantFireEventCount = 0;

    /** a mix of different subjects and listeners pairs in a deliberate order */
    private List<SubjectAndListener> subjectAndListeners = new ArrayList<SubjectAndListener>(5);

    /**
     * Register the specified listener to receive events from the specified
     * subject whenever they are fired.
     */
    public <Subject,Listener,Event> void addListener(Subject subject, Listener listener, EventFormat<Listener,Event> format) {
        // find the latest occurrence where our intended subject acts as a listener
        int latestIndexOfSubjectAsListener = -1;
        for(int i = subjectAndListeners.size() - 1; i >= 0; i--) {
            SubjectAndListener anotherSubjectAndListener = subjectAndListeners.get(i);
            if(anotherSubjectAndListener.getListener() == subject) {
                latestIndexOfSubjectAsListener = i;
                break;
            }
        }

        // find the earliest occurence where our intended listener acts as a subject
        int earliestIndexOfListenerAsSubject = subjectAndListeners.size();
        for(int i = 0; i < subjectAndListeners.size(); i++) {
            SubjectAndListener anotherSubjectAndListener = subjectAndListeners.get(i);
            if(anotherSubjectAndListener.getSubject() == listener) {
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
            if(subjectAndListener.getSubject() != subject) continue;
            if(subjectAndListener.getListener() != listener) continue;
            i.remove();
            break;
        }
    }

    /**
     * Notify all listeners of the specified subject of the specified event.
     */
    public <Subject,Event> void fireEvent(Subject subject, Event event) {
        reentrantFireEventCount++;
        try {
            // this is where fancy reentrancy has to happen
            // IF NOT REENTRANT:
            //     1. Mark the listeners
            //     2. Notify the listeners in order
            // IF REENTRANT
            //     1. Mark the listeners
            //     2. Pop

            // Mark the listeners who need this event
            for(SubjectAndListener subjectAndListener : subjectAndListeners) {
                if(subjectAndListener.getSubject() != subject) continue;
                subjectAndListener.addPendingEvent(event);
            }

            // If this method is reentrant, let someone higher up the stack handle this
            if(reentrantFireEventCount != 1) return;

            // We're the top call, fire events to listeners in order
            while(true) {
                SubjectAndListener nextToFire = null;

                // find the next listener still pending
                for(SubjectAndListener subjectAndListener : subjectAndListeners) {
                    if(subjectAndListener.hasPendingEvent()) {
                        nextToFire = subjectAndListener;
                        break;
                    }
                }

                // there's nobody to notify, we're done
                if(nextToFire == null) return;

                // notify this listener
                nextToFire.firePendingEvent();
            }
        } finally {
            reentrantFireEventCount--;
        }
    }

    /**
     * Adapt any observer-style interface to a common format.
     */
    public interface EventFormat<Listener,Event> {
        void fire(Event event, Listener listener);
    }

    /**
     * Manage a subject/listener pair, plus a possible event that is queued to
     * be fired to the listener from the subject.
     */
    private class SubjectAndListener<Subject,Listener,Event> {
        private Subject subject;
        private Listener listener;
        private Event pendingEvent;
        private EventFormat<Listener,Event> eventFormat;

        public SubjectAndListener(Subject subject, Listener listener, EventFormat<Listener,Event> eventFormat) {
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
                eventFormat.fire(pendingEvent, listener);
            } finally {
                pendingEvent = null;
            }
        }

        public Subject getSubject() {
            return subject;
        }

        public Listener getListener() {
            return listener;
        }

        public String toString() {
            String separator = hasPendingEvent() ? ">>>" : "-->";
            return subject + separator + listener;
        }
    }
}