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
 * Tests the generic FilterList class.
 */
public class FilterListTest extends TestCase {

    /**
     * This test demonstrates a Issue 213.
     */
    public void testRelax() {
        // construct a (contrived) list of initial values
        int[] values = new int [] { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 0, 10, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 1 };
        BasicEventList original = new BasicEventList();
        for(int i = 0; i < values.length; i++) {
            original.add(new Integer(values[i]));
        }
        
        // prepare a filter to filter our list
        MinimumValueMatcherEditor editor = new MinimumValueMatcherEditor();
        FilterList myFilterList = new FilterList(original, editor);
        
        // relax the list
        editor.setMinimum(11);
        editor.setMinimum(10);
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