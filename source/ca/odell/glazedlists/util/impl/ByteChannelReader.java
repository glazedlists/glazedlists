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
    private ByteBuffer buffer;
    
    /** the CharSequence for this ByteBuffer */
    private CharSequence bytesAsCharSequence;
    
    /**
     * Create a reader for the specified channel.
     */
    public ByteChannelReader(SocketChannel channel) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocateDirect(1024);
        bytesAsCharSequence = new ByteBufferSequence(buffer);
        // buffer is always ready to be read from
        buffer.limit(0);
    }
    
    /**
     * Creates a reader for the specified String. This is useful only for testing
     * the ByteChannelReader.
     */
    public ByteChannelReader(String text) {
        try {
            byte[] bytes = text.getBytes("US-ASCII");
            buffer = ByteBuffer.wrap(bytes);
        } catch(java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        channel = null;
        bytesAsCharSequence = new ByteBufferSequence(buffer);
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
        while(!matcher.find()) {
            int bytesRead = suck();
            if(bytesRead == 0) return -1;
        }
        return matcher.start();
    }
    
    /**
     * Consumes the specified regular expression. This simply advances the buffer's
     * position to the end of the regular expression.
     *
     * @throws ParseException if the specified expression is not in the input buffer
     */
    public int consume(String regex) throws IOException, ParseException {
        Matcher matcher = Pattern.compile(regex).matcher(bytesAsCharSequence);
        while(!matcher.find()) {
            int bytesRead = suck();
            if(bytesRead == 0) throw new ParseException(regex + " is not in current buffer", 0);
        }
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
        if(bytesAvailable() < bytes) throw new IllegalArgumentException();

        // prepare the result
        ByteBuffer result = buffer.asReadOnlyBuffer();
        result.limit(result.position() + bytes);

        // consume from the working buffer
        buffer.position(buffer.position() + bytes);
        
        return result;
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
     * Calculate how many bytes are ready to be read immediately.
     */
    public int bytesAvailable() throws IOException {
        // read more bytes if possible
        suckle();

        // return the number of bytes available
        return buffer.remaining();
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
        while(!matcher.find()) {
            int bytesRead = suck();
            if(bytesRead == 0) throw new ParseException(regex + " is not in current buffer", 0);
        }
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
     * Fills the buffer with the contents of the channel.
     *
     * <p>This temporarily flips the buffer from read mode into write mode in order
     * to add more bytes from the channel.
     *
     * @return the number of bytes read, possibly 0 if there is nothing to read immediately
     * @throws IOException if the input buffer is full or if the end of the stream is reached
     */
    private int suck() throws IOException {
        if(buffer.remaining() == buffer.capacity()) throw new IOException("Input buffer is full");
        
        // read in another chunk
        int bytesRead = suckle();
        
        // throw an exception if nothing is read, or the end of file is reached
        if(bytesRead < 0) throw new EOFException("End of stream");
        
        // return the number of bytes read
        return bytesRead;
    }
    
    /**
     * Fills the buffer with as much content as possible. Unlike {@link #suck()},
     * {@link #suckle()} does not mandate that more data be available.
     *
     * @return the number of bytes read, possibly 0 or possibly a negative value
     *      if the end of stream is reached.
     */
    private int suckle() throws IOException {
        buffer.compact();
        int bytesRead = channel.read(buffer);
        buffer.flip();
        return bytesRead;
    }
}

    
/**
 * A CharSequence where each character is a single byte of this parser's 
 * ByteBuffer. The first character is the character at the byte buffer's 
 * position, and the last character is the character immediately preceding
 * the byte buffer's limit.
 *
 * <p>This is used to perform regular expressions against the ByteBuffer.
 */
class ByteBufferSequence implements CharSequence {
    
    /** the byte buffer viewed by this char sequence */
    private ByteBuffer byteBuffer;
    
    /** start offset after position */
    private int startOffset = 0;
    
    /** end offset before limit */
    private int endOffset = 0;

    /**
     * Creates a new ByteBufferSequence that provides characters for the
     * specified ByteBuffer.
     */
    public ByteBufferSequence(ByteBuffer byteBuffer) {
        this(byteBuffer, 0, 0);
    }
    /**
     * Creates a new ByteBufferSequence that provides characters for the
     * specified ByteBuffer with the specified start offsets.
     */
    private ByteBufferSequence(ByteBuffer byteBuffer, int startOffset, int endOffset) {
        if(startOffset < 0) throw new IllegalArgumentException();
        if(endOffset < 0) throw new IllegalArgumentException();
        if(byteBuffer.remaining() - startOffset - endOffset < 0) throw new IllegalArgumentException();

        this.byteBuffer = byteBuffer;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }
    
    /**
     * Returns the character at the specified index.
     */
    public char charAt(int index) {
        return (char)byteBuffer.get(byteBuffer.position() + startOffset + index);
    }

    /**
     * Returns the length of this character sequence.
     */
    public int length() {
        return byteBuffer.remaining() - startOffset - endOffset;
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int start, int end) {
        return new ByteBufferSequence(byteBuffer, start + startOffset, length() - end + endOffset);
    }

    /**
     * �Returns a string containing the characters in this sequence in the same order as this sequence.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for(int c = 0; c < length(); c++) {
            result.append(charAt(c));
        }
        return result.toString();
    }
}

