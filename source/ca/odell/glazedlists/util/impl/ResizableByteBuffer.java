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
    
    /** the hosted byte buffers */
    private List byteBuffers = new ArrayList();
    
    /**
     * Creates a new ResizableByteBuffer.
     */
    public ResizableByteBuffer(int initialSize) {
        grow(initialSize);
    }
    
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
    public ResizableByteBuffer compact() {
        assert(consistentState());
        // rearrange the buffer orders
        while(true) {
            ByteBuffer firstBuffer = (ByteBuffer)byteBuffers.get(0);
            if(position < firstBuffer.capacity()) {
                break;
            } else {
                byteBuffers.remove(0);
                byteBuffers.add(firstBuffer);
                position -= firstBuffer.capacity();
                limit -= firstBuffer.capacity();
                mark -= firstBuffer.capacity();
            }
        }
        
        // compact the buffers
        ByteBuffer firstBuffer = (ByteBuffer)byteBuffers.get(0);
        if(limit > firstBuffer.capacity()) {
            weld();
            firstBuffer = (ByteBuffer)byteBuffers.get(0);
        }
        firstBuffer.limit(limit);
        firstBuffer.position(position);
        firstBuffer.compact();
        position = (limit - position);
        limit = capacity;
        mark = -1;

        assert(consistentState());
        return this;
    }
    
    /**
     * Relative get method. Reads the byte at this buffer's 
     * current position, and then increments the position.
     */
    public byte get() {
        byte result = getBufferAt(position).get();
        position++;
        return result;
    }
    public byte get(int index) {
        return getBufferAt(index).get();
    }
    
    /**
     * Relative put method  (optional operation).
     *
     * Writes the given byte into this buffer at the current 
     * position, and then increments the position.
     */
    public ResizableByteBuffer put(byte b) {
        getBufferAt(position).put(b);
        position++;
        return this;
    }
    
    /**
     * Welds all the buffers together.
     */
    private void weld() {
        assert(consistentState());
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(capacity);
        for(int b = 0; b < byteBuffers.size(); b++) {
            ByteBuffer currentBuffer = (ByteBuffer)byteBuffers.get(b);
            currentBuffer.clear();
            newBuffer.put(currentBuffer);
        }
        byteBuffers.clear();
        byteBuffers.add(newBuffer);
        assert(consistentState());
    }
    
    /**
     * Creates a slice of this buffer of the specified size. This slice is only valid
     * until the next operation on this ResizableByteBuffer.
     *
     * <p>Currently this sometimes creates a new temporary buffer. A better solution
     * is to weld the buffers of this together and return a section of that.
     */
    public ByteBuffer consume(int bytesToRead) {
        assert(consistentState());
        ByteBuffer currentBuffer = getBufferAt(position);

        // weld all our buffers together if necessary
        if(bytesToRead > currentBuffer.remaining()) {
            weld();
            currentBuffer = getBufferAt(position);
        }

        // return a slice of the current buffer
        currentBuffer.limit(currentBuffer.position() + bytesToRead);
        ByteBuffer result = currentBuffer.slice();
        position += bytesToRead;
        assert(consistentState());
        return result;
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
        assert(consistentState());
        if(source.remaining() > remaining()) throw new BufferOverflowException();

        while(source.hasRemaining()) {
            ByteBuffer currentBuffer = getBufferAt(position);
            if(currentBuffer.remaining() >= source.remaining()) {
                int bytesToWrite = source.remaining();
                currentBuffer.put(source);
                position += bytesToWrite;
            } else {
                int bytesToWrite = currentBuffer.remaining();
                ByteBuffer subSource = source.duplicate();
                subSource.limit(subSource.position() + bytesToWrite);
                currentBuffer.put(subSource);
                position += bytesToWrite;
                source.position(source.position() + bytesToWrite);
            }
        }
        assert(consistentState());
    }
    
    /**
     * Reads a sequence of bytes into this buffer from the given channel.
     *
     * An attempt is made to read up to r bytes from the channel, 
     * where r is the number of bytes remaining in the buffer, that is,
     * dst.remaining(), at the moment this method is invoked.
     *
     * Suppose that a byte sequence of length n is read, where  0 <= n <= r. 
     * This byte sequence will be transferred into the buffer so that the first 
     * byte in the sequence is at index p and the last byte is at index 
     * p + n - 1,  where p is the buffer's position at the moment this method is
     * invoked. Upon return the buffer's position will be equal to 
     * p + n; its limit will not have changed.
     *
     * A read operation might not fill the buffer, and in fact it might not
     * read any bytes at all. Whether or not it does so depends upon the 
     * nature and state of the channel. A socket channel in non-blocking mode, 
     * for example, cannot read any more bytes than are immediately available
     * from the socket's input buffer; similarly, a file channel cannot read  
     * any more bytes than remain in the file. It is guaranteed, however, that  
     * if a channel is in blocking mode and there is at least one byte 
     * remaining in the buffer then this method will block until at least one
     * byte is read.
     * 
     * This method may be invoked at any time. If another thread has
     * already initiated a read operation upon this channel, however, then an 
     * invocation of this method will block until the first operation is 
     * complete.
     */
    public int pull(ReadableByteChannel source) throws IOException {
        assert(consistentState());
        int positionBefore = position;
        while(hasRemaining()) {
            ByteBuffer currentBuffer = getBufferAt(position);
            int bytesRead = source.read(currentBuffer);
            if(bytesRead < 0 && positionBefore == position) return bytesRead;
            else if(bytesRead <= 0) break;
            else if(bytesRead > 0) {
                position += bytesRead;
            }
        }
        assert(consistentState());
        return (position - positionBefore);
    }
    
    /**
     * Writes a sequence of bytes to the specified channel from this buffer.
     *
     * An attempt is made to write up to r bytes to the channel, 
     * where r is the number of bytes remaining in the buffer, that is, 
     * dst.remaining(), at the moment this method is invoked.
     *
     * Suppose that a byte sequence of length n is written, where  0 <= n <= r. 
     * This byte sequence will be transferred from the buffer starting at index 
     * p, where p is the buffer's position at the moment this 
     * method is invoked; the index of the last byte written will be  p + n - 1. 
     * Upon return the buffer's position will be equal to
     * p + n; its limit will not have changed.
     *
     * Unless otherwise specified, a write operation will return only after 
     * writing all of the r requested bytes. Some types of channels, 
     * depending upon their state, may write only some of the bytes or possibly 
     * none at all. A socket channel in non-blocking mode, for example, cannot 
     * write any more bytes than are free in the socket's output buffer.
     *
     * This method may be invoked at any time. If another thread has 
     * already initiated a write operation upon this channel, however, then an 
     * invocation of this method will block until the first operation is 
     * complete.
     */
    public int push(WritableByteChannel target) throws IOException {
        assert(consistentState());
        int positionBefore = position;
        while(hasRemaining()) {
            ByteBuffer currentBuffer = getBufferAt(position);
            if(currentBuffer.remaining() > remaining()) currentBuffer.limit(currentBuffer.position() + remaining());
            int bytesWritten = target.write(currentBuffer);
            if(bytesWritten < 0 && positionBefore == position) return bytesWritten;
            else if(bytesWritten <= 0) break;
            else if(bytesWritten > 0) {
                position += bytesWritten;
            }
        }
        assert(consistentState());
        return (position - positionBefore);
    }
    
    /**
     * Gets the buffer at the specified index.
     */
    private ByteBuffer getBufferAt(int index) {
        int bytesRemaining = index;
        for(int b = 0; b < byteBuffers.size(); b++) {
            ByteBuffer currentBuffer = (ByteBuffer)byteBuffers.get(b);
            if(currentBuffer.capacity() > bytesRemaining) {
                currentBuffer.clear();
                currentBuffer.position(bytesRemaining);
                return currentBuffer;
            } else {
                bytesRemaining -= currentBuffer.capacity();
            }
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Grows this ResizableByteBuffer to the specified minimum size. This sets the
     * limit to the new minimum size.
     */
    public void grow(int minSize) {
        assert(consistentState());
        if(capacity >= minSize) return;

        // calculate the size to grow as a nice round number
        int requiredSize = Math.max(capacity, (minSize-capacity));
        
        // create the new buffer
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(requiredSize);
        byteBuffers.add(newBuffer);
        capacity += newBuffer.capacity();
        limit = minSize;
        assert(consistentState());
    }
    
    /**
     * Gets the ResizableByteBuffer as a String. This contains the hosted buffers
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("ResizableBuffer[");
        result.append("pos=").append(position);
        result.append(" lim=").append(limit);
        result.append(" cap=").append(capacity);
        result.append(" children={");
        for(int b = 0; b < byteBuffers.size(); b++) {
            if(b != 0) result.append(", ");
            ByteBuffer currentBuffer = (ByteBuffer)byteBuffers.get(b);
            result.append(currentBuffer.capacity());
        }
        result.append("}]");
        return result.toString();
    }
}
