package ca.odell.glazedlists.matchers;

import junit.framework.TestCase;
import ca.odell.glazedlists.TextMatcherEditor;
import ca.odell.glazedlists.*;

import java.util.List;
import java.util.Arrays;

/**
 * Test the {@link BufferedMatcherEditor}.
 *
 * @author James Lemieux
 */
public class BufferedMatcherEditorTest extends TestCase {

    // The amount of time (in ms) to wait until the CountingMatcherEditorListener is done processing and begins delaying
    private static final long SIMULATED_PROCESSING_DELAY_STARTS = 100;
    // The amount of time (in ms) for the CountingMatcherEditorListener to delay
    private static final long SIMULATED_PROCESSING_DELAY = 250;
    // The amount of time (in ms) to wait until the CountingMatcherEditorListener completes processing AND delaying
    private static final long SIMULATED_PROCESSING_DELAY_WAIT = 300;

    private MatcherEvent matchAll;
    private MatcherEvent matchNone;
    private MatcherEvent matchRelaxed;
    private MatcherEvent matchConstrained;
    private MatcherEvent matchChanged;

    /** combine multiple matcher editors */
    private BufferedMatcherEditor bufferedMatcherEditor;

    /** a matcher editor to help test the bufferedMatcherEditor */
    private TextMatcherEditor textMatcherEditor;

    private FilterList filterList;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        textMatcherEditor = new TextMatcherEditor(new StringFilterator());
        bufferedMatcherEditor = new BufferedMatcherEditor(textMatcherEditor);
        filterList = new FilterList(new BasicEventList(), bufferedMatcherEditor);

        matchAll = new MatcherEvent(bufferedMatcherEditor, MatcherEvent.MATCH_ALL);
        matchNone = new MatcherEvent(bufferedMatcherEditor, MatcherEvent.MATCH_NONE);
        matchRelaxed = new MatcherEvent(bufferedMatcherEditor, MatcherEvent.RELAXED, bufferedMatcherEditor.getMatcher());
        matchConstrained = new MatcherEvent(bufferedMatcherEditor, MatcherEvent.CONSTRAINED, bufferedMatcherEditor.getMatcher());
        matchChanged = new MatcherEvent(bufferedMatcherEditor, MatcherEvent.CHANGED, bufferedMatcherEditor.getMatcher());
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        bufferedMatcherEditor = null;
        textMatcherEditor = null;
        filterList = null;

