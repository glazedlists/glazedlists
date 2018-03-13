/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

/**
 * An {@link EventList} which delegates all List methods to a given source
 * {@link EventList} that may be replaced at runtime using
 * {@link #setSource(EventList)}.
 *
 * <p>Note that the source {@link EventList} must use the same
 * {@link ListEventPublisher} and {@link ReadWriteLock}, particularly if this
 * {@link EventList} is to be used by multiple threads concurrently. To
 * construct an {@link EventList} that shares the {@link ListEventPublisher}
 * and {@link ReadWriteLock} with this {@link PluggableList}, use
 * {@link #createSourceList()}.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td><strong>only {@link #setSource(EventList)}</strong></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>delegates to source EventList</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td></td></tr>
 * </table>
 *
 * @author James Lemieux
 */
public class PluggableList<E> extends TransformedList<E, E> {

    /**
     * Constructs a PluggableList which uses the given <code>publisher</code>
     * and <code>lock</code>. The PluggableList will default to use a
     * {@link BasicEventList} that also uses the same <code>publisher</code>
     * and <code>lock</code>.
     *
     * @param publisher the {@link ListEventPublisher} to use within the {@link PluggableList}
     * @param lock the {@link ReadWriteLock} to use within the {@link PluggableList}
     */
    public PluggableList(ListEventPublisher publisher, ReadWriteLock lock) {
        this(new BasicEventList<E>(publisher, lock));
    }

    /**
     * Constructs a PluggableList which delegates all List methods to the given
     * <code>source</code>. At some future time, the source EventList may be
     * replaced using {@link #setSource(EventList)} and this PluggableList will
     * produce a {@link ListEvent} describing the change in data.
     *
     * @param source the source of data to this PluggableList
     */
    public PluggableList(EventList<E> source) {
        super(source);
        source.addListEventListener(this);
    }

    /**
     * Creates a new {@link EventList} that shares its
     * {@link ca.odell.glazedlists.util.concurrent.ReadWriteLock} and
     * {@link ca.odell.glazedlists.event.ListEventPublisher} with this
     * {@link PluggableList}. This is necessary when this {@link PluggableList}
     * will be used by multiple threads.
     *
     * <p>Note that the created {@link EventList} must be explicitly set as the
     * source of this {@link PluggableList} using {@link #setSource(EventList)}.
     *
     * @return a new EventList appropriate for use as the
     *      {@link #setSource(EventList) source} of this PluggableList
     */
    public EventList<E> createSourceList() {
        return new BasicEventList<>(getPublisher(), getReadWriteLock());
    }

    /**
     * Sets the source EventList to which this PluggableList will delegate all
     * calls. This method is the entire reason that PluggableList exists. It
     * allows the data source of the remaining pipeline to be altered.
     * <p>
     * To ensure correct behaviour when this {@link PluggableList} is used by
     * multiple threads, the given <code>source</code> <strong>must</strong>
     * share the same {@link ca.odell.glazedlists.util.concurrent.ReadWriteLock} and
     * {@link ca.odell.glazedlists.event.ListEventPublisher} with this PluggableList.
     *
     * @param source the new source of data for this PluggableList, and all
     *      downstream EventLists
     * @throws IllegalStateException if this PluggableList is already disposed
     * @throws IllegalArgumentException if any of the following are true
     *         <ul>
     *           <li>the given source is null</li>
     *           <li>the given source has a different ListEventPublisher than this PluggableList</li>
     *           <li>the given source has a different ReadWriteLock than this PluggableList</li>
     *         </ul>
     */
    public void setSource(EventList<E> source) {
        // lock the pipeline while the source list is swapped
        getReadWriteLock().writeLock().lock();
        try {
            if (this.source == null)
                throw new IllegalStateException("setSource may not be called on a disposed PluggableList");

            if (source == null)
                throw new IllegalArgumentException("source may not be null");

            if (!getReadWriteLock().equals(source.getReadWriteLock()))
                throw new IllegalArgumentException("source list must share lock with PluggableList");

            if (!getPublisher().equals(source.getPublisher()))
                throw new IllegalArgumentException("source list must share publisher with PluggableList");

            if (this.source == source)
                return;

            updates.beginEvent();
            // add deletions to the ListEvent for all the elements in the old source
            for (int i = 0, n = size(); i < n; i++)
                updates.elementDeleted(0, get(i));

            this.source.removeListEventListener(this);
            this.source = source;
            this.source.addListEventListener(this);

            // add insertions to the ListEvent for all the elements in the new source
            for (int i = 0, n = size(); i < n; i++)
                updates.elementInserted(i, get(i));

            // broadcast the ListEvent that describes the data change
            updates.commitEvent();
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }

    /** @inheritDoc */
    @Override
    protected boolean isWritable() {
        return true;
    }

    /** @inheritDoc */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        updates.forwardEvent(listChanges);
    }

    /** @inheritDoc */
    @Override
    public void dispose() {
        if (source != null)
            source.removeListEventListener(this);
        source = null;
    }
}