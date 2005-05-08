/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
// access to the volatile implementation pacakge
import ca.odell.glazedlists.impl.rbp.*;
import java.io.*;

/**
 * A {@link ListPeer} manages the network resources for publishing and subscribing
 * to {@link NetworkList}s.
 *
 * <p>A {@link ListPeer} must have its {@link #start()} method complete successfully
 * before it can be used to {@link #publish(EventList,String,ByteCoder) publish()} and
 * {@link #subscribe(String,int,String,ByteCoder) subscribe()} {@link EventList}s.
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
     * Starts the peer. This binds to the listen port and allows connections to
     * be sent and received.
     *
     * @throws IOException if the listen port cannot be binded to. This will be due
     *      to the port being in use or as a consequence of insufficient privileges.
     */
    public void start() throws IOException {
        peer.start();
    }

    /**
     * Stops the peer. This disconnects all active {@link EventList}s and releases
     * the listen port.
     */
    public void stop() {
        peer.stop();
    }

    /**
     * Prints the full state of this ListPeer.
     */
    void print() {
        peer.print();
    }

    /**
     * Publish the specified EventList with the specified name.
     *
     * @param source the {@link EventList} to publish. Each change to this {@link EventList}
     *      will be immediately published to all subscribers.
     * @param path the address that the {@link EventList} shall be published under.
     *      The path must start with a slash character. This must be unique among
     *      all {@link EventList}s published on this {@link ListPeer}.
     * @param byteCoder a helper that can convert the elements of the {@link EventList}
     *      into binary network-transmittable form. Some general purpose {@link ByteCoder}s
     *      are available in the {@link ca.odell.glazedlists.GlazedLists GlazedLists}
     *      factory class.
     * @return a simple decorator of the published {@link EventList}
     *      with additional methods to bring the list offline. This list is writable.
     */
    public NetworkList publish(EventList source, String path, ByteCoder byteCoder) {
        NetworkList published = new NetworkList(source, byteCoder);
        ResourceStatus resourceStatus = peer.publish(published.getResource(), path);
        published.setResourceStatus(resourceStatus);
        published.setWritable(true);
        return published;
    }

    /**
     * Subscribe to the EventList with the specified name.
     *
     * @param host the network hostname of the machine serving the original copy
     *      of the data of interest. Together with the port, this should map
     *      to a proper {@link java.net.InetSocketAddress InetSocketAddress}.
     * @param port the port the {@link EventList} of interest is being published on.
     * @param path the address that the {@link EventList} is published under.
     * @param byteCoder a helper that can convert the binary data from the network
     *      into list elements for this {@link EventList}. Some general purpose {@link ByteCoder}s
     *      are available in the {@link ca.odell.glazedlists.GlazedLists GlazedLists}
     *      factory class.
     * @return the {@link EventList} that gets its data from the specified remote
     *      source. This {@link EventList} will contain no data until the connection
     *      completes. This list is not writable.
     */
    public NetworkList subscribe(String host, int port, String path, ByteCoder byteCoder) {
        NetworkList subscribed = new NetworkList(new BasicEventList(), byteCoder);
        ResourceStatus resourceStatus = peer.subscribe(subscribed.getResource(), host, port, path);
        subscribed.setResourceStatus(resourceStatus);
        return subscribed;
    }
}
