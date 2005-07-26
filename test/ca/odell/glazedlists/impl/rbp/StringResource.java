/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.rbp;

import ca.odell.glazedlists.impl.io.Bufferlo;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple resource for a String.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class StringResource implements Resource {

    /** the read/write lock provides mutual exclusion to access */
    private ReadWriteLock readWriteLock = LockFactory.createReadWriteLock();
    
    /** the value of this resource */
    private String value = "";
    
    /** the listeners to this resource */
    private List listeners = new ArrayList();
    
    /**
     * Get a binary snapshot of this resource in its current state.
     */
    public Bufferlo toSnapshot() {
        Bufferlo result = new Bufferlo();
        result.write(value);
        return result;
    }
    
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
        notifyListeners();
    }
    
    /**
     * Populate this resource with the data from the specified snapshot.
     */
    public void fromSnapshot(Bufferlo snapshot) {
        value = snapshot.toString();
        notifyListeners();
    }
    
    /**
     * Apply the specified delta to the binary image of this resource. After the
     * update has been applied, all {@link ResourceListener}s must be notified.
     */
    public void update(Bufferlo delta) {
        fromSnapshot(delta);
    }
    
    /**
     * Register the {@link ResourceListener} to receive notification when this
     * resource is modified.
     */
     public void addResourceListener(ResourceListener listener) {
         listeners.add(listener);
     }
    
    /**
     * Degregister the {@link ResourceListener} from receiving update events.
     */
    public void removeResourceListener(ResourceListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Gets the lock required to share this resource between multiple threads.
     *
     * @return a re-entrant {@link ReadWriteLock} that guarantees thread safe
     *      access to this list.
     */
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }
    
    /**
     * Notify listeners that the value of this String has changed.
     */
    private void notifyListeners() {
        for(int i = 0; i < listeners.size(); i++) {
            ResourceListener listener = (ResourceListener)listeners.get(i);
            listener.resourceUpdated(this, toSnapshot());
        }
    }
}
