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
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
    }

    /**
     * Verifies that chunks can be sent from the server to the client. This simply
     * sends data to itself.
     */
    public void testServerSendChunk() {
        System.err.println("ServerSendChunk()");
        handlerFactory = new StaticCTPHandlerFactory();
        connectionManager = new CTPConnectionManager(handlerFactory);
        connectionManager.start();

        try {
            StaticCTPHandler client = new StaticCTPHandler();
            client.addExpected("HELLO WORLD");
            
            StaticCTPHandler server = new StaticCTPHandler();
            server.addEnqueued("HELLO WORLD");
            
            handlerFactory.addHandler(server);
            connectionManager.connect("localhost", client);
            
            client.assertComplete((long)1000);
            client.close();
            server.assertComplete((long)1000);
            server.close();
        } finally {
            connectionManager.stop();
        }
    }


    /**
     * Verifies that chunks can be sent from the client to the server. This simply
     * sends data to itself.
     */
    public void testClientSendChunk() {
        System.err.println("ClientSendChunk()");
        handlerFactory = new StaticCTPHandlerFactory();
        connectionManager = new CTPConnectionManager(handlerFactory);
        connectionManager.start();

        try {
            StaticCTPHandler client = new StaticCTPHandler();
            client.addEnqueued("WORLD O HELL");
            
            StaticCTPHandler server = new StaticCTPHandler();
            server.addExpected("WORLD O HELL");
            
            handlerFactory.addHandler(server);
            connectionManager.connect("localhost", client);
            
            client.assertComplete((long)1000);
            server.assertComplete((long)1000);
            client.close();
            server.close();
        } finally {
            connectionManager.stop();
        }
    }


    /**
     * Verifies that large chunks can be sent.
     */
    public void testSendLargeString() {
        System.err.println("sendLargeString()");
        handlerFactory = new StaticCTPHandlerFactory();
        connectionManager = new CTPConnectionManager(handlerFactory);
        connectionManager.start();

        try {
            String clientSendData = randomString(100000);
            String serverSendData = randomString(200000);
            
            StaticCTPHandler client = new StaticCTPHandler();
            client.addEnqueued(clientSendData);
            client.addExpected(serverSendData);
            
            StaticCTPHandler server = new StaticCTPHandler();
            server.addExpected(clientSendData);
            server.addEnqueued(serverSendData);
            
            handlerFactory.addHandler(server);
            connectionManager.connect("localhost", client);
            
            client.assertComplete((long)1000);
            client.close();
            server.assertComplete((long)1000);
            server.close();
        } finally {
            connectionManager.stop();
        }
    }
    
    /**
     * Verifies that chunks can be sent from the client to the server. This simply
     * sends data to itself.
     */
    public void testManyStrings() {
        System.err.println("manyStrings()");
        handlerFactory = new StaticCTPHandlerFactory();
        connectionManager = new CTPConnectionManager(handlerFactory);
        connectionManager.start();

        try {
            StaticCTPHandler client = new StaticCTPHandler();
            StaticCTPHandler server = new StaticCTPHandler();

            for(int i = 0; i < 100; i++) {
                String clientSendData = randomString(2000);
                client.addEnqueued(clientSendData);
                server.addExpected(clientSendData);
                
                String serverSendData = randomString(3000);
                client.addExpected(serverSendData);
                server.addEnqueued(serverSendData);
            }
            
            handlerFactory.addHandler(server);
            connectionManager.connect("localhost", client);
            
            client.assertComplete((long)1000);
            client.close();
            server.assertComplete((long)1000);
            server.close();
        } finally {
            connectionManager.stop();
        }
    }
    
    /**
     * Constructs a random string of the specified length.
     */
    public static String randomString(int length) {
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < length; i++) {
            result.append(randomCharacter());
        }
        return result.toString();
    }
    /**
     * Gets a random character.
     */
    public static char randomCharacter() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random dice = new Random();
        return alphabet.charAt(dice.nextInt(alphabet.length()));
    }
    
    public static void main(String[] args) {
        
        CTPChunkTest test = new CTPChunkTest();
        while(true) {
            test.testClientSendChunk();
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("");
        }
    }
}
