/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
// standard collections
import java.util.*;

/**
 * This test verifies that Diff works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class DiffTest extends TestCase {

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
     * Tests to verify that Diff performs the correct number of changes.
     */
    public void testDiff() {
        assertEquals(4, getChangeCount("algorithm", "logarithm"));
        assertEquals(5, getChangeCount("abcabba", "cbabac"));
    }
    
    /**
     * Counts the number of changes to change target to source.
     */
    private int getChangeCount(String target, String source) {
        EventList targetList = new BasicEventList();
        targetList.addAll(stringToList(target));
        List sourceList = stringToList(source);
        
        ListEventCounter counter = new ListEventCounter();
        targetList.addListEventListener(counter);
        
        GlazedLists.replaceAll(targetList, sourceList, false);
        return counter.getEventCount();
    }
    
    /**
     * Create a list, where each element is a character from the String.
     */
    private List stringToList(String data) {
        List result = new ArrayList();
        for(int c = 0; c < data.length(); c++) {
            result.add(new Character(data.charAt(c)));
        }
        return result;
    }
}
