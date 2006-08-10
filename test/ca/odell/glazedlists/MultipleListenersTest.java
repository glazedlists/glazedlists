/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// the volatile Glazed Lists package
import ca.odell.glazedlists.impl.sort.*;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
// standard collections
import java.util.*;

/**
 * This test verifies that event lists can have multiple listeners.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class MultipleListenersTest extends TestCase {

    /** for randomly choosing list indices */
    private Random random = new Random();

    /**
     * The multiple listeners test creates a single BasicEventList and a
     * set of listeners on it. The basic event list is changed and each
     * listener is changed in sequence to verify that the listeners all
     * receive notification.
     */
    public void testMultipleListeners() {
        BasicEventList root = new BasicEventList();
        List control = new ArrayList();

        // add 1000 elements to start
        for(int i = 0; i < 1000; i++) {
            Integer value = new Integer(random.nextInt(100));
            root.add(value);
            control.add(value);
        }

        // create sorted and filtered derivatives
        Comparator comparator = GlazedLists.comparableComparator();
        SortedList sorted = new SortedList(root, comparator);
        IntegerSizeMatcherEditor matcherEditor = new IntegerSizeMatcherEditor(50);
        FilterList filtered = new FilterList(root, matcherEditor);

        // repeatedly make updates and verify the derivates keep up
        for(int i = 0; i < 30; i++) {
            // add 100 elements
            for(int j = 0; j < 100; j++) {
                Integer value = new Integer(random.nextInt(100));
                root.add(value);
                control.add(value);
            }

            // verify the base list is correct
            assertEquals(root, control);

            // verify that the sorted list is correct
            List sortedControl = new ArrayList();
            sortedControl.addAll(control);
            Collections.sort(sortedControl, sorted.getComparator());
            assertEquals(sortedControl, sorted);

            // verify that the filtered list is correct
            List filteredControl = new ArrayList();
            for(int j = 0; j < control.size(); j++) {
                if(matcherEditor.getMatcher().matches(control.get(j))) {
                    filteredControl.add(control.get(j));
                }
            }
            assertEquals(filteredControl, filtered);

            // adjust the sorter
            if(comparator instanceof ReverseComparator) {
                comparator = GlazedLists.comparableComparator();
            } else {
                comparator = GlazedLists.reverseComparator(comparator);
            }
            sorted.setComparator(comparator);

            // adjust the filter
            matcherEditor.setThreshhold(random.nextInt(100));
        }
    }

    /**
     * A simple filter for filtering integers by size.
     */
    class IntegerSizeMatcherEditor extends AbstractMatcherEditor {
        public IntegerSizeMatcherEditor(int threshhold) {
            setThreshhold(threshhold);
        }
        public void setThreshhold(int threshhold) {
            fireChanged(new IntegerMatcher(threshhold));
        }
        private class IntegerMatcher implements Matcher {
            int threshhold;
            public IntegerMatcher(int threshhold) {
                this.threshhold = threshhold;
            }
            public boolean matches(Object element) {
                Integer integer = (Integer)element;
                return (integer.intValue() >= threshhold);
            }
        }
    }

    /**
     * The main method simply provides access to this class outside of JUnit.
     */
    public static void main(String[] args) {
        new MultipleListenersTest().testMultipleListeners();
    }
}
