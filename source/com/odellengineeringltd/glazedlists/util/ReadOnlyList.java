/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
// For calling methods on the event dispacher thread
import javax.swing.SwingUtilities;


/**
 * A ReadOnlyList is a mutation list that throws an exception for
 * each method that modifies the list.
 *
 * <p>This is useful only when programming defensively. For example, if you
 * need to supply read-only access to your List to another class, using a
 * ReadOnlyList will guarantee that such a list will not be able to
 * modify the list. The ReadOnlyList will still provide an up-to-date
 * view of the list that changes with each update to the list. For a static
 * read only list, simply copy the contents of this list into an ArrayList and
 * use that list.
 *
 * @see com.odellengineeringltd.glazedlists.WritableMutationList
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class ReadOnlyList extends WritableMutationList implements ListEventListener, EventList {

    /**
     * Creates a new ReadOnlyList that is a read only view of the
     * specified list.
     */
    protected ReadOnlyList(EventList source) {
        super(source);
        source.addListEventListener(this);
    }
    
    /**
     * For implementing the ListEventListener interface. When the underlying list
     * changes, this sends notification to listening lists.
     */
    public void listChanged(ListEvent listChanges) {
        // just pass on the changes
        updates.beginAtomicChange();
        while(listChanges.next()) {
            updates.appendChange(listChanges.getIndex(), listChanges.getType());
        }
        updates.commitAtomicChange();
    }

    /**
     * Gets the specified element from the list.
     */
    public Object get(int index) {
        return source.get(index);
    }
    
    /**
     * Gets the total number of elements in the list.
     */
    public int size() {
        return source.size();
    }

    /**
     * Gets the index into the source list for the object with the specified
     * index in this list.
     */
    protected int getSourceIndex(int mutationIndex) {
        return mutationIndex;
    }
    
    /**
     * Tests if this mutation shall accept calls to <code>add()</code>,
     * <code>remove()</code>, <code>set()</code> etc.
     */
    protected boolean isWritable() {
        return false;
    }
}
