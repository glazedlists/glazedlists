/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// standard collections
import java.util.*;
// the core Glazed Lists package
import ca.odell.glazedlists.event.*;

/**
 * An {@link EventList} that does not allow writing operations.
 *
 * <p>The {@link ReadOnlyList} is useful for programming defensively. A
 * {@link ReadOnlyList} is useful to supply an unknown class read-only access
 * to your {@link EventList}. 
 *
 * <p>The {@link ReadOnlyList} will provides an up-to-date view of its source
 * {@link EventList} so changes to the source {@link EventList} will still be
 * reflected. For a static copy of any {@link EventList} it is necessary to copy
 * the contents of that {@link EventList} into an {@link ArrayList}.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * @see ca.odell.glazedlists.TransformedList
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class ReadOnlyList extends TransformedList {

    /**
     * Creates a {@link ReadOnlyList} to provide a view of an {@link EventList}
     * that does not allow write operations.
     */
    public ReadOnlyList(EventList source) {
        super(source);
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // just pass on the changes
        updates.forwardEvent(listChanges);
    }
}
