/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

import java.util.*;
// NIO buffers
import java.nio.*;
// regular expressions
import java.util.regex.*;
import java.text.ParseException;

/**
 * Helper class for parsing text within a ByteBuffer.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ByteBufferParser {

    /** the ByteBuffer to parse text from */
    private ByteBuffer byteBuffer;
    
    /** the CharSequence for this ByteBuffer */
    private CharSequence bytesAsCharSequence;
    
    /**
     * Create a parser for the specified buffer.
     */
    public ByteBufferParser(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        bytesAsCharSequence = new ByteBufferSequence(byteBuffer);
    }
    
    /**
     * Finds the first index of the specified regular expression.
     *
     * @return the index of the specified regular expression, or -1 if that
     *      regular expression does not currently exist in the ByteBuffer.
     */
    public int indexOf(String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(bytesAsCharSequence);
        if(matcher.find()) {
            return matcher.start();
        } else {
            return -1;
        }
    }
    
    /**
     * Consumes the specified regular expression. This simply advances the buffer's
     * position to the end of the regular expression.
     *
     * @throws ParseException if the specified regular expression is not a prefix
     *      of the buffer.
     */
    public int consume(String regex) throws ParseException {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(bytesAsCharSequence);
        if(!matcher.find()) throw new ParseException(bytesAsCharSequence.toString(), 0);
        if(matcher.start() != 0) throw new ParseException(bytesAsCharSequence.toString(), 0);
        byteBuffer.position(byteBuffer.position() + matcher.end());
        return matcher.end();
    }
    
    /**
     * Reads the String up until the specified regular expression and returns it.
     * This advances the buffer's position to the end of the regular expression.
     */
    public String readUntil(String regex) throws ParseException {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(bytesAsCharSequence);
        if(!matcher.find()) throw new ParseException(bytesAsCharSequence.toString(), 0);
        String result = bytesAsCharSequence.subSequence(0, matcher.start()).toString();
        byteBuffer.position(byteBuffer.position() + matcher.end());
        return result;
    }
    
    /**
     * Gets the contents of the ByteBuffer as a String.
     */
    public String toString() {
        return bytesAsCharSequence.toString();
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

