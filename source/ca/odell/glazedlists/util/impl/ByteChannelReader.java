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
 * Helper class for reading Strings within a Channel..
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ByteChannelReader {

    /** the SocketChannel to read from */
    private SocketChannel channel;

    /** the ByteBuffer to parse text from */
    private ByteBuffer buffer;
    
    /** the CharSequence for this ByteBuffer */
    private CharSequence bytesAsCharSequence;
    
    /** whether the channel is out of data */
    private boolean eof = false;
    
    /**
     * Create a reader for the specified channel.
     */
    public ByteChannelReader(SocketChannel channel) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocateDirect(1024);
        bytesAsCharSequence = new ByteBufferSequence(buffer);
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

        eof = true;
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
        if(matcher.start() != 0) throw new ParseException(bytesAsCharSequence.toString(), 0);
        buffer.position(buffer.position() + matcher.end());
        return matcher.end();
    }
    
    /**
     * Reads the String up until the specified regular expression and returns it.
     * This advances the buffer's position to the end of the regular expression.
     *
     * @throws ParseException if the specified expression is not in the input buffer
     */
    public String readUntil(String regex) throws IOException, ParseException {
        Matcher matcher = Pattern.compile(regex).matcher(bytesAsCharSequence);
        while(!matcher.find()) {
            int bytesRead = suck();
            if(bytesRead == 0) throw new ParseException(regex + " is not in current buffer", 0);
        }
        String result = bytesAsCharSequence.subSequence(0, matcher.start()).toString();
        buffer.position(buffer.position() + matcher.end());
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
     * @return the number of bytes read, possibly 0 if there is nothing to read immediately
     * @throws IOException if the input buffer is full
     */
    private int suck() throws IOException {
        if(buffer.remaining() == 0) throw new IOException("Input buffer is full");
        if(eof) return 0;
        
        // read in another chunk
        buffer.compact();
        int bytesRead = channel.read(buffer);
        buffer.flip();
        
        // throw an exception if nothing is read, or the end of file is reached
        if(bytesRead < 0) {
            eof = true;
            return 0;
        }
        
        // return the number of bytes read
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
     * ÊReturns a string containing the characters in this sequence in the same order as this sequence.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for(int c = 0; c < length(); c++) {
            result.append(charAt(c));
        }
        return result.toString();
    }
}

