/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.rbp;

// NIO is used for BRP
import ca.odell.glazedlists.impl.io.Bufferlo;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

/**
 * A resource is a dynamic Object that can publish its changes as a series of deltas.
 * It is also possible to construct a resource using a shapshot.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface Resource {

    /**
     * Get a binary snapshot of this resource in its current state.
     */
    public Bufferlo toSnapshot();

    /**
     * Populate this resource with the data from the specified snapshot.
     */
    public void fromSnapshot(Bufferlo snapshot);

    /**
     * Apply the specified delta to the binary image of this resource. After the
     * update has been applied, all {@link ResourceListener}s must be notified.
     */
    public void update(Bufferlo delta);

    /**
     * Register the {@link ResourceListener} to receive notification when this
     * resource is modified.
     */
    public void addResourceListener(ResourceListener listener);

    /**
     * Degregister the {@link ResourceListener} from receiving update events.
     */
    public void removeResourceListener(ResourceListener listener);

    /**
     * Gets the lock required to share this resource between multiple threads.
     *
     * @return a re-entrant {@link ReadWriteLock} that guarantees thread safe
     *      access to this list.
     */
    public ReadWriteLock getReadWriteLock();
}