        matchAll = null;
        matchNone = null;
        matchRelaxed = null;
        matchConstrained = null;
        matchChanged = null;
    }

    public void testSimpleCoalescing() {
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchAll}, MatcherEvent.MATCH_ALL);
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchNone}, MatcherEvent.MATCH_NONE);
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchRelaxed}, MatcherEvent.RELAXED);
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchConstrained}, MatcherEvent.CONSTRAINED);
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchChanged}, MatcherEvent.CHANGED);
    }

    public void testCoalescingSameElements() {
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchAll, matchAll, matchAll}, MatcherEvent.MATCH_ALL);
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchNone, matchNone, matchNone}, MatcherEvent.MATCH_NONE);
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchRelaxed, matchRelaxed, matchRelaxed}, MatcherEvent.RELAXED);
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchConstrained, matchConstrained, matchConstrained}, MatcherEvent.CONSTRAINED);
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchChanged, matchChanged, matchChanged}, MatcherEvent.CHANGED);
    }

    public void testCoalescingMatchAll() {
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchNone, matchRelaxed, matchConstrained, matchChanged, matchAll}, MatcherEvent.MATCH_ALL);
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchNone, matchAll}, MatcherEvent.MATCH_ALL);
    }

    public void testCoalescingMatchNone() {
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchAll, matchRelaxed, matchConstrained, matchChanged, matchNone}, MatcherEvent.MATCH_NONE);
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchAll, matchNone}, MatcherEvent.MATCH_NONE);
    }

    public void testCoalescingMatchChanged() {
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchAll, matchChanged});
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchNone, matchChanged});

        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchRelaxed, matchConstrained});
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchConstrained, matchRelaxed});

        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchRelaxed, matchRelaxed, matchConstrained});
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchConstrained, matchConstrained, matchRelaxed});

        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchChanged, matchConstrained});
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchChanged, matchRelaxed});

        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchAll, matchConstrained});
        this.runCoalescingMatchChangedTest(new MatcherEvent[] {matchNone, matchRelaxed});
    }

    private void runCoalescingMatchChangedTest(MatcherEvent[] events) {
        this.runCoalescingMatchChangedTest(events, MatcherEvent.CHANGED);
    }

    private void runCoalescingMatchChangedTest(MatcherEvent[] events, int type) {
        final MatcherEvent coalescedMatcherEvent = bufferedMatcherEditor.coalesceMatcherEvents(events);
        // ensure the type is CHANGED
        assertEquals(type, coalescedMatcherEvent.getType());

        // ensure the Matcher returned is == to the last MatcherEvent's Matcher
        assertTrue(bufferedMatcherEditor == coalescedMatcherEvent.getMatcherEditor());

        // ensure the Matcher returned is == to the last MatcherEvent's Matcher
        assertTrue(events[events.length-1].getMatcher() == coalescedMatcherEvent.getMatcher());
    }

    public void testFiltering() throws InterruptedException {
        filterList.addAll(Arrays.asList(new Object[] {"Andy", "Barry", "Colin", "James", "Jesse", "Jesus", "Trevor", "Ursula", "Vanessa", "Zack"}));
        assertEquals(10, filterList.size());

        textMatcherEditor.setFilterText(new String[] {"J"});
        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);
        assertEquals(3, filterList.size());

        textMatcherEditor.setFilterText(new String[] {"ss"});
        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);
        assertEquals(2, filterList.size());
    }

    public void testQueuingConstraints() throws InterruptedException {
        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        bufferedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"J"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "J"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);

        // now fill the queue with constraints one at a time, as through the user were typing "James"
        textMatcherEditor.setFilterText(new String[] {"Ja"});
        textMatcherEditor.setFilterText(new String[] {"Jam"});
        textMatcherEditor.setFilterText(new String[] {"Jame"});
        textMatcherEditor.setFilterText(new String[] {"James"});

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 0, 0, 0, 2, 0);
    }

    public void testQueuingRelaxations() throws InterruptedException {
        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        bufferedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);

        // now fill the queue with constraints one at a time, as through the user were typing "James"
        textMatcherEditor.setFilterText(new String[] {"Jame"});
        textMatcherEditor.setFilterText(new String[] {"Jam"});
        textMatcherEditor.setFilterText(new String[] {"Ja"});
        textMatcherEditor.setFilterText(new String[] {"J"});

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 0, 0, 0, 1, 1);
    }

    public void testQueuingMatchall() throws InterruptedException {
        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        bufferedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);

        textMatcherEditor.setFilterText(new String[] {"Scott"});
        textMatcherEditor.setFilterText(new String[0]);

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 1, 0, 0, 1, 0);
    }

    public void testQueuingChanged() throws InterruptedException {
        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        bufferedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);

        textMatcherEditor.setFilterText(new String[] {"Scott"});

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 0, 0, 1, 1, 0);
    }

    public void testQueuingAllSorts_WithPause() throws InterruptedException {
        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        bufferedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);
        assertCounterState(counter, 0, 0, 0, 1, 0);

        textMatcherEditor.setFilterText(new String[] {"Ja"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 0, 0, 0, 1, 1);

        textMatcherEditor.setFilterText(new String[] {"Col"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 0, 0, 1, 1, 1);

        textMatcherEditor.setFilterText(new String[] {"Colin"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 0, 0, 1, 2, 1);

        textMatcherEditor.setFilterText(new String[0]);

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 1, 0, 1, 2, 1);
    }

    public void testQueuingAllSorts_WithoutPause() throws InterruptedException {
        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        bufferedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        textMatcherEditor.setFilterText(new String[] {"Ja"});
        textMatcherEditor.setFilterText(new String[] {"Col"});
        textMatcherEditor.setFilterText(new String[] {"Colin"});
        textMatcherEditor.setFilterText(new String[0]);

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 1, 0, 0, 0, 0);
    }

    private void assertCounterState(CountingMatcherEditorListener counter, int matchAll, int matchNone, int changed, int constrained, int relaxed) {
        assertEquals(matchAll, counter.matchAll);
        assertEquals(matchNone, counter.matchNone);
        assertEquals(changed, counter.changed);
        assertEquals(constrained, counter.constrained);
        assertEquals(relaxed, counter.relaxed);
    }

    private class CountingMatcherEditorListener implements MatcherEditorListener {
        private int matchAll = 0;
        private int matchNone = 0;
        private int changed = 0;
        private int constrained = 0;
        private int relaxed = 0;

        private void delay() {
            try {
                Thread.sleep(SIMULATED_PROCESSING_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void changedMatcher(MatcherEvent matcherEvent) {
            switch (matcherEvent.getType()) {
                case MatcherEvent.CONSTRAINED: this.constrained++; break;
                case MatcherEvent.RELAXED: this.relaxed++; break;
                case MatcherEvent.CHANGED: this.changed++; break;
                case MatcherEvent.MATCH_ALL: this.matchAll++; break;
                case MatcherEvent.MATCH_NONE: this.matchNone++; break;
            }

            this.delay();
        }
    }

    /**
     * A String's Strings are itself.
     */
    private class StringFilterator implements TextFilterator {
        public void getFilterStrings(List baseList, Object element) {
            baseList.add(element);
        }
    }
}