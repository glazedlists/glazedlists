/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.rbp;

import java.util.*;
// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.io.*;
// for being a JUnit test case
import junit.framework.*;
// NIO is used for CTP
import java.nio.*;
import java.nio.channels.*;
import java.io.UnsupportedEncodingException;

/**
 * Verifies that NetworkList works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class NetworkListTest extends TestCase {

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
     * Verifies that Resources can be published and subscribed to.
     */
    public void testSimpleSubscription() {
        try {
            // prepare the source list
            NetworkList sourceList = new NetworkList(new BasicEventList(), new IntegerCoder());
            String resourceName = "glazedlists://localhost:" + serverPort + "/integers";
            peer.publish(sourceList, resourceName);
            sourceList.add(new Integer(8));
            sourceList.add(new Integer(6));
            sourceList.add(new Integer(7));
            sourceList.add(new Integer(5));
            
            // prepare the target list
            NetworkList targetList = new NetworkList(new BasicEventList(), new IntegerCoder());
            peer.subscribe(targetList, resourceName, "localhost", serverPort);
            
            // verify they're equal after a subscribe
            waitFor(1000);
            assertEquals(sourceList, targetList);
            
            // perform some changes and verify they keep in sync
            sourceList.add(new Integer(3));
            sourceList.add(new Integer(0));
            sourceList.add(new Integer(9));
            waitFor(1000);
            assertEquals(sourceList, targetList);
            
            // clean up after myself
            peer.unsubscribe(resourceName);
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
    
    /**
     * Runs the test of this application
     */
    public static void main(String[] args) {
        NetworkListTest test = new NetworkListTest();
        test.setUp();
        test.testSimpleSubscription();
        test.tearDown();
    }
}
