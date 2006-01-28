/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

// standard java event and observer
import java.util.*;

/**
 * Listens and responds to changes in a dynamic list of objects. This could be
 * implemented by a GUI widget such as a table or combo box to repaint, add, or
 * remove elements when the underlying data changes.
 *
 * <p>When a thread requires notification on the Swing thread for GUI display, the
 * user should not add the implementation of this interface as a listener
 * directly. Instead use a EventThreadProxy, which receives
 * events on the list thread and then fires them on the Swing thread.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial/">Glazed Lists Tutorial</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface ListEventListener<E> extends EventListener {

    /**
     * When the underlying list changes, this notification allows the
     * object to repaint itself or update itself as necessary.
     *
     * <p>It is mandatory that the calling thread has obtained the write lock
     * on the source list. This is because the calling thread will have written
     * to the source list to cause this event. This condition guarantees that
     * no writes can occur while the listener is handling this event.
     * It is an error to write to the source list while processing an event.
     */
    public void listChanged(ListEvent<E> listChanges);
}