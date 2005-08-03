package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextMatcherEditor;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Test the {@link ThreadedMatcherEditor}.
 *
 * @author James Lemieux
 */
public class ThreadedMatcherEditorTest extends TestCase {

    // The amount of time (in ms) to wait until the CountingMatcherEditorListener is done processing and begins delaying
    private static final long SIMULATED_PROCESSING_DELAY_STARTS = 100;
    // The amount of time (in ms) for the CountingMatcherEditorListener to delay
    private static final long SIMULATED_PROCESSING_DELAY = 250;
    // The amount of time (in ms) to wait until the CountingMatcherEditorListener completes processing AND delaying
    private static final long SIMULATED_PROCESSING_DELAY_WAIT = 300;

    private MatcherEditor.Event matchAll;
    private MatcherEditor.Event matchNone;
    private MatcherEditor.Event matchRelaxed;
    private MatcherEditor.Event matchConstrained;
    private MatcherEditor.Event matchChanged;

    /** combine multiple matcher editors */
    private ThreadedMatcherEditor threadedMatcherEditor;

    /** a matcher editor to help test the threadedMatcherEditor */
    private TextMatcherEditor textMatcherEditor;

    private FilterList filterList;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        textMatcherEditor = new TextMatcherEditor(GlazedLists.toStringTextFilterator());
        threadedMatcherEditor = new ThreadedMatcherEditor(textMatcherEditor);
        filterList = new FilterList(new BasicEventList(), threadedMatcherEditor);

        matchAll = new MatcherEditor.Event(threadedMatcherEditor, MatcherEditor.Event.MATCH_ALL);
        matchNone = new MatcherEditor.Event(threadedMatcherEditor, MatcherEditor.Event.MATCH_NONE);
        matchRelaxed = new MatcherEditor.Event(threadedMatcherEditor, MatcherEditor.Event.RELAXED, threadedMatcherEditor.getMatcher());
        matchConstrained = new MatcherEditor.Event(threadedMatcherEditor, MatcherEditor.Event.CONSTRAINED, threadedMatcherEditor.getMatcher());
        matchChanged = new MatcherEditor.Event(threadedMatcherEditor, MatcherEditor.Event.CHANGED, threadedMatcherEditor.getMatcher());
	}

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        threadedMatcherEditor = null;
        textMatcherEditor = null;
        filterList = null;

        matchAll = null;
        matchNone = null;
        matchRelaxed = null;
        matchConstrained = null;
        matchChanged = null;
    }

    public void testSimpleCoalescing() {
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchAll}, MatcherEditor.Event.MATCH_ALL);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchNone}, MatcherEditor.Event.MATCH_NONE);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchRelaxed}, MatcherEditor.Event.RELAXED);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchConstrained}, MatcherEditor.Event.CONSTRAINED);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchChanged}, MatcherEditor.Event.CHANGED);
    }

    public void testCoalescingSameElements() {
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchAll, matchAll, matchAll}, MatcherEditor.Event.MATCH_ALL);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchNone, matchNone, matchNone}, MatcherEditor.Event.MATCH_NONE);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchRelaxed, matchRelaxed, matchRelaxed}, MatcherEditor.Event.RELAXED);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchConstrained, matchConstrained, matchConstrained}, MatcherEditor.Event.CONSTRAINED);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchChanged, matchChanged, matchChanged}, MatcherEditor.Event.CHANGED);
    }

    public void testCoalescingMatchAll() {
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchNone, matchRelaxed, matchConstrained, matchChanged, matchAll}, MatcherEditor.Event.MATCH_ALL);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchNone, matchAll}, MatcherEditor.Event.MATCH_ALL);
    }

    public void testCoalescingMatchNone() {
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchAll, matchRelaxed, matchConstrained, matchChanged, matchNone}, MatcherEditor.Event.MATCH_NONE);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchAll, matchNone}, MatcherEditor.Event.MATCH_NONE);
    }

    public void testCoalescingMatchChanged() {
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchAll, matchChanged});
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchNone, matchChanged});

        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchRelaxed, matchConstrained});
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchConstrained, matchRelaxed});

        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchRelaxed, matchRelaxed, matchConstrained});
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchConstrained, matchConstrained, matchRelaxed});

        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchChanged, matchConstrained});
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchChanged, matchRelaxed});

        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchAll, matchConstrained});
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchNone, matchRelaxed});
    }

    private void runCoalescingMatchChangedTest(MatcherEditor.Event[] events) {
        this.runCoalescingMatchChangedTest(events, MatcherEditor.Event.CHANGED);
    }

    private void runCoalescingMatchChangedTest(MatcherEditor.Event[] events, int expectedType) {
        final MatcherEditor.Event coalescedMatcherEvent = threadedMatcherEditor.coalesceMatcherEvents(events);
        // ensure the expectedType is received
        assertEquals(expectedType, coalescedMatcherEvent.getType());

        // ensure the MatcherEditor returned is == to the threadedMatcherEditor (the MatcherEditor which wraps the source)
        // (that is, we rebrand the coalescedMatcherEvent to look like it originates from the ThreadedMatcherEditor
        // rather than the underlying decorated MatcherEditor)
        assertTrue(threadedMatcherEditor == coalescedMatcherEvent.getMatcherEditor());

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
        threadedMatcherEditor.addMatcherEditorListener(counter);

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
        threadedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);

        // now fill the queue with relaxations one at a time, as through the user were deleting "James"
        textMatcherEditor.setFilterText(new String[] {"Jame"});
        textMatcherEditor.setFilterText(new String[] {"Jam"});
        textMatcherEditor.setFilterText(new String[] {"Ja"});
        textMatcherEditor.setFilterText(new String[] {"J"});

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 0, 0, 0, 1, 1);
    }

    public void testQueuingMatchAll() throws InterruptedException {
        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        threadedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);

        // simulate changing, then clearing the filter text
        textMatcherEditor.setFilterText(new String[] {"Scott"});
        textMatcherEditor.setFilterText(new String[0]);

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 1, 0, 0, 1, 0);
    }

    public void testQueuingChanged() throws InterruptedException {
        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        threadedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);

        textMatcherEditor.setFilterText(new String[] {"Scott"});
        textMatcherEditor.setFilterText(new String[] {"Jesse"});
        textMatcherEditor.setFilterText(new String[] {"Kevin"});

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        assertCounterState(counter, 0, 0, 1, 1, 0);
    }

    public void testQueuingAllSorts_WithPause() throws InterruptedException {
        final CountingMatcherEditorListener counter = new CountingMatcherEditorListener();
        threadedMatcherEditor.addMatcherEditorListener(counter);

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
        threadedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        textMatcherEditor.setFilterText(new String[] {"Ja"});
        textMatcherEditor.setFilterText(new String[] {"Col"});
        textMatcherEditor.setFilterText(new String[] {"Colin"});
        textMatcherEditor.setFilterText(new String[0]);

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
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

    private class CountingMatcherEditorListener implements MatcherEditor.Listener {
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

        public void changedMatcher(MatcherEditor.Event matcherEvent) {
            switch (matcherEvent.getType()) {
                case MatcherEditor.Event.CONSTRAINED: this.constrained++; break;
                case MatcherEditor.Event.RELAXED: this.relaxed++; break;
                case MatcherEditor.Event.CHANGED: this.changed++; break;
                case MatcherEditor.Event.MATCH_ALL: this.matchAll++; break;
                case MatcherEditor.Event.MATCH_NONE: this.matchNone++; break;
            }

            this.delay();
        }
    }
}