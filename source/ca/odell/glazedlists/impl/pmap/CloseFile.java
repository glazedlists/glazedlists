/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Closes the file for reading and writing a persistent map.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class CloseFile implements Runnable {
     
    /** logging */
    private static Logger logger = Logger.getLogger(CloseFile.class.toString());
    
    /** the host map */
    private PersistentMap persistentMap = null;

    /**
     * Create a new CloseFile.
     */
    public CloseFile(PersistentMap persistentMap) {
        this.persistentMap = persistentMap;
    }
    
    /**
     * Close the file.
     */
    public void run() {
        try {
            persistentMap.getFileChannel().close();
        } catch(IOException e) {
            persistentMap.fail(e, "Failed to close file " + persistentMap.getFile().getPath());
        }
    }
}
