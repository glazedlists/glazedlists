/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.nio;

// NIO is used for CTP
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
// logging
import java.util.logging.*;

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
