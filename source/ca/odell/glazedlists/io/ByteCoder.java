/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.io;

import java.util.List;
import java.io.*;

/**
 * An utility interface for converting Objects to bytes for storage or network
 * transport.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface ByteCoder {

    /**
     * Encode the specified Object over the specified OutputStream.
     */
    public void encode(Object source, OutputStream target) throws IOException;
    
    /**
     * Decode the Object from the specified InputStream. The stream should contain
     * exactly one Object and no further bytes before the end of the stream.
     */
    public Object decode(InputStream source) throws IOException;
}
