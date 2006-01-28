/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.rbp;

// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;

/**
 * Tracks the current status of a resource with respect to the network.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface ResourceStatus {

    /**
     * Returns true if this resource is actively being updated by the network.
     */
    public boolean isConnected();
    
    /**
     * Forces this resource to attempt to connect. The results from the attempt
     * will not be visible immediately.
     */
    public void connect();
    
    /**
     * Forces this resource to attempt to disconnect. This will prevent the resource
     * from consuming network resources.
     */
    public void disconnect();
    
    /**
     * Registers the specified listener to receive events about the status of this
     * resource.
     */
    public void addResourceStatusListener(ResourceStatusListener listener);
    
    /**
     * Deregisters the specified listener from receiving events about the status of
     * this resource.
     */
    public void removeResourceStatusListener(ResourceStatusListener listener);
}
