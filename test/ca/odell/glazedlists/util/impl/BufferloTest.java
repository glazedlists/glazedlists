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
}
