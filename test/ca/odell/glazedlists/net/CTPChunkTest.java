/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

import java.util.*;
// for being a JUnit test case
import junit.framework.*;
// NIO is used for CTP
import java.nio.*;
import java.nio.channels.*;
import java.io.UnsupportedEncodingException;

/**
 * A CTPChunk test verifies that the CTPConnection provides proper chunks.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CTPChunkTest extends TestCase {

    /** connection manager to handle incoming connects */
    CTPConnectionManager connectionManager = null;

    /** handler factory manages connection handlers */
    StaticCTPHandlerFactory handlerFactory;
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
        handlerFactory = new StaticCTPHandlerFactory();
        connectionManager = new CTPConnectionManager(handlerFactory);
        connectionManager.start();
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        connectionManager.stop();
    }

    /**
     * Verifies that chunks can be sent from the server to the client. This simply
     * sends data to itself.
     */
    public void testSimpleChunk() {
        StaticCTPHandler incoming = new StaticCTPHandler();
        incoming.addExpected("HELLO WORLD");
        
        StaticCTPHandler outgoing = new StaticCTPHandler();
        outgoing.addEnqueued("HELLO WORLD");
        
        handlerFactory.addHandler(outgoing);
        connectionManager.connect("localhost", incoming);
        
        assertTrue(outgoing.isDone());
        assertTrue(incoming.isDone());
    }
}
