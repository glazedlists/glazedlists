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
 * Helper class for writing Strings to a Channel with one-byte characters..
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ByteChannelWriter {

    /** the SocketChannel to write to */
    private SocketChannel channel;
    
    /** the ByteBuffer to parse text from */
    private ByteBuffer buffer;
    
    /**
     * Create a writer for the specified channel.
     */
    public ByteChannelWriter(SocketChannel channel) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocateDirect(1024);
    }
    
    /**
     * Writes the specified String.
     */
    public void write(CharSequence text) throws IOException {
        for(int c = 0; c < text.length(); c++) {
            if(buffer.remaining() == 0) flush();
            buffer.put((byte)text.charAt(c));
        }
    }
    
    /**
     * Flushes the contents of the buffer to the socket channel.
     */
    public void flush() throws IOException {
        buffer.flip();
        while(buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.clear();
    }
}
