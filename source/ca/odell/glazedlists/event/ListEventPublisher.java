/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.event;

// the core Glazed Lists package
import ca.odell.glazedlists.*;
// for keeping a list of changes
import java.util.*;

/**
 * Manager for distributing {@link ListEvent}s to {@link ListEventListener}s.
 * Because {@link ListEvent}s must be forwarded in a safe order, the
 * {@link ListEventPublisher} manages dependencies between {@link ListEventListener}s
 * and {@link EventList}s. Therefore any {@link ListEventListener} that fires
 * {@link ListEvent}s from within the {@link ListEventListener#listChanged(ListEvent) listChanged()}
 * method shall share the {@link ListEventPublisher} with its source {@link EventList}.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class ListEventPublisher {

    /** the list of DependentListeners managed by this publisher */
    private List dependentListeners = new ArrayList();
    
    /** whether a change is currently in progress */
    private int changesInProgress = 0;
    
    /** a list of EventLists that have their dependencies satisfied */
    private List satisfiedEventLists = new ArrayList();
    
    /** a list of DependentLists that have not had their dependencies satisfied */
    private List unsatisfiedListeners = new ArrayList();
    
    /** the first event to change in a sequence of events */
    private EventList eventCause  = null;
    
    /**
     * Creates a {@link ListEventPublisher}.
     */
    public ListEventPublisher() {
        // do nothing
    }
    
    /**
     * Requires that the specified {@link EventList} be updated before the
     * specified {@link ListEventListener} which depends on it. Dependencies are
     * automatically managed by most {@link EventList}s, so this method shall only
     * be used for {@link EventList}s that have indirect dependencies.
     */
    public void addDependency(EventList dependency, ListEventListener listener) {
        DependentListener dependentListener = getDependentListener(listener);
        if(dependentListener == null) {
            dependentListener = new DependentListener(listener);
            dependentListeners.add(dependentListener);
        }
        dependentListener.getDependencies().add(dependency);
    }

    /**
     * Removes the specified {@link EventList} as a dependency for the specified
     * {@link ListEventListener}.
     */
    public void removeDependency(EventList dependency, ListEventListener listener) {
        DependentListener dependentListener = getDependentListener(listener);
        if(dependentListener == null) throw new IllegalArgumentException();
        List dependencies = dependentListener.getDependencies();
        for(int i = 0; i < dependencies.size(); i++) {
            if(dependencies.get(i) == dependency) {
                dependencies.remove(i);
                break;
            }
        }
        if(dependencies.isEmpty()) dependentListeners.remove(dependentListener);
    }
    
    /**
     * Gets the dependent list for the specified @{ListEventListener}.
     */
    private DependentListener getDependentListener(ListEventListener listener) {
        for(int i = 0; i < dependentListeners.size(); i++) {
            DependentListener dependentListener = (DependentListener)dependentListeners.get(i);
            if(dependentListener.getListener() == listener) return dependentListener;
        }
        return null;
    }
    private DependentListener getDependentListener(Object object) {
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

        // notify all listeners
        for(int i = 0; i < listeners.size(); i++) {
            ListEventListener listener = (ListEventListener)listeners.get(i);
            ListEvent event = (ListEvent)events.get(i);

            // if our listener is managed, fulfill its dependencies first
            if(dependenciesSatisfied(listener)) {
                listener.listChanged(event);
                //if(no events come through) lists dependent on this do not need
                    // events from this to be satisfied
                if(listener instanceof EventList) {
                    satisfiedEventLists.add((EventList)listener);
                }
            // if the dependencies are not satisfied
            } else {
                //System.out.println("Saving pending event");
                DependentListener dependentListener = getDependentListener(listener);
                dependentListener.addPendingEvent(event);
                unsatisfiedListeners.add(dependentListener);
            }
        }
        
        // process all safe pending events
        for(Iterator i = unsatisfiedListeners.iterator(); i.hasNext(); ) {
            DependentListener dependentListener = (DependentListener)i.next();
            if(dependenciesSatisfied(dependentListener)) {
                //System.out.println("Executing pending event");
                i.remove();
                dependentListener.firePendingEvents();
                // this is now satisfied
                if(dependentListener.getListener() instanceof EventList) {
                    satisfiedEventLists.add((EventList)dependentListener.getListener());
                }
            }
        }
        
        // keep track of how many changes we've completed
        changesInProgress--;
        
        // clean up if this is the last change
        if(changesInProgress == 0) {
            eventCause = null;
            if(!unsatisfiedListeners.isEmpty()) {
                throw new IllegalStateException("Unsatisfied ListEventListeners: " + unsatisfiedListeners);
            }
            
            satisfiedEventLists.clear();
        }
    }
    
    /**
     * Returns true if the specified {@link ListEventListener}'s required dependencies
     * have been satisfied.
     *
     * <p>A dependency has been satisfied if it has been notified of the current event,
     * either directly or indirectly.
     *
     * <p>A dependency is required if it is dependent on the event's cause, either
     * directly or indirectly.
     *
     * <p>This method is currently broken. If an {@link EventList} fails to forward
     * any events, it may cause a source {@link EventList} to be unsatisfied.
     */
    public boolean dependenciesSatisfied(ListEventListener listener) {
        return dependenciesSatisfied(getDependentListener(listener));
    }
    public boolean dependenciesSatisfied(DependentListener dependentListener) {
        List dependenciesToSatisfy = dependentListener.getDependencies();
        for(int d = 0; d < dependenciesToSatisfy.size(); d++) {
            EventList dependency = (EventList)dependenciesToSatisfy.get(d);
            
            if(listContains(satisfiedEventLists, dependency)) continue;
            
            DependentListener dependencyListener = getDependentListener(dependency);
            if(dependencyListener == null) continue;
            if(dependencyListener.dependsOn(eventCause)) return false;
        }

        return true;
    }
    
    /**
     * A {@ListEventListener} and the {@EventList}s that it depends on.
     */
    class DependentListener {

        /** the listener to track dependencies for */
        private ListEventListener listener;

        /** the EventLists that this listener is dependent upon */
        private List dependencies = new ArrayList();
        
        /** the events to fire the awaiting listeners */
        private List pendingEvents = new ArrayList();
        
        /**
         * Creates a DependentListener for tracking the dependencies of the specified
         * ListEventListener.
         */
        public DependentListener(ListEventListener listener) {
            this.listener = listener;
        }

        /**
         * Get a {@link List} of {@link EventList}s that this listener is dependent
         * upon.
         */
        public List getDependencies() {
            return dependencies;
        }

        /**
         * Get the listener that this tracks dependencies for.
         */
        public ListEventListener getListener() {
            return listener;
        }
        
        /**
         * Returns true if this {@link DependentListener} depends on the specified
         * {@link EventList}. This checks the recursivly, such that if this has
         * a dependency that depends on the specified cause, then this is dependent.
         */
        public boolean dependsOn(EventList cause) {
            for(int d = 0; d < dependencies.size(); d++) {
                EventList dependency = (EventList)dependencies.get(d);
                if(cause == dependency) return true;
                DependentListener recursive = (DependentListener)getDependentListener(dependency);
                if(recursive != null && recursive.dependsOn(cause)) return true;
            }
            return false;
        }

        /**
         * Get this DependentList for debugging.
         */
        public String toString() {
            StringBuffer result = new StringBuffer();
            result.append(listener.getClass().getName());
            result.append(" (");
            for(Iterator i = dependencies.iterator(); i.hasNext(); ) {
                EventList dependency = (EventList)i.next();
                result.append(dependency.getClass().getName());
                if(i.hasNext()) result.append(", ");
            }
            result.append(")");
            return result.toString();
        }
        
        /**
         * Adds the specified {@linkListEvent} to be fired upon the completion of
         * its dependencies.
         */
        public void addPendingEvent(ListEvent pendingEvent) {
            pendingEvents.add(pendingEvent);
        }

        /**
         * Fires all pending events.
         */
        public void firePendingEvents() {
            for(int i = 0; i < pendingEvents.size(); i++) {
                ListEvent event = (ListEvent)pendingEvents.get(i);
                listener.listChanged(event);
            }
            pendingEvents.clear();
        }
    }
}