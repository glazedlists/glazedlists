/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.util.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the SubList works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SubListTest extends TestCase {
    
    /** for randomly choosing list indicies */
    private Random random = new Random();
    
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
     * Tests to verify that the SubList views a segment of a list.
     */
    public void testSubList() {
        // create a source list of values
        BasicEventList eventList = new BasicEventList();
        List controlList = new ArrayList();
        for(int i = 0; i < 26; i++) {
            eventList.add(new Integer(i));
            controlList.add(new Integer(i));
        }
        
        // ensure all sublists are equal
        for(int i = 0; i < eventList.size(); i++) {
            for(int j = i + 1; j < eventList.size(); j++) {
                assertEquals(eventList.subList(i,j), controlList.subList(i,j));
            }
        }
    }

    /**
     * Tests to verify that the SubList views a segment of a list while
     * that segment changes.
     */
    public void testSubListChanges() {
        // create a source list of values, from 0,1,2...49,100,101..149
        BasicEventList eventList = new BasicEventList();
        for(int i = 0; i < 50; i++) {
            eventList.add(new Integer(i));
        }
        for(int i = 0; i < 50; i++) {
            eventList.add(new Integer(i + 100));
        }
        
        // get the sublist
        List subListBefore = eventList.subList(25, 75);
        
        // change the source list to be 0,1,2,3,...49,50,51,..99,100,101...149
        for(int i = 0; i < 50; i++) {
            eventList.add(50+i, new Integer(50+i));
        }
        
        // ensure the sublist took the change
        List subListAfter = eventList.subList(25, 125);
        assertEquals(subListBefore, subListAfter);
        
        // change the lists again, deleting all odd numbered entries
        for(Iterator i = eventList.iterator(); i.hasNext(); ) {
            Integer current = (Integer)i.next();
            if(current.intValue() % 2 == 1) i.remove();
        }
        
        // ensure the sublists took the change
        subListAfter = eventList.subList(13, 63);
        assertEquals(subListBefore, subListAfter);
    }
}
