/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;
import ca.odell.glazedlists.matchers.*;
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
        EventList<Integer> original = new BasicEventList<Integer>();
        List<Integer> values = GlazedListsTests.intArrayToIntegerCollection(new int [] { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 0, 10, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 1 });
        original.addAll(values);
        
        // prepare a filter to filter our list
        MinimumValueMatcherEditor editor = new MinimumValueMatcherEditor();
        FilterList<Integer> myFilterList = new FilterList<Integer>(original, editor);
        myFilterList.addListEventListener(new ListConsistencyListener(myFilterList, "filter"));
        
        // relax the list
        editor.setMinimum(11);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(11);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        
        // now try constrain
        values = GlazedListsTests.intArrayToIntegerCollection(new int[] { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11 });
        original.clear();
        original.addAll(values);
        
        // constrain the list
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(11);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(12);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));

        // now try more changes
        values = GlazedListsTests.intArrayToIntegerCollection(new int[] { 8, 6, 7, 5, 3, 0, 9 });
        original.clear();
        original.addAll(values);

        // constrain the list
        editor.setMinimum(5);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(1);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(1);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));

    }

	/**
	 * Test Matchers that fire matchAll() and matchNone() events.
	 */
	public void testMatchAllOrNothing() {
		EventList<Integer> baseList = new BasicEventList<Integer>();
		baseList.add(new Integer(1));
		baseList.add(new Integer(2));
		baseList.add(new Integer(3));
		baseList.add(new Integer(4));
		baseList.add(new Integer(5));

		AllOrNothingMatcherEditor matcher = new AllOrNothingMatcherEditor();
		FilterList<Integer> filterList = new FilterList<Integer>(baseList,matcher);
		filterList.addListEventListener(new ListConsistencyListener(filterList, "Filter List"));

		// Test initial size
		assertEquals(5, filterList.size());

		// Clear it
		matcher.showAll(false);
		assertEquals(0, filterList.size());

		// Put it back
		matcher.showAll(true);
		assertEquals(5, filterList.size());
	}
}


/**
 * A MatcherEditor for minimum values.
 */
class MinimumValueMatcherEditor extends AbstractMatcherEditor<Integer> {
    private int minimum = 0;
    public MinimumValueMatcherEditor() {
        minimum = 0;
        currentMatcher = GlazedListsTests.matchAtLeast(0);
    }
    public void setMinimum(int value) {
        if(value < minimum) {
            this.minimum = value;
            fireRelaxed(GlazedListsTests.matchAtLeast(minimum));
        } else if(value == minimum) {
            // do nothing
        } else {
            this.minimum = value;
            fireConstrained(GlazedListsTests.matchAtLeast(minimum));
        }
    }
}

/**
 * Matcher that allows testing matchAll() and matchNone().
 */
class AllOrNothingMatcherEditor extends AbstractMatcherEditor {
    // otherwise nothin'
    private boolean showAll = true;

    /**
     * @param state True show everything, otherwise show nothing
     */
    public void showAll(boolean state) {
        if (state == showAll) return;

        showAll = state;
        if (state) fireMatchAll();
        else fireMatchNone();
    }
}
