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
 * A ResizableByteBuffer is a ByteBuffer that is composed of one or more other
 * ByteBuffers.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ResizableByteBuffer extends AbstractBuffer {
    
    /** each buffer will be a multiple of this value, to keep things tidy */
    private static final int BUFFER_MULTIIPLE = 1024;
    
    /** the hosted byte buffers */
    private List byteBuffers = new ArrayList();
    
    /**
     * Compacts this buffer  (optional operation).
     *
     * The bytes between the buffer's current position and its limit, 
     * if any, are copied to the beginning of the buffer. That is, the 
     * byte at index p = position() is copied  to index zero, the byte at index p + 1 is copied  to index one, and so forth until the byte at index limit() - 1 is copied to index  n = limit() - 1 - p.  The buffer's position is then set to n+1 and its limit is set to  its capacity. The mark, if defined, is discarded.
     *
     * The buffer's position is set to the number of bytes copied, 
     * rather than to zero, so that an invocation of this method can be 
     * followed immediately by an invocation of another relative put  method.
     *
     * Invoke this method after writing data from a buffer in case the 
     * write was incomplete. The following loop, for example, copies bytes from 
     * one channel to another via the buffer buf:
     *
     *   buf.clear();          // Prepare buffer for use
     *   for (;;) {
     *      if (in.read(buf) < 0 && !buf.hasRemaining())
     *      break;        // No more bytes to transfer
     *      buf.flip();
     *      out.write(buf);
     *      buf.compact();    // In case of partial write
     *   }
     */
    private ByteBuffer compact() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Relative get method. Reads the byte at this buffer's 
     * current position, and then increments the position.
     */
    public byte get() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Relative put method  (optional operation).
     *
     * Writes the given byte into this buffer at the current 
     * position, and then increments the position.
     */
    public ByteBuffer put(byte b) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Relative bulk put method  (optional operation).
     *
     * This method transfers the bytes remaining in the given source 
     * buffer into this buffer. If there are more bytes remaining in the 
     * source buffer than in this buffer, that is, if  src.remaining() > remaining(), 
     * then no bytes are transferred and a BufferOverflowException is thrown.
     *
     * Otherwise, this method copies  n = src.remaining() bytes from the given 
     * buffer into this buffer, starting at each buffer's current position. 
     * The positions of both buffers are then incremented by n.
     * 
     * In other words, an invocation of this method of the form 
     * dst.put(src) has exactly the same effect as the loop
     *
     *   while (src.hasRemaining())
     *     dst.put(src.get()); 
     *
     * except that it first checks that there is sufficient space in this 
     * buffer and it is potentially much more efficient.
     */
    public void put(ByteBuffer source) {
/*        if(source.remaining() > remaining()) throw new BufferOverflowException();

        while(source.hasRemaining()) {
            ByteBuffer currentBuffer = getBufferAt(position);
            int writeStart = getOffsetIntoBufferAt(position);
            if(currentBuffer.remaining() >= source.remaining()) {
                int bytesToWrite = source.remaining();
                currentBuffer.put(source);
                position += bytesToWrite;
            } else {
                int bytesToWrite = currentBuffer.remaining();
                ByteBuffer subSource = source.duplicate();
                subSource.limit(subSource.position(), subSource.position() + bytesToWrite);
                currentBuffer.put(subSource);
                position += bytesToWrite;
            }
        }
*/    }
    
    /**
     * Gets the buffer at the specified index.
     */
    private ByteBuffer getBufferAt(int index) {
/*        int bytesRemaining = index;
        for(int b = 0; b < byteBuffers.size(); b++) {
            ByteBuffer currentBuffer = (ByteBuffer)byteBuffers.get(b);
            if(currentBuffer.capacity() > bytesRemaining) return b;
            else bytesRemaining -= currentbuffer.capacity();
        }
*/        throw new IndexOutOfBoundsException();
    }

    /**
     * Gets the offset of the specified index in the resizable buffer into the
     * applicable buffer.
     */
    private int getOffsetIntoBufferAt(int index) {
/*        int bytesRemaining = index;
        for(int b = 0; b < byteBuffers.size(); b++) {
            ByteBuffer currentBuffer = (ByteBuffer)byteBuffers.get(b);
            if(currentBuffer.capacity() > bytesRemaining) return bytesRemaining;
            else bytesRemaining -= currentbuffer.capacity();
        }
*/        throw new IndexOutOfBoundsException();
    }
    
    /**
     * Grows this ResizableByteBuffer to the specified minimum size.
     */
    public void grow(int minSize) {
        if(capacity >= minSize) return;

        // calculate the size to grow as a nice round number
        int requiredSize = Math.max(capacity, (minSize-capacity));
        int newBufferSize = ((requiredSize+BUFFER_MULTIIPLE-1) / BUFFER_MULTIIPLE) * BUFFER_MULTIIPLE;
        
        // create the new buffer
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(newBufferSize);
        byteBuffers.add(newBuffer);
        capacity += newBuffer.capacity();
    }
}
