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
public class PeerConnectionTest extends TestCase {

    /** the peer manages publishing and subscribing */
    private Peer peer;
    
    /** the port to listen on */
    private int serverPort = 5000;
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
        // increment the server port as to not bind to a previously used one
        serverPort++;
        peer = new Peer(serverPort);
        peer.start();
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
            String resourceName = "glazedlists://localhost:" + serverPort + "/stringResource";
            stringResource.setValue("Hello World");
            peer.publish(stringResource, resourceName);
            
            StringResource clone = new StringResource();
            peer.subscribe(clone, resourceName, "localhost", serverPort);
            
            waitFor(1000);
            System.out.println(stringResource.getValue());
            System.out.println(clone.getValue());
            
            stringResource.setValue("World O Hell");
            System.out.println(stringResource.getValue());
            System.out.println(clone.getValue());
            
            waitFor(1000);
            
            System.out.println(stringResource.getValue());
            System.out.println(clone.getValue());
            
            assertEquals(stringResource.getValue(), clone.getValue());

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
        System.out.println("------------------------------------------------");
        System.out.println("START");
        int serverPort = 5000;
        Peer peer = new Peer(serverPort);
        peer.start();
        peer.print();

        System.out.println("------------------------------------------------");
        System.out.println("PUBLISH");
        StringResource stringResource = new StringResource();
        String resourceName = "glazedlists://localhost:" + serverPort + "/stringResource";
        stringResource.setValue("Hello World");
        peer.publish(stringResource, resourceName);
        peer.print();

        System.out.println("------------------------------------------------");
        System.out.println("SUBSCRIBE");
        StringResource clone = new StringResource();
        peer.subscribe(clone, resourceName, "localhost", serverPort);
        waitFor(1000);
        peer.print();

        System.out.println("------------------------------------------------");
        System.out.println("UNSUBSCRIBE");
        peer.unsubscribe(resourceName);
        waitFor(1000);
        peer.print();

        System.out.println("------------------------------------------------");
        System.out.println("TEARED DOWN CONNECTION");
        peer.stop();
        peer.print();
    }
}
