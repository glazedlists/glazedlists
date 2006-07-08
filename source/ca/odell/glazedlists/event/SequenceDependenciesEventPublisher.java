/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.impl.adt.IdentityMultimap;
import ca.odell.glazedlists.EventList;

import java.util.*;

/**
 * Manage listeners, firing events, and making sure that events arrive in order.
 *
 * <p>This manages listeners across multiple objects in a pipeline of observables
 * and their listeners. It implements Martin Fowler's
 * <a href="http://www.martinfowler.com/eaaDev/EventAggregator.html">EventAggregator</a>
 * design.
 *
 * <p>To guarantee a safe notification order, this class makes sure that all an
 * object's dependencies have been notified of a particular event before that
 * object is itself notified. This is tricky because it requires us to interrupt
 * the event flow and control its flow. In this class, event flow is controlled
 * by queueing events and not necessarily firing them during the {@link #fireEvent}
 * method.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
final class SequenceDependenciesEventPublisher extends ListEventPublisher {

    /** keep track of how many times the fireEvent() method is on the stack */
    private int reentrantFireEventCount = 0;
    /** subject to cleanup when this event is completely distributed */
    private Map<Object,EventFormat> subjectsToCleanUp = new IdentityHashMap<Object,EventFormat>();

    /** for proper dependency management, when a listener and subject aren't the same identity */
    private Map<Object,Object> listenersToRelatedSubjects = new IdentityHashMap<Object,Object>();


    /**
     * A mix of different subjects and listeners pairs in a deliberate order.
     * We should be careful not to make changes to this list directly and instead
     * create a copy as necessary
     */
    private List<SubjectAndListener> subjectAndListeners = Collections.emptyList();

    /**
     * We use copy-on-write on the listeners list. This is a copy of the
     * listeners list as it looked immediately before the current change
     * started. If there is no change going on (reentrantFireEventCount == 0),
     * then this should be null.
     */
    private List<SubjectAndListener> subjectsAndListenersForCurrentEvent = null;

    /**
     * Rebuild the subject and listeners list so that all required invariants
     * are met with respect to notification order. That is, for any listener
     * T, all of the subjects S that T listens to have been updated before T
     * receives a change event from any S.
     *
     * <p>This implementation still has some problems and work left to do:
     *  <li>it's big! Can we optimize it? Perhaps shortcutting all the graph
     *     work for simple cases (the 99% case)
     *  <li>it's complex! Can we simplify it?
     *  <li>could we keep the datastructures around? it may be wasteful to
     *     reconstruct them every single time a listener is changed
     */
    private List<SubjectAndListener> orderSubjectsAndListeners(List<SubjectAndListener> subjectsAndListeners) {

        // since we're regenerating the subjectAndListeners list, clear it
        // and re-add the elements
        List<SubjectAndListener> result = new ArrayList<SubjectAndListener>();

        // HashMaps of unprocessed elements, keyed by both source and target
        IdentityMultimap<Object,SubjectAndListener> sourceToPairs = new IdentityMultimap<Object,SubjectAndListener>();
        IdentityMultimap<Object,SubjectAndListener> targetToPairs = new IdentityMultimap<Object,SubjectAndListener>();

        // everything that has all of its listeners already notified in subjectAndListeners
        Map<Object,Boolean> satisfied = new IdentityHashMap<Object,Boolean>();
        // everything that has a listener already notified
        List<Object> satisfiedToDo = new ArrayList<Object>();

        // prepare the initial collections: maps that show how each element is
        // used as source and target in directed edges, plus a list of nodes
        // that have no incoming edges
        for(int i = 0, size = subjectsAndListeners.size(); i < size; i++) {
            SubjectAndListener subjectAndListener = subjectsAndListeners.get(i);
            Object source = subjectAndListener.subject;
            Object target = getRelatedSubject(subjectAndListener.listener);
            sourceToPairs.addValue(source, subjectAndListener);
            targetToPairs.addValue(target, subjectAndListener);

            satisfied.remove(target);
            if(targetToPairs.count(source) == 0) {
                satisfied.put(source, Boolean.TRUE);
            }
        }

        // start with the initial set of sources that don't have dependencies
        satisfiedToDo.addAll(satisfied.keySet());

        // We have a subject which has all of its dependencies satisfied.
        // ie. all edges where this subject is a target are already in
        // subjectAndListeners. Now we want to find further edges from this
        // subject to further objects. We find all of its listeners, and look
        // for one of them where all of its dependencies are in the
        // satisfied list. If we find such a listener, add all its edges
        // to subjectAndListeners and enque it to find it's listeners
        // iteratively
        while(!satisfiedToDo.isEmpty()) {

            // for everything that's not a target,
            Object subject = satisfiedToDo.remove(0);

            // get all listeners to this subject, we try this set because
            // we know at least one of their edges is satisfied, and
            // we hope that all of their edges is satisfied.
            List<SubjectAndListener> sourceTargets = (List<SubjectAndListener>) sourceToPairs.get(subject);

            // can we satisfy this target?
            tryEachTarget:
            for(int t = 0, targetsSize = sourceTargets.size(); t < targetsSize; t++) {
                Object sourceTarget = getRelatedSubject(sourceTargets.get(t).listener);

                // make sure we can satisfy this if all its sources are in satisfiedSources
                List<SubjectAndListener> allSourcesForSourceTarget = (List<SubjectAndListener>) targetToPairs.get(sourceTarget);
                // we've since processed this entire target, we shouldn't process it twice
                if(allSourcesForSourceTarget.size() == 0) continue;
                for(int s = 0, sourcesSize = allSourcesForSourceTarget.size(); s < sourcesSize; s++) {
                    SubjectAndListener sourceAndTarget = allSourcesForSourceTarget.get(s);
                    if(!satisfied.containsKey(sourceAndTarget.subject)) {
                        continue tryEachTarget;
                    }
                }

                // we know we can satisfy this target, add all its edges
                result.addAll(allSourcesForSourceTarget);
                targetToPairs.remove(sourceTarget);

                // this target is no longer considered a target, since all
                // its dependencies are satisfied
                satisfiedToDo.add(sourceTarget);
                satisfied.put(sourceTarget, Boolean.TRUE);
            }
        }

        // if there's remaining targets, we never covered everything
        if(!targetToPairs.isEmpty()) {
            throw new IllegalStateException("Listener cycle detected, " + targetToPairs.values());
        }

        // success!
        return result;
    }
    private Object getRelatedSubject(Object listener) {
        Object subject = listenersToRelatedSubjects.get(listener);
        if(subject == null) return listener;
        return subject;
    }

    /**
     * Register the specified listener to receive events from the specified
     * subject whenever they are fired.
     */
    public synchronized <Subject,Listener,Event> void addListener(Subject subject, Listener listener, EventFormat<Subject,Listener,Event> eventFormat) {
        List<SubjectAndListener> unordered = updateListEventListeners(subject, listener, null, eventFormat);
        subjectAndListeners = orderSubjectsAndListeners(unordered);
    }

    /**
     * Deregister the specified listener from recieving events from the specified
     * subject.
     */
    public synchronized void removeListener(Object subject, Object listener) {
        subjectAndListeners = updateListEventListeners(subject, null, listener, null);
    }

    /**
     * Support method for adding and removing listeners, that also cleans up
     * stale listeners, such as those from weak references.
     *
     * @param listenerToAdd a listener to be added, or <code>null</code>
     * @param listenerToRemove a listener to be removed, or <code>null</code>
     */
    private <Subject,Listener,Event> List<SubjectAndListener> updateListEventListeners(Subject subject, Listener listenerToAdd, Listener listenerToRemove, EventFormat<Subject,Listener,Event> eventFormat) {
        // we'll want to output a copy of all the listeners
        int anticipatedSize = this.subjectAndListeners.size() + (listenerToAdd == null ? - 1 : 1);
        List<SubjectAndListener> result = new ArrayList<SubjectAndListener>(anticipatedSize);

        // walk through, adding all the old listeners to the new listeners list,
        // unless a particular listener is slated for removal for some reaosn
        for(int i = 0; i < subjectAndListeners.size(); i++) {
            final SubjectAndListener originalSubjectAndListener = subjectAndListeners.get(i);

            // if we're supposed to remove this listener, skip it
            if(originalSubjectAndListener.listener == listenerToRemove && originalSubjectAndListener.subject == subject) {
                listenerToRemove = null;
                continue;
            }

            // if this listener is stale, skip it
            if(originalSubjectAndListener.eventFormat.isStale(originalSubjectAndListener.subject, originalSubjectAndListener.listener)) {
                continue;
            }

            // this listener's still good, keep it!
            result.add(originalSubjectAndListener);
        }

        // sanity check to ensure we found the listener we were asked to remove, if any
        if(listenerToRemove != null) {
            throw new IllegalArgumentException("Cannot remove nonexistent listener " + listenerToRemove);
        }

        // add the listener we were asked to add, if any
        if(listenerToAdd != null) {
            result.add(new SubjectAndListener(subject, listenerToAdd, eventFormat));
        }

        return result;
    }


    /** {@inheritDoc} */
    public void addDependency(EventList dependency, ListEventListener listener) {
        // do nothing
    }

    /** {@inheritDoc} */
    public void removeDependency(EventList dependency, ListEventListener listener) {
        // do nothing
    }

    /** {@inheritDoc} */
    public void setRelatedSubject(Object listener, Object relatedSubject) {
        if(relatedSubject != null) {
            listenersToRelatedSubjects.put(listener, relatedSubject);
        } else {
            listenersToRelatedSubjects.remove(listener);
        }
    }

    /** {@inheritDoc} */
    public void removeRelatedSubject(Object listener) {
        listenersToRelatedSubjects.remove(listener);
    }

    /**
     * Get all listeners of the specified object.
     */
    public <Listener> List<Listener> getListeners(Object subject) {
        List<Listener> result = new ArrayList<Listener>();
        for(int i = 0, size = subjectAndListeners.size(); i < size; i++) {
            SubjectAndListener<?,Listener,?> subjectAndListener = subjectAndListeners.get(i);
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
        // keep the subjects and listeners as they are at the beginning of
        // the topmost event, the list won't change because we copy on write
        if(reentrantFireEventCount == 0) {
            subjectsAndListenersForCurrentEvent = subjectAndListeners;
        }

        // keep track of whether this method is being reentered because one
        // event caused another event. If so, we'll fire later
        reentrantFireEventCount++;
        try {

            // record this subject as firing an event, so we can clean up later
            EventFormat previous = subjectsToCleanUp.put(subject, eventFormat);
            if(previous != null) throw new IllegalStateException("Reentrant fireEvent() by \"" + subject + "\"");

            // Mark the listeners who need this event
            //for(SubjectAndListener subjectAndListener : subjectAndListeners) {
            int subjectAndListenersSize = subjectsAndListenersForCurrentEvent.size();
            for(int i = 0; i < subjectAndListenersSize; i++) {
                SubjectAndListener subjectAndListener = subjectsAndListenersForCurrentEvent.get(i);
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
                for(int i = 0; i < subjectAndListenersSize; i++) {
                    SubjectAndListener subjectAndListener = subjectsAndListenersForCurrentEvent.get(i);
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
            for(Iterator<Map.Entry<Object,EventFormat>> i = subjectsToCleanUp.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<Object,EventFormat> subjectAndEventFormat = i.next();
                try {
                    subjectAndEventFormat.getValue().postEvent(subjectAndEventFormat.getKey());
                } catch(RuntimeException e) {
                    if(toRethrow == null) toRethrow = e;
                }
            }
            subjectsToCleanUp.clear();

            // this event is completely finished
            subjectsAndListenersForCurrentEvent = null;

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

        /**
         * Whether the listener is still valid. Usually a listener becomes stale
         * when a weak reference goes out of scope.
         */
        boolean isStale(Subject subject, Listener listener);
    }

    /**
     * Manage a subject/listener pair, plus a possible event that is queued to
     * be fired to the listener from the subject.
     */
    private static class SubjectAndListener<Subject,Listener,Event> {
        private final Subject subject;
        private final Listener listener;
        private final EventFormat<Subject,Listener,Event> eventFormat;
        private Event pendingEvent;

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
            if(pendingEvent == null) throw new IllegalStateException();
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