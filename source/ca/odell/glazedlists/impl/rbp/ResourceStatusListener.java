/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.rbp;

// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;

/**
 * Listens to the current status of a resource with respect to the network.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface ResourceStatusListener {

    /**
     * Called each time a resource becomes connected.
     */
    public boolean resourceConnected(ResourceStatus resource);
    
    /**
     * Called each time a resource's disconnected status changes. This method may
     * be called for each attempt it makes to reconnect to the network.
     */
    public void resourceDisconnected(ResourceStatus resource, Exception cause);
}
