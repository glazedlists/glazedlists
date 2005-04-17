/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.io;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
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
        File fibonacciFile = File.createTempFile("fibonacci", "j81");
        fibonacciFile.deleteOnExit();
        
        int expectedSecondLast = 0;
        int expectedLast = 0;
        int current = 0;
        for(int i = 0; i < 16; i++) {
            FileList fibonacci = new FileList(fibonacciFile, GlazedListsIO.serializableByteCoder());
            
            // base case
            if(fibonacci.size() < 2) {
                current = 1;

            // recursive case
            } else {
                // read the last and second last
                Integer secondLast = (Integer)fibonacci.get(fibonacci.size() - 2);
                assertEquals(expectedSecondLast, secondLast.intValue());
                Integer last = (Integer)fibonacci.get(fibonacci.size() - 1);
                assertEquals(expectedLast, last.intValue());
                
                // prepare the new value
                current = secondLast.intValue() + last.intValue();
            }
            
            // save the new value to the file
            fibonacci.add(new Integer(current));
            fibonacci.close();
            
            // prepare for the next round
            expectedSecondLast = expectedLast;
            expectedLast = current;
        }
    }
}
