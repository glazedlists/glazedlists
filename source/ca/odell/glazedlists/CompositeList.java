/* Glazed Lists                                                 (c) 2003-2006 */
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
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>only {@link #set(int,Object)} and {@link #remove(int)}</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td><strong>not thread safe</strong></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>96 bytes per element</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=25">25</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=93">93</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=96">96</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=162">162</a>
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CompositeList<E> extends CollectionList<EventList<E>, E> {

    public CompositeList() {
        super(new BasicEventList<EventList<E>>(), (Model)GlazedLists.listCollectionListModel());
    }

    /**
     * Create a {@link CompositeList} that uses the given <code>lock</code>.
     * Note that this lock will also be used when {@link #createMemberList
     * building new member lists}.
     *
     * <p>This can be a convenient constructor to use when the member lists
     * are prebuilt ahead of time with a common {@link ReadWriteLock} and it
     * is desirable to compose their union with a {@link CompositeList}.
     *
     * @param lock the {@link ReadWriteLock} to use within the {@link CompositeList}
     */
    public CompositeList(ReadWriteLock lock) {
        this();
        readWriteLock = lock;
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