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
// NIO buffers
import java.nio.*;
// regular expressions
import java.util.regex.*;
import java.text.ParseException;
import java.io.IOException;

/**
 * This test verifies that the ByteChannelReader works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ByteChannelReaderTest extends TestCase {
    
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
     * Tests that consume() works.
     */
    public void testConsume() {
        try {
            ByteChannelReader parser = new ByteChannelReader("hello world");
            parser.consume("hell");
            assertEquals("o world", parser.toString());
            parser.consume("[a-z]\\s[a-z]");
            assertEquals("orld", parser.toString());
            parser.consume("orld");
            assertEquals("", parser.toString());
        } catch(ParseException e) {
            fail(e.getMessage());
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that consume() throws exceptions when the regular expression is not
     * contained.
     */
    public void testConsumeBadInput() {
        try {
            ByteChannelReader parser = new ByteChannelReader("hello world");
            parser.consume("earth");
            fail();
        } catch(ParseException e) {
            // exception is desired output
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that consume() throws exceptions when the regular expression does not
     * start at the beginning of the String..
     */
    public void testConsumeNotAtStart() {
        try {
            ByteChannelReader parser = new ByteChannelReader("hello world");
            parser.consume("ello");
            fail();
        } catch(ParseException e) {
            // exception is desired output
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that readUntil() works.
     */
    public void testReadUntil() {
        try {
            ByteChannelReader parser = new ByteChannelReader("hello world");
            String hello = parser.readUntil("\\s");
            assertEquals("hello", hello);
            assertEquals("world", parser.toString());
            String worl = parser.readUntil("[abcde]+");
            assertEquals("worl", worl);
            assertEquals("", parser.toString());
        } catch(ParseException e) {
            fail(e.getMessage());
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that readUntil() throws an exception if the specified text is not found.
     */
    public void testReadUntilBadInput() {
        try {
            ByteChannelReader parser = new ByteChannelReader("hello world");
            String result = parser.readUntil("earth");
            fail();
        } catch(ParseException e) {
            // exception is desired output
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that indexOf() works.
     */
    public void testIndexOf() {
        try {
            ByteChannelReader parser = new ByteChannelReader("hello world");
            int worldIndex = parser.indexOf("w");
            assertEquals(6, worldIndex);
            assertEquals("hello world", parser.toString());
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that indexOf() returns -1 if the specified text is not found.
     */
    public void testIndexOfBadInput() {
        try {
            ByteChannelReader parser = new ByteChannelReader("hello world");
            int tIndex = parser.indexOf("t");
            assertEquals(-1, tIndex);
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }
}
