/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

// NIO is used for BRP
import java.util.EventListener;

/**
 * Listens to the current status of a {@link NetworkList}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface NetworkListStatusListener extends EventListener {

    /**
     * Called each time a {@link NetworkList} becomes connected.
     */
    public void connected(NetworkList list);

    /**
     * Called each time a {@link NetworkList}'s connection status changes. This
     * method may be called for each attempt it makes to reconnect to the network.
     */
    public void disconnected(NetworkList list, Exception reason);
}