/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl.test;

import java.util.*;
// for being a JUnit test case
import junit.framework.*;
// NIO buffers
import java.nio.*;
// regular expressions
import java.util.regex.*;
// class to test
import ca.odell.glazedlists.util.impl.*;
import java.text.ParseException;

/**
 * This test verifies that the ByteBufferParser works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ByteBufferParserTest extends TestCase {
    
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
     * Gets a parser for the specified string.
     */
    private ByteBufferParser parserFor(String string) {
        try {
            byte[] bytes = string.getBytes("US-ASCII");
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            ByteBufferParser parser = new ByteBufferParser(byteBuffer);
            return parser;
        } catch(java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that consume() works.
     */
    public void testConsume() {
        try {
            ByteBufferParser parser = parserFor("hello world");
            parser.consume("hell");
            assertEquals("o world", parser.toString());
            parser.consume("[a-z]\\s[a-z]");
            assertEquals("orld", parser.toString());
            parser.consume("orld");
            assertEquals("", parser.toString());
        } catch(ParseException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that consume() throws exceptions when the regular expression is not
     * contained.
     */
    public void testConsumeBadInput() {
        try {
            ByteBufferParser parser = parserFor("hello world");
            parser.consume("earth");
            fail();
        } catch(ParseException e) {
            // exception is desired output
        }
    }

    /**
     * Tests that consume() throws exceptions when the regular expression does not
     * start at the beginning of the String..
     */
    public void testConsumeNotAtStart() {
        try {
            ByteBufferParser parser = parserFor("hello world");
            parser.consume("ello");
            fail();
        } catch(ParseException e) {
            // exception is desired output
        }
    }

    /**
     * Tests that readUntil() works.
     */
    public void testReadUntil() {
        try {
            ByteBufferParser parser = parserFor("hello world");
            String hello = parser.readUntil("\\s");
            assertEquals("hello", hello);
            assertEquals("world", parser.toString());
            String worl = parser.readUntil("[abcde]+");
            assertEquals("worl", worl);
            assertEquals("", parser.toString());
        } catch(ParseException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that readUntil() throws an exception if the specified text is not found.
     */
    public void testReadUntilBadInput() {
        try {
            ByteBufferParser parser = parserFor("hello world");
            String result = parser.readUntil("earth");
            fail();
        } catch(ParseException e) {
            // exception is desired output
        }
    }

    /**
     * Tests that indexOf() works.
     */
    public void testIndexOf() {
        ByteBufferParser parser = parserFor("hello world");
        int worldIndex = parser.indexOf("w");
        assertEquals(6, worldIndex);
        assertEquals("hello world", parser.toString());
    }

    /**
     * Tests that indexOf() returns -1 if the specified text is not found.
     */
    public void testIndexOfBadInput() {
        ByteBufferParser parser = parserFor("hello world");
        int tIndex = parser.indexOf("t");
        assertEquals(-1, tIndex);
        assertEquals("hello world", parser.toString());
    }
}
