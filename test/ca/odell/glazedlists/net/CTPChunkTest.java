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
import ca.odell.glazedlists.*;
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
    
    /** the port to listen on */
    private int serverPort = 5000;
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
        throw new IllegalStateException("CTP Tests are broken");
        // increment the server port as to not bind to a previously used one
        /*serverPort++;
        handlerFactory = new StaticCTPHandlerFactory();
        connectionManager = new CTPConnectionManager(handlerFactory, serverPort);
        connectionManager.start();*/
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
    public void testServerSendChunk() {
        try {
            StaticCTPHandler client = new StaticCTPHandler();
            client.addExpected("HELLO WORLD");
            
            StaticCTPHandler server = new StaticCTPHandler();
            server.addEnqueued("HELLO WORLD");
            
            handlerFactory.addHandler(server);
            connectionManager.connect(client, "localhost", serverPort);
            
            client.assertComplete((long)1000);
            client.close();
            server.assertComplete((long)1000);
            server.close();
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }


    /**
     * Verifies that chunks can be sent from the client to the server. This simply
     * sends data to itself.
     */
    public void testClientSendChunk() {
        try {
            StaticCTPHandler client = new StaticCTPHandler();
            client.addEnqueued("WORLD O HELL");
            
            StaticCTPHandler server = new StaticCTPHandler();
            server.addExpected("WORLD O HELL");
            
            handlerFactory.addHandler(server);
            connectionManager.connect(client, "localhost", serverPort);
            
            client.assertComplete((long)1000);
            server.assertComplete((long)1000);
            client.close();
            server.close();
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }


    /**
     * Verifies that large chunks can be sent.
     */
    public void testSendLargeString() {
        try {
            String clientSendData = RandomDataFactory.nextString(100000);
            String serverSendData = RandomDataFactory.nextString(200000);
            
            StaticCTPHandler client = new StaticCTPHandler();
            client.addEnqueued(clientSendData);
            client.addExpected(serverSendData);
            
            StaticCTPHandler server = new StaticCTPHandler();
            server.addExpected(clientSendData);
            server.addEnqueued(serverSendData);
            
            handlerFactory.addHandler(server);
            connectionManager.connect(client, "localhost", serverPort);
            
            client.assertComplete((long)1000);
            client.close();
            server.assertComplete((long)1000);
            server.close();
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    /**
     * Verifies that chunks can be sent from the client to the server. This simply
     * sends data to itself.
     */
    public void testManyStrings() {
        try {
            StaticCTPHandler client = new StaticCTPHandler();
            StaticCTPHandler server = new StaticCTPHandler();

            for(int i = 0; i < 100; i++) {
                String clientSendData = RandomDataFactory.nextString(2000);
                client.addEnqueued(clientSendData);
                server.addExpected(clientSendData);
                
                String serverSendData = RandomDataFactory.nextString(3000);
                client.addExpected(serverSendData);
                server.addEnqueued(serverSendData);
            }
            
            handlerFactory.addHandler(server);
            connectionManager.connect(client, "localhost", serverPort);
            
            client.assertComplete((long)1000);
            client.close();
            server.assertComplete((long)1000);
            server.close();
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    
    public static void main(String[] args) {
        CTPChunkTest test = new CTPChunkTest();
        while(true) {
            test.setUp();
            test.testServerSendChunk();
            test.tearDown();
            System.out.print("\n\n\n\n");
            test.setUp();
            test.testClientSendChunk();
            test.tearDown();
            System.out.print("\n\n\n\n");
            test.setUp();
            test.testSendLargeString();
            test.tearDown();
            System.out.print("\n\n\n\n");
            test.setUp();
            test.testManyStrings();
            test.tearDown();
            System.out.print("\n\n\n\n");
        }
    }
}
