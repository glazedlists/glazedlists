/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the core Glazed Lists package
import ca.odell.glazedlists.event.ListEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link EventList} that shows the current contents of its source {@link EventList}.
 * 
 * <p>When this {@link EventList} is <i>frozen</i>, changes to its source {@link EventList}
 * will not be reflected. Instead, the {@link FreezableList} will continue to show
 * the state of its source {@link EventList} at the time it was frozen. 
 *
 * <p>When this {@link EventList} is <i>thawed</i>, changes to its source
 * {@link EventList} will be reflected.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>writable when thawed (default), not writable when frozen</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(1), writes O(1), freezes O(N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>frozen: 4 bytes per element, thawed: 0 bytes per element</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class FreezableList<E> extends TransformedList<E, E> {

    /** the state of the freezable list */
    private boolean frozen = false;

    /** the frozen objects */
    private List<E> frozenData = new ArrayList<E>();

    /**
     * Creates a {@link FreezableList} that can freeze the view of the specified
     * source {@link EventList}.
     */
    public FreezableList(EventList<E> source) {
        super(source);
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public E get(int index) {
        if(frozen) {
            return frozenData.get(index);
        } else {
            return source.get(index);
        }
    }

    /** {@inheritDoc} */
    public int size() {
        if(frozen) {
            return frozenData.size();
        } else {
            return source.size();
        }
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return !frozen;
    }

    /**
     * Gets whether this {@link EventList} is showing a previous state of the source
     * {@link EventList}.
     *
     * @return <tt>true</tt> if this list is showing a previous state of the source
     *      {@link EventList} or <tt>false</tt> if this is showing the current state
     *      of the source {@link EventList}.
     */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * Locks this {@link FreezableList} on the current state of the source
     * {@link EventList}. While frozen, changes to the source {@link EventList}
     * will not be reflected by this list.
     * 
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void freeze() {
        if(frozen) throw new IllegalStateException("Cannot freeze a list that is already frozen");

        // we are no longer interested in update events
        source.removeListEventListener(this);

        // copy the source array into the frozen list
        frozenData.addAll(source);

        // mark this list as frozen
        frozen = true;
    }

    /**
     * Unlocks this {@link FreezableList} to show the same contents of the source
     * {@link EventList}. When thawed, changes to the source {@link EventList}
     * will be reflected by this list.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void thaw() {
        if(!frozen) throw new IllegalStateException("Cannot thaw a list that is not frozen");

        // prep events to listeners of the thaw
        updates.beginEvent();
        for(int i = 0, size = frozenData.size(); i < size; i++) {
            updates.elementDeleted(0, frozenData.get(i));
        }
        if(source.size() > 0) {
            updates.addInsert(0, source.size() - 1);
        }

        // we don't need our frozen data anymore
        frozenData.clear();
        frozen = false;

        // being listening to update events
        source.addListEventListener(this);

        // fire off the thaw event
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        if(frozen) {
            // when a list change event arrives and this list is frozen,
            // it is possible that the event was queued before this list
            // was frozen. for this reason we do not throw any exceptions
            // but instead silently ignore the event

        } else {
            // just pass on the changes
            updates.forwardEvent(listChanges);
        }
    }
}