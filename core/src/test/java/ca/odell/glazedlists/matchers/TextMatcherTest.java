/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.filter.TextMatcher;
import ca.odell.glazedlists.impl.filter.TextMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

public class TextMatcherTest {

    private List<String> numbers = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

    private List<String> monotonicAlphabet = Arrays.asList("0", "01", "012", "0123", "01234", "012345", "0123456", "01234567", "012345678", "0123456789");

    private List<String> dictionary = Arrays.asList("act", "actor", "enact", "reactor");

    @Test
    public void testConstrainingFilter() {
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> list = new FilterList<String>(new BasicEventList<String>(), textMatcherEditor);

        list.addAll(numbers);
        assertEquals(list, numbers);

        final CountingMatcherEditorListener<String> counter = new CountingMatcherEditorListener<String>();
        textMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"0"});		// constrained
        assertEquals(Arrays.asList("0"), list);
		counter.assertCounterState(0, 0, 0, 1, 0);
    }

	@Test
	public void testRelaxingFilter() {
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> list = new FilterList<String>(new BasicEventList<String>(), textMatcherEditor);

        list.addAll(numbers);

        final CountingMatcherEditorListener<String> counter = new CountingMatcherEditorListener<String>();
        textMatcherEditor.addMatcherEditorListener(counter);

		textMatcherEditor.setFilterText(new String[] {"0"});		// constrained
        assertEquals(Arrays.asList("0"), list);
        textMatcherEditor.setFilterText(new String[0]);				// match all
        assertEquals(numbers, list);
		counter.assertCounterState(1, 0, 0, 1, 0);
		counter.resetCounterState();

		textMatcherEditor.setFilterText(new String[] {"01"});		// constrained
        assertEquals(Collections.EMPTY_LIST, list);
        textMatcherEditor.setFilterText(new String[] {"0"});		// relaxed
        assertEquals(Arrays.asList("0"), list);
		counter.assertCounterState(0, 0, 0, 1, 1);
		counter.resetCounterState();

        textMatcherEditor.setFilterText(new String[] {"0", "1"});	// constrained
        assertEquals(Collections.EMPTY_LIST, list);
        textMatcherEditor.setFilterText(new String[] {"0"});
        assertEquals(Arrays.asList("0"), list);		// relaxed
		counter.assertCounterState(0, 0, 0, 1, 1);
    }

    @Test
    public void testRelaxAndConstrainFilter() {
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> list = new FilterList<String>(new BasicEventList<String>(), textMatcherEditor);

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

    @Test
    public void testClearFilter() {
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> list = new FilterList<String>(new BasicEventList<String>(), textMatcherEditor);

        list.addAll(numbers);

        final CountingMatcherEditorListener<String> counter = new CountingMatcherEditorListener<String>();
        textMatcherEditor.addMatcherEditorListener(counter);

        assertEquals(list, numbers);
        textMatcherEditor.setFilterText(new String[] {"6"});		// constrained
		counter.assertCounterState(0, 0, 0, 1, 0);
		counter.resetCounterState();
		assertEquals(Arrays.asList("6"), list);

		textMatcherEditor.setFilterText(new String[0]);				// match all
		counter.assertCounterState(1, 0, 0, 0, 0);
		assertEquals(list, numbers);
    }

    @Test
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

    @Test
    public void testChangeMode() {
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> list = new FilterList<String>(new BasicEventList<String>(), textMatcherEditor);
        list.addAll(monotonicAlphabet);

        textMatcherEditor.setFilterText(new String[] {"789"});
        assertEquals(Arrays.asList("0123456789"), list);

        textMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        assertEquals(Collections.EMPTY_LIST, list);

        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        assertEquals(Arrays.asList("0123456789"), list);

        list.clear();
        list.addAll(dictionary);
        assertEquals(Collections.EMPTY_LIST, list);

        textMatcherEditor.setFilterText(new String[] {"act"});
        assertEquals(dictionary, list);

        textMatcherEditor.setMode(TextMatcherEditor.STARTS_WITH);
        assertEquals(Arrays.asList("act", "actor"), list);

        textMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
        assertEquals(dictionary, list);

        textMatcherEditor.setMode(TextMatcherEditor.EXACT);
        assertEquals(Arrays.asList("act"), list);

        textMatcherEditor.setFilterText(new String[] {"actor"});
        assertEquals(Arrays.asList("actor"), list);

        textMatcherEditor.setFilterText(new String[] {"badvalue"});
        assertEquals(Arrays.asList(new String[0]), list);
    }

    @Test
    public void testChangeModeNotifications() {
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> list = new FilterList<String>(new BasicEventList<String>(), textMatcherEditor);
        list.addAll(monotonicAlphabet);

        final CountingMatcherEditorListener<String> counter = new CountingMatcherEditorListener<String>();
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

    @Test
    public void testChangeStrategyNotifications() {
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> list = new FilterList<String>(new BasicEventList<String>(), textMatcherEditor);
        list.addAll(monotonicAlphabet);

        final CountingMatcherEditorListener<String> counter = new CountingMatcherEditorListener<String>();
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
    @Test
    public void testFilterBeforeAndAfter() {
        // set up
        EventList<String> unfilteredList = new BasicEventList<String>();
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> filteredList = new FilterList<String>(unfilteredList, textMatcherEditor);

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
    @Test
    public void testFilterDynamic() {
        // set up
        Random random = new Random();
        EventList<String> unfilteredList = new BasicEventList<String>();
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> filteredList = new FilterList<String>(unfilteredList, textMatcherEditor);

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
    @Test
    public void testFilterWritable() {
        // set up
        Random random = new Random();
        EventList<String> unfilteredList = new BasicEventList<String>();
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> filteredList = new FilterList<String>(unfilteredList, textMatcherEditor);

        // apply a filter
        String filter = "5";
        textMatcherEditor.setFilterText(new String[] { filter });

        // apply various operations to a list of strings
        for(int i = 0; i < 4000; i++) {
            List<String> list;
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

    @Test
    public void testNormalizedLatinStrategy() {
        TextMatcherEditor<String> textMatcherEditor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        FilterList<String> list = new FilterList<String>(new BasicEventList<String>(), textMatcherEditor);

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

    @Test
    public void testTextMatcherEquals() {
        TextMatcher<String> matcherA = new TextMatcher<String>(TextMatchers.parse("a b c"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);

        TextMatcher<String> matcherB = new TextMatcher<String>(TextMatchers.parse("c b a"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
        assertTrue(matcherA.equals(matcherB));
        assertTrue(matcherB.equals(matcherA));

        matcherB = new TextMatcher<String>(TextMatchers.parse("\"c\" \"b\" \"a\""), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
        assertTrue(matcherA.equals(matcherB));
        assertTrue(matcherB.equals(matcherA));

        matcherB = new TextMatcher<String>(TextMatchers.parse("a b"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
        assertFalse(matcherA.equals(matcherB));
        assertFalse(matcherB.equals(matcherA));

        matcherB = new TextMatcher<String>(TextMatchers.parse("a b c d"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
        assertFalse(matcherA.equals(matcherB));
        assertFalse(matcherB.equals(matcherA));

        matcherB = new TextMatcher<String>(TextMatchers.parse("a b c"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.STARTS_WITH, TextMatcherEditor.IDENTICAL_STRATEGY);
        assertFalse(matcherA.equals(matcherB));
        assertFalse(matcherB.equals(matcherA));

        matcherB = new TextMatcher<String>(TextMatchers.parse("a b c"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.NORMALIZED_STRATEGY);
        assertFalse(matcherA.equals(matcherB));
        assertFalse(matcherB.equals(matcherA));

        matcherB = new TextMatcher<String>(TextMatchers.parse("+a b c"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
        assertFalse(matcherA.equals(matcherB));
        assertFalse(matcherB.equals(matcherA));

        matcherB = new TextMatcher<String>(TextMatchers.parse("-a b c"), GlazedLists.toStringTextFilterator(), TextMatcherEditor.CONTAINS, TextMatcherEditor.IDENTICAL_STRATEGY);
        assertFalse(matcherA.equals(matcherB));
        assertFalse(matcherB.equals(matcherA));
    }

    /**
     * Intentionally add raw Integers into the list with this TextFilterator in
     * order to validate that TextFilterator is always backwards compatible with
     * old behaviour. (Namely that the .toString() value is used for filtering
     * all of the filterator's object)
     */
    private static class IntegerTextFilterator implements TextFilterator<Integer> {
        @Override
        public void getFilterStrings(List<String> baseList, Integer element) {
            ((List) baseList).add(element);
        }
    }
}
