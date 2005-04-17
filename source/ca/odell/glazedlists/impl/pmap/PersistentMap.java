/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.pmap;

// NIO is used for CTP
import ca.odell.glazedlists.impl.nio.*;
import ca.odell.glazedlists.impl.io.*;
import ca.odell.glazedlists.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
// logging
import java.util.logging.*;

/**
 * A Map whose entries are persisted to disk transactionally.
 *
 * <p>This class uses an extra thread to do all persistence. This means that all
 * operations will be immediate, but will return without having taken effect on disk.
 * To flush the disk, call {@link #flush()}.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class PersistentMap implements Map {
    // Implementation Notes
    //
    // Each entry is stored as a chunk in a file.
    // The chunks are allocated, modified and then turned on
    // All access is serialized

    /** logging */
    private static Logger logger = Logger.getLogger(PersistentMap.class.toString());

    /** read and write without ever flushing meta-data such as file-mod date */
    private static final String FILE_ACCESS_MODE = "rwd";

    /** the file where all the data is stored */
    private File file = null;
    private FileChannel fileChannel = null;

    /** the I/O event queue daemon */
    private NIODaemon nioDaemon = null;

    /** the underlying map stores the objects in RAM */
    private Map map = new HashMap();

    /** converts the key from an Object to bytes and back */
    private ByteCoder keyCoder = null;

    /** the next sequence id to return */
    private int nextSequenceId = 1500;

    /** just allocate bytes in order */
    private int nextAvailableByte = 8;

    /**
     * Creates a new PersistentMap for the specified file that uses the {@link Serializable}
     * interface to convert keys to bytes.
     */
    public PersistentMap(File file) throws IOException {
        this(file, GlazedListsIO.serializableByteCoder());
    }
    /**
     * Creates a new PersistentMap for the specified file that uses the specified
     * {@link ByteCoder} to convert keys to bytes.
     */
    public PersistentMap(File file, ByteCoder keyCoder) throws IOException {
        this.file = file;
        this.keyCoder = keyCoder;

        // set up file access
        fileChannel = new RandomAccessFile(file, FILE_ACCESS_MODE).getChannel();

        // start the nio daemon
        nioDaemon = new NIODaemon();
        nioDaemon.start();

        // load the initial file
        nioDaemon.invokeAndWait(new OpenFile(this));
    }

    /**
     * Closes the file used by this PersistentMap.
     */
    public void close() {
        // close the file
        nioDaemon.invokeAndWait(new CloseFile(this));

        // invalidate the local state
        map = null;
    }

    /**
     * Blocks until all pending writes to disk have completed.
     */
    public void flush() {
        // ensure all pending changes have been written
        nioDaemon.invokeAndWait(NoOp.instance());
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        // This must merge all chunks into one massive chunk.
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if this map contains a mapping for the  specified key.
     */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * Returns true if this map maps one or more keys to the  specified value.
     */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * Returns a collection view of the mappings contained in this map.
     */
    public Set entrySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @throws IllegalArgumentException if value is not a chunk.
     */
    public Object put(Object key, Object value) {
        if(!(value instanceof Chunk)) throw new IllegalArgumentException("value must be a chunk");
        Chunk newValue = (Chunk)value;

        // prepare the chunk for persistence
        newValue.initializeForPersistence(this, key);

        // save the new chunk and update the memory-data
        Chunk oldValue = (Chunk)map.put(key, newValue);

        // write the chunk
        nioDaemon.invokeLater(new AddChunk(this, newValue, oldValue));

        // return the previous value
        return oldValue;
    }

    /**
     * Returns the value to which the specified key is mapped in this weak  hash map, or null if the map contains no mapping for  this key.
     */
    public Object get(Object key) {
        return map.get(key);
    }

    /**
     * Removes the mapping for this key from this map if present.
     */
    public Object remove(Object key) {
        // remove from the memory-map
        Chunk removed = (Chunk)map.remove(key);

        // if there was nothing to remove
        if(removed == null) return null;

        // remove from disk
        nioDaemon.invokeLater(new RemoveChunk(this, removed));

        // return the removed value
        return removed;
    }

    /**
     * Returns a set view of the keys contained in this map.
     */
    public Set keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * Returns a collection view of the values contained in this map.
     */
    public Collection values() {
        return Collections.unmodifiableCollection(map.values());
    }

    /**
     * Returns true if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Copies all of the mappings from the specified map to this map These  mappings will replace any mappings that this map had for any of the  keys currently in the specified map.
     */
    public void putAll(Map m) {
        // this allocates a big chunk that is off and puts all the little chunks inside
        // then the big chunk is split into small chunks that are all on
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the number of key-value mappings in this map.
     */
    public int size() {
        return map.size();
    }

    /**
     * Gets the next logical sequence ID for a new chunk and increments the value
     * for the next caller.
     */
    int nextSequenceId() {
        return nextSequenceId++;
    }

    /**
     * Get the daemon that does all the threading and selection.
     */
    NIODaemon getNIODaemon() {
        return nioDaemon;
    }

    /**
     * Gets the file being written.
     */
    File getFile() {
        return file;
    }

    /**
     * Gets the channel for writing to this file.
     */
    FileChannel getFileChannel() {
        return fileChannel;
    }

    /**
     * Handles a write failure.
     */
    void fail(IOException e, String message) {
        logger.log(Level.SEVERE, message, e);
    }

    /**
     * Handles the specified chunk having been loaded from file.
     */
    void loadedChunk(Chunk chunk) {
        // if this chunk contains active data
        if(chunk.isOn()) {
            map.put(chunk.getKey(), chunk);
            nextSequenceId = Math.max(nextSequenceId, chunk.getSequenceId() + 1);
        }

        // update allocation
        nextAvailableByte = Math.max(nextAvailableByte, chunk.getOffset() + chunk.size());
    }

    /**
     * Get the {@link ByteCoder} used to convert the key Objects to bytes and back.
     */
    ByteCoder getKeyCoder() {
        return keyCoder;
    }

    /**
     * Allocate some space for this chunk. The returned value is an array of two
     * integers. Result[0] == offset into file, Result[1] == number of bytes allocated.
     * Result[2] == the size index being used for this chunk, either 1 or 2, or 0
     * for new space.
     *
     * <p>More bytes may be allocated than necessary, and it is absolutely mandatory
     * that chunks consume the full number of bytes allocated to them.
     */
    void allocate(Chunk value) throws IOException {
        int offset = nextAvailableByte;
        int size = value.bytesRequired();
        nextAvailableByte += size;

        value.allocateAsNew(offset, size);
     }
}
