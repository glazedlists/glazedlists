/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

import java.util.*;
// NIO
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
// regular expressions
import java.util.regex.*;
import java.text.ParseException;
// logging
import java.util.logging.*;
import java.text.ParseException;

/**
 * A high-level class for moving data and parsing protocols.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Bufferlo {
    
    /** the buffers managed by this Bufferlo */
    private LinkedList buffers = new LinkedList();
    
    /** write to this bufferlo */
    private BufferloOutputStream out = new BufferloOutputStream();
    
    /** read from this bufferlo */
    private BufferloInputStream in = new BufferloInputStream();

    /** whether this is read only */
    private boolean readOnly = false;
    
    /** 
     * Populate this Bufferlo with the data from the specified channel.
     */
    public int readFromChannel(ReadableByteChannel source) throws IOException {
        int totalRead = 0;
        while(true) {
            // we need a new place to write into
            ByteBuffer writeInto = getWriteIntoBuffer();
            
            // read in
            int bytesRead = source.read(writeInto);
            doneWriting();

            // figure out what to do next
            if(bytesRead < 0 && totalRead == 0) return bytesRead;
            else if(bytesRead <= 0) return totalRead;
            else totalRead += bytesRead;
        }
    }
    
    /** 
     * Write the content of this Bufferlo to the specified channel.
     */
    public int writeToChannel(WritableByteChannel target) throws IOException {
        if(true) throw new RuntimeException("Convert this to gathering writes");
        int totalWritten = 0;
        while(true) {
            // we need a place to read from
            ByteBuffer readFrom = getReadFromBuffer();
            
            // write out
            int bytesWritten = target.write(readFrom);
            doneReading();
            
            // figure out what to do next
            if(bytesWritten < 0 && totalWritten == 0) return bytesWritten;
            else if(bytesWritten <= 0) return totalWritten;
            else totalWritten += bytesWritten;
        }
    }
    
    /**
     * Gets an InputStream that reads this buffer.
     */
    public BufferloInputStream getInputStream() {
        return in;
    }
    
    /**
     * Gets an OutputStream that writes this buffer.
     */
    public BufferloOutputStream getOutputStream() {
        return out;
    }
    
    /**
     * Duplicates the exact state of this buffer. The returned buffer is read-only.
     */
    public Bufferlo duplicate() {
        Bufferlo result = new Bufferlo();
        for(int i = 0; i < buffers.size(); i++) {
            ByteBuffer buffer = (ByteBuffer)buffers.get(i);
            result.buffers.add(removeTrailingSpace(buffer));
        }
        return result;
    }
    
    /**
     * Read the specified bytes into a new Bufferlo. The returned buffer is read-only.
     */
    public Bufferlo consume(int bytes) {
        Bufferlo result = duplicate();
        result.limit(bytes);
        skip(bytes);
        return result;
    }
    
    /**
     * Writes the specified data to this bufferlo. This shortens the last ByteBuffer
     * in the added set so that it will not be written to. This allows a read-only
     * buffer to be added without it ever being modified.
     * 
     * This will consume the specified Bufferlo.
     */
    public Bufferlo append(Bufferlo data) {
        buffers.addAll(data.buffers);
        data.buffers.clear();
        return this;
    }
    
    /**
     * Appends a the specified data. The data will be consumed so it should be
     * a duplicate if it is to be reused.
     */
    public Bufferlo append(ByteBuffer data) {
        ByteBuffer myCopy = data.slice();
        myCopy.position(myCopy.limit());
        buffers.add(myCopy);
        data.position(data.limit());
        return this;
    }
    
    /**
     * Limits this Bufferlo to the specified size.
     */
    public void limit(int bytes) {
        int bytesLeft = bytes;
        for(ListIterator b = buffers.listIterator(); b.hasNext(); ) {
            ByteBuffer current = (ByteBuffer)b.next();
            
            if(bytesLeft <= 0) {
                b.remove();
            
            } else if(current.capacity() >= bytesLeft) {
                current.position(bytesLeft);
                current.limit(bytesLeft);
            }

            bytesLeft -= current.capacity();
        }
    }
    
    /**
     * Skips the specified number of bytes.
     */
    public void skip(int bytes) {
        int bytesLeft = bytes;
        for(ListIterator b = buffers.listIterator(); b.hasNext(); ) {
            ByteBuffer current = (ByteBuffer)b.next();
            
            if(bytesLeft >= current.limit()) {
                bytesLeft -= current.limit();
                b.remove();
            } else {
                current.position(bytesLeft);
                ByteBuffer smaller = current.slice();
                smaller.position(smaller.limit());
                b.set(smaller);
                break;
            }
        }
    }
    
    /**
     * Write this Bufferlo as a String for debugging.
     */
    public String toString() {
        return buffers.toString();
    }

    /**
     * Creates a new ByteBuffer identical to the parameter but without space trailing
     * after the limit. This allows a buffer to be added to the Bufferlo without
     * that buffer ever being written to.
     *
     * This assumes that position == limit.
     */
    private ByteBuffer removeTrailingSpace(ByteBuffer buffer) {
        ByteBuffer clone = buffer.duplicate();
        clone.position(0);
        ByteBuffer noTrailingSpace = clone.slice();
        noTrailingSpace.position(noTrailingSpace.limit());
        return noTrailingSpace;
    }
    
    /**
     * Write to the Bufferlo as a Stream.
     */
    class BufferloOutputStream extends OutputStream {
        public void write(int b) {
            ByteBuffer writeBuffer = getWriteIntoBuffer();
            writeBuffer.put((byte)b);
            doneWriting();
        }
    }
    
    /**
     * Read from the Bufferlo as a Stream.
     */
    class BufferloInputStream extends InputStream {
        public int read() {
            ByteBuffer readBuffer = getReadFromBuffer();
            if(readBuffer == null) return -1;
            byte result = readBuffer.get();
            doneReading();
            return result;
        }
    }
    
    /**
     * Gets a buffer that we can read from. This buffer must be flipped back into
     * write mode when this is complete by calling doneReading().
     */
    private ByteBuffer getReadFromBuffer() {
        if(buffers.isEmpty()) return null;
        
        ByteBuffer readFrom = (ByteBuffer)buffers.getFirst();
        readFrom.flip();
        
        return readFrom;
    }
    
    /**
     * Finishes reading the current read from buffer.
     */
    private void doneReading() {
        ByteBuffer readFrom = (ByteBuffer)buffers.getFirst();
        
        // if we've exhaused this buffer
        if(!readFrom.hasRemaining()) {
            buffers.removeFirst();

        // we still have more to read from this buffer
        } else {
            int bytesLeftToRead = readFrom.remaining();
            readFrom.limit(readFrom.capacity());
            ByteBuffer noneRead = readFrom.slice();
            noneRead.position(bytesLeftToRead);
            noneRead.limit(bytesLeftToRead);
            buffers.set(0, noneRead);
        }
    }
    
    /**
     * Gets a buffer that we can write data into.
     */
    private ByteBuffer getWriteIntoBuffer() {
        if(readOnly) throw new IllegalStateException("Read only");
        
        // we have a buffer with space remaining
        if(!buffers.isEmpty()) {
            ByteBuffer last = (ByteBuffer)buffers.getLast();
            if(last.position() < last.capacity()) {
                last.limit(last.capacity());
                return last;
            }
        }

        // we need to create a new buffer
        ByteBuffer writeInto = getNewBuffer();
        buffers.addLast(writeInto);
        return writeInto;
    }
    
    /**
     * Finishes writing the current buffer.
     */
    private void doneWriting() {
        ByteBuffer writeInto = (ByteBuffer)buffers.getLast();
        writeInto.limit(writeInto.position());
    }

    /**
     * Gets a new buffer by creating it or removing it from the pool.
     */
    private ByteBuffer getNewBuffer() {
        int BUFFER_SIZE = 8 * 1024; 
        return ByteBuffer.allocateDirect(BUFFER_SIZE);
    }
}
