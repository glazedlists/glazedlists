/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 10:41:07 AM
 */
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
 * Tests {@link ThresholdMatcherSource}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class ThresholdMatcherTest extends TestCase {
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

    ThresholdMatcherSource threshold_matchersource;


    protected void setUp() throws Exception {
        parent_list = GlazedLists.eventList(new LinkedList(INITIAL_LIST));

        threshold_matchersource = new ThresholdMatcherSource();
        threshold_list = new FilterList(parent_list, threshold_matchersource);
    }

    protected void tearDown() throws Exception {
        threshold_list.dispose();
        threshold_list = null;

        parent_list = null;
    }


    public void testNoThreshold() {
        threshold_matchersource.setThreshold(null);

        assertEquals(parent_list.size(), threshold_list.size());
        for (int i = 0; i < parent_list.size(); i++) {
            assertEquals(parent_list.get(i), threshold_list.get(i));
        }
    }


	public void testLogicInverted() {
		threshold_matchersource.setMatchOperation(ThresholdMatcherSource.GREATER_THAN);
        threshold_matchersource.setThreshold(new Integer(5));

		// Not inverted
		threshold_matchersource.setLogicInverted(false);
        assertEquals(threshold_list.toString(), 5, threshold_list.size()); // 6 7 8 9 10
        for (int i = 0; i < 5; i++) {
            assertEquals(new Integer(6 + i), threshold_list.get(i));
        }

		// Inverted
		threshold_matchersource.setLogicInverted(true);
        assertEquals(threshold_list.toString(), 6, threshold_list.size()); // 0 1 2 3 4 5
        for (int i = 0; i < 6; i++) {
            assertEquals(new Integer(i), threshold_list.get(i));
        }

		// Not inverted
		threshold_matchersource.setLogicInverted(false);
        assertEquals(threshold_list.toString(), 5, threshold_list.size()); // 6 7 8 9 10
        for (int i = 0; i < 5; i++) {
            assertEquals(new Integer(6 + i), threshold_list.get(i));
        }
	}


    public void testGreaterThan() {
        threshold_matchersource.setMatchOperation(ThresholdMatcherSource.GREATER_THAN);

        // In the middle
        threshold_matchersource.setThreshold(new Integer(5));
        assertEquals(threshold_list.toString(), 5, threshold_list.size()); // 6 7 8 9 10
        for (int i = 0; i < 5; i++) {
            assertEquals(new Integer(6 + i), threshold_list.get(i));
        }

        // At min value
        threshold_matchersource.setThreshold(new Integer(0));
        assertEquals(parent_list.size() - 1, threshold_list.size());
        for (int i = 0; i < threshold_list.size(); i++) {
            assertEquals(parent_list.get(i + 1), threshold_list.get(i));
        }

        // Below min value
        threshold_matchersource.setThreshold(new Integer(-1));
        assertEquals(parent_list.size(), threshold_list.size());
        for (int i = 0; i < parent_list.size(); i++) {
            assertEquals(parent_list.get(i), threshold_list.get(i));
        }

        // At max value
        threshold_matchersource.setThreshold(new Integer(10));
        assertTrue(threshold_list.isEmpty());

        // Above max value
        threshold_matchersource.setThreshold(new Integer(11));
        assertTrue(threshold_list.isEmpty());
    }


    public void testGreaterThanOrEqual() {
        threshold_matchersource.setMatchOperation(ThresholdMatcherSource.GREATER_THAN_OR_EQUAL);

        // In the middle
        threshold_matchersource.setThreshold(new Integer(5));
        assertEquals(6, threshold_list.size()); // 5 6 7 8 9 10
        for (int i = 0; i < 6; i++) {
            assertEquals(new Integer(5 + i), threshold_list.get(i));
        }

        // At min value
        threshold_matchersource.setThreshold(new Integer(0));
        assertEquals(parent_list.size(), threshold_list.size());
        for (int i = 0; i < parent_list.size(); i++) {
            assertEquals(parent_list.get(i), threshold_list.get(i));
        }

        // Below min value
        threshold_matchersource.setThreshold(new Integer(-1));
        assertEquals(parent_list.size(), threshold_list.size());
        for (int i = 0; i < parent_list.size(); i++) {
            assertEquals(parent_list.get(i), threshold_list.get(i));
        }

        // At max value
        threshold_matchersource.setThreshold(new Integer(10));
        assertEquals(1, threshold_list.size());
        assertEquals(new Integer(10), threshold_list.get(0));

        // Above max value
        threshold_matchersource.setThreshold(new Integer(11));
        assertTrue(threshold_list.isEmpty());
    }


    public void testLessThan() {
        threshold_matchersource.setMatchOperation(ThresholdMatcherSource.LESS_THAN);

        // In the middle
        threshold_matchersource.setThreshold(new Integer(5));
        assertEquals(5, threshold_list.size()); // 0 1 2 3 4
        for (int i = 0; i < 5; i++) {
            assertEquals(new Integer(i), threshold_list.get(i));
        }

        // At min value
        threshold_matchersource.setThreshold(new Integer(0));
        assertTrue(threshold_list.isEmpty());

        // Below min value
        threshold_matchersource.setThreshold(new Integer(-1));
        assertTrue(threshold_list.isEmpty());

        // At max value
        threshold_matchersource.setThreshold(new Integer(10));
        assertEquals(parent_list.size() - 1, threshold_list.size());
        for (int i = 0; i < threshold_list.size(); i++) {
            assertEquals(parent_list.get(i), threshold_list.get(i));
        }

        // Above max value
        threshold_matchersource.setThreshold(new Integer(11));
        assertEquals(parent_list.size(), threshold_list.size());
        for (int i = 0; i < threshold_list.size(); i++) {
            assertEquals(parent_list.get(i), threshold_list.get(i));
        }
    }


    public void testLessThanOrEqual() {
        threshold_matchersource.setMatchOperation(ThresholdMatcherSource.LESS_THAN_OR_EQUAL);

        // In the middle
        threshold_matchersource.setThreshold(new Integer(5));
        assertEquals(6, threshold_list.size()); // 0 1 2 3 4 5
        for (int i = 0; i < 6; i++) {
            assertEquals(new Integer(i), threshold_list.get(i));
        }

        // At min value
        threshold_matchersource.setThreshold(new Integer(0));
        assertEquals(1, threshold_list.size());
        assertEquals(new Integer(0), threshold_list.get(0));

        // Below min value
        threshold_matchersource.setThreshold(new Integer(-1));
        assertTrue(threshold_list.isEmpty());

        // At max value
        threshold_matchersource.setThreshold(new Integer(10));
        assertEquals(parent_list.size(), threshold_list.size());
        for (int i = 0; i < threshold_list.size(); i++) {
            assertEquals(parent_list.get(i), threshold_list.get(i));
        }

        // Above max value
        threshold_matchersource.setThreshold(new Integer(11));
        assertEquals(parent_list.size(), threshold_list.size());
        for (int i = 0; i < threshold_list.size(); i++) {
            assertEquals(parent_list.get(i), threshold_list.get(i));
        }
    }


    public void testEqual() {
        threshold_matchersource.setMatchOperation(ThresholdMatcherSource.EQUAL);

        // In the middle
        threshold_matchersource.setThreshold(new Integer(5));
        assertEquals(1, threshold_list.size());
        assertEquals(new Integer(5), threshold_list.get(0));

        // At min value
        threshold_matchersource.setThreshold(new Integer(0));
        assertEquals(1, threshold_list.size());
        assertEquals(new Integer(0), threshold_list.get(0));

        // Below min value
        threshold_matchersource.setThreshold(new Integer(-1));
        assertTrue(threshold_list.isEmpty());

        // At max value
        threshold_matchersource.setThreshold(new Integer(10));
        assertEquals(1, threshold_list.size());
        assertEquals(new Integer(10), threshold_list.get(0));

        // Above max value
        threshold_matchersource.setThreshold(new Integer(11));
        assertTrue(threshold_list.isEmpty());
    }


    public void testNotEqual() {
        threshold_matchersource.setMatchOperation(ThresholdMatcherSource.NOT_EQUAL);

        // In the middle
        threshold_matchersource.setThreshold(new Integer(5));
        assertEquals(parent_list.size() - 1, threshold_list.size());
        assertFalse(threshold_list.contains(new Integer(5)));

        // At min value
        threshold_matchersource.setThreshold(new Integer(0));
        assertEquals(parent_list.size() - 1, threshold_list.size());
        assertFalse(threshold_list.contains(new Integer(0)));

        // Below min value
        threshold_matchersource.setThreshold(new Integer(-1));
        assertEquals(parent_list.size(), threshold_list.size());
        for (int i = 0; i < threshold_list.size(); i++) {
            assertEquals(parent_list.get(i), threshold_list.get(i));
        }

        // At max value
        threshold_matchersource.setThreshold(new Integer(10));
        assertEquals(parent_list.size() - 1, threshold_list.size());
        assertFalse(threshold_list.contains(new Integer(10)));

        // Above max value
        threshold_matchersource.setThreshold(new Integer(11));
        assertEquals(parent_list.size(), threshold_list.size());
        for (int i = 0; i < threshold_list.size(); i++) {
            assertEquals(parent_list.get(i), threshold_list.get(i));
        }
    }


    public void testComparator() {
        // set to a comparator that uses absolute values. If not used, nothing will match.
        threshold_matchersource.setMatchOperation(ThresholdMatcherSource.EQUAL);
        threshold_matchersource.setThreshold(new Integer(-1));
        threshold_matchersource.setComparator(new Comparator() {
            public int compare(Object o1, Object o2) {
                Integer one = new Integer(Math.abs(((Integer) o1).intValue()));
                Integer two = new Integer(Math.abs(((Integer) o2).intValue()));

                return one.compareTo(two);
            }
        });

        assertEquals(1, threshold_list.size());
    }


    public void testWrites() {

        // Add when not initially shown via filter
        threshold_matchersource.setMatchOperation(ThresholdMatcherSource.LESS_THAN_OR_EQUAL);
        threshold_matchersource.setThreshold(new Integer(0));

        parent_list.add(new Integer(11));

        assertEquals(INITIAL_LIST.size() + 1, parent_list.size());
        assertEquals(1, threshold_list.size());
        assertEquals(new Integer(0), threshold_list.get(0));

        threshold_matchersource.setMatchOperation(ThresholdMatcherSource.GREATER_THAN_OR_EQUAL);
        threshold_matchersource.setThreshold(new Integer(11));

        assertEquals(1, threshold_list.size());
        assertEquals(new Integer(11), threshold_list.get(0));

        // Add when shown by filter
        threshold_list.add(new Integer(12));

        assertEquals(2, threshold_list.size());
        assertEquals(INITIAL_LIST.size() + 2, parent_list.size());
        assertEquals(new Integer(11), threshold_list.get(0));
        assertEquals(new Integer(12), threshold_list.get(1));
    }
}
