/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.event;

// for calling the notification on the Swing thread
import javax.swing.SwingUtilities;

/**
 * This class is a proxy to another ListChangeListener that always uses
 * the Java event dispatch thread. This allows thread unsafe classes to
 * guarantee that list events arrive on the safe thread.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListChangeListenerEventThreadProxy implements ListChangeListener, Runnable {

    /** the list change listener who actually wants notification */
    private ListChangeListener proxyTarget;
    /** the change event to notify with */
    private ListChangeEvent listChanges;
    
    /**
     * Creates a new ListChangeListenerEventThreadProxy for the specified
     * listener.
     */
    public ListChangeListenerEventThreadProxy(ListChangeListener proxyTarget) {
        this.proxyTarget = proxyTarget;
    }

    /**
     * Accepts notification for changes and passes them on. If the calling
     * thread is the Swing thread, the notification will happen immediately
     * and this method will not return until notification is complete. If the
     * calling thread is a different thread, the notification will be queued
     * on the Swing thread's todo queue.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        this.listChanges = listChanges;
        if(SwingUtilities.isEventDispatchThread()) {
            proxyTarget.notifyListChanges(listChanges);
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    /**
     * When run, this simply notifies its target about the changes.
     */
    public void run() {
        proxyTarget.notifyListChanges(listChanges);
    }
}

