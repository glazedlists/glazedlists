package ca.odell.glazedlists.matchers;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.junit.Test;

import ca.odell.glazedlists.FunctionList.Function;
import ca.odell.glazedlists.matchers.SetMatcherEditor.Mode;

public class SetMatcherEditorTest {

    @Test
    public void testSetMatchSet() {
        final SetMatcherEditor<Date, Integer> matcherEditor = SetMatcherEditor.create(new Function<Date, Integer>() {
            @Override
            public Integer evaluate(Date date) {
                return date.getYear();
            }
        });

        final CountingMatcherEditorListener<Date> counter = new CountingMatcherEditorListener<Date>();
        matcherEditor.addMatcherEditorListener(counter);
        counter.assertCounterState(0, 0, 0, 0, 0);

        // give some initial range (changed)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007)));
        counter.assertCounterState(0, 0, 1, 0, 0);

        // relax (more values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007, 2008)));
        counter.assertCounterState(0, 0, 1, 0, 1);

        // constrain (less values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2006, 2007)));
        counter.assertCounterState(0, 0, 1, 1, 1);

        // changed (different values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(0, 0, 2, 1, 1);

        // none (no values with Mode.EMPTY_MATCHES_NONE)
        matcherEditor.setMatchSet(new HashSet<Integer>());
        counter.assertCounterState(0, 1, 2, 1, 1);

        // changed (different values)
        matcherEditor.setMatchSet(new HashSet<Integer>(Arrays.asList(2007, 2008)));
        counter.assertCounterState(0, 1, 3, 1, 1);

        // all (no values with Mode.EMPTY_MATCHES_ALL)
        matcherEditor.setMode(Mode.EMPTY_MATCHES_ALL);
        matcherEditor.setMatchSet(new HashSet<Integer>());
        counter.assertCounterState(1, 1, 3, 1, 1);

        // test changing nothing
        matcherEditor.setMatchSet(new HashSet<Integer>());
        counter.assertCounterState(1, 1, 3, 1, 1);
    }
}
