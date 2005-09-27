/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the core Glazed Lists package
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.util.Iterator;

/**
 * An {@link EventList} composed of multiple source {@link EventList}s. This list
 * shows the contents of its source lists.
 *
 * <p>Note that all contained {@link EventList}s must use the same {@link ReadWriteLock}
 * if this {@link EventList} is to be used my multiple threads concurrently. To
 * construct an {@link EventList} that shares the {@link ReadWriteLock} with this
 * {@link CompositeList}, use {@link #createMemberList()} or
 * {@link BasicEventList#BasicEventList(ReadWriteLock) new BasicEventList(CompositeList.getReadWriteLock())}
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CompositeList<E> extends CollectionList<E,EventList<E>> {

    public CompositeList() {
        super(new BasicEventList<EventList<E>>(), (Model)GlazedLists.listCollectionListModel());
    }
    
    /**
     * Adds the specified {@link EventList} as a source to this {@link CompositeList}.
     */
    public void addMemberList(EventList<E> list) {
        source.add(list);
    }
    
    /**
     * Creates a new {@link EventList} that shares its {@link ReadWriteLock} with
     * this {@link CompositeList}. This is necessary when this {@link CompositeList}
     * will be used by multiple threads.
     *
     * <p>Note that the created {@link EventList} must be explicitly added as a member
     * to this {@link CompositeList} using {@link #addMemberList(EventList)}.
     */
    public EventList<E> createMemberList() {
        return new BasicEventList<E>(getReadWriteLock());
    }
    
    /**
     * Removes the specified {@link EventList} as a source {@link EventList}
     * to this {@link CompositeList}.
     */
    public void removeMemberList(EventList<E> list) {
        for(Iterator<EventList<E>> i = source.iterator(); i.hasNext(); ) {
            if(i.next() == list) {
                i.remove();
                return;
            }
        }
        throw new IllegalArgumentException("Cannot remove list " + list + " which is not in this CompositeList");
    }
}

