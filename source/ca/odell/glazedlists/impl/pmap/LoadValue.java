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
import java.text.ParseException;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * Loads the value for a chunk from disk.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class LoadValue implements Runnable {
     
    /** logging */
    private static Logger logger = Logger.getLogger(LoadValue.class.toString());
    
    /** the chunk with the data of interest */
    private final Chunk chunk;
    
    /** the interested party */
    private final ValueCallback valueCallback;
    
    /**
     * Create a new LoadValue.
     */
    public LoadValue(Chunk chunk, ValueCallback valueCallback) {
        this.chunk = chunk;
        this.valueCallback = valueCallback;
    }
    
    /**
     * Read a chunk's value from disk.
     */
    public void run() {
        try {
            if(!chunk.isOn()) throw new IOException("Chunk has been destroyed");
            valueCallback.valueLoaded(chunk, chunk.readValue());

        } catch(IOException e) {
            chunk.getPersistentMap().fail(e, "Failed to read value from file " + chunk.getPersistentMap().getFile().getPath());
            valueCallback.valueLoaded(chunk, null);
        }
    }
}
