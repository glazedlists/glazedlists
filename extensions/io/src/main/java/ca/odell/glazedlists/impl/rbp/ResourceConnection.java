/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.rbp;

// NIO is used for BRP
/**
 * Maintains the state for a particular resource on a particular connection.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 *
 * @deprecated The io extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@Deprecated
class ResourceConnection {

    /** the connection */
    private PeerConnection connection;

    /** the resource */
    private PeerResource resource;

    /** the resource's current update */
    private int updateId = -1;

    /**
     * Create a new {@link ResourceConnection} to manage the state of the specified
     * connection and resource.
     */
    public ResourceConnection(PeerConnection connection, PeerResource resource) {
        this.connection = connection;
        this.resource = resource;
    }

    /**
     * The current update of this resource connection.
     */
    public void setUpdateId(int updateId) {
        this.updateId = updateId;
    }
    public int getUpdateId() {
        return updateId;
    }

    /**
     * Gets the connection that is interested in this resource.
     */
    public PeerConnection getConnection() {
        return connection;
    }

    /**
     * Gets the resource that is attached to this connection.
     */
    public PeerResource getResource() {
        return resource;
    }
}
