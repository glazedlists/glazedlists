/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

// the core Glazed Lists package
import ca.odell.glazedlists.EventList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Define a strategy for managing dependencies in the observer pattern.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface ListEventPublisher {

    /**
     * Requires that the specified {@link EventList} be updated before the
     * specified {@link ListEventListener} which depends on it. Dependencies are
     * automatically managed by most {@link EventList}s, so this method shall only
     * be used for {@link EventList}s that have indirect dependencies.
     *
     * @deprecated replaced with {@link #setRelatedSubject}, which has different
     *      semantics and takes different arguments, but accomplishes the same goal
     */
    public abstract void addDependency(EventList dependency, ListEventListener listener);

    /**
     * Removes the specified {@link EventList} as a dependency for the specified
     * {@link ListEventListener}. This {@link ListEventListener} will continue to
     * receive {@link ListEvent}s, but there will be no dependency tracking when
     * such events are fired.
     *
     * @deprecated replaced with {@link #removeRelatedSubject}, which has different
     *      semantics and takes different arguments, but accomplishes the same goal
     */
    public abstract void removeDependency(EventList dependency, ListEventListener listener);

    /**
     * Attach the specified listener to the specified subject, so that when
     * dependencies are being prepared, notifying the listener will be
     * considered equivalent to notifying the subject. This makes it possible
     * to support multiple listeners in a single subject, typically using
     * inner classes.
     */
    public abstract void setRelatedSubject(Object listener, Object relatedSubject);

    /**
     * Detach the subject related to the specified listener.
     */
    public abstract void removeRelatedSubject(Object listener);
}