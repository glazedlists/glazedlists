/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.filter.StringTextFilterator;
import ca.odell.glazedlists.impl.filter.TextMatchers;
import junit.framework.TestCase;

import java.util.*;

public class TextMatcherTest extends TestCase {

    private List<Object> numbers = Arrays.asList(new Object[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});

    private List<Object> monotonicAlphabet = Arrays.asList(new Object[] {"0", "01", "012", "0123", "01234", "012345", "0123456", "01234567", "012345678", "0123456789"});

    private List<Object> dictionary = Arrays.asList(new Object[] {"act", "actor", "enact", "reactor"});

    public void testNormalizeValue() {
        assertTrue(Arrays.equals(new String[0], TextMatchers.normalizeFilters(new String[0])));
        assertTrue(Arrays.equals(new String[0], TextMatchers.normalizeFilters(new String[] {null, ""})));
        assertTrue(Arrays.equals(new String[] {"x"}, TextMatchers.normalizeFilters(new String[] {"x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"x", "Y", "z"}, TextMatchers.normalizeFilters(new String[] {null, "", "x", null, "", "Y", null, "", "z", null, ""})));
        assertTrue(Arrays.equals(new String[] {"xyz"}, TextMatchers.normalizeFilters(new String[] {null, "", "x", null, "", "xy", null, "", "xyz", null, ""})));
        assertTrue(Arrays.equals(new String[] {"xyz"}, TextMatchers.normalizeFilters(new String[] {null, "", "xyz", null, "", "xy", null, "", "x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"xyz"}, TextMatchers.normalizeFilters(new String[] {null, "", "xy", null, "", "xyz", null, "", "x", null, ""})));
        assertTrue(Arrays.equals(new String[] {"xyz"}, TextMatchers.normalizeFilters(new String[] {null, "", "xyz", null, "", "xyz", null, "", "xyz", null, ""})));
        assertTrue(Arrays.equals(new String[] {"blackened"}, TextMatchers.normalizeFilters(new String[] {"black", "blackened"})));
        assertTrue(Arrays.equals(new String[] {"this"}, TextMatchers.normalizeFilters(new String[] {"this", "his"})));

        assertTrue(Arrays.equals(new String[] {"blackened", "this"}, TextMatchers.normalizeFilters(new String[] {"blackened", "this"})));
        assertTrue(Arrays.equals(new String[] {"blackened", "this"}, TextMatchers.normalizeFilters(new String[] {"this", "blackened"})));
    }

    public void testIsFilterRelaxed() {
        // removing last filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"x"}, new String[0]));
        // shortening filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xx"}, new String[] {"x"}));
        // removing filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"xx"}));
        // removing filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"y"}));
        // removing and shorterning filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xx", "y"}, new String[] {"x"}));
        // shortening filter term by multiple characters
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xyz"}, new String[] {"x"}));
        // shortening filter term
        assertTrue(TextMatchers.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xy"}));

        assertFalse(TextMatchers.isFilterRelaxed(new String[0], new String[] {"abc"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {""}, new String[] {"abc"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {"xyz"}, new String[] {"abc"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xyz", "abc"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {"xyz"}, new String[] {"xyz", "xy", "x"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {"xyz", ""}, new String[] {"xyz"}));
        assertFalse(TextMatchers.isFilterRelaxed(new String[] {"xyz", ""}, new String[] {"xyz", "xyz"}));
    }

    public void testIsFilterEqual() {
        assertTrue(TextMatchers.isFilterEqual(new String[0], new String[0]));
        assertTrue(TextMatchers.isFilterEqual(new String[] {"x"}, new String[] {"x"}));
        assertTrue(TextMatchers.isFilterEqual(new String[] {"x", "y"}, new String[] {"x", "y"}));
    }

    public void testIsFilterConstrained() {
        // adding the first filter term
        assertTrue(TextMatchers.isFilterConstrained(new String[0], new String[] {"x"}));
        // lengthening filter term
        assertTrue(TextMatchers.isFilterConstrained(new String[] {"x"}, new String[] {"xx"}));
        // adding filter term
        assertTrue(TextMatchers.isFilterConstrained(new String[] {"x"}, new String[] {"x", "y"}));
        // lengthening filter term by multiple characters
        assertTrue(TextMatchers.isFilterConstrained(new String[] {"x"}, new String[] {"xyz"}));
        // lengthening multi character filter term
        assertTrue(TextMatchers.isFilterConstrained(new String[] {"xy"}, new String[] {"xyz"}));
        // removing search terms but covering the old with a single new
        assertTrue(TextMatchers.isFilterConstrained(new String[] {"xyz", "xy", "x"}, new String[] {"xyzz"}));

        assertFalse(TextMatchers.isFilterConstrained(new String[] {"abc"}, new String[0]));
        assertFalse(TextMatchers.isFilterConstrained(new String[] {"abc"}, new String[] {""}));
        assertFalse(TextMatchers.isFilterConstrained(new String[] {"xyz"}, new String[] {"abc"}));
        assertFalse(TextMatchers.isFilterConstrained(new String[] {"xyz", "abc"}, new String[] {"xyz"}));
        assertFalse(TextMatchers.isFilterConstrained(new String[] {"xyz"}, new String[] {"xyz", ""}));
        assertFalse(TextMatchers.isFilterConstrained(new String[] {"xyz", "xyz"}, new String[] {"xyz", ""}));
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

    public void testChangeStrategyNotifications() {
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> list = new FilterList<Object>(new BasicEventList<Object>(), textMatcherEditor);
        list.addAll(monotonicAlphabet);

        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        textMatcherEditor.addMatcherEditorListener(counter);

        assertEquals(TextMatcherEditor.IDENTICAL_STRATEGY, textMatcherEditor.getStrategy());

        // changing the strategy produces no changes if there is no filter text
        textMatcherEditor.setStrategy(TextMatcherEditor.NORMALIZED_STRATEGY);
        assertEquals(TextMatcherEditor.NORMALIZED_STRATEGY, textMatcherEditor.getStrategy());
		counter.assertCounterState(0, 0, 0, 0, 0);

        textMatcherEditor.setStrategy(TextMatcherEditor.IDENTICAL_STRATEGY);
        assertEquals(TextMatcherEditor.IDENTICAL_STRATEGY, textMatcherEditor.getStrategy());
		counter.assertCounterState(0, 0, 0, 0, 0);

        // set some filter text
        textMatcherEditor.setFilterText(new String[] {"012"});
        counter.assertCounterState(0, 0, 0, 1, 0);
        counter.resetCounterState();

        // changing the strategy with filter text present should cause a change
        textMatcherEditor.setStrategy(TextMatcherEditor.NORMALIZED_STRATEGY);
        assertEquals(TextMatcherEditor.NORMALIZED_STRATEGY, textMatcherEditor.getStrategy());
		counter.assertCounterState(0, 0, 1, 0, 0);

        textMatcherEditor.setStrategy(TextMatcherEditor.IDENTICAL_STRATEGY);
        assertEquals(TextMatcherEditor.IDENTICAL_STRATEGY, textMatcherEditor.getStrategy());
		counter.assertCounterState(0, 0, 2, 0, 0);
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

    public void testNormalizedLatinStrategy() {
        TextMatcherEditor<Object> textMatcherEditor = new TextMatcherEditor<Object>(new StringTextFilterator());
        FilterList<Object> list = new FilterList<Object>(new BasicEventList<Object>(), textMatcherEditor);

        list.add("r\u00e9sum\u00e9");
        list.add("Bj\u00f6rk");
        list.add("M\u00fcller");

        textMatcherEditor.setFilterText(new String[] {"\u00f6"});
        assertEquals(1, list.size());
        assertEquals("Bj\u00f6rk", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"o"});
        assertTrue(list.isEmpty());

        textMatcherEditor.setStrategy(TextMatcherEditor.NORMALIZED_STRATEGY);
        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"\u00f6"});
        assertEquals(1, list.size());
        assertEquals("Bj\u00f6rk", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"o"});
        assertEquals(1, list.size());
        assertEquals("Bj\u00f6rk", list.get(0));

        textMatcherEditor.setStrategy(TextMatcherEditor.IDENTICAL_STRATEGY);
        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"M\u00dcLL"});
        assertEquals(1, list.size());
        assertEquals("M\u00fcller", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"MULL"});
        assertTrue(list.isEmpty());

        textMatcherEditor.setStrategy(TextMatcherEditor.NORMALIZED_STRATEGY);
        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"M\u00dcLL"});
        assertEquals(1, list.size());
        assertEquals("M\u00fcller", list.get(0));

        textMatcherEditor.setFilterText(new String[0]);
        textMatcherEditor.setFilterText(new String[] {"MULL"});
        assertEquals(1, list.size());
        assertEquals("M\u00fcller", list.get(0));


        // the uber test ensures that diacritics and case are unimportant when comparing any of these characters
        String uberString = "\u00c0\u00c1\u00c2\u00c3\u00c4\u00c5\u00c7\u00c8\u00c9\u00ca\u00cb\u00cc\u00cd\u00ce\u00cf\u00d1\u00d2\u00d3\u00d4\u00d5\u00d6\u00d9\u00da\u00db\u00dc\u00dd\u00e0\u00e1\u00e2\u00e3\u00e4\u00e5\u00e7\u00e8\u00e9\u00ea\u00eb\u00ec\u00ed\u00ee\u00ef\u00f1\u00f2\u00f3\u00f4\u00f5\u00f6\u00f9\u00fa\u00fb\u00fc\u00fd\u00ff";
        list.add(uberString);
        textMatcherEditor.setFilterText(new String[] {"aaaaaaceeeeiiiinooooouuuuyAAAAAACEEEEIIIINOOOOOUUUUYY"});
        assertEquals(1, list.size());
        assertEquals(uberString, list.get(0));
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