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
    public void testStreams() throws UnsupportedEncodingException, IOException {
        Bufferlo bufferlo = new Bufferlo();
        Writer writer = new OutputStreamWriter(bufferlo.getOutputStream(), "US-ASCII");
        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferlo.getInputStream(), "US-ASCII"));
        
        writer.write("Hello World");
        writer.write("\n");
        assertEquals("Hello World", reader.readLine());
    }

    /*
    public int readFromChannel(ReadableByteChannel source) throws IOException {
    public int writeToChannel(WritableByteChannel target) throws IOException {
    public BufferloInputStream getInputStream() {
    public BufferloOutputStream getOutputStream() {
    public Bufferlo duplicate() {
    public Bufferlo consume(int bytes) {
    public void append(Bufferlo data) {
    public void limit(int bytes) {
    public void skip(int bytes) {
        */
}
