/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.io;

import java.util.*;
// NIO
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
// for being a JUnit test case
import junit.framework.*;
import ca.odell.glazedlists.*;
// regular expressions
import java.util.regex.*;
import java.text.ParseException;
// logging
import java.util.logging.*;
import java.text.ParseException;

/**
 * Tests the Bufferlo.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BufferloTest extends TestCase {
    
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
     * Tests that the streams work.
     */
    public void testStreams() throws IOException {
        Bufferlo bufferlo = new Bufferlo();
        Writer writer = new OutputStreamWriter(bufferlo.getOutputStream(), "US-ASCII");
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferlo.getInputStream(), "US-ASCII"));
        
        // verify write and read works
        writer.write("Hello World");
        writer.write("\n");
        writer.flush();
        assertEquals("Hello World", reader.readLine());
        
        // verify duplicate works
        writer.write("Disco Inferno\n");
        writer.flush();
        Bufferlo disco = bufferlo.duplicate();
        assertEquals("Disco Inferno", reader.readLine());
        BufferedReader discoReader = new BufferedReader(new InputStreamReader(disco.getInputStream(), "US-ASCII"));
        assertEquals("Disco Inferno", discoReader.readLine());
        
        // verify consume works
        writer.write("World\n O Hell\n");
        writer.flush();
        Bufferlo world = bufferlo.consume(6);
        BufferedReader worldReader = new BufferedReader(new InputStreamReader(world.getInputStream(), "US-ASCII"));
        assertEquals("World", worldReader.readLine());
        assertEquals(" O Hell", reader.readLine());
        
        // verify we can write to a duplicate
        Writer discoWriter = new OutputStreamWriter(disco.getOutputStream(), "US-ASCII");
        discoWriter.write("Burito Jungle\n");
        discoWriter.flush();
        assertEquals("Burito Jungle", discoReader.readLine());
        writer.write("Taco Bell\n");
        writer.flush();
        assertEquals("Taco Bell", reader.readLine());

        // verify append works
        writer.write("John Travolta");
        writer.flush();
        discoWriter.write(" in Grease\n");
        discoWriter.flush();
        bufferlo.append(disco);
        assertEquals("John Travolta in Grease", reader.readLine());
        discoWriter.write("No Grease\n");
        discoWriter.flush();
        assertEquals("No Grease", discoReader.readLine());
        
        // verify that skip and limit work
        Writer worldWriter = new OutputStreamWriter(world.getOutputStream(), "US-ASCII");
        worldWriter.write("Dasher Dancer Prancer Vixen");
        worldWriter.flush();
        world.skip(7);
        world.limit(6);
        worldWriter.write("\n");
        worldWriter.flush();
        assertEquals("Dancer", worldReader.readLine());
    }

    /**
     * Tests that consume() works.
     */
    public void testConsume() {
        try {
            Bufferlo parser = getBufferlo("hello world");
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
            Bufferlo parser = getBufferlo("hello world");
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
            Bufferlo parser = getBufferlo("hello world");
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
            Bufferlo parser = getBufferlo("hello world");
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
            Bufferlo parser = getBufferlo("hello world");
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
            Bufferlo parser = getBufferlo("hello world");
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
            Bufferlo parser = getBufferlo("hello world");
            int tIndex = parser.indexOf("t");
            assertEquals(-1, tIndex);
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests that streams mixed with data works.
     */
    public void testMix() throws IOException, ParseException {
        Bufferlo bufferlo = new Bufferlo();
        Writer writer = new OutputStreamWriter(bufferlo.getOutputStream(), "US-ASCII");
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferlo.getInputStream(), "US-ASCII"));
        
        // write a bunch of bytes
        int bytesWritten = 0;
        for(int i = 0; i < 10; i++) {
            // write some strings
            writer.write("Hello World");
            writer.flush();
            bytesWritten += 11;
            
            // write some buffers
            bufferlo.write("World O Hell");
            bytesWritten += 12;

            // we should be consistent
            assertEquals(bytesWritten, bufferlo.length());
        }
        
        // verify we wrote what we thought
        int bytesRemaining = bytesWritten;
        for(int i = 0; i < 10; i++) {
            // read some bytes
            byte[] data = bufferlo.consumeBytes(5);
            assertEquals(data[0], (byte)'H');
            assertEquals(data[4], (byte)'o');
            bytesRemaining -= 5;
            
            // read some text
            bufferlo.consume(" WorldWorld ");
            bytesRemaining -= 12;
            
            // read some bytes
            byte[] data2 = bufferlo.consumeBytes(6);
            assertEquals(data2[0], (byte)'O');
            assertEquals(data2[5], (byte)'l');
            bytesRemaining -= 6;
            
            // we should be consistent
            assertEquals(bytesRemaining, bufferlo.length());
        }
    }
    
    /**
     * Tests that all bytes from -128 thru 127 work. This is necessary to verfiy
     * that there are no problems with byte encoding.
     */
    public void testAllByteValues() throws IOException {
        Bufferlo bufferlo = new Bufferlo();
        InputStream in = bufferlo.getInputStream();
        OutputStream out = bufferlo.getOutputStream();
        for(int i = -128; i <= 127; i++) {
            byte b = (byte)i;
            byte[] valueOut = new byte[] { b };
            out.write(valueOut);
            out.flush();
            byte[] valueIn = new byte[1];
            in.read(valueIn);
            assertEquals(valueOut[0], valueIn[0]);
        }
    }
    
    /**
     * Gets a Bufferlo with the specified contents.
     */
    private Bufferlo getBufferlo(String contents) throws IOException {
        Bufferlo bufferlo = new Bufferlo();
        Writer writer = new OutputStreamWriter(bufferlo.getOutputStream(), "US-ASCII");
        writer.write(contents);
        writer.flush();
        return bufferlo;
    }
    
    public static void main(String[] args) throws IOException {
        new BufferloTest().testAllByteValues();
    }
}
