/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.ctp;

// NIO is used for CTP
import ca.odell.glazedlists.impl.nio.NIODaemon;
import ca.odell.glazedlists.impl.nio.NIOServer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * The CTPConnectionManager provides managed access to multiple CTP connections
 * for both incoming and outgoing data.
 *
 * <p>Each instance of this class owns a single thread which is used to perform
 * all read and write operations on all connections. A pool of other threads are
 * used to notify the handlers of the data and status of a connection.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 *
 * @deprecated The io extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@Deprecated
public final class CTPConnectionManager implements NIOServer {

    /** default port to bind to */
    private static final int DEFAULT_PORT = 5309;

    /** port to listen for incoming connections */
    private int listenPort = -1;

    /** factory for handlers of incoming connections */
    private CTPHandlerFactory handlerFactory;

    /** the I/O event queue daemon */
    private NIODaemon nioDaemon = null;

    /**
     * Creates a connection manager that handles incoming connections using the
     * specified connect handler. This binds to the default port.
     */
    public CTPConnectionManager(CTPHandlerFactory handlerFactory) {
        this(handlerFactory, DEFAULT_PORT);
    }

    /**
     * Creates a connection manager that handles incoming connections using the
     * specified connect handler. This binds to the specified port.
     */
    public CTPConnectionManager(CTPHandlerFactory handlerFactory, int listenPort) {
        this.handlerFactory = handlerFactory;
        this.listenPort = listenPort;
    }

    /**
     * Starts the CTPConnectionManager listening to incoming connections and
     * managing outgoing connections.
     *
     * @return true if the server successfully binds to the listen port.
     */
    public synchronized boolean start() throws IOException {
        // verify we haven't already started
        if(nioDaemon != null) throw new IllegalStateException();

        // start the nio daemon
        nioDaemon = new NIODaemon();
        nioDaemon.start();

        // start the server
        try {
            nioDaemon.invokeAndWait(new StartServer(this, listenPort));
        } catch(RuntimeException e) {
            nioDaemon.stop();
            if(e.getCause() instanceof IOException) throw (IOException)e.getCause();
            else throw e;
        }

        // success
        return true;
    }

    /**
     * Stops the CTPConnectionManager and closes all connections.
     */
    public void stop() {
        nioDaemon.stop();
    }

    /**
     * Get the daemon that does all the threading and selection.
     */
    public NIODaemon getNIODaemon() {
        return nioDaemon;
    }

    /**
     * Handle an incoming connection.
     *
     * <p>This creates a CTPServerProtocol to handle the connection.
     *
     * @return the SelectionKey that is attached to the created connection.
     */
    @Override
    public void handleAccept(SelectionKey key, Selector selector) {
        // construct the channels and selectors
        SocketChannel channel = null;
        SelectionKey channelKey = null;
        try {
            // peel the connection from the SocketChannel
            ServerSocketChannel server = (ServerSocketChannel)key.channel();
            channel = server.accept();

            // configure the channel for no-blocking and selection
            if(channel == null) return;
            channel.configureBlocking(false);
            channelKey = channel.register(selector, 0);
        } catch(IOException e) {
            // the accept failed, there's nothing to clean up
            return;
        }

        // construct handlers for this connection
        CTPHandler handler = handlerFactory.constructHandler();
        CTPConnection server = CTPConnection.server(channelKey, handler, this);
        channelKey.attach(server);
        server.handleConnect();
    }

    /**
     * Connect to the specified host.
     */
    public void connect(CTPHandler handler, String host, int port) {
        nioDaemon.invokeLater(new OpenConnection(this, handler, host, port));
    }
    public void connect(CTPHandler handler, String host) {
        connect(handler, host, DEFAULT_PORT);
    }
}
