/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.ctp;

import java.util.*;
// for being a JUnit test case
import junit.framework.*;
import ca.odell.glazedlists.*;
// NIO is used for CTP
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.UnsupportedEncodingException;

/**
 * A CTPConnectionTest verifies that behaviour is correct when connections fail.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CTPConnectionTest extends TestCase {

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
     * Verifies that an Exception is thrown if there is a bind failure.
     */
    public void testRepeatedBind() {
        StaticCTPHandlerFactory handlerFactory = new StaticCTPHandlerFactory();
        int serverPort = 6000;
        
        // the first bind should succeed
        CTPConnectionManager connectionManager = null;
        try {
            connectionManager = new CTPConnectionManager(handlerFactory, serverPort);
            connectionManager.start();
        } catch(IOException e) {
            fail(e.getMessage());
        }

        // the second bind should fail
        try {
            CTPConnectionManager connectionManager2 = new CTPConnectionManager(handlerFactory, serverPort);
            connectionManager2.start();
            
            // bind worked, we're sunk
            fail();
        } catch(IOException e) {
            // the bind failed as expected
        } finally {
            connectionManager.stop();
        }
    }
    
    /**
     * Verifies that a connect timeout causes the CTPHandler to be closed.
     */
    public void testConnectionRefused() throws IOException {
        StaticCTPHandlerFactory handlerFactory = new StaticCTPHandlerFactory();
        int serverPort = 6001;
        int connectPort = 6002;
        
        CTPConnectionManager connectionManager = new CTPConnectionManager(handlerFactory, serverPort);
        connectionManager.start();

        StaticCTPHandler refused = new StaticCTPHandler();
        connectionManager.connect(refused, "localhost", connectPort);

        refused.assertClosed((long)1000);
        
        connectionManager.stop();
    }
    
    
    /**
     * Verifies that a connect timeout causes the CTPHandler to be closed.
     */
    public void testConnectionTimeOut() throws IOException {
        System.err.println("Skipping timeout test which takes 77 seconds to run");
        /*  // uncomment this block to run the timeout test
        
        StaticCTPHandlerFactory handlerFactory = new StaticCTPHandlerFactory();
        int serverPort = 6003;
        int connectPort = 6004;
        
        CTPConnectionManager connectionManager = new CTPConnectionManager(handlerFactory, serverPort);
        connectionManager.start();

        StaticCTPHandler timeOut = new StaticCTPHandler();
        connectionManager.connect(timeOut, "255.255.255.0", connectPort);

        Exception reason = timeOut.assertClosed((long)77000);
        System.out.println("Fail message: " + reason);
        
        connectionManager.stop();
        */
    }
    
    public static void main(String[] args) {
        CTPConnectionTest test = new CTPConnectionTest();
        while(true) {
            test.setUp();
            test.testRepeatedBind();
            test.tearDown();
            System.out.print("\n\n\n\n");
        }
    }
}
