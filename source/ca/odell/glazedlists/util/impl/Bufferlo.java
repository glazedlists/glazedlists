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

/**
 * A high-level class for moving data and parsing protocols.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Bufferlo implements CharSequence {
    
    /** the buffers managed by this Bufferlo */
    private LinkedList buffers = new LinkedList();
    
    /** write to this bufferlo */
    private BufferloOutputStream out = new BufferloOutputStream();
    
    /** read from this bufferlo */
    private BufferloInputStream in = new BufferloInputStream();

    /** whether this is read only */
    private boolean readOnly = false;
    
    /** 
     * Populate this Bufferlo with the data from the specified channel.
     */
    public int readFromChannel(ReadableByteChannel source) throws IOException {
        int totalRead = 0;
        while(true) {
            // we need a new place to write into
            ByteBuffer writeInto = getWriteIntoBuffer();
            
            // read in
            int bytesRead = source.read(writeInto);
            doneWriting();

            // figure out what to do next
            if(bytesRead < 0 && totalRead == 0) return bytesRead;
            else if(bytesRead <= 0) return totalRead;
            else totalRead += bytesRead;
        }
    }
    
    /** 
     * Write the content of this Bufferlo to the specified channel.
     */
    public long writeToChannel(GatheringByteChannel target, SelectionKey selectionKey) throws IOException {
        // verify we can still write
        if(!selectionKey.isValid()) throw new IOException("Key cancelled");
        
        // nothing to write
        if(length() == 0) return 0;
        
        // make all buffers readable
        ByteBuffer[] toWrite = new ByteBuffer[buffers.size()];
        for(int b = 0; b < buffers.size(); b++) {
            ByteBuffer buffer = (ByteBuffer)buffers.get(b);
            buffer.flip();
            toWrite[b] = buffer;
        }
        
        // write them all out
        long totalWritten = target.write(toWrite);
        
        // restore the state on all buffers
        for(ListIterator b = buffers.listIterator(); b.hasNext(); ) {
            ByteBuffer buffer = (ByteBuffer)b.next();

            if(!buffer.hasRemaining()) {
                b.remove();
            } else if(buffer.position() > 0) {
                int bytesLeftToRead = buffer.remaining();
                buffer.limit(buffer.capacity());
                ByteBuffer noneRead = buffer.slice();
                noneRead.position(bytesLeftToRead);
                noneRead.limit(bytesLeftToRead);
                b.set(noneRead);
            } else {
                buffer.position(buffer.limit());
            }
        }
        
        // adjust the key based on whether we have leftovers 
        if(length() > 0) {
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        } else {
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
        }

        // return the count of bytes written
        return totalWritten;
    }
    
    /**
     * Gets an InputStream that reads this buffer.
     */
    public BufferloInputStream getInputStream() {
        return in;
    }
    
    /**
     * Gets an OutputStream that writes this buffer.
     */
    public BufferloOutputStream getOutputStream() {
        return out;
    }
    
    /**
     * Duplicates the exact state of this buffer. The returned buffer is read-only.
     */
    public Bufferlo duplicate() {
        Bufferlo result = new Bufferlo();
        for(int i = 0; i < buffers.size(); i++) {
            ByteBuffer buffer = (ByteBuffer)buffers.get(i);
            result.buffers.add(removeTrailingSpace(buffer));
        }
        return result;
    }
    
    /**
     * Read the specified bytes into a new Bufferlo. The returned buffer is read-only.
     */
    public Bufferlo consume(int bytes) {
        assert(bytes >= 0 && bytes <= length());
        Bufferlo result = duplicate();
        result.limit(bytes);
        skip(bytes);
        return result;
    }
    
    /**
     * Writes the specified String to this Bufferlo.
     */
    public void write(String data) {
        append(stringToBytes(data));
    }
    
    /**
     * Converts the specified String to bytes, one byte per character. The input
     * String must contain US-ASCII one-byte characters or the result of this
     * method is not specified.
     */
    public static final ByteBuffer stringToBytes(String in) {
        try {
            return ByteBuffer.wrap(in.getBytes("US-ASCII"));
        } catch(UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
    
    /**
     * Writes the specified data to this bufferlo. This shortens the last ByteBuffer
     * in the added set so that it will not be written to. This allows a read-only
     * buffer to be added without it ever being modified.
     * 
     * This will consume the specified Bufferlo.
     */
    public Bufferlo append(Bufferlo data) {
        buffers.addAll(data.buffers);
        data.buffers.clear();
        return this;
    }
    
    /**
     * Appends a the specified data. The data will be consumed so it should be
     * a duplicate if it is to be reused.
     */
    public Bufferlo append(ByteBuffer data) {
        ByteBuffer myCopy = data.slice();
        myCopy.position(myCopy.limit());
        buffers.add(myCopy);
        data.position(data.limit());
        return this;
    }
    
    /**
     * Limits this Bufferlo to the specified size.
     */
    public void limit(int bytes) {
        int bytesLeft = bytes;
        for(ListIterator b = buffers.listIterator(); b.hasNext(); ) {
            ByteBuffer current = (ByteBuffer)b.next();
            
            if(bytesLeft <= 0) {
                b.remove();
            
            } else if(current.capacity() >= bytesLeft) {
                current.position(bytesLeft);
                current.limit(bytesLeft);
            }

            bytesLeft -= current.capacity();
        }
    }
    
    /**
     * Skips the specified number of bytes.
     */
    public void skip(int bytes) {
        assert(bytes >= 0 && bytes <= length());
        int bytesLeft = bytes;
        for(ListIterator b = buffers.listIterator(); b.hasNext(); ) {
            ByteBuffer current = (ByteBuffer)b.next();
            
            if(bytesLeft >= current.limit()) {
                bytesLeft -= current.limit();
                b.remove();
            } else {
                current.position(bytesLeft);
                ByteBuffer smaller = current.slice();
                smaller.position(smaller.limit());
                b.set(smaller);
                break;
            }
        }
    }
    
    /**
     * Creates a new ByteBuffer identical to the parameter but without space trailing
     * after the limit. This allows a buffer to be added to the Bufferlo without
     * that buffer ever being written to.
     *
     * This assumes that position == limit.
     */
    private ByteBuffer removeTrailingSpace(ByteBuffer buffer) {
        ByteBuffer clone = buffer.duplicate();
        clone.position(0);
        ByteBuffer noTrailingSpace = clone.slice();
        noTrailingSpace.position(noTrailingSpace.limit());
        return noTrailingSpace;
    }
    
    /**
     * Write to the Bufferlo as a Stream.
     */
    class BufferloOutputStream extends OutputStream {
        public void write(int b) {
            ByteBuffer writeBuffer = getWriteIntoBuffer();
            writeBuffer.put((byte)b);
            doneWriting();
        }
    }
    
    /**
     * Read from the Bufferlo as a Stream.
     */
    class BufferloInputStream extends InputStream {
        public int read() {
            ByteBuffer readBuffer = getReadFromBuffer();
            if(readBuffer == null) return -1;
            byte result = readBuffer.get();
            doneReading();
            return result;
        }
    }
    
    /**
     * Gets a buffer that we can read from. This buffer must be flipped back into
     * write mode when this is complete by calling doneReading().
     */
    private ByteBuffer getReadFromBuffer() {
        if(buffers.isEmpty()) return null;
        
        ByteBuffer readFrom = (ByteBuffer)buffers.getFirst();
        readFrom.flip();
        
        return readFrom;
    }
    
    /**
     * Finishes reading the current read from buffer.
     */
    private void doneReading() {
        ByteBuffer readFrom = (ByteBuffer)buffers.getFirst();
        
        // if we've exhaused this buffer
        if(!readFrom.hasRemaining()) {
            buffers.removeFirst();

        // we still have more to read from this buffer
        } else {
            int bytesLeftToRead = readFrom.remaining();
            readFrom.limit(readFrom.capacity());
            ByteBuffer noneRead = readFrom.slice();
            noneRead.position(bytesLeftToRead);
            noneRead.limit(bytesLeftToRead);
            buffers.set(0, noneRead);
        }
    }
    
    /**
     * Gets a buffer that we can write data into.
     */
    private ByteBuffer getWriteIntoBuffer() {
        if(readOnly) throw new IllegalStateException("Read only");
        
        // we have a buffer with space remaining
        if(!buffers.isEmpty()) {
            ByteBuffer last = (ByteBuffer)buffers.getLast();
            if(last.position() < last.capacity()) {
                last.limit(last.capacity());
                return last;
            }
        }

        // we need to create a new buffer
        ByteBuffer writeInto = getNewBuffer();
        buffers.addLast(writeInto);
        return writeInto;
    }
    
    /**
     * Finishes writing the current buffer.
     */
    private void doneWriting() {
        ByteBuffer writeInto = (ByteBuffer)buffers.getLast();
        writeInto.limit(writeInto.position());
    }

    
    
    /**
     * Get the number of bytes available.
     */
    public int length() {
        int bytesAvailable = 0;
        for(Iterator b = buffers.iterator(); b.hasNext(); ) {
            ByteBuffer buffer = (ByteBuffer)b.next();
            bytesAvailable += buffer.position();
        }
        return bytesAvailable;
    }
    
    /**
     * Gets the character at the specified index.
     */
    public char charAt(int index) {
        int bytesLeft = index;
        for(Iterator b = buffers.iterator(); b.hasNext(); ) {
            ByteBuffer buffer = (ByteBuffer)b.next();
            if(bytesLeft < buffer.position()) {
                return (char)buffer.get(bytesLeft);
            } else {
                bytesLeft -= buffer.position();
            }
        }
        throw new IndexOutOfBoundsException();
    }
    
    /**
     * Returns a new character sequence that is a subsequence of this.
     */
    public CharSequence subSequence(int start, int end) {
        Bufferlo clone = duplicate();
        clone.skip(start);
        clone.limit(end - start);
        return clone;
    }
    
    /**
     * Gets this Bufferlo as a String.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for(int c = 0; c < length(); c++) {
            result.append(charAt(c));
        }
        return result.toString();
    }
    
    public String toDebugString() {
        return "BUFFERLO {" + buffers + "}";
    }
    

    /**
     * Finds the first index of the specified regular expression.
     *
     * @return the index of the specified regular expression, or -1 if that
     *      regular expression does not currently exist in the ByteBuffer. This
     *      index is volatile because further operations on this ByteBufferParser
     *      may influence the location of the specified regular expression.
     */
    public int indexOf(String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(this);
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
    public int consume(String regex) throws ParseException {
        Matcher matcher = Pattern.compile(regex).matcher(this);
        if(!matcher.find()) throw new ParseException(regex + " is not in current buffer", 0);
        if(matcher.start() != 0) throw new ParseException(regex + " is not a prefix of " + this, 0);
        skip(matcher.end());
        return matcher.end();
    }

    /**
     * Reads the String up until the specified regular expression and returns it.
     * This advances the buffer's position to the end of the regular expression.
     *
     * @throws ParseException if the specified expression is not in the input buffer
     */
    public String readUntil(String regex) throws ParseException {
        return readUntil(regex, true);
    }
    
    /**
     * Reads the String up until the specified regular expression and returns it.
     *
     * @param consume true to advance the buffer's position to the end of the
     *      regular expression, or false to not modify the buffer
     * @throws ParseException if the specified expression is not in the input buffer
     */
    public String readUntil(String regex, boolean consume) throws ParseException {
        Matcher matcher = Pattern.compile(regex).matcher(this);
        if(!matcher.find()) throw new ParseException(regex + " is not in current buffer", 0);
        String result = subSequence(0, matcher.start()).toString();
        if(consume) skip(matcher.end());
        return result;
    }

    /**
     * Gets a new buffer by creating it or removing it from the pool.
     */
    private ByteBuffer getNewBuffer() {
        int BUFFER_SIZE = 8 * 1024; 
        return ByteBuffer.allocateDirect(BUFFER_SIZE);
    }
}
