/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// for calling the notification on the SWT thread
import org.eclipse.swt.widgets.Display;

/**
 * This class is a proxy to another ListEventListener that always uses the SWT
 * user interface thread. This allows thread unsafe classes to guarantee that
 * {@link ListEvent}s arrive on the safe thread.
 *
 * @see Display
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class UserInterfaceThreadProxy implements ListEventListener, Runnable {

    /** the list change listener who actually wants notification */
    private ListEventListener proxyTarget;

    /** the change event to notify with */
    private ListEvent listChanges;
    
    /** the display which owns the user interface thread */
    private Display display;
    
    /**
     * Creates a {@link UserInterfaceThreadProxy} for the specified listener on
     * the specified {@link Display}.
     */
    public UserInterfaceThreadProxy(ListEventListener proxyTarget, Display display) {
        this.proxyTarget = proxyTarget;
        this.display = display;
    }

    /**
     * Accepts notification for changes and passes them on. If the calling thread
     * is the Display thread, the notification will happen immediately and this
     * method will not return until notification is complete. If the calling
     * thread is a different thread, the notification will be queued
     * on the Display thread's todo queue.
     */
    public void listChanged(ListEvent listChanges) {
        this.listChanges = listChanges;
        if(display.getThread() == Thread.currentThread()) {
            proxyTarget.listChanged(listChanges);
        } else {
            display.asyncExec(this);
        }
    }

    /**
     * When run, this simply notifies its target about the changes.
     */
    public void run() {
        proxyTarget.listChanged(listChanges);
    }
}
