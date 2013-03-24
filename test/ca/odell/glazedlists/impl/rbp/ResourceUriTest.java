/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.rbp;

// for being a JUnit test case
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Validates ResourceUri.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ResourceUriTest {

    /**
     * Verifies that ResourceUri is sound.
     */
    @Test
    public void testUris() {
        // compare a couple uris
        ResourceUri localCustomers = ResourceUri.local("/Customers");
        ResourceUri localCustomers2 = ResourceUri.localOrRemote("glazedlists://localhost:1000/Customers", "localhost", 1000);
        assertEquals(localCustomers, localCustomers2);
        assertEquals(localCustomers.hashCode(), localCustomers2.hashCode());
        
        ResourceUri remoteCustomers = ResourceUri.localOrRemote("glazedlists://localhost:1000/Customers", "localhost", 2000);
        assertFalse(remoteCustomers.isLocal());
        
        ResourceUri remoteCustomers2 = ResourceUri.localOrRemote("glazedlists://localhost:1000/Customers", "localhost.com", 1000);
        assertFalse(remoteCustomers2.isLocal());
        
        ResourceUri remoteCustomers3 = ResourceUri.remote("localhost", 1000, "/Customers");
        assertEquals(remoteCustomers, remoteCustomers3);
        assertEquals(remoteCustomers, remoteCustomers2);
    }
}
