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
 * A {@link ListPeer} manages the network resources for publishing and subscribing
 * to {@link NetworkList}s. 
 *
 * <p>A {@link ListPeer} must have its {@link #start()} method complete successfully
 * before it can be used to {@link #publish(EventList,String,ByteCoder) publish()} and
 * {@link #subscribe(String,String,int,ByteCoder) subscribe()} {@link EventList}s.
 *
 * <p>When a {@link ListPeer} is started, it listens for incoming connections on
 * the specified port. When it is stopped, all active {@link NetworkList}s are
 * {@link NetworkList#disconnect() disconnected}, and the port is closed.
 *
 * <p>To bring and individual {@link NetworkList} online or offline, use its
 * {@link NetworkList#disconnect() disconnect()} and {@link NetworkList#connect() connect()}
 * methods.
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
    
    public void print() {
        peer.print();
    }
    
    /**
     * Publish the specified EventList with the specified name.
     */
    public NetworkList publish(EventList source, String resourceName, ByteCoder byteCoder) {
        NetworkList published = new NetworkList(source, byteCoder);
        ResourceStatus resourceStatus = peer.publish(published.getResource(), resourceName);
        published.setResourceStatus(resourceStatus);
        published.setWritable(true);
        return published;
    }
    
    /**
     * Subscribe to the EventList with the specified name.
     */
    public NetworkList subscribe(String resourceName, String host, int port, ByteCoder byteCoder) {
        NetworkList subscribed = new NetworkList(new BasicEventList(), byteCoder);
        ResourceStatus resourceStatus = peer.subscribe(subscribed.getResource(), resourceName, host, port);
        subscribed.setResourceStatus(resourceStatus);
        return subscribed;
    }
}
