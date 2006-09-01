/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swt;

// Java collections are used for underlying data storage
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.gui.ThreadProxyEventList;
import org.eclipse.swt.widgets.Display;

/**
 * Proxies events from all threads to the SWT event dispatch thread. This allows
 * any thread to write a source {@link EventList} that will be updated on the
 * SWT thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SWTThreadProxyEventList extends ThreadProxyEventList {

    /** the display which owns the user interface thread */
    private Display display;
    
    /**
     * Create a {@link SWTThreadProxyEventList} that mirrors the specified source
     * {@link EventList} for access on the Swing thread.
     */
    public SWTThreadProxyEventList(EventList source, Display display) {
        super(source);
        this.display = display;
    }

    /**
     * Schedule the specified runnable to be run on the proxied thread.
     */
    protected void schedule(Runnable runnable) {
        if(display.getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            display.asyncExec(runnable);
        }
    }
}
