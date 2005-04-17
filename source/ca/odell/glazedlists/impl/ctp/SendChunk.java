/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.ctp;

// NIO is used for CTP
import ca.odell.glazedlists.impl.nio.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
import ca.odell.glazedlists.impl.io.Bufferlo;
// logging
import java.util.logging.*;

/**
 * Sends a chunk of data on the NIO thread.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class SendChunk implements Runnable {
     
    /** the destination */
    private CTPConnection connection;

    /** the content */
    private Bufferlo data;

    /**
     * Create a new SendChunk.
     */
    public SendChunk(CTPConnection connection, Bufferlo data) {
        this.connection = connection;
        this.data = data;
    }
    
    /**
     * Writes the data.
     */
    public void run() {
        if(connection.state != CTPConnection.STATE_READY) throw new IllegalStateException();
        
        try {
            // calculate the total bytes remaining
            int totalRemaining = (data != null) ? data.length() : 0;
            
            // write the chunk
            String chunkSizeInHex = Integer.toString(totalRemaining, 16);
            connection.writer.write(chunkSizeInHex);
            connection.writer.write("\r\n");
            if(data != null) connection.writer.append(data);
            connection.writer.write("\r\n");
            connection.writer.writeToChannel(connection.socketChannel, connection.selectionKey);
            
        } catch(IOException e) {
            connection.close(e);
        }
    }
}
