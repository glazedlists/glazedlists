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
 * A chunk of a file.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class Chunk {

    /** the host PersistentMap */
    private PersistentMap persistentMap = null;
    
    /** the offset of this chunk into the file */
    private int offset = -1;
    
    /** the current size of this chunk and previous/next size */
    private int[] size = new int[] { -1, -1 };
    /** whether the size 0 or size 1 */
    private int sizeToUse = -1;

    /** whether this chunk is on or off */
    private boolean on = false;
    
    /** the order in which this chunk was written to file */
    private int sequenceId = -1;
    
    /** the key for this chunk, should be immutable or treated as such */
    private Object key = null;
    private Bufferlo keyBytes = null;
    
    /** the value for this chunk */
    private Bufferlo valueBytes = null;
    
    /**
     * Creates a {@link Chunk} with the specified value.
     */
    public Chunk(Bufferlo value) {
        valueBytes = value.duplicate();
    }
    
    /**
     * Creates a {@link Chunk} from the specified data from disk.
     */
    private Chunk(PersistentMap persistentMap, int offset, boolean on, int sizeToUse, int[] size) {
        this.persistentMap = persistentMap;
        this.offset = offset;
        this.on = on;
        this.sizeToUse = sizeToUse;
        this.size = size;
    }
    
    /**
     * Fetches the value for this chunk and sends it to teh specified ValueCallback.
     */
    public void fetchValue(ValueCallback valueCallback) {
        synchronized(this) {
            if(valueBytes != null) {
                valueCallback.valueLoaded(this, valueBytes.duplicate());
                return;
            }
        }
        
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the value for this Chunk. Since the value may not be available immediately,
     * this method will block until it is available. For non-blocking access, use
     * the {@link fetchValue(ValueCallback)} method.
     */
    public Bufferlo getValue() {
        return BlockingValueCallback.get(this);
    }
    
    
    /**
     * Get the current size of this chunk.
     */
    int size() {
        return size[sizeToUse];
    }
    
    /**
     * Gets whether this Chunk contains active data.
     */
    boolean isOn() {
        return on;
    }
    
    /**
     * Get the offset of this chunk.
     */
    public int getOffset() {
        return offset;
    }
    
    /**
     * Gets the key that indexes this chunk.
     */
    Object getKey() {
        return key;
    }
    
    /**
     * Sets the ID in which this chunk was created.
     */
    void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }
    
    /**
     * Get the sequence of this chunk.
     */
    int getSequenceId() {
        return sequenceId;
    }

    /**
     * Sets up how this chunk will be persisted.
     */
    void initializeForPersistence(PersistentMap persistentMap, Object key) {
        // we can't reuse chunks (yet)
        if(this.persistentMap != null) throw new IllegalStateException("doubly-initialized chunk");
        
        // save the host
        this.persistentMap = persistentMap;
        
        // save the key and take a binary snapshot of it
        this.key = key;
        try {
            keyBytes = new Bufferlo();
            persistentMap.getKeyCoder().encode(key, keyBytes.getOutputStream());
        } catch(IOException e) {
            persistentMap.fail(e, "Unexpected encoding exception");
        }
    }
    
    /**
     * Get the number of bytes required to write out this chunk.
     */
    int bytesRequired() {
        int required = 0;
        
        // header
        required += 4; // on/off
        required += 4; // sizeToUse
        required += 4; // size one
        required += 4; // size two

        // body
        required += 4; // sequence ID
        required += 4; // key size
        required += 4; // value size
        required += keyBytes.length(); // key
        required += valueBytes.length(); // value
        
        return required;
    }
    
    /**
     * Set the size of this Chunk. This writes the new size to disk. This requires
     * 1 flush to disk.
     */
    void allocateAsNew(int offset, int size) throws IOException {
        this.offset = offset;
        this.sizeToUse = 0;
        this.size[0] = size;
        this.size[1] = size;
        this.on = false;
        
        // write the size
        FileChannel fileChannel = persistentMap.getFileChannel();
        Bufferlo sizeData = new Bufferlo();
        DataOutputStream sizeDataOut = new DataOutputStream(sizeData.getOutputStream());
        
        sizeDataOut.writeInt(0); // on == false
        sizeDataOut.writeInt(sizeToUse);
        sizeDataOut.writeInt(this.size[0]);
        sizeDataOut.writeInt(this.size[1]);
        sizeData.writeToChannel(fileChannel.position(offset));
        fileChannel.force(false);
    }
    
    /**
     * Deletes this Chunk. This simply marks the Chunk's on value to off.
     */
    void delete() throws IOException {
        assert(offset != -1);
        if(!on) return;
        
        this.on = false;

        // turn the chunk off
        FileChannel fileChannel = persistentMap.getFileChannel();
        Bufferlo sizeData = new Bufferlo();
        DataOutputStream sizeDataOut = new DataOutputStream(sizeData.getOutputStream());
         
        sizeDataOut.writeInt(0); // on == false
        sizeData.writeToChannel(fileChannel.position(offset));
        fileChannel.force(false);
     }
    
    /**
     * Reads the chunk into memory.
     */
    static Chunk readChunk(PersistentMap persistentMap) throws IOException {
        // prepare to read
        FileChannel fileChannel = persistentMap.getFileChannel();
        Bufferlo sizeData = new Bufferlo();
        DataInputStream dataIn = new DataInputStream(sizeData.getInputStream());
        
        // read the header
        int offset = (int)fileChannel.position();
        int bytesRequired = 16;
        int read = sizeData.readFromChannel(fileChannel, bytesRequired);
        if(read < 0) return null;
        else if(read < bytesRequired) throw new IOException("Insufficent bytes available");
        
        // parse the header
        int on = dataIn.readInt();
        int sizeToUse = dataIn.readInt();
        int size[] = new int[] { -1 , -1 };
        size[0] = dataIn.readInt();
        size[1] = dataIn.readInt();
        
        // validate the header data
        if(on != 0 && on != 1) throw new IOException("Unexpected on value: " + on);
        if(sizeToUse != 0 && sizeToUse != 1) throw new IOException("Unexpected size to use value " + sizeToUse);
        if(size[sizeToUse] < 0) throw new IOException("Unexpected size: " + size[sizeToUse]);
        
        // header success
        Chunk chunk = new Chunk(persistentMap, offset, (on == 1), sizeToUse, size);
        
        // read the data
        if(chunk.on) {
            chunk.readData();
        }
        
        // adjust the position to after this chunk
        fileChannel.position(offset + size[sizeToUse]);
        
        // success
        return chunk;
    }
    
    /**
     * Writes the data of this chunk to file. This requires 2 flushes to disk.
     */
    void writeData() throws IOException {
        assert(offset != -1);
        assert(!on);
        
        // prepare to write
        FileChannel fileChannel = persistentMap.getFileChannel();
        Bufferlo chunkData = new Bufferlo();
        DataOutputStream chunkDataOut = new DataOutputStream(chunkData.getOutputStream());
        
        // write the data
        chunkDataOut.writeInt(sequenceId);
        chunkDataOut.writeInt(keyBytes.length());
        chunkDataOut.writeInt(valueBytes.length());
        chunkData.append(keyBytes);
        chunkData.append(valueBytes);
        
        chunkData.writeToChannel(fileChannel.position(offset + 16));
        fileChannel.force(false);
        
        // turn the written data on
        on = true;
        chunkDataOut.writeInt(1); // on == true
        chunkData.writeToChannel(fileChannel.position(offset));
        fileChannel.force(false);
    }
    
    /**
     * Reads the data from the file to this chunk. This requires 1 read from disk.
     */
    private void readData() throws IOException {
        assert(offset != -1);
        assert(size() != -1);
        
        // prepare to read
        FileChannel fileChannel = persistentMap.getFileChannel();
        Bufferlo chunkAsBytes = new Bufferlo();
        DataInputStream dataIn = new DataInputStream(chunkAsBytes.getInputStream());

        // read the whole chunk
        int bytesRequired = size();
        int read = chunkAsBytes.readFromChannel(fileChannel.position(offset), bytesRequired);
        if(read < bytesRequired) throw new IOException("Expected " + bytesRequired + " but found " + read + " bytes");
        
        // skip the chunk header
        dataIn.readInt(); // on/off
        dataIn.readInt(); // size to use
        dataIn.readInt(); // size 1
        dataIn.readInt(); // size 2
        
        // read the data
        sequenceId = dataIn.readInt();
        int keyBytesLength = dataIn.readInt();
        int valueBytesLength = dataIn.readInt();
        keyBytes = chunkAsBytes.consume(keyBytesLength);
        valueBytes = chunkAsBytes.consume(valueBytesLength);
        
        // skip any excess
        chunkAsBytes.skip(chunkAsBytes.length());
        
        // process the read data
        key = persistentMap.getKeyCoder().decode(keyBytes.getInputStream());
    }
}
