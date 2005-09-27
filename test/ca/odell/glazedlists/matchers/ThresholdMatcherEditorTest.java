/* Glazed Lists                                                      (c) 2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * Tests {@link ThresholdMatcherEditor}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class ThresholdMatcherEditorTest extends TestCase {
	private static final List INITIAL_LIST = Arrays.asList(new Integer[]{
		new Integer(0),
		new Integer(1),
		new Integer(2),
		new Integer(3),
		new Integer(4),
		new Integer(5),
		new Integer(6),
		new Integer(7),
		new Integer(8),
		new Integer(9),
		new Integer(10)
	});

	EventList parent_list;
	FilterList threshold_list;

	ThresholdMatcherEditor threshold_matchereditor;


	protected void setUp() throws Exception {
		parent_list = GlazedLists.eventList(new LinkedList(INITIAL_LIST));

		threshold_matchereditor = new ThresholdMatcherEditor();
		threshold_list = new FilterList(parent_list, threshold_matchereditor);
	}

	protected void tearDown() throws Exception {
		threshold_list.dispose();
		threshold_list = null;

		parent_list = null;
	}


	public void testNoThreshold() {
		threshold_matchereditor.setThreshold(null);

		assertEquals(parent_list.size(), threshold_list.size());
		for (int i = 0; i < parent_list.size(); i++) {
			assertEquals(parent_list.get(i), threshold_list.get(i));
		}
	}

    /**
     * Test that toggling between equal and not equal works as expected.
     */
    public void testToggleEqual() {
		threshold_matchereditor.setThreshold(new Integer(5));

		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
		assertEquals(1, threshold_list.size());

		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.NOT_EQUAL);
		assertEquals(10, threshold_list.size());

		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
		assertEquals(1, threshold_list.size());
	}

    /**
     * Test that toggling between operations works as expected.
     */
    public void testToggleOperations() {
		threshold_matchereditor.setThreshold(new Integer(6));

		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
		assertEquals(5, threshold_list.size());

		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
		assertEquals(4, threshold_list.size());

		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
		assertEquals(5, threshold_list.size());

		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
		assertEquals(7, threshold_list.size());

		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
		assertEquals(6, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
        assertEquals(7, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
        assertEquals(1, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
        assertEquals(6, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
        assertEquals(1, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
        assertEquals(4, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
        assertEquals(1, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
        assertEquals(5, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
        assertEquals(1, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
        assertEquals(7, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
        assertEquals(4, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
        assertEquals(7, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
        assertEquals(6, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
        assertEquals(5, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
        assertEquals(6, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
        assertEquals(4, threshold_list.size());

        threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);
        assertEquals(6, threshold_list.size());
	}


	public void testLogicInverted() {
		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);
		threshold_matchereditor.setThreshold(new Integer(5));

		// Not inverted
		//threshold_matchereditor.setLogicInverted(false);
		assertEquals(threshold_list.toString(), 5, threshold_list.size()); // 6 7 8 9 10
		for (int i = 0; i < 5; i++) {
			assertEquals(new Integer(6 + i), threshold_list.get(i));
		}

		// Inverted
//		threshold_matchereditor.setLogicInverted(true);
//		assertEquals(threshold_list.toString(), 6, threshold_list.size()); // 0 1 2 3 4 5
//		for (int i = 0; i < 6; i++) {
//			assertEquals(new Integer(i), threshold_list.get(i));
//		}
//
		threshold_matchereditor.setThreshold(new Integer(7));
//		assertEquals(threshold_list.toString(), 8, threshold_list.size()); // 0 1 2 3 4 5 6 7
//		for (int i = 0; i < 6; i++) {
//			assertEquals(new Integer(i), threshold_list.get(i));
//		}

		// Not inverted
//		threshold_matchereditor.setLogicInverted(false);
		assertEquals(threshold_list.toString(), 3, threshold_list.size()); // 8 9 10
		for (int i = 0; i < 3; i++) {
			assertEquals(new Integer(8 + i), threshold_list.get(i));
		}
	}


	public void testGreaterThan() {
		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN);

		// In the middle
		threshold_matchereditor.setThreshold(new Integer(5));
		assertEquals(threshold_list.toString(), 5, threshold_list.size()); // 6 7 8 9 10
		for (int i = 0; i < 5; i++) {
			assertEquals(new Integer(6 + i), threshold_list.get(i));
		}

		// At min value
		threshold_matchereditor.setThreshold(new Integer(0));
		assertEquals(parent_list.size() - 1, threshold_list.size());
		for (int i = 0; i < threshold_list.size(); i++) {
			assertEquals(parent_list.get(i + 1), threshold_list.get(i));
		}

		// Below min value
		threshold_matchereditor.setThreshold(new Integer(-1));
		assertEquals(parent_list.size(), threshold_list.size());
		for (int i = 0; i < parent_list.size(); i++) {
			assertEquals(parent_list.get(i), threshold_list.get(i));
		}

		// At max value
		threshold_matchereditor.setThreshold(new Integer(10));
		assertTrue(threshold_list.isEmpty());

		// Above max value
		threshold_matchereditor.setThreshold(new Integer(11));
		assertTrue(threshold_list.isEmpty());
	}


	public void testGreaterThanOrEqual() {
		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);

		// In the middle
		threshold_matchereditor.setThreshold(new Integer(5));
		assertEquals(6, threshold_list.size()); // 5 6 7 8 9 10
		for (int i = 0; i < 6; i++) {
			assertEquals(new Integer(5 + i), threshold_list.get(i));
		}

		// At min value
		threshold_matchereditor.setThreshold(new Integer(0));
		assertEquals(parent_list.size(), threshold_list.size());
		for (int i = 0; i < parent_list.size(); i++) {
			assertEquals(parent_list.get(i), threshold_list.get(i));
		}

		// Below min value
		threshold_matchereditor.setThreshold(new Integer(-1));
		assertEquals(parent_list.size(), threshold_list.size());
		for (int i = 0; i < parent_list.size(); i++) {
			assertEquals(parent_list.get(i), threshold_list.get(i));
		}

		// At max value
		threshold_matchereditor.setThreshold(new Integer(10));
		assertEquals(1, threshold_list.size());
		assertEquals(new Integer(10), threshold_list.get(0));

		// Above max value
		threshold_matchereditor.setThreshold(new Integer(11));
		assertTrue(threshold_list.isEmpty());
	}


	public void testLessThan() {
		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN);

		// In the middle
		threshold_matchereditor.setThreshold(new Integer(5));
		assertEquals(threshold_list.toString(), 5, threshold_list.size()); // 0 1 2 3 4
		for (int i = 0; i < 5; i++) {
			assertEquals(new Integer(i), threshold_list.get(i));
		}

		// At min value
		threshold_matchereditor.setThreshold(new Integer(0));
		assertTrue(threshold_list.isEmpty());

		// Below min value
		threshold_matchereditor.setThreshold(new Integer(-1));
		assertTrue(threshold_list.isEmpty());

		// At max value
		threshold_matchereditor.setThreshold(new Integer(10));
		assertEquals(parent_list.size() - 1, threshold_list.size());
		for (int i = 0; i < threshold_list.size(); i++) {
			assertEquals(parent_list.get(i), threshold_list.get(i));
		}

		// Above max value
		threshold_matchereditor.setThreshold(new Integer(11));
		assertEquals(parent_list.size(), threshold_list.size());
		for (int i = 0; i < threshold_list.size(); i++) {
			assertEquals(parent_list.get(i), threshold_list.get(i));
		}
	}


	public void testLessThanOrEqual() {
		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);

		// In the middle
		threshold_matchereditor.setThreshold(new Integer(5));
		assertEquals(6, threshold_list.size()); // 0 1 2 3 4 5
		for (int i = 0; i < 6; i++) {
			assertEquals(new Integer(i), threshold_list.get(i));
		}

		// At min value
		threshold_matchereditor.setThreshold(new Integer(0));
		assertEquals(1, threshold_list.size());
		assertEquals(new Integer(0), threshold_list.get(0));

		// Below min value
		threshold_matchereditor.setThreshold(new Integer(-1));
		assertTrue(threshold_list.isEmpty());

		// At max value
		threshold_matchereditor.setThreshold(new Integer(10));
		assertEquals(parent_list.size(), threshold_list.size());
		for (int i = 0; i < threshold_list.size(); i++) {
			assertEquals(parent_list.get(i), threshold_list.get(i));
		}

		// Above max value
		threshold_matchereditor.setThreshold(new Integer(11));
		assertEquals(parent_list.size(), threshold_list.size());
		for (int i = 0; i < threshold_list.size(); i++) {
			assertEquals(parent_list.get(i), threshold_list.get(i));
		}
	}


	public void testEqual() {
		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);

		// In the middle
		threshold_matchereditor.setThreshold(new Integer(5));
		assertEquals(1, threshold_list.size());
		assertEquals(new Integer(5), threshold_list.get(0));

		// At min value
		threshold_matchereditor.setThreshold(new Integer(0));
		assertEquals(1, threshold_list.size());
		assertEquals(new Integer(0), threshold_list.get(0));

		// Below min value
		threshold_matchereditor.setThreshold(new Integer(-1));
		assertTrue(threshold_list.isEmpty());

		// At max value
		threshold_matchereditor.setThreshold(new Integer(10));
		assertEquals(1, threshold_list.size());
		assertEquals(new Integer(10), threshold_list.get(0));

		// Above max value
		threshold_matchereditor.setThreshold(new Integer(11));
		assertTrue(threshold_list.isEmpty());
	}


	public void testNotEqual() {
		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.NOT_EQUAL);

		// In the middle
		threshold_matchereditor.setThreshold(new Integer(5));
		assertEquals(parent_list.size() - 1, threshold_list.size());
		assertFalse(threshold_list.contains(new Integer(5)));

		// At min value
		threshold_matchereditor.setThreshold(new Integer(0));
		assertEquals(parent_list.size() - 1, threshold_list.size());
		assertFalse(threshold_list.toString(), threshold_list.contains(new Integer(0)));

		// Below min value
		threshold_matchereditor.setThreshold(new Integer(-1));
		assertEquals(parent_list.size(), threshold_list.size());
		for (int i = 0; i < threshold_list.size(); i++) {
			assertEquals(parent_list.get(i), threshold_list.get(i));
		}

		// At max value
		threshold_matchereditor.setThreshold(new Integer(10));
		assertEquals(parent_list.size() - 1, threshold_list.size());
		assertFalse(threshold_list.contains(new Integer(10)));

		// Above max value
		threshold_matchereditor.setThreshold(new Integer(11));
		assertEquals(parent_list.size(), threshold_list.size());
		for (int i = 0; i < threshold_list.size(); i++) {
			assertEquals(parent_list.get(i), threshold_list.get(i));
		}
	}


	public void testComparator() {
		// set to a comparator that uses absolute values. If not used, nothing will match.
		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.EQUAL);
		threshold_matchereditor.setThreshold(new Integer(-1));
		threshold_matchereditor.setComparator(new AbsComparator());

		assertEquals(1, threshold_list.size());
	}

    public void testWrites() {

		// Add when not initially shown via filter
		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.LESS_THAN_OR_EQUAL);
		threshold_matchereditor.setThreshold(new Integer(0));

		parent_list.add(new Integer(11));

		assertEquals(INITIAL_LIST.size() + 1, parent_list.size());
		assertEquals(1, threshold_list.size());
		assertEquals(new Integer(0), threshold_list.get(0));

		threshold_matchereditor.setMatchOperation(ThresholdMatcherEditor.GREATER_THAN_OR_EQUAL);
		threshold_matchereditor.setThreshold(new Integer(11));

		assertEquals(1, threshold_list.size());
		assertEquals(new Integer(11), threshold_list.get(0));

		// Add when shown by filter
		threshold_list.add(new Integer(12));

		assertEquals(2, threshold_list.size());
		assertEquals(INITIAL_LIST.size() + 2, parent_list.size());
		assertEquals(new Integer(11), threshold_list.get(0));
		assertEquals(new Integer(12), threshold_list.get(1));
	}

    /**
     * Compare two Integers by their absolute value.
     */
    private static class AbsComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Integer one = new Integer(Math.abs(((Integer) o1).intValue()));
            Integer two = new Integer(Math.abs(((Integer) o2).intValue()));

            return one.compareTo(two);
        }
    }
}