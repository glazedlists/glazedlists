/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the core Glazed Lists package
import ca.odell.glazedlists.event.*;
// an arraylist holds the frozen data
import java.util.ArrayList;

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
 * <p><strong><font color="#FF0000">Warning:</font></strong> When a
 * {@link FreezableList} is <i>frozen</i>, it is not writable via its API. Calls to
 * {@link #set(int,Object) set()}, {@link #add(Object) add()}, etc. will throw a
 * {@link RuntimeException}.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class FreezableList extends TransformedList implements ListEventListener {

    /** the state of the freezable list */
    private boolean frozen = false;
    
    /** the frozen objects */
    private ArrayList frozenData = new ArrayList();
    
    /**
     * Creates a {@link FreezableList} that can freeze the view of the specified
     * source {@link EventList}.
     */
    public FreezableList(EventList source) {
        super(source);
        source.addListEventListener(this);
    }
    
    /** {@inheritDoc} */
    public Object get(int index) {
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
    /*protected int getSourceIndex(int mutationIndex) {
        return mutationIndex;
    }*/
    
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
     */
    public void freeze() {
        getReadWriteLock().writeLock().lock();
        try {
            if(frozen) throw new IllegalStateException("Cannot freeze a list that is already frozen");
            
            // we are no longer interested in update events
            ((EventList)source).removeListEventListener(this);
            
            // copy the source array into the frozen list
            frozenData.addAll(source);
            
            // mark this list as frozen
            frozen = true;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
    
    /**
     * Unlocks this {@link FreezableList} to show the same contents of the source
     * {@link EventList}. When thawed, changes to the source {@link EventList}
     * will be reflected by this list.
     */
    public void thaw() {
        getReadWriteLock().writeLock().lock();
        try {
            if(!frozen) throw new IllegalStateException("Cannot thaw a list that is not frozen");
            
            // mark this list as thawed
            frozen = false;
            int frozenDataSize = frozenData.size();
            frozenData.clear();
            
            // fire events to listeners of the thaw
            updates.beginEvent();
            if(frozenDataSize > 0) updates.addDelete(0, frozenDataSize - 1);
            if(source.size() > 0) updates.addInsert(0, source.size() - 1);
            updates.commitEvent();

            // being listening to update events
            ((EventList)source).addListEventListener(this);
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
    
    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
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
