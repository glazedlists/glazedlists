/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import junit.framework.TestCase;
import ca.odell.glazedlists.impl.filter.*;
// standard collections
import java.util.*;

public class DefaultTextFilterListTest extends TestCase {

    private List numbers = Arrays.asList(new Object[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});

    private List monotonicAlphabet = Arrays.asList(new Object[] {"0", "01", "012", "0123", "01234", "012345", "0123456", "01234567", "012345678", "0123456789"});

    public void testNormalizeValue() {
        FilterList list = new FilterList(new BasicEventList(), new TextMatcherEditor());

        assertTrue(Arrays.equals(new String[0], TextMatcher.normalizeFilters(new String[0])));
        assertTrue(Arrays.equals(new String[0], TextMatcher.normalizeFilters(new String[] {null, ""})));
        assertTrue(Arrays.equals(new String[] {"X"}, TextMatcher.normalizeFilters(new String[] {"x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"X", "Y", "Z"}, TextMatcher.normalizeFilters(new String[] {null, "", "x", null, "", "Y", null, "", "z", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, TextMatcher.normalizeFilters(new String[] {null, "", "x", null, "", "xy", null, "", "xyz", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, TextMatcher.normalizeFilters(new String[] {null, "", "xyz", null, "", "xy", null, "", "x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, TextMatcher.normalizeFilters(new String[] {null, "", "xy", null, "", "xyz", null, "", "x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, TextMatcher.normalizeFilters(new String[] {null, "", "xyz", null, "", "xyz", null, "", "xyz", null, ""})));
        assertTrue(Arrays.equals(new String[] {"BLACKENED"}, TextMatcher.normalizeFilters(new String[] {"black", "blackened"})));
        assertTrue(Arrays.equals(new String[] {"THIS"}, TextMatcher.normalizeFilters(new String[] {"this", "his"})));
    }

    public void testIsFilterRelaxed() {
        // removing last filter term
        assertTrue(TextMatcher.isFilterRelaxed(new String[] {"x"}, new String[0]));
        // shortening filter term
        assertTrue(TextMatcher.isFilterRelaxed(new String[] {"xx"}, new String[] {"x"}));
        // removing filter term
        assertTrue(TextMatcher.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"xx"}));
        // removing filter term
        assertTrue(TextMatcher.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"y"}));
        // removing and shorterning filter term
        assertTrue(TextMatcher.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"x"}));
        // shortening filter term by multiple characters
        assertTrue(TextMatcher.isFilterRelaxed(new String[] {"xyz"}, new String[] {"x"}));
        // shortening filter term
        assertTrue(TextMatcher.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xy"}));

        assertFalse(TextMatcher.isFilterRelaxed(new String[0], new String[] {"abc"}));
        assertFalse(TextMatcher.isFilterRelaxed(new String[] {""}, new String[] {"abc"}));
        assertFalse(TextMatcher.isFilterRelaxed(new String[] {"xyz"}, new String[] {"abc"}));
        assertFalse(TextMatcher.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xyz", "abc"}));
        assertFalse(TextMatcher.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xyz", "xy", "x"}));
        assertFalse(TextMatcher.isFilterRelaxed(new String[] {"xyz", ""}, new String[] {"xyz"}));
        assertFalse(TextMatcher.isFilterRelaxed(new String[] {"xyz", ""}, new String[] {"xyz", "xyz"}));
    }

    public void testIsFilterEqual() {
        assertTrue(TextMatcher.isFilterEqual(new String[0], new String[0]));
        assertTrue(TextMatcher.isFilterEqual(new String[] {"x"}, new String[] {"x"}));
        assertTrue(TextMatcher.isFilterEqual(new String[] {"x", "y"}, new String[] {"x", "y"}));
    }

    public void testIsFilterConstrained() {
        // adding the first filter term
        assertTrue(TextMatcher.isFilterConstrained(new String[0], new String[] {"x"}));
        // lengthening filter term
        assertTrue(TextMatcher.isFilterConstrained(new String[] {"x"}, new String[] {"xx"}));
        // adding filter term
        assertTrue(TextMatcher.isFilterConstrained(new String[] {"x"}, new String[] {"x", "y"}));
        // lengthening filter term by multiple characters
        assertTrue(TextMatcher.isFilterConstrained(new String[] {"x"}, new String[] {"xyz"}));
        // lengthening multi character filter term
        assertTrue(TextMatcher.isFilterConstrained(new String[] {"xy"}, new String[] {"xyz"}));
        // removing search terms but covering the old with a single new
        assertTrue(TextMatcher.isFilterConstrained(new String[] {"xyz", "xy", "x"}, new String[] {"xyzz"}));

        assertFalse(TextMatcher.isFilterConstrained(new String[] {"abc"}, new String[0]));
        assertFalse(TextMatcher.isFilterConstrained(new String[] {"abc"}, new String[] {""}));
        assertFalse(TextMatcher.isFilterConstrained(new String[] {"xyz"}, new String[] {"abc"}));
        assertFalse(TextMatcher.isFilterConstrained(new String[] {"xyz", "abc"}, new String[] {"xyz"}));
        assertFalse(TextMatcher.isFilterConstrained(new String[] {"xyz"}, new String[] {"xyz", ""}));
        assertFalse(TextMatcher.isFilterConstrained(new String[] {"xyz", "xyz"}, new String[] {"xyz", ""}));
    }

    public void testConstrainingFilter() {
        TextMatcherEditor textMatcherEditor = new TextMatcherEditor(new StringTextFilterator());
        FilterList list = new FilterList(new BasicEventList(), textMatcherEditor);
        
        list.addAll(numbers);
        assertEquals(list, numbers);

        textMatcherEditor.setFilterText(new String[] {"0"});
        assertEquals(Arrays.asList(new Object[] {"0"}), list);
    }

    public void testRelaxingFilter() {
        TextMatcherEditor textMatcherEditor = new TextMatcherEditor(new StringTextFilterator());
        FilterList list = new FilterList(new BasicEventList(), textMatcherEditor);

        list.addAll(numbers);

        textMatcherEditor.setFilterText(new String[] {"0"});
        assertEquals(Arrays.asList(new Object[] {"0"}), list);
        textMatcherEditor.setFilterText(new String[0]);
        assertEquals(numbers, list);

        textMatcherEditor.setFilterText(new String[] {"01"});
        assertEquals(Collections.EMPTY_LIST, list);
        textMatcherEditor.setFilterText(new String[] {"0"});
        assertEquals(Arrays.asList(new Object[] {"0"}), list);

        textMatcherEditor.setFilterText(new String[] {"0", "1"});
        assertEquals(Collections.EMPTY_LIST, list);
        textMatcherEditor.setFilterText(new String[] {"0"});
        assertEquals(Arrays.asList(new Object[] {"0"}), list);
    }

    public void testRelaxAndConstrainFilter() {
        TextMatcherEditor textMatcherEditor = new TextMatcherEditor(new StringTextFilterator());
        FilterList list = new FilterList(new BasicEventList(), textMatcherEditor);

        list.addAll(monotonicAlphabet);

        // constrain the monotonicAlphabet one char at a time in a single search term
        String filterText = "";
        for (int i = 0; i < monotonicAlphabet.size(); i++) {
            filterText += i;
            textMatcherEditor.setFilterText(new String[] {filterText});
            assertEquals(monotonicAlphabet.size()-i, list.size());
        }

        // relax the monotonicAlphabet one char at a time in a single search term
        for (int i = monotonicAlphabet.size(); i > 0 ; i--) {
            assertEquals(monotonicAlphabet.size()-i+1, list.size());
            filterText = filterText.substring(0, filterText.length()-1);
            textMatcherEditor.setFilterText(new String[] {filterText});
        }

        // relax the monotonicAlphabet one char at a time in multiple search terms
        String[] filterTexts;
        for (int i = 0; i < monotonicAlphabet.size(); i++) {
            filterTexts = new String[i+1];
            for (int j = 0; j < filterTexts.length; j++)
                filterTexts[j] = String.valueOf(j);

            textMatcherEditor.setFilterText(filterTexts);
            assertEquals(monotonicAlphabet.size()-i, list.size());
        }

        // relax the monotonicAlphabet one char at a time in multiple search terms
        for (int i = monotonicAlphabet.size(); i > 0 ; i--) {
            filterTexts = new String[i];
            for (int j = 0; j < filterTexts.length; j++)
                filterTexts[j] = String.valueOf(j);

            textMatcherEditor.setFilterText(filterTexts);
            assertEquals(monotonicAlphabet.size()-i+1, list.size());
        }
    }

    public void testClearFilter() {
        TextMatcherEditor textMatcherEditor = new TextMatcherEditor(new StringTextFilterator());
        FilterList list = new FilterList(new BasicEventList(), textMatcherEditor);
        
        list.addAll(numbers);

        assertEquals(list, numbers);
        textMatcherEditor.setFilterText(new String[] {"6"});
        assertEquals(Arrays.asList(new String[] {"6"}), list);
        textMatcherEditor.setFilterText(new String[0]);
        assertEquals(list, numbers);
    }


    /**
     * Test to verify that the filter is working correctly when values
     * are being added to a list.
     */
    public void testFilterBeforeAndAfter() {
        // set up
        Random random = new Random();
        BasicEventList unfilteredList = new BasicEventList();
        TextMatcherEditor textMatcherEditor = new TextMatcherEditor(new StringTextFilterator());
        FilterList filteredList = new FilterList(unfilteredList, textMatcherEditor);
        
        // apply a filter
        String filter = "7";
        textMatcherEditor.setFilterText(new String[] { filter });

        // populate a list with strings
        for(int i = 1000; i < 2000; i++) {
            unfilteredList.add("" + i);
        }

        // build a control list of the desired results
        ArrayList controlList = new ArrayList();
        for(Iterator i = unfilteredList.iterator(); i.hasNext(); ) {
            String element = (String)i.next();
            if(element.indexOf(filter) != -1) controlList.add(element);
        }

        // verify the lists are equal
        assertEquals(controlList, filteredList);

        // destroy the filter
        textMatcherEditor.setFilterText(new String[] { });
        assertEquals(filteredList, unfilteredList);

        // apply the filter again and verify the lists are equal
        textMatcherEditor.setFilterText(new String[] { filter });
        assertEquals(controlList, filteredList);
    }

    /**
     * Test to verify that the filter is working correctly when the list
     * is changing by adds, removes and deletes.
     */
    public void testFilterDynamic() {
        // set up
        Random random = new Random();
        BasicEventList unfilteredList = new BasicEventList();
        TextMatcherEditor textMatcherEditor = new TextMatcherEditor(new StringTextFilterator());
        FilterList filteredList = new FilterList(unfilteredList, textMatcherEditor);
        
        // apply a filter
        String filter = "5";
        textMatcherEditor.setFilterText(new String[] { filter });

        // apply various operations to a list of strings
        for(int i = 0; i < 4000; i++) {
            int operation = random.nextInt(4);
            int value = random.nextInt(10);
            int index = unfilteredList.isEmpty() ? 0 : random.nextInt(unfilteredList.size());

            if(operation <= 1 || unfilteredList.isEmpty()) {
                unfilteredList.add(index, "" + value);
            } else if(operation == 2) {
                unfilteredList.remove(index);
            } else if(operation == 3) {
                unfilteredList.set(index, "" + value);
            }
        }

        // build a control list of the desired results
        ArrayList controlList = new ArrayList();
        for(Iterator i = unfilteredList.iterator(); i.hasNext(); ) {
            String element = (String)i.next();
            if(element.indexOf(filter) != -1) controlList.add(element);
        }

        // verify the lists are equal
        assertEquals(controlList, filteredList);
    }

    /**
     * Test to verify that the filter correctly handles modification.
     *
     * This performs a sequence of operations. Each operation is performed on
     * either the filtered list or the unfiltered list. The list where the
     * operation is performed is selected at random.
     */
    public void testFilterWritable() {
        // set up
        Random random = new Random();
        BasicEventList unfilteredList = new BasicEventList();
        TextMatcherEditor textMatcherEditor = new TextMatcherEditor(new StringTextFilterator());
        FilterList filteredList = new FilterList(unfilteredList, textMatcherEditor);
        
        // apply a filter
        String filter = "5";
        textMatcherEditor.setFilterText(new String[] { filter });

        // apply various operations to a list of strings
        for(int i = 0; i < 4000; i++) {
            List list;
            if(random.nextBoolean()) list = filteredList;
            else list = unfilteredList;
            int operation = random.nextInt(4);
            int value = random.nextInt(10);
            int index = list.isEmpty() ? 0 : random.nextInt(list.size());

            if(operation <= 1 || list.isEmpty()) {
                list.add(index, "" + value);
            } else if(operation == 2) {
                list.remove(index);
            } else if(operation == 3) {
                list.set(index, "" + value);
            }
        }

        // build a control list of the desired results
        ArrayList controlList = new ArrayList();
        for(Iterator i = unfilteredList.iterator(); i.hasNext(); ) {
            String element = (String)i.next();
            if(element.indexOf(filter) != -1) controlList.add(element);
        }

        // verify the lists are equal
        assertEquals(controlList, filteredList);
    }
    
    /**
     * A filterator for strings.
     */
    private class StringTextFilterator implements TextFilterator {
        public void getFilterStrings(List baseList, Object element) {
            baseList.add(element);
        }
    }
}