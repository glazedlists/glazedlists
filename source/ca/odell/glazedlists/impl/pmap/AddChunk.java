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
 * Adds a chunk to the persistent map.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class AddChunk implements Runnable {
     
    /** logging */
    private static Logger logger = Logger.getLogger(AddChunk.class.toString());
    
    /** the host map */
    private final PersistentMap persistentMap;

    /** the value to write out */
    private final Chunk newValue;
    
    /** the value to erase */
    private final Chunk oldValue;
    
    /**
     * Create a new AddChunk.
     */
    public AddChunk(PersistentMap persistentMap, Chunk newValue, Chunk oldValue) {
        this.persistentMap = persistentMap;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }
    
    /**
     * Write the chunk to disk.
     *
     * <p>This is a multiple stage procedure:
     * <ol>
     *   <li>Figure out how many bytes are needed for this chunk:
     *   <li>Allocate that many bytes in the file for the new section
     *   <li>Clean up the new section: mark it as empty and of the new size (flush 1)
     *   <li>Fill the new section (flush 2)
     *   <li>Mark the new section as not empty any more (flush 3)
     * </ol>
     */
    public void run() {
        FileChannel fileChannel = persistentMap.getFileChannel();
        
        try {
            // allocate
            persistentMap.allocate(newValue);
            
            // get a sequence id
            newValue.setSequenceId(persistentMap.nextSequenceId());
            
            // write out the data
            newValue.writeData();
            
            // clear the old value
            if(oldValue != null) {
                oldValue.delete();
            }

        } catch(IOException e) {
            persistentMap.fail(e, "Failed to write to file " + persistentMap.getFile().getPath());
        }
        logger.info("Successfully wrote value for key \"" + newValue.getKey() + "\"");
    }
}
