/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Loads the value for a chunk from disk.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 *
 * @deprecated The io extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@Deprecated
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
    @Override
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
