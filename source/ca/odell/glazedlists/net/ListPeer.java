/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.io.*;
import ca.odell.glazedlists.event.*;
// access to the volatile implementation pacakge
import ca.odell.glazedlists.impl.rbp.*;
import ca.odell.glazedlists.impl.io.*;
// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;

/**
 * A {@link ListPeer} provides functions to publish and subscribe to EventLists.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListPeer {
    
    /** the peer manages the actual resources */
    private Peer peer;
    
    /**
     * Creates a new ListPeer that binds to the specified port.
     */
    public ListPeer(int listenPort) {
        this.peer = new Peer(listenPort);
    }
    
    /**
     * Starts the peer.
     */
    public void start() throws IOException {
        peer.start();
    }
    
    /**
     * Stops the peer.
     */
    public void stop() {
        peer.stop();
    }
    
    /**
     * Publish the specified EventList with the specified name.
     */
    public NetworkList publish(EventList source, String resourceName, ByteCoder byteCoder) {
        NetworkList published = new NetworkList(source, byteCoder);
        ResourceStatus resourceStatus = peer.publish(published, resourceName);
        published.setResourceStatus(resourceStatus);
        published.setWritable(true);
        return published;
    }
    
    /**
     * Subscribe to the EventList with the specified name.
     */
    public NetworkList subscribe(String resourceName, String host, int port, ByteCoder byteCoder) {
        NetworkList subscribed = new NetworkList(new BasicEventList(), byteCoder);
        ResourceStatus resourceStatus = peer.subscribe(subscribed, resourceName, host, port);
        subscribed.setResourceStatus(resourceStatus);
        return subscribed;
    }
}
