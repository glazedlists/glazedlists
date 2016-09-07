/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.ctp;

// NIO is used for CTP
import ca.odell.glazedlists.impl.io.Bufferlo;

import java.io.IOException;

/**
 * Sends a chunk of data on the NIO thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
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
    @Override
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
