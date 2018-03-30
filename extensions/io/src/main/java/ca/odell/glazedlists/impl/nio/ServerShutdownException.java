/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.nio;

// NIO is used for CTP
/**
 * The reason a connection is closed when the server is shutdown.
 *
 * @deprecated The io extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@Deprecated
class ServerShutdownException extends Exception {

    /**
     * Creates a new ServerShutdownException.
     */
    public ServerShutdownException() {
        super("Server shutting down");
    }
}
