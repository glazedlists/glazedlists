/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;
import ca.odell.glazedlists.matchers.*;
import ca.odell.glazedlists.impl.filter.*;
// standard collections
import java.util.*;

/**
 * Tests the generic FilterList class.
 */
public class FilterListTest extends TestCase {

    /**
     * This test demonstrates Issue 213.
     */
    public void testRelax() {
        // construct a (contrived) list of initial values
        BasicEventList original = new BasicEventList();
        List values = intArrayToIntegerCollection(new int [] { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 0, 10, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 1 });
        original.addAll(values);
        
        // prepare a filter to filter our list
        MinimumValueMatcherEditor editor = new MinimumValueMatcherEditor();
        FilterList myFilterList = new FilterList(original, editor);
        myFilterList.addListEventListener(new ConsistencyTestList(myFilterList, "filter"));
        
        // relax the list
        editor.setMinimum(11);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(11);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        
        // now try constrain
        values = intArrayToIntegerCollection(new int[] { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11 });
        original.clear();
        original.addAll(values);
        
        // constrain the list
        editor.setMinimum(10);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(11);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(12);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));

        // now try more changes
        values = intArrayToIntegerCollection(new int[] { 8, 6, 7, 5, 3, 0, 9 });
        original.clear();
        original.addAll(values);

        // constrain the list
        editor.setMinimum(5);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(1);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));
        editor.setMinimum(1);
        assertEquals(myFilterList, filter(original, editor.getMatcher()));

    }

    /**
     * Convert the specified int[] array to a List of Integers.
     */
    private List intArrayToIntegerCollection(int[] values) {
        List result = new ArrayList();
        for(int i = 0; i < values.length; i++) {
            result.add(new Integer(values[i]));
        }
        return result;
    }
    
    /**
     * Manually apply the specified filter to the specified list.
     */
    private List filter(List input, Matcher matcher) {
        List result = new ArrayList();
        for(Iterator i = input.iterator(); i.hasNext(); ) {
            Object element = i.next();
            if(matcher.matches(element)) result.add(element);
        }
        return result;
    }
    
    /**
     * A MatcherEditor for minimum values.
     */
    static class MinimumValueMatcherEditor extends AbstractMatcherEditor {
        private int minimum = 0;
        public MinimumValueMatcherEditor() {
            minimum = 0;
            currentMatcher = new MinimumValueMatcher(0);
        }
        public void setMinimum(int value) {
            if(value < minimum) {
                this.minimum = value;
                fireRelaxed(new MinimumValueMatcher(minimum));
            } else if(value == minimum) {
                // do nothing
            } else {
                this.minimum = value;
                fireConstrained(new MinimumValueMatcher(minimum));
            }
        }
    }
    
    /**
     * This matcher matches everything greater than its minimum.
     */
    static class MinimumValueMatcher implements Matcher {
        private int minimum;
        public MinimumValueMatcher(int minimum) {
            this.minimum = minimum;
        }
        public boolean matches(Object value) {
            return ((Integer)value).intValue() >= minimum;
        }
    }
}