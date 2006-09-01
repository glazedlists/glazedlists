/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Verifies that NetworkList works.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class NetworkListTest extends TestCase {

    /** the peer manages publishing and subscribing */
    private ListPeer peer;
    
    /** the port to listen on */
    private static int serverPort = 5100;
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
        try {
            // increment the server port as to not bind to a previously used one
            serverPort++;
            peer = new ListPeer(serverPort);
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
     * Verifies that Resources can be published and subscribed to.
     */
    public void testSimpleSubscription() {
        try {
            // prepare the source list
            String path = "/integers";
            EventList sourceListTS = GlazedLists.threadSafeList(new BasicEventList());
            NetworkList sourceList = peer.publish(sourceListTS, path, GlazedListsIO.serializableByteCoder());
            SimpleNetworkListStatusListener sourceListener = new SimpleNetworkListStatusListener(sourceList);
            waitFor(1000);
            assertTrue(sourceListener.isConnected());
            sourceListTS.add(new Integer(8));
            sourceListTS.add(new Integer(6));
            sourceListTS.add(new Integer(7));
            sourceListTS.add(new Integer(5));
            
            // prepare the target list
            NetworkList targetList = peer.subscribe("localhost", serverPort, path, GlazedListsIO.serializableByteCoder());
            SimpleNetworkListStatusListener targetListener = new SimpleNetworkListStatusListener(targetList);
            
            // verify they're equal after a subscribe
            waitFor(1000);
            assertTrue(targetListener.isConnected());
            assertEquals(sourceList, targetList);
            
            // perform some changes and verify they keep in sync
            sourceListTS.add(new Integer(3));
            sourceListTS.add(new Integer(0));
            sourceListTS.add(new Integer(9));
            waitFor(1000);
            assertEquals(sourceList, targetList);
            
            // clean up after myself
            targetList.disconnect();
            waitFor(1000);
            assertFalse(targetListener.isConnected());
            assertTrue(sourceListener.isConnected());
            
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    /**
     * Verifies that the client can disconnect and reconnect.
     */
    public void testClientDisconnect() {
        try {
            // prepare the source list
            String path = "/integers";
            EventList sourceListTS = GlazedLists.threadSafeList(new BasicEventList());
            NetworkList sourceList = peer.publish(sourceListTS, path, GlazedListsIO.serializableByteCoder());
            sourceListTS.add(new Integer(8));
            sourceListTS.add(new Integer(6));
            
            // prepare the target list
            NetworkList targetList = peer.subscribe("localhost", serverPort, path, GlazedListsIO.serializableByteCoder());
            SimpleNetworkListStatusListener targetListener = new SimpleNetworkListStatusListener(targetList);
            
            // verify they're equal after a subscribe
            waitFor(1000);
            assertTrue(targetListener.isConnected());
            assertTrue(targetList.isConnected());
            assertEquals(sourceList, targetList);
            List snapshot = new ArrayList();
            snapshot.addAll(sourceListTS);
            
            // disconnect the client
            targetList.disconnect();
            waitFor(1000);
            assertFalse(targetListener.isConnected());
            assertFalse(targetList.isConnected());
            
            // change the source list
            sourceListTS.add(new Integer(7));
            sourceListTS.add(new Integer(5));
            
            // they client should be out of date
            waitFor(1000);
            assertEquals(snapshot, targetList);
            
            // bring the target list back to life
            targetList.connect();
            waitFor(1000);
            assertTrue(targetListener.isConnected());
            assertTrue(targetList.isConnected());
            assertEquals(sourceList, targetList);
            
            // clean up after myself
            targetList.disconnect();
            waitFor(1000);
            
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Verifies that the server can disconnect and reconnect.
     */
    public void testServerDisconnect() {
        try {
            // prepare the source list
            String path = "/integers";
            EventList sourceListTS = GlazedLists.threadSafeList(new BasicEventList());
            NetworkList sourceList = peer.publish(sourceListTS, path, GlazedListsIO.serializableByteCoder());
            SimpleNetworkListStatusListener sourceListener = new SimpleNetworkListStatusListener(sourceList);
            sourceListTS.add(new Integer(8));
            sourceListTS.add(new Integer(6));
            
            // prepare the target list
            NetworkList targetList = peer.subscribe("localhost", serverPort, path, GlazedListsIO.serializableByteCoder());
            SimpleNetworkListStatusListener targetListener = new SimpleNetworkListStatusListener(targetList);
            
            // verify they're equal after a subscribe
            waitFor(1000);
            assertTrue(sourceListener.isConnected());
            assertTrue(sourceList.isConnected());
            assertTrue(targetListener.isConnected());
            assertTrue(targetList.isConnected());
            assertEquals(sourceList, targetList);
            List snapshot = new ArrayList();
            snapshot.addAll(sourceListTS);
            
            // disconnect the server
            //targetList.disconnect(); System.out.println("WARNING: TARGET DISCONNECT FIRST FOR CONCURRENCY PROBLEM");
            sourceList.disconnect();
            waitFor(1000);
            assertFalse(sourceListener.isConnected());
            assertFalse(sourceList.isConnected());
            assertFalse(targetListener.isConnected());
            assertFalse(targetList.isConnected());
            
            // change the source list
            sourceListTS.add(new Integer(7));
            sourceListTS.add(new Integer(5));
            
            // they client should be out of date
            waitFor(1000);
            assertEquals(snapshot, targetList);
            
            // bring the source and target list back to life
            sourceList.connect();
            targetList.connect();
            waitFor(1000);
            assertTrue(sourceListener.isConnected());
            assertTrue(sourceList.isConnected());
            assertTrue(targetListener.isConnected());
            assertTrue(targetList.isConnected());
            assertEquals(sourceList, targetList);
            
            // clean up after myself
            targetList.disconnect();
            waitFor(1000);
            
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Verifies that many listeners can subscribe to a resource.
     */
    public void testManyListeners() {
        try {
            // prepare the source list
            String path = "/integers";
            EventList sourceListTS = GlazedLists.threadSafeList(new BasicEventList());
            NetworkList sourceList = peer.publish(sourceListTS, path, GlazedListsIO.serializableByteCoder());
            sourceListTS.add(new Integer(8));
            sourceListTS.add(new Integer(6));
            int connectPort = serverPort;
            
            // prepare the listener's peers
            List peers = new ArrayList();
            for(int p = 0; p < 4; p++) {
                serverPort++;
                ListPeer listenerPeer = new ListPeer(serverPort);
                listenerPeer.start();
                peers.add(listenerPeer);
            }
            
            // prepare the listeners
            List listeners = new ArrayList();
            for(Iterator p = peers.iterator(); p.hasNext(); ) {
                ListPeer listenerPeer = (ListPeer)p.next();
                NetworkList listener = listenerPeer.subscribe("localhost", connectPort, path, GlazedListsIO.serializableByteCoder());
                listeners.add(listener);
            }
            
            // verify they're equal after a subscribe
            waitFor(1000);
            for(Iterator i = listeners.iterator(); i.hasNext(); ) {
                NetworkList listener = (NetworkList)i.next();
                assertEquals(sourceList, listener);
            }

            // perform some changes
            sourceListTS.add(new Integer(3));
            sourceListTS.add(new Integer(0));
            sourceListTS.add(new Integer(9));

            // verify they're still in sync
            waitFor(1000);
            for(Iterator i = listeners.iterator(); i.hasNext(); ) {
                NetworkList listener = (NetworkList)i.next();
                assertEquals(sourceList, listener);
            }
            
            // clean up the listener's connections
            for(Iterator p = peers.iterator(); p.hasNext(); ) {
                ListPeer listenerPeer = (ListPeer)p.next();
                listenerPeer.stop();
            }
            
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    

    /**
     * Verifies that the server can unpublish a value.
     */
    public void testServerUnpublish() {
        try {
            // prepare the source list
            String path = "/integers";
            EventList sourceListTS = GlazedLists.threadSafeList(new BasicEventList());
            NetworkList sourceList = peer.publish(sourceListTS, path, GlazedListsIO.serializableByteCoder());
            sourceListTS.add(new Integer(8));
            sourceListTS.add(new Integer(6));
            
            // prepare the target list
            NetworkList targetList = peer.subscribe("localhost", serverPort, path, GlazedListsIO.serializableByteCoder());
            
            // verify they're equal after a subscribe
            waitFor(1000);
            assertEquals(sourceList, targetList);
            List snapshot = new ArrayList();
            snapshot.addAll(sourceListTS);
            
            // disconnect the first list
            //targetList.disconnect(); waitFor(1000); System.out.println("WARNING: TARGET DISCONNECT FIRST FOR CONCURRENCY PROBLEM");
            sourceList.disconnect();
            waitFor(1000);
            assertFalse(targetList.isConnected());
            
            // prepare the second source list
            EventList sourceList2TS = GlazedLists.threadSafeList(new BasicEventList());
            NetworkList sourceList2 = peer.publish(sourceList2TS, path, GlazedListsIO.serializableByteCoder());
            sourceList2TS.add(new Integer(7));
            sourceList2TS.add(new Integer(5));
            
            // verify they're equal after a new connect
            targetList.connect();
            waitFor(1000);
            assertEquals(sourceList2, targetList);
            
            // disconnect the second list
            //targetList.disconnect(); waitFor(1000); System.out.println("WARNING: TARGET DISCONNECT FIRST FOR CONCURRENCY PROBLEM");
            sourceList2.disconnect();
            waitFor(1000);
            assertFalse(targetList.isConnected());
            
            // verify they're equal after a new connect
            sourceList.connect();
            targetList.connect();
            waitFor(1000);
            assertEquals(sourceList, targetList);
            
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    /**
     * Verifies that nothing breaks when a client subscribes during a flurry of updates.
     */
    public void testFrequentUpdates() {
        try {
            // prepare the source list
            String path = "/integers";
            EventList sourceListTS = GlazedLists.threadSafeList(new BasicEventList());
            NetworkList sourceList = peer.publish(sourceListTS, path, GlazedListsIO.serializableByteCoder());

            // prepare the target list
            NetworkList targetList = peer.subscribe("localhost", serverPort, path, GlazedListsIO.serializableByteCoder());
            targetList.disconnect();
            
            // retry a handful of times
            for(int j = 0; j < 5; j++) {
                waitFor(1000);
                sourceListTS.clear();
                targetList.connect();
                for(int i = 0; i < 25; i++) {
                    sourceListTS.add(new Integer(i));
                }
                // test that they're equal
                waitFor(1000);
                assertEquals(sourceList, targetList);
                targetList.disconnect();
            }
            
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
