/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.EventList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manager for distributing {@link ListEvent}s to {@link ListEventListener}s.
 * Because {@link ListEvent}s must be forwarded in a safe order, the
 * {@link GraphDependenciesListEventPublisher} manages dependencies between {@link ListEventListener}s
 * and {@link EventList}s. Therefore any {@link ListEventListener} that fires
 * {@link ListEvent}s from within the {@link ListEventListener#listChanged}
 * method shall share the {@link GraphDependenciesListEventPublisher} with its source {@link EventList}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class GraphDependenciesListEventPublisher implements ListEventPublisher {

    /** the list of DependentListeners managed by this publisher */
    private List<DependentListener> dependentListeners = new ArrayList<DependentListener>();

    /** whether a change is currently in progress */
    private int changesInProgress = 0;

    /** the first caught RuntimeException that must be rethrown */
    private RuntimeException toRethrow = null;

    /** a list of EventLists that have their dependencies satisfied */
    private List<EventList> satisfiedEventLists = new ArrayList<EventList>();

    /** a list of DependentLists that have not had their dependencies satisfied */
    private List<GraphDependenciesListEventPublisher.DependentListener> unsatisfiedListeners = new ArrayList<GraphDependenciesListEventPublisher.DependentListener>();

    /** the first event to change in a sequence of events */
    private EventList eventCause = null;

    /**
     * Creates a {@link ca.odell.glazedlists.event.GraphDependenciesListEventPublisher}.
     */
    public GraphDependenciesListEventPublisher() {
        // do nothing
    }

    /**
     * Requires that the specified {@link ca.odell.glazedlists.EventList} be updated before the
     * specified {@link ca.odell.glazedlists.event.ListEventListener} which depends on it. Dependencies are
     * automatically managed by most {@link ca.odell.glazedlists.EventList}s, so this method shall only
     * be used for {@link ca.odell.glazedlists.EventList}s that have indirect dependencies.
     */
    public void addDependency(EventList dependency, ListEventListener listener) {
        GraphDependenciesListEventPublisher.DependentListener dependentListener = getDependentListener(listener);
        if(dependentListener == null) {
            dependentListener = new GraphDependenciesListEventPublisher.DependentListener(listener);
            dependentListeners.add(dependentListener);
        }
        dependentListener.getDependencies().add(dependency);
    }

    /**
     * Removes the specified {@link ca.odell.glazedlists.EventList} as a dependency for the specified
     * {@link ca.odell.glazedlists.event.ListEventListener}. This {@link ca.odell.glazedlists.event.ListEventListener} will continue to
     * receive {@link ca.odell.glazedlists.event.ListEvent}s, but there will be no dependency tracking when
     * such events are fired.
     */
    public void removeDependency(EventList dependency, ListEventListener listener) {
        GraphDependenciesListEventPublisher.DependentListener dependentListener = getDependentListener(listener);

        // if this dependency is explicitly unmanaged
        if(dependentListener == null) return;

        // remove the dependency
        List dependencies = dependentListener.getDependencies();
        for(int i = 0; i < dependencies.size(); i++) {
            if(dependencies.get(i) == dependency) {
                dependencies.remove(i);
                break;
            }
        }
        if(dependencies.isEmpty()) dependentListeners.remove(dependentListener);
    }

    /** {@inheritDoc} */
    public void setRelatedSubject(Object listener, Object relatedSubject) {
        // do nothing, these are for sequence dependencies event publisher
    }

    /** {@inheritDoc} */
    public void clearRelatedSubject(Object listener) {
        // do nothing, these are for sequence dependencies event publisher
    }

    /** {@inheritDoc} */
    public void setRelatedListener(Object subject, Object relatedListener) {
        // do nothing, these are for sequence dependencies event publisher
    }

    /** {@inheritDoc} */
    public void clearRelatedListener(Object subject, Object relatedListener) {
        // do nothing, these are for sequence dependencies event publisher
    }

    /**
     * Gets the dependent list for the specified @{ListEventListener}.
     */
    private GraphDependenciesListEventPublisher.DependentListener getDependentListener(ListEventListener listener) {
        for(int i = 0; i < dependentListeners.size(); i++) {
            GraphDependenciesListEventPublisher.DependentListener dependentListener = dependentListeners.get(i);
            if(dependentListener.getListener() == listener) return dependentListener;
        }
        return null;
    }
    private GraphDependenciesListEventPublisher.DependentListener getDependentListener(Object object) {
        if(object instanceof ListEventListener) {
            return getDependentListener((ListEventListener)object);
        } else {
            return null;
        }
    }

    /**
     * Gets whether the specified list contains the specified goal by value.
     */
    private boolean listContains(List eventLists, Object goal) {
        for(int i = 0; i < eventLists.size(); i++) {
            if(eventLists.get(i) == goal) return true;
        }
        return false;
    }

    /**
     * Fires the specified events to the specified listeners.
     */
    void fireEvent(EventList source, List listeners, List events) {
        // keep track of how many changes we've completed
        if(changesInProgress == 0) {
            eventCause = source;
        }
        changesInProgress++;

        // populate the list of satisfied EventLists
        if(!listContains(satisfiedEventLists, source)) satisfiedEventLists.add(source);

        // process listeners that don't have dependencies
        for(int i = 0; i < listeners.size(); i++) {
            ListEventListener listener = (ListEventListener)listeners.get(i);
            ListEvent event = (ListEvent)events.get(i);

            // if the dependencies are not satisfied
            if(!dependenciesSatisfied(listener)) {
                GraphDependenciesListEventPublisher.DependentListener dependentListener = getDependentListener(listener);
                dependentListener.addPendingEvent(event);
                unsatisfiedListeners.add(dependentListener);
                continue;
            }

            // satisfy this listener
            try {
                listener.listChanged(event);
                if(listener instanceof EventList) {
                    satisfiedEventLists.add((EventList)listener);
                }
            // if notification failed, handle that problem later
            } catch(RuntimeException newProblem) {
                if(toRethrow == null) toRethrow = newProblem;
            }
        }

        // process listeners that have dependencies
        for(Iterator<DependentListener> i = unsatisfiedListeners.iterator(); i.hasNext(); ) {
            GraphDependenciesListEventPublisher.DependentListener dependentListener = i.next();
            if(!dependenciesSatisfied(dependentListener)) continue;

            // satisfy this listener
            i.remove();
            try {
                dependentListener.firePendingEvents();
                if(dependentListener.getListener() instanceof EventList) {
                    satisfiedEventLists.add((EventList)dependentListener.getListener());
                }
            // if notification failed, handle that problem later
            } catch(RuntimeException newProblem) {
                if(toRethrow == null) toRethrow = newProblem;
            }
        }

        // keep track of how many changes we've completed
        changesInProgress--;

        // clean up if this is the last change
        if(changesInProgress == 0) {
            // reset state, including the list of who has been satisfied
            eventCause = null;
            satisfiedEventLists.clear();

            // if there are listeners not yet notified
            if(!unsatisfiedListeners.isEmpty()) {
                throw new IllegalStateException("Unsatisfied ListEventListeners: " + unsatisfiedListeners);
            }

            // pass any saved RuntimeExceptions up to the source
            if(toRethrow != null) {
                RuntimeException usersProblem = toRethrow;
                toRethrow = null;
                throw usersProblem;
            }
        }
    }

    /**
     * Returns true if the specified {@link ca.odell.glazedlists.event.ListEventListener}'s required dependencies
     * have been satisfied.
     *
     * <p>A dependency has been satisfied if it has been notified of the current event,
     * either directly or indirectly.
     *
     * <p>A dependency is required if it is dependent on the event's cause, either
     * directly or indirectly.
     *
     * <p>This method is currently broken. If an {@link ca.odell.glazedlists.EventList} fails to forward
     * any events, it may cause a source {@link ca.odell.glazedlists.EventList} to be unsatisfied.
     */
    public boolean dependenciesSatisfied(ListEventListener listener) {
        return dependenciesSatisfied(getDependentListener(listener));
    }
    public boolean dependenciesSatisfied(GraphDependenciesListEventPublisher.DependentListener dependentListener) {
        // if this dependency is explicitly unmanaged
        if(dependentListener == null) return true;

        // this dependency is managed, test if it is satisfied
        List<EventList> dependenciesToSatisfy = dependentListener.getDependencies();
        for(int d = 0; d < dependenciesToSatisfy.size(); d++) {
            EventList dependency = dependenciesToSatisfy.get(d);

            if(listContains(satisfiedEventLists, dependency)) continue;

            GraphDependenciesListEventPublisher.DependentListener dependencyListener = getDependentListener(dependency);
            if(dependencyListener == null) continue;
            if(dependencyListener.dependsOn(eventCause)) return false;
        }

        return true;
    }

    /**
     * A {@link ca.odell.glazedlists.event.ListEventListener} and the {@link ca.odell.glazedlists.EventList}s that it depends on.
     */
    class DependentListener {

        /** the listener to track dependencies for */
        private ListEventListener listener;

        /** the EventLists that this listener is dependent upon */
        private List<EventList> dependencies = new ArrayList<EventList>();

        /** the events to fire the awaiting listeners */
        private List<ListEvent> pendingEvents = new ArrayList<ListEvent>();

        /**
         * Creates a DependentListener for tracking the dependencies of the specified
         * ListEventListener.
         */
        public DependentListener(ListEventListener listener) {
            this.listener = listener;
        }

        /**
         * Get this DependentList for debugging.
         */
        public String toString() {
            StringBuffer result = new StringBuffer();
            result.append(listener.getClass().getName());
            result.append("\n");
            for(Iterator<EventList> i = dependencies.iterator(); i.hasNext(); ) {
                result.append(" > DEPENDS ON > ").append(i.next().getClass().getName()).append("\n"); //.append(", LIST CONTENTS=" + dependency).append("\n");
            }
            return result.toString();
        }

        /**
         * Get a {@link java.util.List} of {@link ca.odell.glazedlists.EventList}s that this listener is dependent
         * upon.
         */
        public List<EventList> getDependencies() {
            return dependencies;
        }

        /**
         * Get the listener that this tracks dependencies for.
         */
        public ListEventListener getListener() {
            return listener;
        }

        /**
         * Returns true if this {@link ca.odell.glazedlists.event.GraphDependenciesListEventPublisher.DependentListener} depends on the specified
         * {@link ca.odell.glazedlists.EventList}. This checks the recursivly, such that if this has
         * a dependency that depends on the specified cause, then this is dependent.
         */
        public boolean dependsOn(EventList cause) {
            for(int d = 0; d < dependencies.size(); d++) {
                EventList dependency = dependencies.get(d);
                if(cause == dependency) return true;
                GraphDependenciesListEventPublisher.DependentListener recursive = getDependentListener(dependency);
                if(recursive != null && recursive.dependsOn(cause)) return true;
            }
            return false;
        }

        /**
         * Adds the specified {@link ca.odell.glazedlists.event.ListEvent} to be fired upon the completion of
         * its dependencies.
         */
        public void addPendingEvent(ListEvent pendingEvent) {
            pendingEvents.add(pendingEvent);
        }

        /**
         * Fires all pending events.
         */
        public void firePendingEvents() {
            try {
                for(int i = 0; i < pendingEvents.size(); i++) {
                    ListEvent event = pendingEvents.get(i);
                    listener.listChanged(event);
                }
            } finally {
                pendingEvents.clear();
            }
        }
    }
}
