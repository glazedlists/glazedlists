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
 * Manager for distributing {@link ListEvent}s.
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
    
    /**
     * Creates a {@link ListEventPublisher}.
     */
    public ListEventPublisher() {
        // do nothing
    }
    
    /**
     * Adds the specified dependency to the specified EventList.
     */
    void addDependency(ListEventListener listener, EventList dependency) {
        DependentListener dependentListener = getDependentListener(listener);
        if(dependentListener == null) {
            dependentListener = new DependentListener(listener);
            dependentListeners.add(dependentListener);
        }
        dependentListener.getDependencies().add(dependency);
    }
    /**
     * Removes the specified dependency for the specified EventList.
     */
    void removeDependency(ListEventListener listener, EventList dependency) {
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
     * Gets the dependent list for the specified EventList.
     */
    private DependentListener getDependentListener(ListEventListener listener) {
        for(int i = 0; i < dependentListeners.size(); i++) {
            DependentListener dependentListener = (DependentListener)dependentListeners.get(i);
            if(dependentListener.getListener() == listener) return dependentListener;
        }
        return null;
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
    private boolean listContainsAll(List eventLists, List goal) {
        for(int i = 0; i < goal.size(); i++) {
            if(!listContains(eventLists, goal.get(i))) return false;
        }
        return true;
    }
    

    /**
     * Fires the specified events to the specified listeners.
     */
    void fireEvent(EventList source, List listeners, List events) {
        // keep track of how many changes we've completed
        changesInProgress++;
        
        // populate the list of satisfied EventLists
        List satisfiedRecursively = new ArrayList();
        satisfiedRecursively.clear();
        satisfiedRecursively.add(source);
        while(!satisfiedRecursively.isEmpty()) {
            // add this event list
            EventList eventList = (EventList)satisfiedRecursively.remove(satisfiedRecursively.size() - 1);
            if(listContains(satisfiedEventLists, eventList)) continue;
            satisfiedEventLists.add(eventList);
            // continue the search for this event list's dependencies
            if(eventList instanceof ListEventListener) {
                DependentListener dependentListener = getDependentListener((ListEventListener)eventList);
                if(dependentListener != null) {
                    satisfiedRecursively.addAll(dependentListener.getDependencies());
                }
            }
        }
        assert(satisfiedEventLists.contains(source));

        // notify all listeners
        for(int i = 0; i < listeners.size(); i++) {
            ListEventListener listener = (ListEventListener)listeners.get(i);
            ListEvent event = (ListEvent)events.get(i);

            // if our listener is managed, fulfill its dependencies first
            DependentListener dependentListener = getDependentListener(listener);
            if(dependentListener != null) {
                // if the dependencies are satisfied
                if(listContainsAll(satisfiedEventLists, dependentListener.getDependencies())) {
                    listener.listChanged(event);
                    // this is now satisfied
                    if(listener instanceof EventList) {
                        satisfiedEventLists.add((EventList)listener);
                    }
                // if the dependencies are not satisfied
                } else {
                    //System.out.println("Saving pending event");
                    dependentListener.addPendingEvent(listener, event);
                    unsatisfiedListeners.add(dependentListener);
                }

            // if our listener is unmanaged, notify right away
            } else {
                listener.listChanged(event);
                // this is now satisfied
                if(listener instanceof EventList) {
                    satisfiedEventLists.add((EventList)listener);
                }
            }
        }
        
        // process all safe pending events
        for(Iterator i = unsatisfiedListeners.iterator(); i.hasNext(); ) {
            DependentListener dependentListener = (DependentListener)i.next();
            if(listContainsAll(satisfiedEventLists, dependentListener.getDependencies())) {
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
            if(!unsatisfiedListeners.isEmpty()) {
                throw new IllegalStateException("Unsatisfied ListEventListeners: " + unsatisfiedListeners);
            }
            
            satisfiedEventLists.clear();
        }
    }
    
    /**
     * A {@ListEventListener} and the {@EventList}s that it depends on.
     */
    class DependentListener {

        /** the listener to track dependencies for */
        private ListEventListener listener;

        /** the EventLists that this listener is dependent upon */
        private List dependencies = new ArrayList();
        
        /** the listeners awaiting notification */
        private List pendingListeners = new ArrayList();
        /** the events to fire the awaiting listeners */
        private List pendingEvents = new ArrayList();
        
        /**
         * Creates a DependentListener for tracking the dependencies of the specified
         * ListEventListener.
         */
        public DependentListener(ListEventListener listener) {
            this.listener = listener;
        }

        public List getDependencies() {
            return dependencies;
        }

        public ListEventListener getListener() {
            return listener;
        }

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

        public void addPendingEvent(ListEventListener pendingListener, ListEvent pendingEvent) {
            if(pendingListener != listener) throw new IllegalStateException();
            pendingListeners.add(pendingListener);
            pendingEvents.add(pendingEvent);
        }

        public void firePendingEvents() {
            for(int i = 0; i < pendingListeners.size(); i++) {
                ListEventListener listener = (ListEventListener)pendingListeners.get(i);
                ListEvent event = (ListEvent)pendingEvents.get(i);
                listener.listChanged(event);
            }
            pendingListeners.clear();
            pendingEvents.clear();
        }
    }
}