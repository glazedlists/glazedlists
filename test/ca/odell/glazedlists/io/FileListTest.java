/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.io;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.util.*;
// standard collections
import java.util.*;
// for testing files
import java.io.*;
// for testing colors
import java.awt.Color;


/**
 * This test verifies that the FileList works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class FileListTest extends TestCase {
    
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
     * Creates a file list, writes a value and reads a value. The written value
     * is the sum of the last two values read. If less than 2 values are read, then 1 is
     * written.
     */
    public void testCreate() throws IOException {
        File listFile = File.createTempFile("fibonacci", "j81");
        listFile.deleteOnExit();
        
        for(int i = 0; i < 50; i++) {
            FileList fibonacci = new FileList(listFile, ByteCoderFactory.serializable());
            System.out.println(fibonacci);
            
            if(fibonacci.size() < 2) {
                fibonacci.add(new Integer(1));
            } else {
                Integer secondLast = (Integer)fibonacci.get(fibonacci.size() - 2);
                Integer last = (Integer)fibonacci.get(fibonacci.size() - 1);
                fibonacci.add(new Integer(secondLast.intValue() + last.intValue()));
            }
            
            fibonacci.close();
        }
    }
}
