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
 * A CharSequence where each character is a single byte of this parser's 
 * ByteBuffer. The first character is the character at the byte buffer's 
 * position, and the last character is the character immediately preceding
 * the byte buffer's limit.
 *
 * <p>This is used to perform regular expressions against the ByteBuffer.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ByteBufferCharSequence implements CharSequence {
    
    /** the byte buffer viewed by this char sequence */
    private ResizableByteBuffer byteBuffer;
    
    /** start offset after position */
    private int startOffset = 0;
    
    /** end offset before limit */
    private int endOffset = 0;

    /**
     * Creates a new ByteBufferSequence that provides characters for the
     * specified ByteBuffer.
     */
    public ByteBufferCharSequence(ResizableByteBuffer byteBuffer) {
        this(byteBuffer, 0, 0);
    }
    /**
     * Creates a new ByteBufferSequence that provides characters for the
     * specified ByteBuffer with the specified start offsets.
     */
    private ByteBufferCharSequence(ResizableByteBuffer byteBuffer, int startOffset, int endOffset) {
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
        return new ByteBufferCharSequence(byteBuffer, start + startOffset, length() - end + endOffset);
    }

    /**
     * �Returns a string containing the characters in this sequence in the same 
     * order as this sequence.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for(int c = 0; c < length(); c++) {
            result.append(charAt(c));
        }
        return result.toString();
    }
}
