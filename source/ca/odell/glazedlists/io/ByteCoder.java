/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

import java.io.*;

/**
 * An utility interface for converting Objects to bytes for storage or network
 * transport. For some common, general-purpose {@link ByteCoder}s, see the
 * {@link ca.odell.glazedlists.GlazedLists GlazedLists} factory class.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface ByteCoder {

    /**
     * Encode the specified Object over the specified {@link OutputStream}.
     */
    public void encode(Object source, OutputStream target) throws IOException;

    /**
     * Decode the Object from the specified {@link InputStream}. The stream should contain
     * exactly one Object and no further bytes before the end of the stream.
     */
    public Object decode(InputStream source) throws IOException;
}