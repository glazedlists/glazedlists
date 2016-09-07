/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.hibernate;

import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

/**
 * Custom EventListType to test list categories.
 */
public final class TestEventListType2 extends EventListType {
    /** Lock as constant. */
    public static final ReadWriteLock LOCK = LockFactory.DEFAULT.createReadWriteLock();
    /** Publisher as constant. */
    public static final ListEventPublisher PUBLISHER = ListEventAssembler.createListEventPublisher();

    /** Constructor which sets a list category. */
    public TestEventListType2() {
        useListCategory("Test2", LOCK, PUBLISHER);
    }
}