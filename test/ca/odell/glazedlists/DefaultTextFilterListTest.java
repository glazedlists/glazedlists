/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import junit.framework.TestCase;
// standard collections
import java.util.*;

public class DefaultTextFilterListTest extends TestCase {

    private List numbers = Arrays.asList(new Object[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});

    private List monotonicAlphabet = Arrays.asList(new Object[] {"0", "01", "012", "0123", "01234", "012345", "0123456", "01234567", "012345678", "0123456789"});

    public void testNormalizeValue() {
        DefaultTextFilterList list = new DefaultTextFilterList(new BasicEventList());

        assertTrue(Arrays.equals(new String[0], list.normalizeFilter(new String[0])));
        assertTrue(Arrays.equals(new String[0], list.normalizeFilter(new String[] {null, ""})));
        assertTrue(Arrays.equals(new String[] {"X"}, list.normalizeFilter(new String[] {"x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"X", "Y", "Z"}, list.normalizeFilter(new String[] {null, "", "x", null, "", "Y", null, "", "z", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, list.normalizeFilter(new String[] {null, "", "x", null, "", "xy", null, "", "xyz", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, list.normalizeFilter(new String[] {null, "", "xyz", null, "", "xy", null, "", "x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, list.normalizeFilter(new String[] {null, "", "xy", null, "", "xyz", null, "", "x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"XYZ"}, list.normalizeFilter(new String[] {null, "", "xyz", null, "", "xyz", null, "", "xyz", null, ""})));
        assertTrue(Arrays.equals(new String[] {"BLACKENED"}, list.normalizeFilter(new String[] {"black", "blackened"})));
        assertTrue(Arrays.equals(new String[] {"THIS"}, list.normalizeFilter(new String[] {"this", "his"})));
    }

    public void testIsFilterRelaxed() {
        // removing last filter term
        assertTrue(DefaultTextFilterList.isFilterRelaxed(new String[] {"x"}, new String[0]));
        // shortening filter term
        assertTrue(DefaultTextFilterList.isFilterRelaxed(new String[] {"xx"}, new String[] {"x"}));
        // removing filter term
        assertTrue(DefaultTextFilterList.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"xx"}));
        // removing filter term
        assertTrue(DefaultTextFilterList.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"y"}));
        // removing and shorterning filter term
        assertTrue(DefaultTextFilterList.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"x"}));
        // shortening filter term by multiple characters
        assertTrue(DefaultTextFilterList.isFilterRelaxed(new String[] {"xyz"}, new String[] {"x"}));
        // shortening filter term
        assertTrue(DefaultTextFilterList.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xy"}));

        assertFalse(DefaultTextFilterList.isFilterRelaxed(new String[0], new String[] {"abc"}));
        assertFalse(DefaultTextFilterList.isFilterRelaxed(new String[] {""}, new String[] {"abc"}));
        assertFalse(DefaultTextFilterList.isFilterRelaxed(new String[] {"xyz"}, new String[] {"abc"}));
        assertFalse(DefaultTextFilterList.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xyz", "abc"}));
        assertFalse(DefaultTextFilterList.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xyz", "xy", "x"}));
        assertFalse(DefaultTextFilterList.isFilterRelaxed(new String[] {"xyz", ""}, new String[] {"xyz"}));
        assertFalse(DefaultTextFilterList.isFilterRelaxed(new String[] {"xyz", ""}, new String[] {"xyz", "xyz"}));
    }

    public void testIsFilterEqual() {
        assertTrue(DefaultTextFilterList.isFilterEqual(new String[0], new String[0]));
        assertTrue(DefaultTextFilterList.isFilterEqual(new String[] {"x"}, new String[] {"x"}));
        assertTrue(DefaultTextFilterList.isFilterEqual(new String[] {"x", "y"}, new String[] {"x", "y"}));
    }

    public void testIsFilterConstrained() {
        // adding the first filter term
        assertTrue(DefaultTextFilterList.isFilterConstrained(new String[0], new String[] {"x"}));
        // lengthening filter term
        assertTrue(DefaultTextFilterList.isFilterConstrained(new String[] {"x"}, new String[] {"xx"}));
        // adding filter term
        assertTrue(DefaultTextFilterList.isFilterConstrained(new String[] {"x"}, new String[] {"x", "y"}));
        // lengthening filter term by multiple characters
        assertTrue(DefaultTextFilterList.isFilterConstrained(new String[] {"x"}, new String[] {"xyz"}));
        // lengthening multi character filter term
        assertTrue(DefaultTextFilterList.isFilterConstrained(new String[] {"xy"}, new String[] {"xyz"}));
        // removing search terms but covering the old with a single new
        assertTrue(DefaultTextFilterList.isFilterConstrained(new String[] {"xyz", "xy", "x"}, new String[] {"xyzz"}));

        assertFalse(DefaultTextFilterList.isFilterConstrained(new String[] {"abc"}, new String[0]));
        assertFalse(DefaultTextFilterList.isFilterConstrained(new String[] {"abc"}, new String[] {""}));
        assertFalse(DefaultTextFilterList.isFilterConstrained(new String[] {"xyz"}, new String[] {"abc"}));
        assertFalse(DefaultTextFilterList.isFilterConstrained(new String[] {"xyz", "abc"}, new String[] {"xyz"}));
        assertFalse(DefaultTextFilterList.isFilterConstrained(new String[] {"xyz"}, new String[] {"xyz", ""}));
        assertFalse(DefaultTextFilterList.isFilterConstrained(new String[] {"xyz", "xyz"}, new String[] {"xyz", ""}));
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


    /**
     * Test to verify that the filter is working correctly when values
     * are being added to a list.
     */
    public void testFilterBeforeAndAfter() {
        // set up
        Random random = new Random();
        BasicEventList unfilteredList = new BasicEventList();
        DefaultTextFilterList filteredList = new DefaultTextFilterList(unfilteredList, new StringTextFilterator());
        
        // apply a filter
        String filter = "7";
        filteredList.setFilterText(new String[] { filter });

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
        filteredList.setFilterText(new String[] { });
        assertEquals(filteredList, unfilteredList);

        // apply the filter again and verify the lists are equal
        filteredList.setFilterText(new String[] { filter });
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
        DefaultTextFilterList filteredList = new DefaultTextFilterList(unfilteredList, new StringTextFilterator());
        
        // apply a filter
        String filter = "5";
        filteredList.setFilterText(new String[] { filter });

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
        DefaultTextFilterList filteredList = new DefaultTextFilterList(unfilteredList, new StringTextFilterator());
        
        // apply a filter
        String filter = "5";
        filteredList.setFilterText(new String[] { filter });

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