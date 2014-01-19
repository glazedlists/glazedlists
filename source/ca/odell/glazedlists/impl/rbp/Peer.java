/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.rbp;

// NIO is used for BRP
import ca.odell.glazedlists.impl.ctp.CTPConnectionManager;
import ca.odell.glazedlists.impl.ctp.CTPHandler;
import ca.odell.glazedlists.impl.ctp.CTPHandlerFactory;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * A peer manages publishing and subscribing to resources.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Peer implements CTPHandlerFactory {
    
    /** logging */
    private static Logger logger = Logger.getLogger(Peer.class.toString());

    /** the resources being subscribed to */ 
    Map subscribed = new TreeMap();
    
    /** the resources being published */
    Map published = new TreeMap();
     
    /** the active connections to peers */
    List connections = new ArrayList();
    
    /** the connection management */
    private CTPConnectionManager connectionManager;
    
    /**
     * Creates a new peer that binds to the specified port.
     */
    public Peer(int listenPort) {
        this.connectionManager = new CTPConnectionManager(this, listenPort);
    }
    
    /**
     * Upon a connect, a CTPHandler is required to handle the data of this connection.
     * The returned CTPHandler will be delegated to handle the connection's data.
     */
    @Override
    public CTPHandler constructHandler() {
        PeerConnection incoming = new PeerConnection(this);
        connections.add(incoming);
        return incoming;
    }

    /**
     * Starts the peer.
     */
    public void start() throws IOException {
        connectionManager.start();
    }
    
    /**
     * Stops the peer.
     */
    public void stop() {
        connectionManager.getNIODaemon().invokeAndWait(new StopRunnable());
    }
    private class StopRunnable implements Runnable {
        @Override
        public void run() {
            // unsubscribe from everything
            for(Iterator s = subscribed.values().iterator(); s.hasNext(); ) {
                PeerResource resource = (PeerResource)s.next();
                resource.status().disconnect();
            }
            subscribed.clear();
            
            // unpublish everything
            logger.warning("Closing with published entries");
            
            // close all connections
            List connectionsToClose = new ArrayList();
            connectionsToClose.addAll(connections);
            for(Iterator c = connectionsToClose.iterator(); c.hasNext(); ) {
                PeerConnection connection = (PeerConnection)c.next();
                connection.close();
            }
            
            // stop the connection manager
            connectionManager.stop();
        }
    }
    
    /**
     * Prints the current state of this peer.
     */
    public void print() {
        System.out.println(" --------  --------  --------  --------  --------  --------  --------  -------- ");
        System.out.println("Subscribed Resources:");
        for(Iterator s = subscribed.values().iterator(); s.hasNext(); ) {
            PeerResource resource = (PeerResource)s.next();
            resource.print();
        }
        System.out.println("");
        System.out.println("Published Resources:");
        for(Iterator s = published.values().iterator(); s.hasNext(); ) {
            PeerResource resource = (PeerResource)s.next();
            resource.print();
        }
        System.out.println("");
        System.out.println("Connections:");
        for(Iterator s = connections.iterator(); s.hasNext(); ) {
            PeerConnection connection = (PeerConnection)s.next();
            connection.print();
        }
        System.out.println("");
    }
    
    /**
     * Subscribe to the specified resource.
     */
    public ResourceStatus subscribe(Resource resource, String host, int port, String path) {
        PeerResource peerResource = new PeerResource(this, resource, ResourceUri.remote(host, port, path));
        return peerResource.status();
    }
    
    /**
     * Publish the specified resource.
     */
    public ResourceStatus publish(Resource resource, String path) {
        PeerResource peerResource = new PeerResource(this, resource, ResourceUri.local(path));
        return peerResource.status();
    }
    
     /**
      * Gets the specified published resource.
      */
     PeerResource getPublishedResource(ResourceUri resourceUri) {
         return (PeerResource)published.get(resourceUri);
     }
     
     /**
      * Gets the specified connection, or creates it if necessary.
      */
     PeerConnection getConnection(String host, int port) {
         PeerConnection peerConnection = new PeerConnection(this);
         connectionManager.connect(peerConnection, host, port);
         connections.add(peerConnection);
         return peerConnection;
     }
     
     /**
      * Runs the specified task on the network thread.
      */
     void invokeLater(Runnable runnable) {
         connectionManager.getNIODaemon().invokeLater(runnable);
     }
}
