/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains a repository of strategies for dealing with Exceptions
 * which occur any where in the application. Clients may register Exception
 * handlers to deal with these exception in any way they see fit. If no
 * registered Exception handler recognizes an exception that has been raised
 * it is ignored by default.
 *
 * @author James Lemieux
 */
public final class Exceptions {

    /**
     * The interface Exception handlers must implement. It has two methods to
     * containing the logic for each of its concerns: recognizing Exceptions
     * it can handle and handling those Exceptions appropriately.
     */
    public interface Handler {
        /**
         * Return <tt>true</tt> if this Handler can work with the given
         * Exception; <tt>false</tt> otherwise.
         */
        public boolean recognize(Exception e);

        /**
         * React to the given Exception in any way seen fit. This may include
         * notifying the user, writing to a log, or any other valid logic.
         */
        public void handle(Exception e);
    }

    /** The one instance of the Exceptions class that is allowed. */
    private static final Exceptions singleton = new Exceptions();

    /**
     * Returns a handle to the single instance of the Exceptions class which is
     * allowed to exist.
     */
    public static Exceptions getInstance() {
        return singleton;
    }

    /**
     * A list of handlers which handle Exceptions of a global nature.
     */
    private final List<Handler> handlers = new ArrayList<Handler>();

    /**
     * Add <code>h</code> to the collection of {@link Handler}s consulted when
     * an Exception is raised from within the application.
     */
    public void addHandler(Handler h) {
        handlers.add(h);
    }

    /**
     * Remove <code>h</code> from the collection of {@link Handler}s consulted when
     * an Exception is raised from within the application.
     */
    public void removeHandler(Handler h) {
        handlers.remove(h);
    }

    /**
     * Attempt to locate a {@link Handler} which
     * {@link Handler#recognize recognizes} the given Exception and give it a
     * chance to {@link Handler#handle handle} it.
     */
    public void handle(Exception e) {
        for (Iterator<Handler> i = handlers.iterator(); i.hasNext();) {
            Handler handler = i.next();
            if (handler.recognize(e)) {
                handler.handle(e);
                return;
            }
        }
    }
}