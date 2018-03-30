/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Adds a chunk to the persistent map.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 *
 * @deprecated The io extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@Deprecated
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
    @Override
    public void run() {
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

            logger.info("Successfully wrote value for key \"" + newValue.getKey() + "\"");
        } catch(IOException e) {
            persistentMap.fail(e, "Failed to write to file " + persistentMap.getFile().getPath());
        }
    }
}
