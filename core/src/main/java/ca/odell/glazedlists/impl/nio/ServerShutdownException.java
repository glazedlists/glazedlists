/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.nio;

// NIO is used for CTP
/**
 * The reason a connection is closed when the server is shutdown.
 */
class ServerShutdownException extends Exception {

    /**
     * Creates a new ServerShutdownException.
     */
    public ServerShutdownException() {
        super("Server shutting down");
    }
}
