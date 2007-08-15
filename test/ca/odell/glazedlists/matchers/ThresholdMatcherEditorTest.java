/* Glazed Lists                                                      (c) 2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.FunctionList;
import junit.framework.TestCase;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

/**
 * Tests {@link ThresholdMatcherEditor}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class ThresholdMatcherEditorTest extends TestCase {

    private static final Integer MINUS_ONE = new Integer(-1);
    private static final Integer ZERO = new Integer(0);
    private static final Integer ONE = new Integer(1);
    private static final Integer TWO = new Integer(2);
    private static final Integer THREE = new Integer(3);
    private static final Integer FOUR = new Integer(4);
    private static final Integer FIVE = new Integer(5);
    private static final Integer SIX = new Integer(6);
    private static final Integer SEVEN = new Integer(7);
    private static final Integer EIGHT = new Integer(8);
    private static final Integer NINE = new Integer(9);
    private static final Integer TEN = new Integer(10);
    private static final Integer ELEVEN = new Integer(11);
    private static final Integer TWELVE = new Integer(12);

    private static final List<Integer> INITIAL_LIST = new ArrayList<Integer>(10);
    static {
        for (int i = 0; i < 11; i++)
            INITIAL_LIST.add(new Integer(i));
    }

	EventList<Integer> sourceList;
	FilterList<Integer> filterList;
	ThresholdMatcherEditor<Integer,Integer> thresholdMatcherEditor;

	protected void setUp() throws Exception {
		sourceList = GlazedLists.eventList(INITIAL_LIST);
		thresholdMatcherEditor = new ThresholdMatcherEditor<Integer,Integer>();
		filterList = new FilterList<Integer>(sourceList, thresholdMatcherEditor);
	}

	protected void tearDown() throws Exception {
		filterList.dispose();
		filterList = null;
		sourceList = null;
        thresholdMatcherEditor = null;
    }

	public void testNoThreshold() {
		thresholdMatcherEditor.setThreshold(null);

		assertEquals(sourceList.size(), filterList.size());
		for (int i = 0; i < sourceList.size(); i++) {
			assertEquals(sourceList.get(i), filterList.get(i));
		}
	}

    /**
     * Test that toggling between equal and not equal works as expected.
     */
    public void testToggleEqual() {
		thresholdMatcherEditor.setThreshold(FIVE);

		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
		assertEquals(1, filterList.size());

		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.NOT_EQUAL);
		assertEquals(10, filterList.size());

		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
		assertEquals(1, filterList.size());
	}

    /**
     * Test that toggling between operations works as expected.
     */
    public void testToggleOperations() {
		thresholdMatcherEditor.setThreshold(SIX);

		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
		assertEquals(5, filterList.size());

		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
		assertEquals(4, filterList.size());

		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
		assertEquals(5, filterList.size());

		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
		assertEquals(7, filterList.size());

		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
		assertEquals(6, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
        assertEquals(7, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
        assertEquals(1, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
        assertEquals(6, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
        assertEquals(1, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
        assertEquals(4, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
        assertEquals(1, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
        assertEquals(5, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
        assertEquals(1, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
        assertEquals(7, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
        assertEquals(4, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
        assertEquals(7, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
        assertEquals(6, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
        assertEquals(5, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
        assertEquals(6, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
        assertEquals(4, filterList.size());

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
        assertEquals(6, filterList.size());
	}

	public void testLogic() {
		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
		thresholdMatcherEditor.setThreshold(FIVE);

		assertEquals(filterList.toString(), 5, filterList.size()); // 6 7 8 9 10
		for (int i = 0; i < 5; i++) {
			assertEquals(new Integer(6 + i), filterList.get(i));
		}

		thresholdMatcherEditor.setThreshold(SEVEN);

		assertEquals(filterList.toString(), 3, filterList.size()); // 8 9 10
		for (int i = 0; i < 3; i++) {
			assertEquals(new Integer(8 + i), filterList.get(i));
		}
	}

	public void testGreaterThan() {
		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);

		// In the middle
		thresholdMatcherEditor.setThreshold(FIVE);
		assertEquals(filterList.toString(), 5, filterList.size()); // 6 7 8 9 10
		for (int i = 0; i < 5; i++) {
			assertEquals(new Integer(6 + i), filterList.get(i));
		}

		// At min value
		thresholdMatcherEditor.setThreshold(ZERO);
		assertEquals(sourceList.size() - 1, filterList.size());
		for (int i = 0; i < filterList.size(); i++) {
			assertEquals(sourceList.get(i + 1), filterList.get(i));
		}

		// Below min value
		thresholdMatcherEditor.setThreshold(MINUS_ONE);
		assertEquals(sourceList.size(), filterList.size());
		for (int i = 0; i < sourceList.size(); i++) {
			assertEquals(sourceList.get(i), filterList.get(i));
		}

		// At max value
		thresholdMatcherEditor.setThreshold(TEN);
		assertTrue(filterList.isEmpty());

		// Above max value
		thresholdMatcherEditor.setThreshold(ELEVEN);
		assertTrue(filterList.isEmpty());
	}

	public void testGreaterThanOrEqual() {
		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);

		// In the middle
		thresholdMatcherEditor.setThreshold(FIVE);
		assertEquals(6, filterList.size()); // 5 6 7 8 9 10
		for (int i = 0; i < 6; i++) {
			assertEquals(new Integer(5 + i), filterList.get(i));
		}

		// At min value
		thresholdMatcherEditor.setThreshold(ZERO);
		assertEquals(sourceList.size(), filterList.size());
		for (int i = 0; i < sourceList.size(); i++) {
			assertEquals(sourceList.get(i), filterList.get(i));
		}

		// Below min value
		thresholdMatcherEditor.setThreshold(MINUS_ONE);
		assertEquals(sourceList.size(), filterList.size());
		for (int i = 0; i < sourceList.size(); i++) {
			assertEquals(sourceList.get(i), filterList.get(i));
		}

		// At max value
		thresholdMatcherEditor.setThreshold(TEN);
		assertEquals(1, filterList.size());
		assertEquals(TEN, filterList.get(0));

		// Above max value
		thresholdMatcherEditor.setThreshold(ELEVEN);
		assertTrue(filterList.isEmpty());
	}

	public void testLessThan() {
		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);

		// In the middle
		thresholdMatcherEditor.setThreshold(FIVE);
		assertEquals(filterList.toString(), 5, filterList.size()); // 0 1 2 3 4
		for (int i = 0; i < 5; i++) {
			assertEquals(new Integer(i), filterList.get(i));
		}

		// At min value
		thresholdMatcherEditor.setThreshold(ZERO);
		assertTrue(filterList.isEmpty());

		// Below min value
		thresholdMatcherEditor.setThreshold(MINUS_ONE);
		assertTrue(filterList.isEmpty());

		// At max value
		thresholdMatcherEditor.setThreshold(TEN);
		assertEquals(sourceList.size() - 1, filterList.size());
		for (int i = 0; i < filterList.size(); i++) {
			assertEquals(sourceList.get(i), filterList.get(i));
		}

		// Above max value
		thresholdMatcherEditor.setThreshold(ELEVEN);
		assertEquals(sourceList.size(), filterList.size());
		for (int i = 0; i < filterList.size(); i++) {
			assertEquals(sourceList.get(i), filterList.get(i));
		}
	}

	public void testLessThanOrEqual() {
		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);

		// In the middle
		thresholdMatcherEditor.setThreshold(FIVE);
		assertEquals(6, filterList.size()); // 0 1 2 3 4 5
		for (int i = 0; i < 6; i++) {
			assertEquals(new Integer(i), filterList.get(i));
		}

		// At min value
		thresholdMatcherEditor.setThreshold(ZERO);
		assertEquals(1, filterList.size());
		assertEquals(ZERO, filterList.get(0));

		// Below min value
		thresholdMatcherEditor.setThreshold(MINUS_ONE);
		assertTrue(filterList.isEmpty());

		// At max value
		thresholdMatcherEditor.setThreshold(TEN);
		assertEquals(sourceList.size(), filterList.size());
		for (int i = 0; i < filterList.size(); i++) {
			assertEquals(sourceList.get(i), filterList.get(i));
		}

		// Above max value
		thresholdMatcherEditor.setThreshold(ELEVEN);
		assertEquals(sourceList.size(), filterList.size());
		for (int i = 0; i < filterList.size(); i++) {
			assertEquals(sourceList.get(i), filterList.get(i));
		}
	}

	public void testEqual() {
		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);

		// In the middle
		thresholdMatcherEditor.setThreshold(FIVE);
		assertEquals(1, filterList.size());
		assertEquals(FIVE, filterList.get(0));

		// At min value
		thresholdMatcherEditor.setThreshold(ZERO);
		assertEquals(1, filterList.size());
		assertEquals(ZERO, filterList.get(0));

		// Below min value
		thresholdMatcherEditor.setThreshold(MINUS_ONE);
		assertTrue(filterList.isEmpty());

		// At max value
		thresholdMatcherEditor.setThreshold(TEN);
		assertEquals(1, filterList.size());
		assertEquals(TEN, filterList.get(0));

		// Above max value
		thresholdMatcherEditor.setThreshold(ELEVEN);
		assertTrue(filterList.isEmpty());
	}

	public void testNotEqual() {
		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.NOT_EQUAL);

		// In the middle
		thresholdMatcherEditor.setThreshold(FIVE);
		assertEquals(sourceList.size() - 1, filterList.size());
		assertFalse(filterList.contains(FIVE));

		// At min value
		thresholdMatcherEditor.setThreshold(ZERO);
		assertEquals(sourceList.size() - 1, filterList.size());
		assertFalse(filterList.toString(), filterList.contains(ZERO));

		// Below min value
		thresholdMatcherEditor.setThreshold(MINUS_ONE);
		assertEquals(sourceList.size(), filterList.size());
		for (int i = 0; i < filterList.size(); i++) {
			assertEquals(sourceList.get(i), filterList.get(i));
		}

		// At max value
		thresholdMatcherEditor.setThreshold(TEN);
		assertEquals(sourceList.size() - 1, filterList.size());
		assertFalse(filterList.contains(TEN));

		// Above max value
		thresholdMatcherEditor.setThreshold(ELEVEN);
		assertEquals(sourceList.size(), filterList.size());
		for (int i = 0; i < filterList.size(); i++) {
			assertEquals(sourceList.get(i), filterList.get(i));
		}
	}

	public void testComparator() {
		// set to a comparator that uses absolute values. If not used, nothing will match.
		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
		thresholdMatcherEditor.setThreshold(MINUS_ONE);
		thresholdMatcherEditor.setComparator(new AbsComparator());

		assertEquals(1, filterList.size());
	}

    public void testWrites() {
		// Add when not initially shown via filter
		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
		thresholdMatcherEditor.setThreshold(ZERO);

		sourceList.add(ELEVEN);

		assertEquals(INITIAL_LIST.size() + 1, sourceList.size());
		assertEquals(1, filterList.size());
		assertEquals(ZERO, filterList.get(0));

		thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
		thresholdMatcherEditor.setThreshold(ELEVEN);

		assertEquals(1, filterList.size());
		assertEquals(ELEVEN, filterList.get(0));

		// Add when shown by filter
		filterList.add(TWELVE);

		assertEquals(2, filterList.size());
		assertEquals(INITIAL_LIST.size() + 2, sourceList.size());
		assertEquals(ELEVEN, filterList.get(0));
		assertEquals(TWELVE, filterList.get(1));
	}

    public void testFunction() {
        sourceList = GlazedLists.eventList(INITIAL_LIST);
        thresholdMatcherEditor = new ThresholdMatcherEditor<Integer,Integer>(null, null, null, new FirstNumberFunction());
        filterList = new FilterList<Integer>(sourceList, thresholdMatcherEditor);

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
        thresholdMatcherEditor.setThreshold(ONE);
        assertEquals(2, filterList.size());
        assertEquals(ONE, filterList.get(0));
        assertEquals(TEN, filterList.get(1));

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
        assertEquals(3, filterList.size());
        assertEquals(ZERO, filterList.get(0));
        assertEquals(ONE, filterList.get(1));
        assertEquals(TEN, filterList.get(2));

        thresholdMatcherEditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
        thresholdMatcherEditor.setThreshold(FIVE);
        assertEquals(5, filterList.size());
        assertEquals(FIVE, filterList.get(0));
        assertEquals(SIX, filterList.get(1));
        assertEquals(SEVEN, filterList.get(2));
        assertEquals(EIGHT, filterList.get(3));
        assertEquals(NINE, filterList.get(4));
    }

    private static class FirstNumberFunction implements FunctionList.Function<Integer, Integer> {
        public Integer evaluate(Integer sourceValue) {
            if (sourceValue == null) return null;

            return new Integer(String.valueOf(sourceValue.intValue()).substring(0, 1));
        }
    }

    /**
     * Compare two Integers by their absolute value.
     */
    private static class AbsComparator implements Comparator<Integer> {
        public int compare(Integer o1, Integer o2) {
            return Math.abs(o1.intValue()) - Math.abs(o2.intValue());
        }
    }
}