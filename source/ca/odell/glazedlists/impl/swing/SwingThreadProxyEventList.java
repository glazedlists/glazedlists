/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.swing;

// Java collections are used for underlying data storage
import java.util.*;
// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.impl.gui.*;
// for calling the notification on the Swing thread
import javax.swing.SwingUtilities;

/**
 * Proxies events from all threads to the Swing event dispatch thread. This allows
 * any thread to write a source {@link EventList} that will be updated on the
 * Swing thread.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SwingThreadProxyEventList extends ThreadProxyEventList {

    /**
     * Create a {@link SwingThreadProxyEventList} that mirrors the specified source
     * {@link EventList} for access on the Swing thread.
     */
    public SwingThreadProxyEventList(EventList source) {
        super(source);
    }

    /**
     * Schedule the specified runnable to be run on the proxied thread.
     */
    protected void schedule(Runnable runnable) {
        if(SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
}
