/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

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

