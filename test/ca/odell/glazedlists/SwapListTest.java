/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import junit.framework.TestCase;
import ca.odell.glazedlists.matchers.*;
import ca.odell.glazedlists.impl.filter.*;
// standard collections
import java.util.*;

/**
 * Tests the SwapList class.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SwapListTest extends TestCase {

    /**
     * Test the basic functions of SwapList.
     */
    public void testBasicBehavior() {
        BasicEventList source = new BasicEventList();
        source.add("racecar");
        source.add("god");
        source.add("naomi");

        // make sure the swap list works out-of-the box
        SwapList swapList = new SwapList(source, new ReverseStringAlternateFinder());
        swapList.addListEventListener(new ConsistencyTestList(swapList, "Swap List")); 
        assertEquals(Arrays.asList(new String[] { "racecar", "dog", "imoan" }), swapList);
        
        // make sure the swap list handles simple changes
        source.set(2, "amanda");
        source.add("van");
        source.add("firebird");
        source.remove(1);
        assertEquals(Arrays.asList(new String[] { "racecar", "adnama", "nav", "driberif" }), swapList);
        
        // make sure the swap list handles complex changes
        source.addAll(Arrays.asList(new String[] { "nachos", "pizza", "hamburgers" }));
        source.removeAll(Arrays.asList(new String[] { "hamburgers", "van", "racecar" }));
        assertEquals(Arrays.asList(new String[] { "adnama", "driberif", "sohcan", "azzip" }), swapList);
        
        // make sure the swap list is readable
        assertEquals(2, swapList.indexOf("sohcan"));
        assertEquals(-1, swapList.indexOf("nachos"));
        assertEquals(true, swapList.containsAll(Arrays.asList(new String[] { "driberif", "adnama" })));
        assertEquals(false, swapList.containsAll(Arrays.asList(new String[] { "adnama", "pizza" })));
        assertEquals("azzip", swapList.get(3));
        
        // make sure the swap list is modifiable
        swapList.remove("sohcan");
        swapList.retainAll(Arrays.asList(new String[] { "driberif", "azzip" }));
        assertEquals(Arrays.asList(new String[] { "driberif", "azzip" }), swapList);
        
    }


    /**
     * Find the alternate of a String by reversing it.
     */
    public class ReverseStringAlternateFinder implements AlternateFinder {
        /** {@inheritDoc} */
        public Object createAlternate(Object sourceElement) {
            char[] chars = ((String)sourceElement).toCharArray();
            char[] reverse = new char[chars.length];
            for(int c = 0; c < chars.length; c++) {
                reverse[c] = chars[chars.length - 1 - c];
            }
            return new String(reverse);
        }
            
        /** {@inheritDoc} */
        public Object updateAlternate(Object sourceElement, Object previousAlternate) {
            return createAlternate(sourceElement);
        }
        
        /** {@inheritDoc} */
        public void deleteAlternate(Object previousAlternate) {
            // do nothing
        }
    }
}