/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

/**
 * A SubscribedSource listens to changes on the wire and publishes them as update
 * events.
 */ 
class SubscribedSource implements UpdateSource {
    
    /** the host publishing this source */
    private String publishingHost = null;
    
    /** the source on the published host to subscribe to */
    private String sourceName = null;
    
    /**
     * Creates a new SubscribedSource that connects to the specified list and
     * provides its updates locally.
     */
    public SubscribedSource(String uri) {
        // break up the uri
        String[] parts = uri.split("/");
        if(parts.length != 4
            || !parts[0].equalsIgnoreCase("usp")
            || !parts[1].equalsIgnoreCase("")) {
            throw new IllegalArgumentException("uri must be of the form usp://host/sourcename");
        }

        publishingHost = parts[2];
        sourceName = parts[3];
    }
    
    /**
     * Registers the specified listener to receive update data as it
     * is produced. The specified listener will immediately receive a
     * call to updateStatus() and will continue to receive such calls
     * as they occur or until the listener is removed.
     */
    public void addUpdateListener(UpdateListener listener) {
         // this listener gets to hear update events that come in on the wire
    }

    /**
     * Removes the specified listener to prevent that listener from
     * receiving update events.
     */
    public void removeUpdateListener(UpdateListener listener) {
        // remove the listener
    }
}

