package ca.odell.glazedlists.matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test the {@link ThreadedMatcherEditor}.
 *
 * @author James Lemieux
 * @author Holger Brands
 */
public class ThreadedMatcherEditorTest {

    // The amount of time (in ms) to wait until the CountingMatcherEditorListener is done processing and begins delaying
    private static final long SIMULATED_PROCESSING_DELAY_STARTS = 200;
    // The amount of time (in ms) for the CountingMatcherEditorListener to delay
    private static final long SIMULATED_PROCESSING_DELAY = 250;
    // The amount of time (in ms) to wait until the CountingMatcherEditorListener completes processing AND delaying
    private static final long SIMULATED_PROCESSING_DELAY_WAIT = 400;

    private MatcherEditor.Event<String> matchAll;
    private MatcherEditor.Event<String> matchNone;
    private MatcherEditor.Event<String> matchRelaxed;
    private MatcherEditor.Event<String> matchConstrained;
    private MatcherEditor.Event<String> matchChanged;

    /** combine multiple matcher editors */
    private ThreadedMatcherEditor<String> threadedMatcherEditor;

    /** a matcher editor to help test the threadedMatcherEditor */
    private TextMatcherEditor<String> textMatcherEditor;

    private FilterList<String> filterList;

    /**
     * Prepare for the test.
     */
    @Before
    public void setUp() {
        Awaitility.setDefaultTimeout(3, TimeUnit.SECONDS);

        textMatcherEditor = new TextMatcherEditor<>(GlazedLists.toStringTextFilterator());
        threadedMatcherEditor = new ThreadedMatcherEditor<>(textMatcherEditor);
        filterList = new FilterList<>(new BasicEventList<String>(), threadedMatcherEditor);

        matchAll = new MatcherEditor.Event<>(threadedMatcherEditor, MatcherEditor.Event.MATCH_ALL, Matchers.trueMatcher());
        matchNone = new MatcherEditor.Event<>(threadedMatcherEditor, MatcherEditor.Event.MATCH_NONE, Matchers.falseMatcher());
        matchRelaxed = new MatcherEditor.Event<>(threadedMatcherEditor, MatcherEditor.Event.RELAXED, threadedMatcherEditor.getMatcher());
        matchConstrained = new MatcherEditor.Event<>(threadedMatcherEditor, MatcherEditor.Event.CONSTRAINED, threadedMatcherEditor.getMatcher());
        matchChanged = new MatcherEditor.Event<>(threadedMatcherEditor, MatcherEditor.Event.CHANGED, threadedMatcherEditor.getMatcher());
    }

    /**
     * Clean up after the test.
     */
    @After
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

