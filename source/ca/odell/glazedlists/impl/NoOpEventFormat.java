/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.event.SequenceDependenciesEventPublisher;
import ca.odell.glazedlists.ListSelection;

/**
 * An EventFormat used to specify explicit dependencies, but that doesn't
 * actually fire events. This is useful when one listener has multiple subjects
 * that share the same data, such as in {@link ListSelection}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class NoOpEventFormat implements SequenceDependenciesEventPublisher.EventFormat {
    public static final SequenceDependenciesEventPublisher.EventFormat INSTANCE = new NoOpEventFormat();
    private  NoOpEventFormat() {
        // prevent instantiation
    }
    public void fire(Object subject, Object event, Object listener) {
        throw new UnsupportedOperationException();
    }
    public void postEvent(Object subject) {
        throw new UnsupportedOperationException();
    }
    public boolean isStale(Object subject, Object listener) {
        return false;
    }
}
