/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.gui.ThreadProxyEventList;

import java.awt.EventQueue;

/**
 * Proxies events from all threads to the Swing event dispatch thread. This allows
 * any thread to write a source {@link EventList} that will be updated on the
 * Swing thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SwingThreadProxyEventList<E> extends ThreadProxyEventList<E> {

    /**
     * Create a {@link SwingThreadProxyEventList} that mirrors the specified source
     * {@link EventList} for access on the Swing thread.
     */
    public SwingThreadProxyEventList(EventList<E> source) {
        super(source);
    }

    /**
     * Schedule the specified runnable to be run on the proxied thread.
     */
    @Override
    protected void schedule(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            EventQueue.invokeLater(runnable);
        }
    }
}