/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

import java.util.*;

/**
 * The PublishedSource listens to changes in the published lists and
 * forwards those changes on the wire.
 */
class PublishedSource implements UpdateListener {

    /** the source of updates being published */
    private UpdateSource source;
    
    /** the name to publish this source under */
    private String sourceName;
    
    /** subscribering PeerConnections */
    private List subscribers;
    
    
    /**
     * Create a new PublishedSource that publishes the specified source
     * on the specified uri.
     */
    public PublishedSource(UpdateSource source, String sourceName) {
        this.source = source;
        this.sourceName = sourceName;
        
        // listen
        source.addUpdateListener(this);
    }
    
    /**
     * Subscribes the specified peer connection to receive updates as they
     * occur.
     */
    public void addSubscriber(PeerConnection peerConnection) {
        subscribers.add(peerConnection);
    }
    
    /**
     * Handler for update data arriving.
     */
    public void update(UpdateSource source, byte[] data) {
         // forward this event to all subscribers
    }

    /**
     * Handler for the status of the update link. Usually broken links
     * will be automatically repaired if possible. The client is notified
     * of the status in order to warn of possible delays.
     */
    public void updateStatus(UpdateSource source, int status, Exception reason) {
         // if this list breaks, notify all subscribers
    }
}
