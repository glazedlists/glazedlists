/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.nio;

// NIO is used for CTP
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * A task that gracefully shuts down the NIO daemon.
 */
class Shutdown implements Runnable {

    /** logging */
    private static Logger logger = Logger.getLogger(Shutdown.class.toString());

    /** the I/O event queue daemon */
    private NIODaemon nioDaemon = null;

    /**
     * Create a new NIOShutdown that shuts down a server using the specified
     * NIODaemon.
     */
    public Shutdown(NIODaemon nioDaemon) {
        this.nioDaemon = nioDaemon;
    }

    /**
     * Runs the specified task.
     */
    @Override
    public void run() {
        logger.info("Cleaning up listening socket and closing " + (nioDaemon.getSelector().keys().size()-1) + " connections");

        // kill all connections
        for(Iterator k = nioDaemon.getSelector().keys().iterator(); k.hasNext(); ) {
            SelectionKey key = (SelectionKey)k.next();

            // close an invalid connection
            if(!key.isValid()) {
                NIOAttachment attachment = (NIOAttachment)key.attachment();
                attachment.close(new IOException("Connection closed"));

            // close the server socket
            } else if((key.interestOps() & SelectionKey.OP_ACCEPT) != 0) {
                try {
                    ServerSocketChannel server = (ServerSocketChannel)key.channel();
                    server.close();
                    key.cancel();
                } catch(IOException e) {
                    logger.warning("Error closing server socket, " + e.getMessage());
                }

            // close a connection socket
            } else {
                NIOAttachment attachment = (NIOAttachment)key.attachment();
                attachment.close(new ServerShutdownException());
            }
        }
    }
}
