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
 * Validates ResourceUri.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ResourceUriTest extends TestCase {

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
     * Verifies that ResourceUri is sound.
     */
    public void testUris() {
        // compare a couple uris
        ResourceUri localCustomers = ResourceUri.local("/Customers");
        ResourceUri localCustomers2 = ResourceUri.localOrRemote("glazedlists://localhost:1000/Customers", "localhost", 1000);
        assertEquals(localCustomers, localCustomers2);
        assertEquals(localCustomers.hashCode(), localCustomers2.hashCode());
        
        ResourceUri remoteCustomers = ResourceUri.localOrRemote("glazedlists://localhost:1000/Customers", "localhost", 2000);
        assertFalse(remoteCustomers.isLocal());
        assertFalse(remoteCustomers.equals(localCustomers));
        assertFalse(localCustomers.equals(remoteCustomers));
        
        ResourceUri remoteCustomers2 = ResourceUri.localOrRemote("glazedlists://localhost:1000/Customers", "localhost.com", 1000);
        assertFalse(remoteCustomers2.isLocal());
        assertFalse(remoteCustomers2.equals(localCustomers));
        assertFalse(localCustomers.equals(remoteCustomers2));
        
        ResourceUri remoteCustomers3 = ResourceUri.remote("localhost", 1000, "/Customers");
        assertEquals(remoteCustomers, remoteCustomers3);
        assertEquals(remoteCustomers, remoteCustomers2);
    }
}
