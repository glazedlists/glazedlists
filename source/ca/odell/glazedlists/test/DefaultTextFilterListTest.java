/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.test;

import junit.framework.TestCase;
import ca.odell.glazedlists.DefaultTextFilterList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.EventList;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class DefaultTextFilterListTest extends TestCase {

    private List numbers = Arrays.asList(new Object[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});

    private List monotonicAlphabet = Arrays.asList(new Object[] {"0", "01", "012", "0123", "01234", "012345", "0123456", "01234567", "012345678", "0123456789"});

    public void testNormalizeValue() {
        TrivialDefaultTextFilterList list = new TrivialDefaultTextFilterList(new BasicEventList());

        assertTrue(Arrays.equals(new String[0], list.normalizeFilter(new String[0])));
        assertTrue(Arrays.equals(new String[0], list.normalizeFilter(new String[] {null, ""})));
        assertTrue(Arrays.equals(new String[] {"X"}, list.normalizeFilter(new String[] {"x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"X", "Y", "Z"}, list.normalizeFilter(new String[] {null, "", "x", null, "", "Y", null, "", "z", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, list.normalizeFilter(new String[] {null, "", "x", null, "", "xy", null, "", "xyz", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, list.normalizeFilter(new String[] {null, "", "xyz", null, "", "xy", null, "", "x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, list.normalizeFilter(new String[] {null, "", "xy", null, "", "xyz", null, "", "x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, list.normalizeFilter(new String[] {null, "", "xyz", null, "", "xyz", null, "", "xyz", null, ""})));
    }

    public void testIsFilterRelaxed() {
        DefaultTextFilterList list = new DefaultTextFilterList(new BasicEventList());

        // removing last filter term
        assertTrue(list.isFilterRelaxed(new String[] {"x"}, new String[0]));
        // shortening filter term
        assertTrue(list.isFilterRelaxed(new String[] {"xx"}, new String[] {"x"}));
        // removing filter term
        assertTrue(list.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"xx"}));
        // removing filter term
        assertTrue(list.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"y"}));
        // removing and shorterning filter term
        assertTrue(list.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"x"}));
        // shortening filter term by multiple characters
        assertTrue(list.isFilterRelaxed(new String[] {"xyz"}, new String[] {"x"}));
        // shortening filter term
        assertTrue(list.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xy"}));

        assertFalse(list.isFilterRelaxed(new String[0], new String[] {"abc"}));
        assertFalse(list.isFilterRelaxed(new String[] {""}, new String[] {"abc"}));
        assertFalse(list.isFilterRelaxed(new String[] {"xyz"}, new String[] {"abc"}));
        assertFalse(list.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xyz", "abc"}));
        assertFalse(list.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xyz", "xy", "x"}));
        assertFalse(list.isFilterRelaxed(new String[] {"xyz", ""}, new String[] {"xyz"}));
        assertFalse(list.isFilterRelaxed(new String[] {"xyz", ""}, new String[] {"xyz", "xyz"}));
    }

    public void testIsFilterEqual() {
        DefaultTextFilterList list = new DefaultTextFilterList(new BasicEventList());

        assertTrue(list.isFilterEqual(new String[0], new String[0]));
        assertTrue(list.isFilterEqual(new String[] {"x"}, new String[] {"x"}));
        assertTrue(list.isFilterEqual(new String[] {"x", "y"}, new String[] {"x", "y"}));
    }

    public void testIsFilterConstrained() {
        DefaultTextFilterList list = new DefaultTextFilterList(new BasicEventList());

        // adding the first filter term
        assertTrue(list.isFilterConstrained(new String[0], new String[] {"x"}));
        // lengthening filter term
        assertTrue(list.isFilterConstrained(new String[] {"x"}, new String[] {"xx"}));
        // adding filter term
        assertTrue(list.isFilterConstrained(new String[] {"x"}, new String[] {"x", "y"}));
        // lengthening filter term by multiple characters
        assertTrue(list.isFilterConstrained(new String[] {"x"}, new String[] {"xyz"}));
        // lengthening multi character filter term
        assertTrue(list.isFilterConstrained(new String[] {"xy"}, new String[] {"xyz"}));
        // removing search terms but covering the old with a single new
        assertTrue(list.isFilterConstrained(new String[] {"xyz", "xy", "x"}, new String[] {"xyzz"}));

        assertFalse(list.isFilterConstrained(new String[] {"abc"}, new String[0]));
        assertFalse(list.isFilterConstrained(new String[] {"abc"}, new String[] {""}));
        assertFalse(list.isFilterConstrained(new String[] {"xyz"}, new String[] {"abc"}));
        assertFalse(list.isFilterConstrained(new String[] {"xyz", "abc"}, new String[] {"xyz"}));
        assertFalse(list.isFilterConstrained(new String[] {"xyz"}, new String[] {"xyz", ""}));
        assertFalse(list.isFilterConstrained(new String[] {"xyz", "xyz"}, new String[] {"xyz", ""}));
    }

    public void testConstrainingFilter() {
        DefaultTextFilterList list = new DefaultTextFilterList(new BasicEventList(), new StringTextFilterator());
        list.addAll(numbers);
        assertEquals(list, numbers);

        list.setFilterText(new String[] {"0"});
        assertEquals(Arrays.asList(new Object[] {"0"}), list);
    }

    public void testRelaxingFilter() {
        DefaultTextFilterList list = new DefaultTextFilterList(new BasicEventList(), new StringTextFilterator());
        list.addAll(numbers);

        list.setFilterText(new String[] {"0"});
        assertEquals(Arrays.asList(new Object[] {"0"}), list);
        list.setFilterText(new String[0]);
        assertEquals(numbers, list);

        list.setFilterText(new String[] {"01"});
        assertEquals(Collections.EMPTY_LIST, list);
        list.setFilterText(new String[] {"0"});
        assertEquals(Arrays.asList(new Object[] {"0"}), list);

        list.setFilterText(new String[] {"0", "1"});
        assertEquals(Collections.EMPTY_LIST, list);
        list.setFilterText(new String[] {"0"});
        assertEquals(Arrays.asList(new Object[] {"0"}), list);
    }

    public void testRelaxAndConstrainFilter() {
        DefaultTextFilterList list = new DefaultTextFilterList(new BasicEventList(), new StringTextFilterator());
        list.addAll(monotonicAlphabet);

        // constrain the monotonicAlphabet one char at a time in a single search term
        String filterText = "";
        for (int i = 0; i < monotonicAlphabet.size(); i++) {
            filterText += i;
            list.setFilterText(new String[] {filterText});
            assertEquals(monotonicAlphabet.size()-i, list.size());
        }

        // relax the monotonicAlphabet one char at a time in a single search term
        for (int i = monotonicAlphabet.size(); i > 0 ; i--) {
            assertEquals(monotonicAlphabet.size()-i+1, list.size());
            filterText = filterText.substring(0, filterText.length()-1);
            list.setFilterText(new String[] {filterText});
        }

        // relax the monotonicAlphabet one char at a time in multiple search terms
        String[] filterTexts;
        for (int i = 0; i < monotonicAlphabet.size(); i++) {
            filterTexts = new String[i+1];
            for (int j = 0; j < filterTexts.length; j++)
                filterTexts[j] = String.valueOf(j);

            list.setFilterText(filterTexts);
            assertEquals(monotonicAlphabet.size()-i, list.size());
        }

        // relax the monotonicAlphabet one char at a time in multiple search terms
        for (int i = monotonicAlphabet.size(); i > 0 ; i--) {
            filterTexts = new String[i];
            for (int j = 0; j < filterTexts.length; j++)
                filterTexts[j] = String.valueOf(j);

            list.setFilterText(filterTexts);
            assertEquals(monotonicAlphabet.size()-i+1, list.size());
        }
    }

    public void testClearFilter() {
        DefaultTextFilterList list = new DefaultTextFilterList(new BasicEventList(), new StringTextFilterator());
        list.addAll(numbers);

        assertEquals(list, numbers);
        list.setFilterText(new String[] {"6"});
        assertEquals(Arrays.asList(new String[] {"6"}), list);
        list.setFilterText(new String[0]);
        assertEquals(list, numbers);
    }

    private class TrivialDefaultTextFilterList extends DefaultTextFilterList {
        public TrivialDefaultTextFilterList(EventList source) {
            super(source);
        }

        public TrivialDefaultTextFilterList(EventList source, TextFilterator filterator) {
            super(source, filterator);
        }

        protected String[] normalizeFilter(String[] strings) {
            return super.normalizeFilter(strings);
        }
    }

    private class StringTextFilterator implements TextFilterator {
        public void getFilterStrings(List baseList, Object element) {
            baseList.add(element);
        }
    }
}