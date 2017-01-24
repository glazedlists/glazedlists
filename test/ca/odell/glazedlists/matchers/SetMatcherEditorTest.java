package ca.odell.glazedlists.matchers;

import static org.junit.Assert.assertEquals;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList.Function;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.SetMatcherEditor.Mode;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

public class SetMatcherEditorTest {

	private Date july2007 = new Date(1183248000000L);
	private Date july2008 = new Date(1214870400000L);
	private Date july2009 = new Date(1246406400000L);

	private EventList<Date> source;

	@Before
	public void setup() {
		source = GlazedLists.eventListOf(july2007, july2008, july2009);
		assertEquals(2007, july2007.getYear() + 1900);
		assertEquals(2008, july2008.getYear() + 1900);
		assertEquals(2009, july2009.getYear() + 1900);
	}

    @Test
    public void testWhiteListEmptyMatchNone() {
        final SetMatcherEditor<Date, Integer> matcherEditor = SetMatcherEditor.create(Mode.WHITELIST_EMPTY_MATCH_NONE, new Function<Date, Integer>() {
            @Override
            public Integer evaluate(Date date) {
                return date.getYear() + 1900;
            }
        });
        EventList<Date> filterList = new FilterList<Date>(source, matcherEditor);
        final CountingMatcherEditorListener<Date> counter = new CountingMatcherEditorListener<Date>();
        matcherEditor.addMatcherEditorListener(counter);
        counter.assertCounterState(0, 0, 0, 0, 0);
        assertEquals(Arrays.asList(), filterList);

        // give some initial range (changed)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007)));
        counter.assertCounterState(0, 0, 1, 0, 0);
        assertEquals(Arrays.asList(july2007), filterList);

        // relax (more values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007, 2008)));
        counter.assertCounterState(0, 0, 1, 0, 1);
        assertEquals(Arrays.asList(july2007, july2008), filterList);

        // constrain (less values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007)));
        counter.assertCounterState(0, 0, 1, 1, 1);
        assertEquals(Arrays.asList(july2007), filterList);

        // changed (different values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(0, 0, 2, 1, 1);
        assertEquals(Arrays.asList(july2007, july2008), filterList);

        // empty set
        matcherEditor.setMatchSet(new HashSet<Integer>());
        counter.assertCounterState(0, 1, 2, 1, 1);
        assertEquals(Arrays.asList(), filterList);

        // changed (different values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(0, 1, 3, 1, 1);
        assertEquals(Arrays.asList(july2007, july2008), filterList);

        // test changing nothing
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(0, 1, 3, 1, 1);
        assertEquals(Arrays.asList(july2007, july2008), filterList);
    }

    @Test
    public void testWhiteListEmptyMatchAll() {
        final SetMatcherEditor<Date, Integer> matcherEditor = SetMatcherEditor.create(Mode.WHITELIST_EMPTY_MATCH_ALL, new Function<Date, Integer>() {
            @Override
            public Integer evaluate(Date date) {
                return date.getYear() + 1900;
            }
        });
        EventList<Date> filterList = new FilterList<Date>(source, matcherEditor);
        final CountingMatcherEditorListener<Date> counter = new CountingMatcherEditorListener<Date>();
        matcherEditor.addMatcherEditorListener(counter);
        counter.assertCounterState(0, 0, 0, 0, 0);
        assertEquals(Arrays.asList(july2007, july2008, july2009), filterList);

        // give some initial range (changed)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007)));
        counter.assertCounterState(0, 0, 1, 0, 0);
        assertEquals(Arrays.asList(july2007), filterList);

        // relax (more values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007, 2008)));
        counter.assertCounterState(0, 0, 1, 0, 1);
        assertEquals(Arrays.asList(july2007, july2008), filterList);

        // constrain (less values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007)));
        counter.assertCounterState(0, 0, 1, 1, 1);
        assertEquals(Arrays.asList(july2007), filterList);

        // changed (different values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(0, 0, 2, 1, 1);
        assertEquals(Arrays.asList(july2007, july2008), filterList);

        // empty set
        matcherEditor.setMatchSet(new HashSet<Integer>());
        counter.assertCounterState(1, 0, 2, 1, 1);
        assertEquals(Arrays.asList(july2007, july2008, july2009), filterList);

        // changed (different values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(1, 0, 3, 1, 1);
        assertEquals(Arrays.asList(july2007, july2008), filterList);

        // test changing nothing
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(1, 0, 3, 1, 1);
        assertEquals(Arrays.asList(july2007, july2008), filterList);
    }

    @Test
    public void testBlackList() {
        final SetMatcherEditor<Date, Integer> matcherEditor = SetMatcherEditor.create(Mode.BLACKLIST, new Function<Date, Integer>() {
            @Override
            public Integer evaluate(Date date) {
                return date.getYear() + 1900;
            }
        });
        EventList<Date> filterList = new FilterList<Date>(source, matcherEditor);
        final CountingMatcherEditorListener<Date> counter = new CountingMatcherEditorListener<Date>();
        matcherEditor.addMatcherEditorListener(counter);
        counter.assertCounterState(0, 0, 0, 0, 0);
        assertEquals(Arrays.asList(july2007, july2008, july2009), filterList);

        // give some initial range (changed)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007)));
        counter.assertCounterState(0, 0, 1, 0, 0);
        assertEquals(Arrays.asList(july2008, july2009), filterList);

        // constrain (more values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007, 2008)));
        counter.assertCounterState(0, 0, 1, 1, 0);
        assertEquals(Arrays.asList(july2009), filterList);

        // relax (less values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007)));
        counter.assertCounterState(0, 0, 1, 1, 1);
        assertEquals(Arrays.asList(july2008, july2009), filterList);

        // changed (different values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(0, 0, 2, 1, 1);
        assertEquals(Arrays.asList(july2009), filterList);

        // none
        matcherEditor.setMatchSet(new HashSet<Integer>());
        counter.assertCounterState(1, 0, 2, 1, 1);
        assertEquals(Arrays.asList(july2007, july2008, july2009), filterList);

        // changed (different values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(1, 0, 3, 1, 1);
        assertEquals(Arrays.asList(july2009), filterList);

        // test changing nothing
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(1, 0, 3, 1, 1);
        assertEquals(Arrays.asList(july2009), filterList);
    }
}
