/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// for maps of headers
import java.util.*;


/**
 * A USPPeer is a peer in an update subscription protocol network. Each
 * peer may publish lists which they have access to or subscribe to the
 * lists of a remote peer.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class USPPeer {
    
    /** although updates have unique USP uris, they share the same HTTP URI */ 
    private static final String HTTP_URI = "/";
    
    /** the currently open peers, by their connections */
    private Map peers = new HashMap();
    
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
     * @param uri a local address that may start with a slash character
     *      such as "/contacts" or "" for the default source.
     */
    public void publish(UpdateSource source, String uri) {
    }
    
    /**
     * Subscribes to UpdateSource at the specified address.
     *
     * @param uri a remote address that starts with the "usp://"
     *      protocol identifier. For a list called "contacts" on a
     *      computer named "odell.ca", use "usp://odell.ca/contacts".
     */
    public UpdateSource subscribe(String uri) throws USPException {
        return null;
    }
    
    
    /**
     * The USPPeerCTPHandler handles messages from the CTPProtocol to the
     * USPPeer. This is where all incoming network data comes in.
     */
    class USPPeerCTPHandler implements CTPHandler {
        
        /**
         * Handles an HTTP request or response from the specified connection.
         */
        public void receiveConnect(CTPProtocol source, Integer code, String uri, Map headers) {
            // this is a request for a local source
            if(uri != null) {
                // throw out this connection if possible
                CTPServerProtocol serverProtocol = (CTPServerProtocol)source;
                if(!uri.equals(HTTP_URI)) {
                    try {
                        serverProtocol.sendResponse(NOT_FOUND, null);
                    } catch(CTPException e) {
                        serverProtocol.close();
                    }
                    return;
                }
                
                // we have a new subscription requesting peer
                PeerConnection peerConnection = new PeerConnection(serverProtocol);
                try {
                    serverProtocol.sendResponse(OK, null);
                } catch(CTPException e) {
                    serverProtocol.close();
                    return;
                }
                peers.put(serverProtocol, peerConnection);
                System.out.println("Received request from " + source);
                
            // this is a response for a remote source
            } else if(code != null) {
                // close this connection if it has failed
                CTPClientProtocol clientProtocol = (CTPClientProtocol)source;
                if(code.intValue() != OK) {
                    PeerConnection peerConnection = (PeerConnection)peers.get(clientProtocol);
                    peerConnection.updateStatus(UpdateListener.SERVICE_UNAVAILABLE, null);
                    clientProtocol.close();
                    peers.remove(clientProtocol);
                }
                
                // we have successfully connected to the peer
                System.out.println("Received response from " + source);
            }
        }
    
        /**
         * Handles reception of the specified chunk of data.
         */
        public void receiveChunk(CTPProtocol source, byte[] data) {
            
            
        }
    
        /**
         * Handles the connection being closed by the remote client.
         */
        public void connectionClosed(CTPProtocol source, Exception reason) {
            
            
        }
    }
}

/**
 * Models a single connection to a specified peer.
 */
class PeerConnection {
    
    /** our connection to the peer */
    private CTPProtocol peer;
    /** the source lists this peer is publishing that we are subscribing to */
    private List peerSources = new ArrayList();
    /** the update listeners this peer is interested in receiving updates from */
    private List listenersToPeer = new ArrayList();

    /**
     * Creates a new peer connection to the specified client.
     */
    public PeerConnection(CTPProtocol peer) {
        this.peer = peer;
    }
    
    /**
     * Closes the connection with this peer. This notifies all local listeners
     * subscribed to lists from this peer that it is no longer available.
     */
    public void updateStatus(int status, Exception reason) {
        for(Iterator i = peerSources.iterator(); i.hasNext(); ) {
            BasicUpdateSource source = (BasicUpdateSource)i.next();
            source.updateStatus(status, reason);
        }
    }
}
    
/**
 * A simple implementation of update source that allows another class
 * to call the update() and updateStatus() methods.
 */
class BasicUpdateSource implements UpdateSource {
    
    /** the list of listeners to this update source */
    private List listeners = new ArrayList();
    
    /**
     * Registers the specified listener to receive update data as it
     * is produced. The specified listener will immediately receive a
     * call to updateStatus() and will continue to receive such calls
     * as they occur or until the listener is removed.
     */
    public void addUpdateListener(UpdateListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the specified listener to prevent that listener from
     * receiving update events.
     */
    public void removeUpdateListener(UpdateListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifies all listeners of this peer about the specified update.
     */
    public void update(byte[] data) {
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            UpdateListener listener = (UpdateListener)i.next();
            listener.update(this, data);
        }
    }
    
    /**
     * Notifies all listeners of this peer about the specified status change.
     */
    public void updateStatus(int status, Exception reason) {
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            UpdateListener listener = (UpdateListener)i.next();
            listener.updateStatus(this, status, reason);
        }
    }
}

/**
 * An UpdateSource is a producer for update events.
 */ 
interface UpdateSource {
    
    /**
     * Registers the specified listener to receive update data as it
     * is produced. The specified listener will immediately receive a
     * call to updateStatus() and will continue to receive such calls
     * as they occur or until the listener is removed.
     */
    public void addUpdateListener(UpdateListener listener);
    /**
     * Removes the specified listener to prevent that listener from
     * receiving update events.
     */
    public void removeUpdateListener(UpdateListener listener);
}

/**
 * An UpdateListener is a consumer for update events.
 */
interface UpdateListener {

    /** updates are being delivered normally */
    public static final int OK = 200; 
    /** updates are stalled due to a timeout, reconnection not necessary */
    public static final int REQUEST_TIMEOUT = 408; 
    /** updates are stalled due to service unavailable, check network */
    public static final int SERVICE_UNAVAILABLE = 503; 
    
    /**
     * Handler for update data arriving.
     */
    public void update(UpdateSource source, byte[] data);

    /**
     * Handler for the status of the update link. Usually broken links
     * will be automatically repaired if possible. The client is notified
     * of the status in order to warn of possible delays.
     */
    public void updateStatus(UpdateSource source, int status, Exception reason);
}

/**
 * A USPException is thrown when there is an error subscribing to an update
 * source.
 */
class USPException extends Exception {
    public USPException(String message) {
        super(message);
    }
    public USPException(Exception cause) {
        super(cause);
    }
}
