/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

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

