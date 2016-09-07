/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.nio;

// NIO is used for CTP
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Handles incoming NIO connections.
 */
public interface NIOServer {

    /**
     * Handle an accept-ready selection key.
     */
    public void handleAccept(SelectionKey selectionKey, Selector selector);

}
