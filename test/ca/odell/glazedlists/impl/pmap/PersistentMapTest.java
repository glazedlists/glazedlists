/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.pmap;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.io.*;
import ca.odell.glazedlists.util.*;
// standard collections
import java.util.*;
// for testing files
import java.io.*;

/**
 * This test verifies that the PersistentMap works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class PersistentMapTest extends TestCase {
    
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
     * Creates a map file, reads a key and writes a key. The written value is double
     * the value read. If no value is found (which is the case when the file is new),
     * then the written value is one. The key's value will therefore follow the
     * sequence null, 1, 2, 4, 8.
     */
    public void testCreate() throws IOException {
        File mapFile = File.createTempFile("counting", "j81");
        mapFile.deleteOnExit();
        
        Integer value = null;
        Integer expectedValue = null;
        String key = "lucky#";
        
        for(int i = 0; i < 5; i++) {
            PersistentMap map = new PersistentMap(mapFile);

            // read the current value
            Chunk chunk = (Chunk)map.get(key);
            if(chunk != null) {
                Bufferlo valueBufferlo = chunk.getValue();
                value = Integer.valueOf(valueBufferlo.toString());
            } else {
                value = null;
            }
            assertEquals(expectedValue, value);
            
            // write the next value
            expectedValue = (expectedValue == null) ? new Integer(1) : new Integer(expectedValue.intValue() * 2);
            Bufferlo expectedValueBufferlo = new Bufferlo();
            expectedValueBufferlo.write(expectedValue.toString());
            map.put(key, new Chunk(expectedValueBufferlo));
            
            map.close();
        }
    }
}
