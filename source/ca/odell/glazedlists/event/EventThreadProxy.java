/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.event;

// for calling the notification on the Swing thread
import javax.swing.SwingUtilities;

/**
 * This class is a proxy to another ListEventListener that always uses
 * the Java event dispatch thread. This allows thread unsafe classes to
 * guarantee that list events arrive on the safe thread.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class EventThreadProxy implements ListEventListener, Runnable {

    /** the list change listener who actually wants notification */
    private ListEventListener proxyTarget;
    /** the change event to notify with */
    private ListEvent listChanges;
    
    /**
     * Creates a new EventThreadProxy for the specified
     * listener.
     */
    public EventThreadProxy(ListEventListener proxyTarget) {
        this.proxyTarget = proxyTarget;
    }

    /**
     * Accepts notification for changes and passes them on. If the calling
     * thread is the Swing thread, the notification will happen immediately
     * and this method will not return until notification is complete. If the
     * calling thread is a different thread, the notification will be queued
     * on the Swing thread's todo queue.
     */
    public void listChanged(ListEvent listChanges) {
        this.listChanges = listChanges;
        if(SwingUtilities.isEventDispatchThread()) {
            proxyTarget.listChanged(listChanges);
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    /**
     * When run, this simply notifies its target about the changes.
     */
    public void run() {
        proxyTarget.listChanged(listChanges);
    }
}

