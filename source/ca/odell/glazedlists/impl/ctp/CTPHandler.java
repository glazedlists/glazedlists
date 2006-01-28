/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.ctp;

import java.util.*;
import java.nio.*;
import ca.odell.glazedlists.impl.io.Bufferlo;

/**
 * A callback interface for classes that implement a CTPConnection.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface CTPHandler {
    
    /**
     * Handles the connection being ready for chunks to be sent.
     */
    public void connectionReady(CTPConnection source);

    /**
     * Handles reception of the specified chunk of data. This chunk should be able
     * to be cleanly concatenated with the previous and following chunks without
     * problem by the reader.
     *
     * @param data A list of ByteBuffers containing the bytes for this chunk. The
     *      relevant bytes start at data.position() and end at data.limit(). These
     *      buffers are only valid for the duration of this method call.
     */
    public void receiveChunk(CTPConnection source, Bufferlo data);

    /**
     * Handles the connection being closed by the remote client. This will also
     * be called if there is a connection error, which is the case when a remote
     * host sends data that cannot be interpretted by CTPConnection.
     *
     * @param reason An exception if the connection was closed as the result of
     *      a failure. This may be null.
     */
    public void connectionClosed(CTPConnection source, Exception reason);
}
