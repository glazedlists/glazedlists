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
 * The AbstractBuffer is a helper class for implementing custom Buffers. Since
 * the Buffer class is not extendable for practical purposes,
 * this class is only a Buffer by similarity of interface and not by true
 * inheritance.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class AbstractBuffer {
    
    protected int capacity = 0;
    protected int limit = 0;
    protected int mark = -1;
    protected int position = 0;
    
    /**
     * Returns this buffer's capacity.
     */
    public int capacity() {
        return capacity;
    }
    
    /**
     * Clears this buffer. The position is set to zero, the limit is set to 
     * the capacity, and the mark is discarded.
     *
     * Invoke this method before using a sequence of channel-read or 
     * put operations to fill this buffer. For example:
     *
     *   buf.clear();     // Prepare buffer for reading
     *   in.read(buf);    // Read data
     *
     * This method does not actually erase the data in the buffer, but it 
     * is named as if it did because it will most often be used in situations in
     * which that might as well be the case.
     */
    public AbstractBuffer clear() {
        position = 0;
        limit = capacity;
        mark = -1;
        return this;
    }
    
    /**
     * Flips this buffer. The limit is set to the current position and then 
     * the position is set to zero. If the mark is defined then it is  discarded.
     *
     * After a sequence of channel-read or put operations, invoke 
     * this method to prepare for a sequence of channel-write or relative 
     * get operations. For example:
     *
     * buf.put(magic);    // Prepend header
     * in.read(buf);      // Read data into rest of buffer
     * buf.flip();        // Flip buffer
     * out.write(buf);    // Write header + data to channel
     *
     * This method is often used in conjunction with the compact method when
     * transferring data from  one place to another.
     */
    public AbstractBuffer flip() {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }
    
    /**
     * Tells whether there are any elements between the current position and  the limit.
     */
    public boolean hasRemaining() {
        return position < limit;
    }
    
    /**
     * Tells whether or not this buffer is read-only.
     */
    public boolean isReadOnly() {
        return false;
    }
    
    /**
     * Returns this buffer's limit.
     */
    public int limit() {
        return limit;
    }
    
    /**
     * Sets this buffer's limit. If the position is larger than the new limit
     * then it is set to the new limit. If the mark is defined and larger than the
     * new limit then it is discarded
     */
    public AbstractBuffer limit(int newLimit) {
        this.limit = newLimit;
        if(position > limit) position = limit;
        if(mark > limit) mark = -1;
        return this;
    }
    
    /**
     * Sets this buffer's mark at its position.
     */
    public AbstractBuffer mark() {
        this.mark = position;
        return this;
    }
    
    /**
     * Returns this buffer's position.
     */
    public int position() {
        return position;
    }
    
    /**
     * Sets this buffer's position. If the mark is defined and larger than the
     * new position then it is discarded.
     */
    public AbstractBuffer position(int newPosition) {
        this.position = newPosition;
        if(mark > position) mark = -1;
        return this;
    }
    
    /**
     * Returns the number of elements between the current position and the  limit.
     */
    public int remaining() {
        return limit - position;
    }
    
    /**
     * Resets this buffer's position to the previously-marked position.
     * Invoking this method neither changes nor discards the mark's  value.
     */
    public AbstractBuffer reset() {
        if(mark == -1) throw new InvalidMarkException();
        position = mark;
        return this;
    }
    
    /**
     * Rewinds this buffer. The position is set to zero and the mark is  discarded.
     *
     * Invoke this method before a sequence of channel-write or get 
     * operations, assuming that the limit has already been set 
     * appropriately. For example:
     *
     * out.write(buf);    // Write remaining data
     * buf.rewind();      // Rewind buffer
     * buf.get(array);    // Copy data into array
     */
    public AbstractBuffer rewind() {
        position = 0;
        mark = -1;
        return this;
    }
    
    /**
     * Returns true if this buffer is in a consistent state.
     */
    protected boolean consistentState() {
        return((mark == -1 || 0 <= mark) && mark <= position && position <= limit && limit <= capacity);
    }
}
