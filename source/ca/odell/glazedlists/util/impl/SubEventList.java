/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;

/**
 * A SubEventList is a view of a sub-range of an EventList.
 *
 * <p>Although the <code>SubEventList</code>'s size is initially fixed, the 
 * <code>SubEventList</code> can change size as a consequence of changes to
 * the source list that occur within the range covered by the <code>SubEventList</code>.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class SubEventList extends TransformedList implements ListEventListener {

    /** the start index of this list, inclusive */
    private int startIndex;
    
    /** the end index of this list, exclusive */
    private int endIndex;
    
    /**
     * Creates a new SubEventList that covers the specified range of indices
     * in the source list.
     *
     * @param startIndex the start index of the source list, inclusive
     * @param endIndex the end index of the source list, exclusive
     * @param source the source list to view
     * @param automaticallyRemove true if this SubEventList should deregister itself
     *      from the ListEventListener list of the source list once it is
     *      otherwise out of scope.
     *
     * @see ca.odell.glazedlists.event.WeakReferenceProxy
     */
    public SubEventList(EventList source, int startIndex, int endIndex, boolean automaticallyRemove) {
        super(source);
        
        // do consistency checking
        if(startIndex < 0 || endIndex < startIndex || endIndex > source.size()) {
            throw new IllegalArgumentException("The range " + startIndex + "-" + endIndex + " is not valid over a list of size " + source.size());
        }
        
        // save the sublist bounds
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    
        // listen directly or via a proxy that will do garbage collection
        if(automaticallyRemove) {
            source.addListEventListener(new WeakReferenceProxy(source, this));
        } else {
            source.addListEventListener(this);
        }
    }
    
    /** {@inheritDoc} */
    public int size() {
        return endIndex - startIndex;
    }
    
    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        return mutationIndex + startIndex;
    }
    
    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        updates.beginEvent();

        // if this sublist is size one, just move the start and end index
        if(listChanges.isReordering() && size() == 1) {
            int[] reorderMap = listChanges.getReorderMap();
            for(int r = 0; r < reorderMap.length; r++) {
                if(reorderMap[r] == startIndex) {
                    startIndex = r;
                    endIndex = startIndex + 1;
                    break;
                }
            }

        // handle regular change events by shifting indices as necessary
        } else {
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();
                
                // if it is a change before
                if(changeIndex < startIndex || (changeType == ListEvent.INSERT && changeIndex == startIndex)) {
                    if(changeType == ListEvent.INSERT) {
                        startIndex++;
                        endIndex++;
                    } else if(changeType == ListEvent.DELETE) {
                        startIndex--;
                        endIndex--;
                    }
                // if it is a change within
                } else if(changeIndex < endIndex) {
                    if(changeType == ListEvent.INSERT) {
                        endIndex++;
                        updates.addInsert(changeIndex - startIndex);
                    } else if(changeType == ListEvent.UPDATE) {
                        updates.addInsert(changeIndex - startIndex);
                    } else if(changeType == ListEvent.DELETE) {
                        endIndex--;
                        updates.addDelete(changeIndex - startIndex);
                    }
                // if it is a change after
                } else {
                    // do nothing
                }
            }
        }
        assert(startIndex <= endIndex);
        updates.commitEvent();
    }
}
