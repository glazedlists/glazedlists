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
 * Helper class for reading Strings within a Channel..
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ByteChannelReader {

    /** logging */
    private static Logger logger = Logger.getLogger(ByteChannelReader.class.toString());

    /** the SocketChannel to read from */
    private SocketChannel channel;
    
    /** the ByteBuffer to parse text from must always ready to be read from */
    private int initialBufferSize = 256;
    private ResizableByteBuffer buffer = new ResizableByteBuffer(initialBufferSize);

    /** the CharSequence for this ByteBuffer */
    private CharSequence bytesAsCharSequence = new ByteBufferCharSequence(buffer);

    /**
     * Create a reader for the specified channel.
     */
    public ByteChannelReader(SocketChannel channel) {
        this.channel = channel;
        // buffer is always ready to be read from
        buffer.flip();
    }
    
    /**
     * Creates a reader for the specified String. This is useful only for testing
     * the ByteChannelReader.
     */
    public ByteChannelReader(String text) {
        try {
            byte[] bytes = text.getBytes("US-ASCII");
            ByteBuffer word = ByteBuffer.wrap(bytes);
            buffer.grow(word.limit());
            buffer.clear();
            buffer.put(word);
            buffer.flip();
        } catch(java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        channel = null;
    }
    
    /**
     * Finds the first index of the specified regular expression.
     *
     * @return the index of the specified regular expression, or -1 if that
     *      regular expression does not currently exist in the ByteBuffer. This
     *      index is volatile because further operations on this ByteBufferParser
     *      may influence the location of the specified regular expression.
     */
    public int indexOf(String regex) throws IOException {
        Matcher matcher = Pattern.compile(regex).matcher(bytesAsCharSequence);
        if(!matcher.find()) return -1;
        return matcher.start();
    }
    
    /**
     * Consumes the specified regular expression. This simply advances the buffer's
     * position to the end of the regular expression.
     *
     * @throws ParseException if the specified expression is not in the input buffer
     * @return the number of bytes consumed.
     */
    public int consume(String regex) throws IOException, ParseException {
        Matcher matcher = Pattern.compile(regex).matcher(bytesAsCharSequence);
        if(!matcher.find()) throw new ParseException(regex + " is not in current buffer", 0);
        if(matcher.start() != 0) throw new ParseException(regex + " is not a prefix of " + bytesAsCharSequence.toString(), 0);
        buffer.position(buffer.position() + matcher.end());
        return matcher.end();
    }
    
    /**
     * Consumes the specified number of bytes and returns a read-only ByteBuffer
     * containing the consumed bytes. The returned ByteBuffer is only valid until
     * the next method call to ByteChannelReader.
     */
    public ByteBuffer readBytes(int bytes) throws IOException {
        if(!bytesAvailable(bytes)) throw new IllegalArgumentException();
        return buffer.consume(bytes);
    }

    /**
     * Reads all remaining bytes. 
     */
    public ByteBuffer readBytes() throws IOException {
        return readBytes(buffer.remaining());
    }

    /**
     * Reads the String up until the specified regular expression and returns it.
     * This advances the buffer's position to the end of the regular expression.
     *
     * @throws ParseException if the specified expression is not in the input buffer
     */
    public String readUntil(String regex) throws IOException, ParseException {
        return readUntil(regex, true);
    }
    
    /**
     * Reads the String up until the specified regular expression and returns it.
     *
     * @param consume true to advance the buffer's position to the end of the
     *      regular expression, or false to not modify the buffer
     * @throws ParseException if the specified expression is not in the input buffer
     */
    public String readUntil(String regex, boolean consume) throws IOException, ParseException {
        Matcher matcher = Pattern.compile(regex).matcher(bytesAsCharSequence);
        if(!matcher.find()) throw new ParseException(regex + " is not in current buffer", 0);
        String result = bytesAsCharSequence.subSequence(0, matcher.start()).toString();
        if(consume) buffer.position(buffer.position() + matcher.end());
        return result;
    }
    
    /**
     * Gets the contents of the ByteBuffer as a String.
     */
    public String toString() {
        return bytesAsCharSequence.toString();
    }
    
    /**
     * Returns true if the specified number of bytes are available.
     */
    public boolean bytesAvailable(int required) throws IOException {
        return(buffer.remaining() >= required);
    }
    
    /**
     * Fills the input buffer with as much content as possible.
     *
     * @return the number of bytes read, possibly 0 or possibly a negative value
     *      if the end of stream is reached.
     * @throws IllegalStateException if there are no bytes to read.
     * @throws EOFException when the end of the file is reached.
     */
    public int fill() throws IOException {
        buffer.compact();
        int totalBytesRead = 0;
        while(true) {
            // make sure there's room to read
            if(!buffer.hasRemaining()) buffer.grow(buffer.capacity() * 2);
            
            // read in some bytes if possible
            int bytesRead = buffer.pull(channel);
            
            // if there's nothing to read
            if(bytesRead <= 0) {
                buffer.flip();
                if(totalBytesRead != 0) return totalBytesRead;
                else if(bytesRead < 0) throw new EOFException("End of stream");
                else throw new IllegalStateException("No bytes to read");
                
            // keep reading
            } else {
                totalBytesRead += bytesRead;
            }
        }
    }
}
