/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

import java.util.*;

/**
 * A USPPeer is a peer in an update subscription protocol network. Each
 * peer may publish lists which they have access to or subscribe to the
 * lists of a remote peer.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class USPPeer {
    
    /** the currently open peers, by their connections */
    private Map peers = new HashMap();
    
    /** the PublishedSources by their source names */
    private Map published = new HashMap();
    
    /**
     * Creates a new USPPeer that listends on the specified local TCP/IP
     * port.
     *
     * The implementation of this shall do the following:
     * 1. Bind to the specified listen port or throw a USPException if
     *    such a port is unavailable.
     * 2. Start a handler thread to handle connections from the listen
     *    port. As connections are received, published lists are made
     *    available to remote clients.
     */
    public USPPeer(int listenPort) throws USPException {
        
        // 1 bind to listen port
        
        // 2 create handler thread
        
        // 3 start handler thread
    }
    
    /**
     * Publishes the specified UpdateSource at the specified address.
     *
     * @param sourceName a local address that may start with a slash character
     *      such as "/contacts" or "" for the default source.
     */
    public void publish(UpdateSource source, String sourceName) {
        PublishedSource publishedSource = new PublishedSource(source, sourceName);
        published.put(sourceName, publishedSource);
    }
    
    /**
     * Subscribes to UpdateSource at the specified address.
     *
     * @param uri a remote address that starts with the "usp://"
     *      protocol identifier. For a list called "contacts" on a
     *      computer named "odell.ca", use "usp://odell.ca/contacts".
     */
    public UpdateSource subscribe(String uri) {
        return new SubscribedSource(uri);
    }
}


