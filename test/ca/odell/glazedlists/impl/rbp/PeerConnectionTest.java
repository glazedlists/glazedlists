/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.rbp;

import java.util.*;
// for being a JUnit test case
import junit.framework.*;
// NIO is used for CTP
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * A CTPChunk test verifies that the CTPConnection provides proper chunks.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class PeerConnectionTest extends TestCase {

    /** the peer manages publishing and subscribing */
    private Peer peer;
    
    /** the port to listen on */
    private static int serverPort = 5200;
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
        try {
            // increment the server port as to not bind to a previously used one
            serverPort++;
            peer = new Peer(serverPort);
            peer.start();
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        peer.stop();
    }

    /**
     * Verifies that peers can be started and stopped.
     */
    public void testStartStop() {
        // everything is done in setUp and tearDown
    }

    /**
     * Verifies that Resources can be published and subscribed to.
     */
    public void testPeerConnection() {
        try {
            StringResource stringResource = new StringResource();
            String path = "/stringResource";
            stringResource.setValue("Hello World");
            ResourceStatus status = peer.publish(stringResource, path);
            
            StringResource clone = new StringResource();
            peer.subscribe(clone, "localhost", serverPort, path);
            
            waitFor(1000);
            assertEquals(stringResource.getValue(), clone.getValue());
            
            stringResource.setValue("World O Hell");
            waitFor(1000);
            assertEquals(stringResource.getValue(), clone.getValue());

            status.disconnect();
            waitFor(1000);
            
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Waits for the specified duration of time. This hack method should be replaced
     * with something else that uses notification.
     */
    private static void waitFor(long time) {
        try {
            Object lock = new Object();
            synchronized(lock) {
                lock.wait(time);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        PeerConnectionTest test = new PeerConnectionTest();
        test.setUp();
        test.testPeerConnection();
        test.tearDown();
    }
}
