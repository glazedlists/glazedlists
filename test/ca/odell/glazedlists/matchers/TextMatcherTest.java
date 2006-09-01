/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.filter.StringTextFilterator;
import ca.odell.glazedlists.impl.filter.TextMatcher;
import junit.framework.TestCase;

import java.util.*;

public class TextMatcherTest extends TestCase {

    private List<Object> numbers = Arrays.asList(new Object[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});

    private List<Object> monotonicAlphabet = Arrays.asList(new Object[] {"0", "01", "012", "0123", "01234", "012345", "0123456", "01234567", "012345678", "0123456789"});

    private List<Object> dictionary = Arrays.asList(new Object[] {"act", "actor", "enact", "reactor"});

    public void testNormalizeValue() {
        assertTrue(Arrays.equals(new String[0], TextMatcher.normalizeFilters(new String[0])));
        assertTrue(Arrays.equals(new String[0], TextMatcher.normalizeFilters(new String[] {null, ""})));
        assertTrue(Arrays.equals(new String[] {"x"}, TextMatcher.normalizeFilters(new String[] {"x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"x", "Y", "z"}, TextMatcher.normalizeFilters(new String[] {null, "", "x", null, "", "Y", null, "", "z", null, ""})));
        assertTrue(Arrays.equals(new String[] {"xyz"}, TextMatcher.normalizeFilters(new String[] {null, "", "x", null, "", "xy", null, "", "xyz", null, ""})));
        assertTrue(Arrays.equals(new String[] {"xyz"}, TextMatcher.normalizeFilters(new String[] {null, "", "xyz", null, "", "xy", null, "", "x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"xyz"}, TextMatcher.normalizeFilters(new String[] {null, "", "xy", null, "", "xyz", null, "", "x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"xyz"}, TextMatcher.normalizeFilters(new String[] {null, "", "xyz", null, "", "xyz", null, "", "xyz", null, ""})));
        assertTrue(Arrays.equals(new String[] {"blackened"}, TextMatcher.normalizeFilters(new String[] {"black", "blackened"})));
        assertTrue(Arrays.equals(new String[] {"this"}, TextMatcher.normalizeFilters(new String[] {"this", "his"})));

        assertTrue(Arrays.equals(new String[] {"blackened", "this"}, TextMatcher.normalizeFilters(new String[] {"blackened", "this"})));
        assertTrue(Arrays.equals(new String[] {"blackened", "this"}, TextMatcher.normalizeFilters(new String[] {"this", "blackened"})));
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
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> list = new FilterList<Object>(new BasicEventList<Object>(), textMatcherEditor);
        
        list.addAll(numbers);
        assertEquals(list, numbers);

        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        textMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"0"});		// constrained
        assertEquals(Arrays.asList(new Object[] {"0"}), list);
		counter.assertCounterState(0, 0, 0, 1, 0);
    }


//	public void testLogicInversion() {
//        TextMatcherEditor textMatcherEditor = new TextMatcherEditor(new StringTextFilterator());
//		textMatcherEditor.setLogicInverted(true);
//		FilterList list = new FilterList(new BasicEventList(), textMatcherEditor);
//
//        list.addAll(monotonicAlphabet);
//
//        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
//        textMatcherEditor.addMatcherEditorListener(counter);
//
//		// Make sure it includes everything to start with (no filter)
//		assertEquals(monotonicAlphabet.size(), list.size());
//
//		textMatcherEditor.setFilterText(new String[] {"9"});		// constrained
//		assertEquals(monotonicAlphabet.size() - 1, list.size());
//		counter.assertCounterState(0, 0, 0, 1, 0);
//		counter.resetCounterState();
//
//		textMatcherEditor.setFilterText(new String[] {"1"});		// changed
//		assertEquals(1, list.size());
//		counter.assertCounterState(0, 0, 1, 0, 0);
//		counter.resetCounterState();
//
//		// Go back to normal logic. Should now match all but 1
//		textMatcherEditor.setLogicInverted(false);
//		assertEquals(monotonicAlphabet.size() - 1, list.size());
//		counter.assertCounterState(0, 0, 1, 0, 0);
//		counter.resetCounterState();
//
//		// Return to inverted
//		textMatcherEditor.setLogicInverted(true);
//		counter.assertCounterState(0, 0, 1, 0, 0);
//		counter.resetCounterState();
//
//		// Clear it
//		textMatcherEditor.setFilterText(null);
//		assertEquals(monotonicAlphabet.size(), list.size());
//		counter.assertCounterState(1, 0, 0, 0, 0);
//	}

	public void testRelaxingFilter() {
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> list = new FilterList<Object>(new BasicEventList<Object>(), textMatcherEditor);

        list.addAll(numbers);

        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        textMatcherEditor.addMatcherEditorListener(counter);

		textMatcherEditor.setFilterText(new String[] {"0"});		// constrained
        assertEquals(Arrays.asList(new Object[] {"0"}), list);
        textMatcherEditor.setFilterText(new String[0]);				// match all
        assertEquals(numbers, list);
		counter.assertCounterState(1, 0, 0, 1, 0);
		counter.resetCounterState();

		textMatcherEditor.setFilterText(new String[] {"01"});		// constrained
        assertEquals(Collections.EMPTY_LIST, list);
        textMatcherEditor.setFilterText(new String[] {"0"});		// relaxed
        assertEquals(Arrays.asList(new Object[] {"0"}), list);
		counter.assertCounterState(0, 0, 0, 1, 1);
		counter.resetCounterState();

        textMatcherEditor.setFilterText(new String[] {"0", "1"});	// constrained
        assertEquals(Collections.EMPTY_LIST, list);
        textMatcherEditor.setFilterText(new String[] {"0"});
        assertEquals(Arrays.asList(new Object[] {"0"}), list);		// relaxed
		counter.assertCounterState(0, 0, 0, 1, 1);
    }

    public void testRelaxAndConstrainFilter() {
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> list = new FilterList<Object>(new BasicEventList<Object>(), textMatcherEditor);

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
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> list = new FilterList<Object>(new BasicEventList<Object>(), textMatcherEditor);
        
        list.addAll(numbers);

        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        textMatcherEditor.addMatcherEditorListener(counter);

        assertEquals(list, numbers);
        textMatcherEditor.setFilterText(new String[] {"6"});		// constrained
		counter.assertCounterState(0, 0, 0, 1, 0);
		counter.resetCounterState();
		assertEquals(Arrays.asList(new String[] {"6"}), list);

		textMatcherEditor.setFilterText(new String[0]);				// match all
		counter.assertCounterState(1, 0, 0, 0, 0);
		assertEquals(list, numbers);
    }

    public void testMatchNonStrings() {
        final TextMatcherEditor<Integer> textMatcherEditor = new TextMatcherEditor<Integer>(new IntegerTextFilterator());
        FilterList<Integer> list = new FilterList<Integer>(new BasicEventList<Integer>(), textMatcherEditor);

        list.add(new Integer(10));
        list.add(new Integer(3));
        list.add(new Integer(11));
        list.add(new Integer(2));
        list.add(new Integer(12));
        list.add(new Integer(9));
        list.add(new Integer(103));
        list.add(new Integer(7));

        textMatcherEditor.setFilterText(new String[] {"1"});
        assertEquals(4, list.size());
        assertEquals(list.get(0), new Integer(10));
        assertEquals(list.get(1), new Integer(11));
        assertEquals(list.get(2), new Integer(12));
        assertEquals(list.get(3), new Integer(103));
    }

    public void testChangeMode() {
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> list = new FilterList<Object>(new BasicEventList<Object>(), textMatcherEditor);
        list.addAll(monotonicAlphabet);

        textMatcherEditor.setFilterText(new String[] {"789"});
        assertEquals(Arrays.asList(new String[] {"0123456789"}), list);

        textMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        assertEquals(Collections.EMPTY_LIST, list);

        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        assertEquals(Arrays.asList(new String[] {"0123456789"}), list);

        list.clear();
        list.addAll(dictionary);
        assertEquals(Collections.EMPTY_LIST, list);

        textMatcherEditor.setFilterText(new String[] {"act"});
        assertEquals(dictionary, list);

        textMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        assertEquals(Arrays.asList(new String[] {"act", "actor"}), list);

        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        assertEquals(dictionary, list);
    }

    public void testChangeModeNotifications() {
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> list = new FilterList<Object>(new BasicEventList<Object>(), textMatcherEditor);
        list.addAll(monotonicAlphabet);

        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        textMatcherEditor.addMatcherEditorListener(counter);

        assertEquals(TextMatcherEditor.CONTAINS, textMatcherEditor.getMode());

        // changing the mode produces no changes if there is no filter text
        textMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        assertEquals(TextMatcherEditor.STARTS_WITH, textMatcherEditor.getMode());
		counter.assertCounterState(0, 0, 0, 0, 0);

        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        assertEquals(TextMatcherEditor.CONTAINS, textMatcherEditor.getMode());
		counter.assertCounterState(0, 0, 0, 0, 0);

        // set some filter text
        textMatcherEditor.setFilterText(new String[] {"012"});
        counter.assertCounterState(0, 0, 0, 1, 0);
        counter.resetCounterState();

        // changing the mode with filter text present should constrain or relax
        textMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        assertEquals(TextMatcherEditor.STARTS_WITH, textMatcherEditor.getMode());
		counter.assertCounterState(0, 0, 0, 1, 0);

        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        assertEquals(TextMatcherEditor.CONTAINS, textMatcherEditor.getMode());
		counter.assertCounterState(0, 0, 0, 1, 1);
    }

    /**
     * Test to verify that the filter is working correctly when values
     * are being added to a list.
     */
    public void testFilterBeforeAndAfter() {
        // set up
        EventList<Object> unfilteredList = new BasicEventList<Object>();
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> filteredList = new FilterList<Object>(unfilteredList, textMatcherEditor);
        
        // apply a filter
        String filter = "7";
        textMatcherEditor.setFilterText(new String[] { filter });

        // populate a list with strings
        for(int i = 1000; i < 2000; i++) {
            unfilteredList.add(String.valueOf(i));
        }

        // build a control list of the desired results
        List<String> controlList = new ArrayList<String>();
        for(Iterator i = unfilteredList.iterator(); i.hasNext(); ) {
            String element = (String) i.next();
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
        EventList<Object> unfilteredList = new BasicEventList<Object>();
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> filteredList = new FilterList<Object>(unfilteredList, textMatcherEditor);
        
        // apply a filter
        String filter = "5";
        textMatcherEditor.setFilterText(new String[] { filter });

        // apply various operations to a list of strings
        for(int i = 0; i < 4000; i++) {
            int operation = random.nextInt(4);
            int value = random.nextInt(10);
            int index = unfilteredList.isEmpty() ? 0 : random.nextInt(unfilteredList.size());

            if(operation <= 1 || unfilteredList.isEmpty()) {
                unfilteredList.add(index, String.valueOf(value));
            } else if(operation == 2) {
                unfilteredList.remove(index);
            } else if(operation == 3) {
                unfilteredList.set(index, String.valueOf(value));
            }
        }

        // build a control list of the desired results
        List<String> controlList = new ArrayList<String>();
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
        EventList<Object> unfilteredList = new BasicEventList<Object>();
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> filteredList = new FilterList<Object>(unfilteredList, textMatcherEditor);
        
        // apply a filter
        String filter = "5";
        textMatcherEditor.setFilterText(new String[] { filter });

        // apply various operations to a list of strings
        for(int i = 0; i < 4000; i++) {
            List<Object> list;
            if(random.nextBoolean()) list = filteredList;
            else list = unfilteredList;
            int operation = random.nextInt(4);
            int value = random.nextInt(10);
            int index = list.isEmpty() ? 0 : random.nextInt(list.size());

            if(operation <= 1 || list.isEmpty()) {
                list.add(index, String.valueOf(value));
            } else if(operation == 2) {
                list.remove(index);
            } else if(operation == 3) {
                list.set(index, String.valueOf(value));
            }
        }

        // build a control list of the desired results
        List<String> controlList = new ArrayList<String>();
        for(Iterator i = unfilteredList.iterator(); i.hasNext(); ) {
            String element = (String)i.next();
            if(element.indexOf(filter) != -1) controlList.add(element);
        }

        // verify the lists are equal
        assertEquals(controlList, filteredList);
    }

    /**
     * Intentionally add raw Integers into the list with this TextFilterator in
     * order to validate that TextFilterator is always backwards compatible with
     * old behaviour. (Namely that the .toString() value is used for filtering
     * all of the filterator's object)
     */
    private static class IntegerTextFilterator implements TextFilterator<Integer> {
        public void getFilterStrings(List<String> baseList, Integer element) {
            ((List) baseList).add(element);
        }
    }
}