    @Test
    public void testSimpleCoalescing() {
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchAll}, MatcherEditor.Event.MATCH_ALL);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchNone}, MatcherEditor.Event.MATCH_NONE);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchRelaxed}, MatcherEditor.Event.RELAXED);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchConstrained}, MatcherEditor.Event.CONSTRAINED);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchChanged}, MatcherEditor.Event.CHANGED);
    }

    @Test
    public void testCoalescingSameElements() {
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchAll, matchAll, matchAll}, MatcherEditor.Event.MATCH_ALL);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchNone, matchNone, matchNone}, MatcherEditor.Event.MATCH_NONE);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchRelaxed, matchRelaxed, matchRelaxed}, MatcherEditor.Event.RELAXED);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchConstrained, matchConstrained, matchConstrained}, MatcherEditor.Event.CONSTRAINED);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchChanged, matchChanged, matchChanged}, MatcherEditor.Event.CHANGED);
    }

    @Test
    public void testCoalescingMatchAll() {
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchNone, matchRelaxed, matchConstrained, matchChanged, matchAll}, MatcherEditor.Event.MATCH_ALL);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchNone, matchAll}, MatcherEditor.Event.MATCH_ALL);
    }

    @Test
    public void testCoalescingMatchNone() {
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchAll, matchRelaxed, matchConstrained, matchChanged, matchNone}, MatcherEditor.Event.MATCH_NONE);
        this.runCoalescingMatchChangedTest(new MatcherEditor.Event[] {matchAll, matchNone}, MatcherEditor.Event.MATCH_NONE);
    }

    @Test
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

    @SuppressWarnings("unchecked")
    private void runCoalescingMatchChangedTest(MatcherEditor.Event[] events, int expectedType) {
        final MatcherEditor.Event<String> coalescedMatcherEvent = coalesceMatcherEvents(threadedMatcherEditor, events);
        // ensure the expectedType is received
        assertThat(coalescedMatcherEvent.getType()).isEqualTo(expectedType);

        // ensure the MatcherEditor returned is == to the threadedMatcherEditor (the MatcherEditor which wraps the source)
        // (that is, we rebrand the coalescedMatcherEvent to look like it originates from the ThreadedMatcherEditor
        // rather than the underlying decorated MatcherEditor)
        assertThat(coalescedMatcherEvent.getMatcherEditor()).isSameAs(threadedMatcherEditor);

        // ensure the Matcher returned is == to the last MatcherEvent's Matcher
        assertThat(coalescedMatcherEvent.getMatcher()).isSameAs(events[events.length-1].getMatcher());
    }

    protected <E> MatcherEditor.Event<E> coalesceMatcherEvents(ThreadedMatcherEditor<E> threadedMatcherEditor, MatcherEditor.Event<E>[] matcherEvents) {
        return threadedMatcherEditor.coalesceMatcherEvents(Arrays.asList(matcherEvents));
    }


    @Test
    public void testFiltering() throws InterruptedException {
        filterList.addAll(Arrays.asList("Andy", "Barry", "Colin", "James", "Jesse", "Jesus", "Trevor", "Ursula", "Vanessa", "Zack"));
        assertThat(filterList).size().isEqualTo(10);

        textMatcherEditor.setFilterText(new String[] {"J"});
        await().until(() -> filterList.size() == 3);
        assertThat(filterList).containsExactly("James", "Jesse", "Jesus");

        textMatcherEditor.setFilterText(new String[] {"ss"});
        await().until(() -> filterList.size() == 2);
        assertThat(filterList).containsExactly("Jesse", "Vanessa");
    }

    @Test
    public void testQueuingConstraints() throws InterruptedException {
        final CountingMatcherEditorListener<String> counter =
            new CountingMatcherEditorListener<>(SIMULATED_PROCESSING_DELAY);
        threadedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"J"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "J"
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);
        await().until(() -> counter.getChangeCount() > 0);

        // now fill the queue with constraints one at a time, as through the user were typing "James"
        textMatcherEditor.setFilterText(new String[] {"Ja"});
        textMatcherEditor.setFilterText(new String[] {"Jam"});
        textMatcherEditor.setFilterText(new String[] {"Jame"});
        textMatcherEditor.setFilterText(new String[] {"James"});

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
//        counter.assertCounterState(0, 0, 0, 2, 0);
        await().untilAsserted(() -> counter.assertCounterState(0, 0, 0, 2, 0));
    }

    @Test
    public void testQueuingRelaxations() throws InterruptedException {
        final CountingMatcherEditorListener<String> counter =
            new CountingMatcherEditorListener<>(SIMULATED_PROCESSING_DELAY);
        threadedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);
        await().until(() -> counter.getChangeCount() > 0);

        // now fill the queue with relaxations one at a time, as through the user were deleting "James"
        textMatcherEditor.setFilterText(new String[] {"Jame"});
        textMatcherEditor.setFilterText(new String[] {"Jam"});
        textMatcherEditor.setFilterText(new String[] {"Ja"});
        textMatcherEditor.setFilterText(new String[] {"J"});

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
//        counter.assertCounterState(0, 0, 0, 1, 1);
        await().untilAsserted(() -> counter.assertCounterState(0, 0, 0, 1, 1));
    }

    @Test
    public void testQueuingMatchAll() throws InterruptedException {
        final CountingMatcherEditorListener<String> counter =
            new CountingMatcherEditorListener<>(SIMULATED_PROCESSING_DELAY);
        threadedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);
        await().until(() -> counter.getChangeCount() > 0);

        // simulate changing, then clearing the filter text
        textMatcherEditor.setFilterText(new String[] {"Scott"});
        textMatcherEditor.setFilterText(new String[0]);

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
//        counter.assertCounterState(1, 0, 0, 1, 0);
        await().untilAsserted(() -> counter.assertCounterState(1, 0, 0, 1, 0));
    }

    @Test
    public void testQueuingChanged() throws InterruptedException {
        final CountingMatcherEditorListener<String> counter =
            new CountingMatcherEditorListener<>(SIMULATED_PROCESSING_DELAY);
        threadedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);
        await().until(() -> counter.getChangeCount() > 0);

        textMatcherEditor.setFilterText(new String[] {"Scott"});
        textMatcherEditor.setFilterText(new String[] {"Jesse"});
        textMatcherEditor.setFilterText(new String[] {"Kevin"});

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
//        counter.assertCounterState(0, 0, 1, 1, 0);
        await().untilAsserted(() -> counter.assertCounterState(0, 0, 1, 1, 0));
    }

    @Test
    public void testQueuingAllSorts_WithPause() throws InterruptedException {
        final CountingMatcherEditorListener<String> counter =
            new CountingMatcherEditorListener<>(SIMULATED_PROCESSING_DELAY);
        threadedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "James"
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_STARTS);
//        counter.assertCounterState(0, 0, 0, 1, 0);
        await().untilAsserted(() -> counter.assertCounterState(0, 0, 0, 1, 0));

        textMatcherEditor.setFilterText(new String[] {"Ja"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "Ja"
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
//        counter.assertCounterState(0, 0, 0, 1, 1);
        await().untilAsserted(() -> counter.assertCounterState(0, 0, 0, 1, 1));

        textMatcherEditor.setFilterText(new String[] {"Col"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "Col"
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
//        counter.assertCounterState(0, 0, 1, 1, 1);
        await().untilAsserted(() -> counter.assertCounterState(0, 0, 1, 1, 1));

        textMatcherEditor.setFilterText(new String[] {"Colin"});
        // ensure we pause to let the time slice end and the Queue Thread to start and begin processing the "Colin"
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
//        counter.assertCounterState(0, 0, 1, 2, 1);
        await().untilAsserted(() -> counter.assertCounterState(0, 0, 1, 2, 1));

        textMatcherEditor.setFilterText(new String[0]);

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
//        counter.assertCounterState(1, 0, 1, 2, 1);
        await().untilAsserted(() -> counter.assertCounterState(1, 0, 1, 2, 1));

        // since we wait for each change to the filter text to clear, we should
        // expect to find exactly the same number of total changes
        assertThat(counter.getChangeCount()).isEqualTo(5);
    }

    @Test
    public void testQueuingAllSorts_WithoutPause() throws InterruptedException {
        filterList.addAll(Arrays.asList("Andy", "Barry", "Colin", "James", "Jesse", "Jesus", "Trevor", "Ursula", "Vanessa", "Zack"));
        assertThat(filterList).size().isEqualTo(10);

        final CountingMatcherEditorListener<String> counter =
            new CountingMatcherEditorListener<>(SIMULATED_PROCESSING_DELAY);
        threadedMatcherEditor.addMatcherEditorListener(counter);

        textMatcherEditor.setFilterText(new String[] {"James"});
        textMatcherEditor.setFilterText(new String[] {"Ja"});
        textMatcherEditor.setFilterText(new String[] {"Col"});
        textMatcherEditor.setFilterText(new String[] {"Colin"});
        textMatcherEditor.setFilterText(new String[0]);

        // ensure the matching finishes, and then check if each of the methods were fired the expected number of times
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
//        Thread.sleep(SIMULATED_PROCESSING_DELAY_WAIT);
        await().until(() -> filterList.size() == 10);

        // because of modern multi-core processors, we can't predict EXACTLY
        // how the ThreadedMatcherEditor combined filters, but we do know that
        // SOMETHING must have been combined, and thus the number of changes
        // should be less than the number of times we changed the filter text
        assertThat(counter.getChangeCount()).isLessThan(5);
    }

    @Test
    public void testCustomExecutor() {
        TextMatcherEditor<String> matcherEditor = new TextMatcherEditor<>(GlazedLists.toStringTextFilterator());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ThreadedMatcherEditor<String> threadedMatcherEditor = new ThreadedMatcherEditor<>(matcherEditor, executor);
        assertThat(threadedMatcherEditor.getExecutor()).isEqualTo(executor);
    }
}
