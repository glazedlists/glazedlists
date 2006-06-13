/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.EventList;

import java.util.*;

/**
 * Manage listeners, firing events, and making sure that events arrive in order.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
final class SequenceDependenciesEventPublisher extends ListEventPublisher {

    /** keep track of how many times the fireEvent() method is on the stack */
    private int reentrantFireEventCount = 0;
    /** subject to cleanup when this event is completely distributed */
    private Map<Object,EventFormat> subjectsToCleanUp = new IdentityHashMap<Object,EventFormat>();

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
    private static List<SubjectAndListener> orderSubjectsAndListeners(List<SubjectAndListener> subjectsAndListeners) {

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
            sourceToPairs.addValue(subjectAndListener.subject, subjectAndListener);
            targetToPairs.addValue(subjectAndListener.listener, subjectAndListener);

            satisfied.remove(subjectAndListener.listener);
            if(targetToPairs.count(subjectAndListener.subject) == 0) {
                satisfied.put(subjectAndListener.subject, Boolean.TRUE);
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
            List<SubjectAndListener> sourceTargets = sourceToPairs.get(subject);

            // can we satisfy this target?
            tryEachTarget:
            for(int t = 0, targetsSize = sourceTargets.size(); t < targetsSize; t++) {
                SubjectAndListener sourceTarget = sourceTargets.get(t);

                // make sure we can satisfy this if all its sources are in satisfiedSources
                List<SubjectAndListener> allSourcesForSourceTarget = targetToPairs.get(sourceTarget.listener);
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
                targetToPairs.remove(sourceTarget.listener);

                // this target is no longer considered a target, since all
                // its dependencies are satisfied
                satisfiedToDo.add(sourceTarget.listener);
                satisfied.put(sourceTarget.listener, Boolean.TRUE);
            }
        }

        // if there's remaining targets, we never covered everything
        if(!targetToPairs.isEmpty()) {
            throw new IllegalStateException("Listener cycle detected, " + targetToPairs.values());
        }

        // success!
        return result;
    }

    /**
     * Register the specified listener to receive events from the specified
     * subject whenever they are fired.
     */
    public synchronized <Subject,Listener,Event> void addListener(Subject subject, Listener listener, EventFormat<Subject,Listener,Event> format) {
        // create a new list, then order it so dependencies are safe
        List<SubjectAndListener> unordered = concatenate(this.subjectAndListeners, Collections.singletonList(new SubjectAndListener(subject, listener, format)));
        this.subjectAndListeners = orderSubjectsAndListeners(unordered);

        // todo:
        // we're not out of the woods yet! We still need to walk through the
        // listeners list and trim obsolete listeners (from weak reference
        // proxies etc)
    }

    /**
     * Deregister the specified listener from recieving events from the specified
     * subject.
     */
    public synchronized void removeListener(Object subject, Object listener) {
        subjectAndListeners = new ArrayList<SubjectAndListener>(subjectAndListeners);

        // remove by identity (==), not equals()
        for(Iterator<SubjectAndListener> i = subjectAndListeners.iterator(); i.hasNext(); ) {
            SubjectAndListener subjectAndListener = i.next();
            if(subjectAndListener.subject != subject) continue;
            if(subjectAndListener.listener != listener) continue;
            i.remove();
            return;
        }

        // todo:
        // we're not out of the woods yet! We still need to walk through the
        // listeners list and trim obsolete listeners (from weak reference
        // proxies etc)

        throw new IllegalArgumentException("Cannot remove nonexistent listener " + listener);
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

    /**
     * A poor man's multimap, used only to reduce the complexity code that deals
     * with these otherwise painful structures.
     */
    private static class IdentityMultimap<K,V> extends IdentityHashMap<K,List<V>> {
        public void addValue(K key, V value) {
            List<V> values = super.get(key);
            if(values == null) {
                values = new ArrayList<V>(2);
                put(key, values);
            }
            values.add(value);
        }
        public List<V> get(Object key) {
            List<V> values = super.get(key);
            return values == null ? Collections.EMPTY_LIST : values;
        }
        public int count(Object key) {
            List<V> values = super.get(key);
            return values == null ? 0 : values.size();
        }
    }

    /**
     * Concatenate two lists to create a third list.
     */
    private static <E> List<E> concatenate(List<E> a, List<E> b) {
        List<E> aAndB = new ArrayList<E>(a.size() + b.size());
        aAndB.addAll(a);
        aAndB.addAll(b);
        return aAndB;
    }
}