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
 * Sends a chunk of data on the CTPConnectionManager thread.
 */
class CTPChunkToSend implements CTPRunnable {
     
    /** logging */
    private static Logger logger = Logger.getLogger(CTPChunkToSend.class.toString());

    /** the destination */
    private CTPConnection connection;

    /** the content */
    private ByteBuffer data;

    /**
     * Create a new CTPConnectionToEstablish.
     */
    public CTPChunkToSend(CTPConnection connection, ByteBuffer data) {
        this.connection = connection;
        this.data = data;
    }
    
    /**
     * Writes the data.
     */
    public void run(Selector selector, CTPConnectionManager manager) {
        if(connection.state != CTPConnection.STATE_READY) throw new IllegalStateException();
        
        try {
            String chunkSizeInHex = Integer.toString(data.remaining(), 16);
            connection.writer.write(chunkSizeInHex);
            connection.writer.write("\r\n");
            connection.writer.write(data);
            connection.writer.write("\r\n");
            connection.writer.flush();
            
        } catch(IOException e) {
            connection.close(e);
        }
    }
}
