/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;

/**
 * Listens to the current status of a {@link NetworkList} with respect to the network.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SimpleNetworkListStatusListener implements NetworkListStatusListener {
    
    /** whether the list is connected */
    private boolean connected = false;

    /** why its disconnected */
    private Exception reason = null;

    /**
     * Create a new {@link SimpleNetworkListStatusListener} that listens for events
     * from the specified {@link NetworkList}.
     */
    public SimpleNetworkListStatusListener(NetworkList target) {
        target.addStatusListener(this);
        connected = target.isConnected();
    }
    
    /**
     * Called each time a resource becomes connected.
     */
    public void connected(NetworkList list) {
        connected = true;
    }
    
    /**
     * Called each time a resource's disconnected status changes. This method may
     * be called for each attempt it makes to reconnect to the network.
     */
    public void disconnected(NetworkList list, Exception reason) {
        connected = false;
        this.reason = reason;
    }
    
    /**
     * Get whether the NetworkList reported a connection.
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Get why the NetworkList disconnected.
     */
    public Exception getDisconnectReason() {
        return reason;
    }
}
