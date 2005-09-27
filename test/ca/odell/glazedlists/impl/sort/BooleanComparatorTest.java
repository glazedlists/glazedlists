package ca.odell.glazedlists.impl.sort;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.SortedList;
import junit.framework.TestCase;

public class BooleanComparatorTest extends TestCase {
    public void testComparator() {
        SortedList<Boolean> booleanList = new SortedList<Boolean>(new BasicEventList<Boolean>(), new BooleanComparator());
        booleanList.add(Boolean.TRUE);
        booleanList.add(null);
        booleanList.add(Boolean.FALSE);

        assertEquals(null, booleanList.get(0));
        assertEquals(Boolean.FALSE, booleanList.get(1));
        assertEquals(Boolean.TRUE, booleanList.get(2));
    }
}