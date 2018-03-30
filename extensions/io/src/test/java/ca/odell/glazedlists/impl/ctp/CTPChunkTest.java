/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.ctp;

import static org.junit.Assert.fail;

// for being a JUnit test case
import ca.odell.glazedlists.RandomDataFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * A CTPChunk test verifies that the CTPConnection provides proper chunks.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 *
 * @deprecated The io extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@Deprecated
public class CTPChunkTest {

    /** connection manager to handle incoming connects */
    private CTPConnectionManager connectionManager = null;

    /** handler factory manages connection handlers */
    private StaticCTPHandlerFactory handlerFactory;

    /** the port to listen on */
    private static int serverPort = 5000;

    /**
     * Prepare for the test.
     */
    @Before
    public void setUp() {
        try {
            // increment the server port as to not bind to a previously used one
            serverPort++;
            handlerFactory = new StaticCTPHandlerFactory();
            connectionManager = new CTPConnectionManager(handlerFactory, serverPort);
            connectionManager.start();
        } catch(IOException e) {
            fail(e.getMessage());
        }
    }

    /**

     * Clean up after the test.
     */
    @After
    public void tearDown() {
        connectionManager.stop();
    }

    /**
     * Verifies that chunks can be sent from the server to the client. This simply
     * sends data to itself.
     */
    @Test
    public void testServerSendChunk() throws Exception {
        StaticCTPHandler client = new StaticCTPHandler();
        client.addExpected("HELLO WORLD");

        StaticCTPHandler server = new StaticCTPHandler();
        server.addEnqueued("HELLO WORLD");

        handlerFactory.addHandler(server);
        connectionManager.connect(client, "localhost", serverPort);

        client.assertComplete(1000);
        client.close();
        server.assertComplete(1000);
        server.close();
    }


    /**
     * Verifies that chunks can be sent from the client to the server. This simply
     * sends data to itself.
     */
    @Test
    public void testClientSendChunk() throws Exception {
        StaticCTPHandler client = new StaticCTPHandler();
        client.addEnqueued("WORLD O HELL");

        StaticCTPHandler server = new StaticCTPHandler();
        server.addExpected("WORLD O HELL");

        handlerFactory.addHandler(server);
        connectionManager.connect(client, "localhost", serverPort);

        client.assertComplete(1000);
        server.assertComplete(1000);
        client.close();
        server.close();
    }


    /**
     * Verifies that large chunks can be sent.
     */
    @Test
    public void testSendLargeString() throws Exception {
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

        client.assertComplete(1000);
        client.close();
        server.assertComplete(1000);
        server.close();
    }

    /**
     * Verifies that chunks can be sent from the client to the server. This simply
     * sends data to itself.
     */
    @Test
    public void testManyStrings() throws Exception {
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

        client.assertComplete(1000);
        client.close();
        server.assertComplete(1000);
        server.close();
    }
}
