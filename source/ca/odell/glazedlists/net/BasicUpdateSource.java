/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

import java.util.*;

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
    public void fireUpdate(byte[] data) {
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            UpdateListener listener = (UpdateListener)i.next();
            listener.update(this, data);
        }
    }
    
    /**
     * Notifies all listeners of this peer about the specified status change.
     */
    public void fireUpdateStatus(int status, Exception reason) {
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            UpdateListener listener = (UpdateListener)i.next();
            listener.updateStatus(this, status, reason);
        }
    }
}
