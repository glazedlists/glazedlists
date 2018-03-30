/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.ctp;

// NIO is used for CTP
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Logger;

/**
 * A task that starts the CTP Connection manager.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 *
 * @deprecated The io extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@Deprecated
class StartServer implements Runnable {

    /** logging */
    private static Logger logger = Logger.getLogger(StartServer.class.toString());

    /** the I/O event queue daemon */
    private CTPConnectionManager connectionManager = null;

    /** port to listen for incoming connections */
    private int listenPort = -1;

    /**
     * Create a new CTPStartUp that starts a server using the specified
     * NIODaemon.
     */
    public StartServer(CTPConnectionManager connectionManager, int listenPort) {
        this.connectionManager = connectionManager;
        this.listenPort = listenPort;
    }

    /**
     * Runs the specified task.
     */
    @Override
    public void run() {
        try {
            // open a channel and bind
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverChannel.socket();
            serverSocket.setReuseAddress(false); // fix for Apple JVM bug 3922515
            InetSocketAddress listenAddress = new InetSocketAddress(listenPort);
            serverSocket.bind(listenAddress);

            // prepare for non-blocking, selectable IO
            serverChannel.configureBlocking(false);
            serverChannel.register(connectionManager.getNIODaemon().getSelector(), SelectionKey.OP_ACCEPT);
            connectionManager.getNIODaemon().setServer(connectionManager);

            // bind success
            logger.info("Connection Manager ready, listening on " + listenAddress);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
