/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// for event list utilities, iterators and comparators
import ca.odell.glazedlists.util.*;
// volatile implementation support
import ca.odell.glazedlists.util.impl.*;
import ca.odell.glazedlists.util.concurrent.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * An {@link EventList} that shows the unique elements from its source {@link EventList}
 * ordered by the frequency of their appearance.
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
 * <p><font size="5"><strong><font color="#FF0000">Warning:</font></strong> This
 * class is a technology preview and is subject to API changes.</font>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class PopularityList extends TransformedList {

    /** the list of distinct elements */
    private UniqueList uniqueList;
    
    /**
     * @param comparator The {@link Comparator} used to determine equality.
     */
    public PopularityList(EventList source, Comparator uniqueComparator) {
        this(source, new UniqueList(source, uniqueComparator));
    }
    public PopularityList(EventList source) {
        this(source, new UniqueList(source));
    }
    private PopularityList(EventList source, UniqueList uniqueList) {
        super(new SortedList(uniqueList, new PopularityComparator(uniqueList)));
        this.uniqueList = uniqueList;
        uniqueList.setFireCountChangeEvents(true);

        // listen for changes to the source list
        ((SortedList)super.source).addListEventListener(this);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    public void dispose() {
        SortedList sortedSource = (SortedList)source;
        super.dispose();
        sortedSource.dispose();
        uniqueList.dispose();
    }
}

/**
 * Compares objects by their popularity.
 */
class PopularityComparator implements Comparator {
    private UniqueList target;
    public PopularityComparator(UniqueList target) {
        this.target = target;
    }
    public int compare(Object a, Object b) {
        int aCount = target.getCount(a);
        int bCount = target.getCount(b);
        return bCount - aCount;
    }
}