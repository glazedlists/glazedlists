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
    
    /** the ByteBuffer to parse text from */
    private ByteBuffer buffer;
    
    /** initially the write buffer consumes a single kilobyte of memory */
    private static final int INITIAL_BUFFER_SIZE = 1024;
    
    /**
     * Create a writer for the specified channel.
     */
    public ByteChannelWriter(SocketChannel channel, SelectionKey selectionKey) {
        this.channel = channel;
        this.selectionKey = selectionKey;
        this.buffer = ByteBuffer.allocateDirect(INITIAL_BUFFER_SIZE);
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
        write(textAsByteBuffer);
    }
    
    /**
     * Writes the specified ByteBuffer.
     *
     * <p>This write is performed in two stages. First all data is written directly
     * to the channel until the channel is full. Then remaining data is copied to
     * a local buffer to be written later.
     */
    public void write(ByteBuffer source) throws IOException {
        // write local bytes first to maintain sequence
        flush();

        // write all we can directly to the channel
        if(!buffer.hasRemaining()) {
            while(source.hasRemaining()) {
                int written = channel.write(source);
                if(written == 0) break;
            }
        }
        
        // if we've written all we need to, we're done
        if(!source.hasRemaining()) return;
        
        // resize the local buffer as necessary
        if(buffer.remaining() < source.remaining()) {
            growLocalBuffer(source.remaining());
        }
        
        // write the rest to local buffers
        buffer.put(source);
        requestFlush();
    }
    
    /**
     * Marks this writer to flush when the channel can be written.
     */
    public void requestFlush() {
        // mark this needing a flush
        if(buffer.position() > 0) {
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        }
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
        buffer.flip();
        while(true) {
            // verify we can still write
            if(!selectionKey.isValid()) throw new IOException("Key cancelled");
            
            // if we have nothing more to write
            if(!buffer.hasRemaining()) {
                selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                buffer.clear();
                return true;
            }

            // write some bytes
            int written = channel.write(buffer);

            // if we cannot write anything more at the current time
            if(written == 0) {
                buffer.compact();
                return false;
            }
        }
    }
    
    /**
     * Grow the local buffer to the specified minimum size.
     */
    public void growLocalBuffer(int minsize) throws IOException {
        throw new IOException("Failed to grow local write buffer to " + minsize + " bytes");
    }
}
