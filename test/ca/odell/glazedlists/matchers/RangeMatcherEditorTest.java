package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.Date;

import org.junit.Test;

public class RangeMatcherEditorTest {

    private static final Date apr = GlazedListsTests.createDate(2006, 3, 15);
    private static final Date may = GlazedListsTests.createDate(2006, 4, 15);
    private static final Date jun = GlazedListsTests.createDate(2006, 5, 15);
    private static final Date jul = GlazedListsTests.createDate(2006, 6, 15);
    private static final Date aug = GlazedListsTests.createDate(2006, 7, 15);
    private static final Date sep = GlazedListsTests.createDate(2006, 8, 15);

    @Test
    public void testSetRange() {
        final RangeMatcherEditor<Date,Date> matcherEditor = new RangeMatcherEditor<Date,Date>();
        final CountingMatcherEditorListener<Date> counter = new CountingMatcherEditorListener<Date>();
        matcherEditor.addMatcherEditorListener(counter);
        counter.assertCounterState(0, 0, 0, 0, 0);

        // give some initial range
        matcherEditor.setRange(may, jul);
        counter.assertCounterState(0, 0, 0, 1, 0);

        // relax the end
        matcherEditor.setRange(may, aug);
        counter.assertCounterState(0, 0, 0, 1, 1);

        // relax the start
        matcherEditor.setRange(apr, aug);
        counter.assertCounterState(0, 0, 0, 1, 2);

        // constrain the end
        matcherEditor.setRange(apr, jul);
        counter.assertCounterState(0, 0, 0, 2, 2);

        // constrain the start
        matcherEditor.setRange(jun, jul);
        counter.assertCounterState(0, 0, 0, 3, 2);

        // relax both the start and end
        matcherEditor.setRange(apr, sep);
        counter.assertCounterState(0, 0, 0, 3, 3);

        // constrain both the start and end
        matcherEditor.setRange(may, aug);
        counter.assertCounterState(0, 0, 0, 4, 3);

        // constrain the start and relax the end
        matcherEditor.setRange(jun, sep);
        counter.assertCounterState(0, 0, 1, 4, 3);

        // relax the start and constrain the end
        matcherEditor.setRange(may, aug);
        counter.assertCounterState(0, 0, 2, 4, 3);

        // test changing nothing
        matcherEditor.setRange(may, aug);
        counter.assertCounterState(0, 0, 2, 4, 3);
    }

    @Test
    public void testSetRangeWithNulls() {
        final RangeMatcherEditor<Date,Date> matcherEditor = new RangeMatcherEditor<Date,Date>();
        final CountingMatcherEditorListener<Date> counter = new CountingMatcherEditorListener<Date>();
        matcherEditor.addMatcherEditorListener(counter);
        counter.assertCounterState(0, 0, 0, 0, 0);

        // set a range
        matcherEditor.setRange(may, jul);
        counter.assertCounterState(0, 0, 0, 1, 0);

        // null out the end
        matcherEditor.setRange(may, null);
        counter.assertCounterState(0, 0, 0, 1, 1);

        // null out the beginning
        matcherEditor.setRange(null, null);
        counter.assertCounterState(1, 0, 0, 1, 1);

        // resetting nulls should produce no event
        matcherEditor.setRange(null, null);
        counter.assertCounterState(1, 0, 0, 1, 1);

        // reset the range
        matcherEditor.setRange(may, jul);
        counter.assertCounterState(1, 0, 0, 2, 1);

        // null out the beginning
        matcherEditor.setRange(null, jul);
        counter.assertCounterState(1, 0, 0, 2, 2);

        // null out the end
        matcherEditor.setRange(null, null);
        counter.assertCounterState(2, 0, 0, 2, 2);

        // reset the range
        matcherEditor.setRange(may, jul);
        counter.assertCounterState(2, 0, 0, 3, 2);

        // null out both the beginning end
        matcherEditor.setRange(null, null);
        counter.assertCounterState(3, 0, 0, 3, 2);

        // set the start
        matcherEditor.setRange(may, null);
        counter.assertCounterState(3, 0, 0, 4, 2);

        // null out both the beginning end
        matcherEditor.setRange(null, null);
        counter.assertCounterState(4, 0, 0, 4, 2);

        // set the end
        matcherEditor.setRange(null, jul);
        counter.assertCounterState(4, 0, 0, 5, 2);
    }
}
