/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

import java.util.*;
// for being a JUnit test case
import junit.framework.*;
// NIO
import java.nio.*;
import java.nio.channels.*;
import java.io.*;

/**
 * This test verifies that the ResizableByteBuffer class works ok.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ResizableByteBufferTest extends TestCase {

    /**
     * Prepare for the test.
     */
    public void setUp() {
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
    }

    /**
     * Verifies that the ResizableByteBuffer grows properly.
     */
    public void testGrow() {
        // create a resizable buffer of size 10
        ResizableByteBuffer resizable = new ResizableByteBuffer(10);
        ByteBuffer buffer = createRandomBuffer(10);
        resizable.clear();
        resizable.put(buffer);
        
        // validate the resizable buffer equals its contents
        resizable.flip();
        buffer.flip();
        assertTrue(buffersEqual(buffer, resizable));
        
        // clear the resizable buffer and populate it to size 20
        ByteBuffer buffer2 = createRandomBuffer(20);
        resizable.grow(buffer2.capacity());
        resizable.clear();
        resizable.put(buffer2);

        // validate that it equals its contents
        resizable.flip();
        buffer2.flip();
        assertTrue(buffersEqual(buffer2, resizable));
        
        // add a buffer of size 30
        ByteBuffer buffer3 = ByteBuffer.allocate(30);
        buffer3.put((ByteBuffer)buffer2.flip());
        buffer3.put((ByteBuffer)buffer.flip());
        resizable.grow(30);
        resizable.put((ByteBuffer)buffer.flip());
        
        // validate it equals its contents
        buffer3.flip();
        resizable.flip();
        assertTrue(buffersEqual(buffer3, resizable));
        
        // validate that consume works in a single buffer
        resizable.flip();
        buffer.flip();
        resizable.position(20);
        assertEquals(buffer, resizable.consume(10));
        
        // validate that consume works across buffers
        resizable.flip();
        buffer3.flip();
        ByteBuffer firstThirty = resizable.consume(30);
        assertEquals(buffer3, firstThirty);
        resizable.flip();
    }
    
    /**
     * Tests that the compact() method works as advertised.
     */
    public void testCompactMultipleBuffers() {
        // create a resizable buffer of size 10
        ResizableByteBuffer resizable = new ResizableByteBuffer(10);
        resizable.grow(20);
        resizable.grow(50);
        resizable.grow(100);
        
        resizable.position(90);
        resizable.limit(100);
        resizable.compact();
        assertEquals(10, resizable.position());
        assertEquals(90, resizable.remaining());
    }
    
    /**
     * Tests that the compact() method works as advertised.
     */
    public void testCompactSingleBuffer() {
        // create a resizable buffer of size 10
        ResizableByteBuffer resizable = new ResizableByteBuffer(10);
        resizable.clear();
        resizable.position(8);
        resizable.compact();
        assertEquals(8, resizable.remaining());
    }

    /**
     * Creates a random buffer full of random data.
     */
    private static ByteBuffer createRandomBuffer(int size) {
        Random dice = new Random();
        byte[] result = new byte[size];
        dice.nextBytes(result);
        return ByteBuffer.wrap(result);
    }
    
    /**
     * Tests if the specified buffers are equal.
     */
    private boolean buffersEqual(ByteBuffer buffer, ResizableByteBuffer resizable) {
        if(buffer.remaining() != resizable.remaining()) return false;
        boolean result = true;
        while(buffer.hasRemaining()) {
            if(buffer.get() != resizable.get()) result = false;
        }
        return result;
    }
}
