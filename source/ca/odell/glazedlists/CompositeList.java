/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.util.Iterator;

/**
 * An {@link EventList} composed of multiple source {@link EventList}s. This list
 * shows the contents of its source lists.
 *
 * <p>Note that all contained {@link EventList}s must use the same {@link ListEventPublisher} and 
 * {@link ReadWriteLock}, particularly if this {@link EventList} is to be used my multiple threads 
 * concurrently. To construct an {@link EventList} that shares the {@link ListEventPublisher} and 
 * {@link ReadWriteLock} with this {@link CompositeList}, use {@link #createMemberList()}.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
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
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class CompositeList<E> extends CollectionList<EventList<E>, E> {

    public CompositeList() {
        super(new BasicEventList<EventList<E>>(), (Model)GlazedLists.listCollectionListModel());
    }

    /**
     * Create a {@link CompositeList} that uses the given <code>lock</code>. Note that this lock
     * will also be used when {@link #createMemberList building new member lists}.
     * <p>
     * This can be a convenient constructor to use when the member lists are prebuilt ahead of time
     * with a common {@link ReadWriteLock} and it is desirable to compose their union with a
     * {@link CompositeList}.
     * 
     * @param lock the {@link ReadWriteLock} to use within the {@link CompositeList}
     * @deprecated replaced by {@link #CompositeList(ListEventPublisher, ReadWriteLock)}, because
     *             prebuilt member lists should share lock <em>and</em> publisher with the
     *             CompositeList.
     */
    public CompositeList(ReadWriteLock lock) {
        super(new BasicEventList<EventList<E>>(lock), (Model)GlazedLists.listCollectionListModel());
    }
    
    /**
     * Create a {@link CompositeList} that uses the given <code>publisher</code> and
     * <code>lock</code>. Note that this publisher and lock will also be used when
     * {@link #createMemberList building new member lists}.
     * <p>
     * This can be a convenient constructor to use when the member lists are prebuilt ahead of time
     * with a common {@link ListEventPublisher} and {@link ReadWriteLock} and it is desirable to
     * compose their union with a {@link CompositeList}.
     * 
     * @param publisher the {@link ListEventPublisher} to use within the {@link CompositeList}
     * @param lock the {@link ReadWriteLock} to use within the {@link CompositeList}
     */
    public CompositeList(ListEventPublisher publisher, ReadWriteLock lock) {
        super(new BasicEventList<EventList<E>>(publisher, lock), (Model)GlazedLists.listCollectionListModel());
    }
        
    /**
     * Adds the specified {@link EventList} as a source to this {@link CompositeList}.
     * <p>
     * To ensure correct behaviour when this {@link CompositeList} is used by multiple threads, the
     * specified EventList has to share the same {@link ReadWriteLock} and
     * {@link ListEventPublisher} with this CompositeList.
     * 
     * @throws IllegalArgumentException if the specified EventList uses a different
     *         {@link ReadWriteLock} or {@link ListEventPublisher}
     * @see #createMemberList()
     */
    public void addMemberList(EventList<E> member) {
        if (!getPublisher().equals(member.getPublisher()))
            throw new IllegalArgumentException("Member list must share publisher with CompositeList");

        if (!getReadWriteLock().equals(member.getReadWriteLock()))
            throw new IllegalArgumentException("Member list must share lock with CompositeList");

        source.add(member);
    }
    
    /**
     * Creates a new {@link EventList} that shares its {@link ReadWriteLock} and
     * {@link ListEventPublisher} with this {@link CompositeList}. This is
     * necessary when this {@link CompositeList} will be used by multiple
     * threads.
     *
     * <p>Note that the created {@link EventList} must be explicitly added as a member
     * to this {@link CompositeList} using {@link #addMemberList(EventList)}.
     *
     * @return a new EventList appropriate for use as a
     *      {#link #addMemberList member list} of this CompositeList
     */
    public <E> EventList<E> createMemberList() {
        return new BasicEventList<E>(getPublisher(), getReadWriteLock());
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