/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
 * Removes a chunk from the persistent map.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class RemoveChunk implements Runnable {
     
    /** logging */
    private static Logger logger = Logger.getLogger(RemoveChunk.class.toString());
    
    /** the host map */
    private final PersistentMap persistentMap;

    /** the value to erase */
    private final Chunk chunk;
    
    /**
     * Create a new RemoveChunk.
     */
    public RemoveChunk(PersistentMap persistentMap, Chunk chunk) {
        this.persistentMap = persistentMap;
        this.chunk = chunk;
    }
    
    /**
     * Removes the chunk by marking it off.
     */
    public void run() {
        FileChannel fileChannel = persistentMap.getFileChannel();
        
        try {
            chunk.delete();
            logger.info("Successfully removed value for key \"" + chunk.getKey() + "\"");
            
        } catch(IOException e) {
            persistentMap.fail(e, "Failed to write to file " + persistentMap.getFile().getPath());
        }
    }
}
