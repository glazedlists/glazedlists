/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.ctp;

// NIO is used for CTP
import ca.odell.glazedlists.impl.nio.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * Closes a connection on the NIO thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class CloseConnection implements Runnable {
    
    /** logging */
    private static Logger logger = Logger.getLogger(CloseConnection.class.toString());

    /** the target connection */
    private CTPConnection connection;
    
    /** the reason for the connection to be closed */
    private Exception reason;
    
    /**
     * Creates a CTPConnectionToClose that closes the specified connection.
     */
    public CloseConnection(CTPConnection connection, Exception reason) {
        this.connection = connection;
        this.reason = reason;
    }
    
    /**
     * Runs this task.
     */
    public void run() {        
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
                connection.sendChunk(null);
            }
        }
        
        // try to flush what we have left
        try {
            connection.writer.writeToChannel(connection.socketChannel, connection.selectionKey);
        } catch(IOException e) {
            // if this flush failed, there's nothing we can do
        }
        if(connection.writer.length() > 0) logger.warning("Close proceeding with unsent data");

        // close the socket
        try {
            connection.socketChannel.close();
            connection.selectionKey.cancel();
        } catch(IOException e) {
            // if this close failed, there's nothing we can do
        }

        // log the close
        if(reason != null) {
            logger.log(Level.WARNING, "Closed connection to " + connection + " due to " + reason, reason);
        } else {
            logger.info("Closed connection to " + connection);
        }
        
        // close the connection for use
        connection.state = CTPConnection.STATE_CLOSED_PERMANENTLY;
        connection.handler.connectionClosed(connection, reason);
    }
}
