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
import java.io.IOException;
// regular expressions
import java.util.regex.*;
import java.text.ParseException;

/**
 * Helper class for to a Channel.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ByteChannelWriter {

    /** the SelectionKey to notify which operations we are interested in */
    private SelectionKey selectionKey;
    
    /** the SocketChannel to write to */
    private SocketChannel channel;
    
    /** the ResizableByteBuffer to write to */
    private static final int INITIAL_BUFFER_SIZE = 256;
    private ResizableByteBuffer buffer = new ResizableByteBuffer(INITIAL_BUFFER_SIZE);
    
    /** the ByteBuffers that are pending writes */
    private List pendingBuffers = new ArrayList();
    
    /**
     * Create a writer for the specified channel.
     */
    public ByteChannelWriter(SocketChannel channel, SelectionKey selectionKey) {
        this.channel = channel;
        this.selectionKey = selectionKey;
    }
    
    /**
     * Writes the specified String, one byte per character.
     *
     * <p>This method needs optimization because it performs an unnecessary copy of
     * the source String.
     */
    public void write(String text) throws IOException {
        byte[] textAsBytes = text.getBytes("US-ASCII");
        ByteBuffer textAsByteBuffer = ByteBuffer.wrap(textAsBytes);
        pendingBuffers.add(textAsByteBuffer.duplicate());
    }
    
    /**
     * Writes the specified ByteBuffer. The specified buffer is not written
     * immediately so it is an error to modify that buffer. Once {@link #flush()}
     * has been called, the buffer may be modified. 
     */
    public void write(ByteBuffer source) throws IOException {
        pendingBuffers.add(source.duplicate());
    }
    
    /**
     * Flushes the contents of the buffer to the socket channel.
     *
     * <p>This only flushes what can be written immediately. This is limited by
     * the underlying socket implementation's buffer size, network load, etc.
     *
     * @return true if all remaining data has been flushed.
     */
    public boolean flush() throws IOException {
        // verify we can still write
        if(!selectionKey.isValid()) throw new IOException("Key cancelled");
        
        // prepare the data to write
        long totalBytes = 0;
        ByteBuffer[] buffers = new ByteBuffer[1 + pendingBuffers.size()];
        buffer.flip();
        buffers[0] = buffer.getBuffer();
        totalBytes += buffers[0].remaining();
        for(int b = 0; b < pendingBuffers.size(); b++) {
            buffers[b + 1] = (ByteBuffer)pendingBuffers.get(b);
            totalBytes += buffers[b + 1].remaining();
        }
        
        // write what we can
        long written = channel.write(buffers);
        long leftOver = totalBytes - written;
        
        // copy the unwritten buffers into the first buffer
        buffer.position(Math.min((int)written, buffer.limit()));
        buffer.compact();
        if(leftOver > 0) {
            buffer.grow(buffer.capacity() + (int)leftOver);
            for(int b = 0; b < pendingBuffers.size(); b++) {
                buffer.put((ByteBuffer)pendingBuffers.get(b));
            }
        }
        pendingBuffers.clear();
        
        // if we have nothing more to write
        if(leftOver == 0) {
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
            return true;
        } else {
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
            return false;
        }
    }
}
