/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists;

// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.SortedSet;
// For calling methods on the event dispacher thread
import javax.swing.SwingUtilities;


/**
 * An event list is a list that can send events to listeners.
 * An event list may be implemented as a filtering list or for
 * multiple displays at the same data.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface EventList extends List {

    /**
     * Registers the specified listener to receive change updates for this list.
     */
    public void addListChangeListener(ListChangeListener listChangeListener);

    /**
     * Gets the source list that this list depends on. This may return the same
     * object, or another object. This is useful for synchronization of chained
     * lists, so that dependent lists can be synchronized on the root list to
     * prevent deadlocks.
     */
    public EventList getRootList();
}
