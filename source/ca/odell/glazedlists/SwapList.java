/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.*;
// the core Glazed Lists package
import ca.odell.glazedlists.event.*;

/**
 * An {@link EventList} that swaps the elements of its source {@link EventList}
 * to an alternate value.
 *
 * <p>This can be used to wrap the elements of an {@link EventList} to add more
 * data. For example, if the elements of a {@link EventList} represent a model,
 * a {@link SwapList} can be used to add view and controller objects.
 *
 * <p>Although this list supports operations like {@link #add(Object) add()} and
 * {@link #set(int,Object) set()}, these methods will simply be called on the
 * source {@link EventList} without adjusting the paramter values. In effect,
 * the method {@link SwapList#add(Object) swapList.add()} is equivalent to
 * {@link EventList#add(Object) source.add()}.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(1), writes O(1), source changes O(C*N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>4 bytes per element</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class SwapList extends TransformedList {

    /** the swapped in objects */
    private ArrayList alternates = new ArrayList();
    
    /** used to map objects to their alternates */
    private AlternateFinder alternateFinder;
    
    /**
     * Creates a {@link SwapList} that contains alternate elements for the specified
     * source {@link EventList}.
     */
    public SwapList(EventList source, AlternateFinder alternateFinder) {
        super(source);
        this.alternateFinder = alternateFinder;
        
        // populate the initial set of alternates
        for(Iterator i = source.iterator(); i.hasNext(); ) {
            alternates.add(alternateFinder.createAlternate(i.next()));
        }

        // handle changes to the source list
        source.addListEventListener(this);
    }
    
    /** {@inheritDoc} */
    public final Object get(int index) {
        return alternates.get(index);
    }
    
    /** {@inheritDoc} */
    public final int size() {
        return alternates.size();
    }
    
    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }
    
    /** {@inheritDoc} */
    public final void listChanged(ListEvent listChanges) {
        // update the alternates
        while(listChanges.next()) {
            int index = listChanges.getIndex();
            int type = listChanges.getType();

            if(type == ListEvent.INSERT) {
                Object alternate = alternateFinder.createAlternate(source.get(index));
                alternates.add(index, alternate);
                
            } else if(type == ListEvent.DELETE) {
                Object alternate = alternates.remove(index);
                alternateFinder.deleteAlternate(alternate);

            } else if(type == ListEvent.UPDATE) {
                Object previous = alternates.get(index);
                Object updated = alternateFinder.updateAlternate(source.get(index), previous);
                alternates.set(index, updated);
            }
        }

        // pass on the changes
        listChanges.reset();
        updates.forwardEvent(listChanges);
    }
}
