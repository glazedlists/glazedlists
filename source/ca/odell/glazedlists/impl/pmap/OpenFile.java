/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
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
 * Opens a file for reading and writing a persistent map.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class OpenFile implements Runnable {
     
    /** logging */
    private static Logger logger = Logger.getLogger(OpenFile.class.toString());
    
    /** the host map */
    private PersistentMap persistentMap = null;

    /**
     * Create a new OpenFile.
     */
    public OpenFile(PersistentMap persistentMap) {
        this.persistentMap = persistentMap;
    }
    
    /**
     * Open the file, read any data if it exists, and prepare everything.
     */
    public void run() {
        FileChannel fileChannel = persistentMap.getFileChannel();
        
        try {
            // if the file doesn't already exist
            if(fileChannel.size() == 0) {
                createFile();
                return;
            }
            
            // first read the header
            readHeader();
            
            // now read the data
            while(true) {
                Chunk chunk = Chunk.readChunk(persistentMap);
                if(chunk == null) break;
                persistentMap.loadedChunk(chunk);
                if(chunk.isOn()) {
                    logger.info("Successfully loaded key \"" + chunk.getKey() + "\"");
                }
            }
        
        } catch(IOException e) {
            persistentMap.fail(e, "Failed to access file " + persistentMap.getFile().getPath());
        }
    }
    
    /**
     * Reads the file header, or creates a new file if the header is broken or
     * doesn't exist.
     */
    private void readHeader() throws IOException {
        try {
            // process the file header
            Bufferlo fileHeader = new Bufferlo();
            fileHeader.readFromChannel(persistentMap.getFileChannel(), 8);
            fileHeader.consume("GLAZED\n\n");
                
        } catch(ParseException e) {
            // the file header is broken, bail
            throw new IOException("The file cannot be read because it is not of the expected type");
        }
        
        logger.info("Successfully read file header");
    }
    
    /**
     * Creates the PersistentMap file.
     */
    private void createFile() throws IOException {
        // write the file header
        Bufferlo fileHeader = new Bufferlo();
        fileHeader.write("GLAZED\n\n");
        persistentMap.getFileChannel().position(0);
        fileHeader.writeToChannel(persistentMap.getFileChannel());
        logger.info("Successfully created file");
    }
}
