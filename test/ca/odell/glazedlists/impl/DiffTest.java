/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
// standard collections
import java.util.*;

/**
 * This test verifies that Diff works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class DiffTest extends TestCase {

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
     * Tests to verify that Diff performs the correct number of changes.
     */
    public void testDiff() {
        assertEquals(4, getChangeCount("algorithm", "logarithm", false));
        assertEquals(5, getChangeCount("abcabba", "cbabac", false));
        assertEquals(0, getChangeCount("Jesse", "JESSE", true));
        assertEquals(8, getChangeCount("Jesse", "JESSE", false));
    }
    
    /**
     * Tests that diff works for a large number of elements.
     */
    public void testMemory() {
        EventList sequence = new BasicEventList(new SparseDifferencesList(new ReallyBigList(1000 * 1000)));
        List modifiedSequence = new SparseDifferencesList(new ReallyBigList(1000 * 1000));
        assertEquals(0, getChangeCount(sequence, modifiedSequence, false, null));

        Random dice = new Random();
        for(int i = 0; i < 10; i++) {
            modifiedSequence.set(dice.nextInt(modifiedSequence.size()), new Object());
        }
        assertEquals(20, getChangeCount(sequence, modifiedSequence, false, null));
        assertEquals(sequence, modifiedSequence);
    }

    /**
     * Counts the number of changes to change target to source.
     */
    private int getChangeCount(EventList targetList, List sourceList, boolean updates, Comparator comparator) {
        ListEventCounter counter = new ListEventCounter();
        targetList.addListEventListener(counter);

        if(comparator != null) GlazedLists.replaceAll(targetList, sourceList, false, comparator);
        else GlazedLists.replaceAll(targetList, sourceList, false);

        return counter.getEventCount();
    }

    /**
     * Converts the strings to lists and counts the changes between them.
     *
     * <p>If case sensitivity is specified, an appropriate {@link Comparator} will be
     * used to determine equality between elements.
     */
    private int getChangeCount(String target, String source, boolean caseSensitive) {
        EventList targetList = new BasicEventList();
        targetList.addAll(stringToList(target));
        List sourceList = stringToList(source);

        return getChangeCount(targetList, sourceList, false, caseSensitive ? GlazedLists.caseInsensitiveComparator() : null);
    }

    /**
     * Create a list, where each element is a character from the String.
     */
    private List stringToList(String data) {
        List result = new ArrayList();
        for(int c = 0; c < data.length(); c++) {
            result.add(data.substring(c, c+1));
        }
        return result;
    }

    /**
     * A list that returns the integer index as the row value.
     */
    private class ReallyBigList extends AbstractList {
        private int size;
        public ReallyBigList(int size) {
            this.size = size;
        }
        public Object get(int index) {
            return new Integer(index);
        }

        public int size() {
            return size;
        }
        public Object remove(int index) {
            size--;
            return new Integer(index);
        }
        public void add(int index, Object value) {
            size++;
        }
    }

    /**
     * Decorates a list with a small set of changes.
     */
    private class SparseDifferencesList extends AbstractList {
        private Map values = new HashMap();
        private List delegate;
        public SparseDifferencesList(List delegate) {
            this.delegate = delegate;
        }
        public Object get(int index) {
            Object mapValue = values.get(new Integer(index));
            if(mapValue != null) return mapValue;

            return delegate.get(index);
        }
        public int size() {
            return delegate.size();
        }

        public Object set(int index, Object value) {
            return values.put(new Integer(index), value);
        }
        public void add(int index, Object element) {
            delegate.add(index, element);
            set(index, element);
        }
        public Object remove(int index) {
            return delegate.remove(index);
        }
    }
}
