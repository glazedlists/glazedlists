/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.io;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.io.*;
import ca.odell.glazedlists.event.*;
// access to the volatile implementation pacakge
import ca.odell.glazedlists.impl.pmap.PersistentMap;
import ca.odell.glazedlists.impl.pmap.Chunk;
import ca.odell.glazedlists.impl.io.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;

/**
 * An {@link EventList} that is persisted to disk.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class FileList extends TransformedList {

    /** the destination file, just for user convenience */
    private File file = null;
    
    /** how bytes are encoded and decoded */
    private ByteCoder byteCoder;
    
    /** the underlying storage of ListEvents */
    private PersistentMap storage = null;

    /** the ID of the next update to write to disc */
    private int nextUpdateId = 81;
    
    /** whether this list can be modified */
    private boolean writable = true;
    
    /**
     * Create a {@link FileList} that stores its data in the specified file.
     */
    public FileList(File file, ByteCoder byteCoder) throws IOException {
        super(new BasicEventList());
        this.file = file;
        this.byteCoder = byteCoder;
        
        // store the updates in a persistent map
        storage = new PersistentMap(file);

        // sequence the updates
        SortedMap sequentialUpdates = new TreeMap();
        for(Iterator k = storage.keySet().iterator(); k.hasNext(); ) {
            Integer key = (Integer)k.next();
            Bufferlo valueBuffer = ((Chunk)storage.get(key)).getValue();
            sequentialUpdates.put(key, valueBuffer);
        }
        
        // replay all the updates from the file
        for(Iterator u = sequentialUpdates.keySet().iterator(); u.hasNext(); ) {
            Integer key = (Integer)u.next();
            Bufferlo update = (Bufferlo)sequentialUpdates.get(key);
            ListEventToBytes.toListEvent(update, this, byteCoder);
        }
        
        // prepare the next update id to use
        if(!sequentialUpdates.isEmpty()) {
            nextUpdateId = ((Integer)sequentialUpdates.lastKey()).intValue() + 1;
        }
        
        // now that we're up-to-date, listen for further events
        source.addListEventListener(this);
    }
    
    /** {@inheritDoc} */
    public boolean isWritable() {
        return writable;
    }
    
    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        // write the change to disc
        try {
            ListEvent listChangesCopy = new ListEvent(listChanges);
            Bufferlo listChangesBytes = ListEventToBytes.toBytes(listChangesCopy, byteCoder);
            storage.put(new Integer(nextUpdateId), new Chunk(listChangesBytes));
            nextUpdateId++;

        } catch(IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
        
        // forward the event to interested listeners
        updates.forwardEvent(listChanges);
    }
    
    /**
     * Closes this FileList so that it consumes no disc resources. The list may
     * continue to be read until it is {@link dispose() disposed}.
     */
    public void close() {
        if(storage != null) storage.close();
        storage = null;
        writable = false;
    }

    /** {@inheritDoc} */
    public void dispose() {
        close();
        super.dispose();
    }
}
