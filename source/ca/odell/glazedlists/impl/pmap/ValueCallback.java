/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
import ca.odell.glazedlists.impl.nio.*;
import ca.odell.glazedlists.impl.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * Listens for the loading of a value.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface ValueCallback {

    /**
     * Handles a value being completely loaded into memory and ready to read.
     */
    public void valueLoaded(Chunk member, Bufferlo value);
    
}
