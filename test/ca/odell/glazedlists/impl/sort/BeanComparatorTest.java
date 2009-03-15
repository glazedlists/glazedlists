/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.sort;

// for being a JUnit test case
import ca.odell.glazedlists.GlazedLists;

import junit.framework.TestCase;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This test verifies that the BeanComparator works as expected.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 * @author Andrea Aime
 */
public class BeanComparatorTest extends TestCase {

    /**
     * Tests that comparison by property works.
     */
    public void testCompare() {
        Comparator comparator = GlazedLists.beanPropertyComparator(Position.class, "position", new String[0]);

        assertTrue(comparator.compare(new Position(4), new Position(1)) > 0);
        assertTrue(comparator.compare(new Position(1), new Position(4)) < 0);
        assertTrue(comparator.compare(new Position(3), new Position(3)) == 0);
    }

    /**
     * Tests that the equals() method of the comparator works.
     */
    public void testEquals() {
        final String[] empty = {};
        Comparator red = GlazedLists.beanPropertyComparator(Color.class, "red", empty);
        Comparator blue = GlazedLists.beanPropertyComparator(Color.class, "blue", empty);
        Comparator rouge = GlazedLists.beanPropertyComparator(Color.class, "red", empty);
        Comparator blueBeer = GlazedLists.beanPropertyComparator(LabattBeer.class, "blue", empty);

        assertTrue(red.equals(rouge));
        assertFalse(blue.equals(blueBeer));
        assertFalse(red.equals(blue));
    }

    /**
     * Tests that comparison by property works.
     */
    public void testSort() {
        // prepare the sample list
        List unsorted = new ArrayList();
        unsorted.add(new Position(4));
        unsorted.add(new Position(1));
        unsorted.add(new Position(3));

        List sorted1 = new ArrayList();
        sorted1.addAll(unsorted);
        Collections.sort(sorted1);

        List sorted2 = new ArrayList();
        sorted2.addAll(unsorted);
        Collections.sort(sorted2, GlazedLists.beanPropertyComparator(Position.class, "position", new String[0]));

        assertEquals(sorted1, sorted2);
    }

    /**
     * Simple class that sorts in the same order as its position value.
     */
    public static class Position implements Comparable {
        private int position;
        public Position(int position) {
            this.position = position;
        }
        public int getPosition() { return position; }
        public void setPosition(int position) { this.position = position; }
        @Override
        public String toString() {
            return "P:" + position;
        }
        public int compareTo(Object o) {
            return position - ((Position)o).position;
        }
    }

    /**
     * Simple class with a blue property.
     */
    public static class LabattBeer {
        public int getBlue() { return 24; }
        public int getBlueLight() { return 6; }
        public int getDry() { return 6; }
        public int getWildcat() { return 24; }
    }
}
