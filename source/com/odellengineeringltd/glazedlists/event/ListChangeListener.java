/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.event;

// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;

/**
 * Listens and responds to changes in a dynamic list of objects. This could be
 * implemented by a GUI widget such as a table or combo box to repaint, add, or
 * remove elements when the underlying data changes.
 *
 * When a thread requires notification on the Swing thread for GUI display, the
 * user should not add the implementation of this interface as a listener
 * directly. Instead use a ListChangeListenerEventThreadProxy, which receives
 * events on the list thread and then fires them on the Swing thread.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface ListChangeListener {

    /**
     * When the underlying list changes, this notification allows the
     * object to repaint itself or update itself as necessary.
     *
     * The receiving class must iterate through all of the changes in
     * the list change event or else the change objects will remain
     * in memory indefinitely. The easiest way to iterate through the
     * changes is in a while loop like this:
     *
     * <tt><pre><code>
     * while(listChanges.next()) {
     *    
     *     // get the current change info
     *    int unsortedIndex = listChanges.getIndex();
     *    int changeType = listChanges.getType();
     *
     *    // handle change with the specified index and type
     * }
     * </code></tt></pre>
     */
    public void notifyListChanges(ListChangeEvent listChanges);
}
