/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// NIO is used for CTP
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * A CTPRunnable that closes a connection.
 */
class CTPConnectionToClose implements CTPRunnable {
    
    /** logging */
    private static Logger logger = Logger.getLogger(CTPConnectionToClose.class.toString());

    /** the target connection */
    private CTPConnection connection;
    
    /** the reason for the connection to be closed */
    private Exception reason;
    
    /**
     * Creates a CTPConnectionToClose that closes the specified connection.
     */
    public CTPConnectionToClose(CTPConnection connection, Exception reason) {
        this.connection = connection;
        this.reason = reason;
    }
    
    /**
     * Runs the specified task.
     *
     * @param selector the selector being shared by all connections.
     * @return true unless the server shall shutdown due to a shutdown request or
     *      an unrecoverable failure.
     */
    public void run(Selector selector, CTPConnectionManager manager) {
        // if this is already closed, we're done
        if(connection.state == CTPConnection.STATE_CLOSED_PERMANENTLY) return;
        
        // close is not a result of a connection error, so say goodbye
        if(reason == null || !(reason instanceof IOException)) {

            // if we haven't yet responded, respond now
            if(connection.state == CTPConnection.STATE_SERVER_AWAITING_REQUEST) {
                connection.state = CTPConnection.STATE_SERVER_CONSTRUCTING_RESPONSE;
                connection.sendResponse(CTPConnection.RESPONSE_ERROR, Collections.EMPTY_MAP);

            // if we've already responded, send an empty chunk
            } else if(connection.state == CTPConnection.STATE_READY) {
                connection.sendChunk(ByteBuffer.wrap(new byte[0]));
            }
        }
        
        // try to flush what we have left
        try {
            connection.writer.flush();
        } catch(IOException e) {
            // if this flush failed, there's nothing we can do
        }

        // close the socket
        try {
            connection.socketChannel.close();
            connection.selectionKey.cancel();
        } catch(IOException e) {
            // if this close failed, there's nothing we can do
        }

        // log the close
        if(reason != null) {
            logger.log(Level.FINE, "Closed connection to " + this + " due to " + reason, reason);
        } else {
            logger.log(Level.FINE, "Closed connection to " + this);
        }
        
        // close the connection for use
        connection.state = CTPConnection.STATE_CLOSED_PERMANENTLY;
        connection.handler.connectionClosed(connection, reason);
    }
}
