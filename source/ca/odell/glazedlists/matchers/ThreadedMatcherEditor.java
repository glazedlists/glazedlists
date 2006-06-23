/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import java.util.*;

/**
 * A MatcherEditor which decorates a source MatcherEditor with functionality.
 * Specifically, this MatcherEditor is meant to act as a buffer for
 * MatcherEvents to smooth out disparities between the rate at which
 * MatcherEvents are produced by the source MatcherEditor and the rate at
 * which they are consumed by registered MatcherEditorListeners. <p>
 *
 * Internally, a {@link ThreadedMatcherEditor} enqueues MatcherEvents as they
 * they are received from the source MatcherEditor. The MatcherEvents on the
 * queue are fired by another Thread as rapidly as the MatcherEditorListeners
 * can consume them. Two methods exist on this class which enable subclasses
 * to customize aspects of processing queued MatcherEvents:
 *
 * <ol>
 *   <li> {@link #executeMatcherEventQueueRunnable(Runnable)} is consulted when
 *        a Thread must be selected to execute the given Runnable which will
 *        drain the queue of MatcherEvents. Subclasses may override to
 *        customize which Thread is used.
 *
 *   <li> {@link #coalesceMatcherEvents(List)} is used to compress
 *        many enqueued MatcherEvents into a single representative
 *        MatcherEvent. This implies a contract between all registered
 *        MatcherEditorListeners and this {@link ThreadedMatcherEditor} that
 *        guarantees that processing the coalesced MatcherEvent is equivalent
 *        to processing all MatcherEvents sequentially.
 * </ol>
 *
 * Typical usage patterns of ThreadedMatcherEditor resemble:
 *
 * <pre>
 *   MatcherEditor threadedMatcherEditor = new ThreadedMatcherEditor(new AnyMatcherEditor());
 *   FilterList filterList = new FilterList(new BasicEventList(), threadedMatcherEditor);
 * </pre>
 *
 * @author James Lemieux
 */
public class ThreadedMatcherEditor<E> extends AbstractMatcherEditor<E> {

    /** The underlying MatcherEditor whose MatcherEvents are being queued and fired on an alternate Thread. */
    private final MatcherEditor<E> source;

    /**
     * The LinkedList acting as a queue of MatcherEditor.Event in the order in which they are received
     * from {@link #source}. We take great care to ensure that the queue's monitor is held before it
     * is queried or mutated.
     */
    private final List<MatcherEditor.Event<E>> matcherEventQueue = new LinkedList<MatcherEditor.Event<E>>();

    /**
     * The MatcherEditorListener which reacts to MatcherEvents from the {@link #source}
     * by enqueuing them for firing on another Thread at some later time.
     */
    private MatcherEditor.Listener<E> queuingMatcherEditorListener = new QueuingMatcherEditorListener();

    /**
     * <tt>true</tt> indicates a Thread is currently executing the
     * {@link #drainMatcherEventQueueRunnable} to drain the {@link #matcherEventQueue}.
     */
    private boolean isDrainingQueue = false;

    /**
     * The {@link Runnable} containing the logic to drain the queue of MatcherEvents until it is empty.
     * The Runnable is executed on a Thread using {@link #executeMatcherEventQueueRunnable(Runnable)}.
     */
    private Runnable drainMatcherEventQueueRunnable = new DrainMatcherEventQueueRunnable();

    /**
     * Creates a ThreadedMatcherEditor which wraps the given <code>source</code>.
     * MatcherEvents fired from the <code>source</code> will be enqueued within
     * this MatcherEditor until they are processed on an alternate Thread.
     *
     * @param source the MatcherEditor to wrap with buffering functionality
     * @throws NullPointerException if <code>source</code> is <code>null</code>
     */
    public ThreadedMatcherEditor(MatcherEditor<E> source) {
        if (source == null)
            throw new NullPointerException("source may not be null");
        this.source = source;
        this.source.addMatcherEditorListener(this.queuingMatcherEditorListener);
    }

    /**
     * Returns the current Matcher specified by the source {@link MatcherEditor}.
     *
     * @return the current Matcher specified by the source {@link MatcherEditor}
     */
    public Matcher<E> getMatcher() {
        return this.source.getMatcher();
    }

    /**
     * This method implements the strategy for coalescing many queued
     * MatcherEvents into a single representative MatcherEvent. Listeners which
     * process the MatcherEvent returned from this method should match the state
     * that would exist if each of the <code>matcherEvents</code> were fired
     * sequentially. In general, any group of <code>matcherEvents</code> can be
     * succesfully coalesced as a single MatcherEvent with a type of
     * <code>changed</code>, however, this method's default implementation
     * uses a few heuristics to do more intelligent coalescing in order to
     * gain speed improvements:
     *
     * <ol>
     *   <li> if <code>matcherEvents</code> ends in a MatcherEvent which is a
     *        {@link MatcherEditor.Event#MATCH_ALL} or {@link MatcherEditor.Event#MATCH_NONE}
     *        type, the last MatcherEvent is returned, regardless of previous
     *        MatcherEvents <p>
     *
     *   <li> if <code>matcherEvents</code> only contains a series of
     *        monotonically constraining MatcherEvents, the final MatcherEvent
     *        is returned <p>
     *
     *   <li> if <code>matcherEvents</code> only contains a series of
     *        monotonically relaxing MatcherEvents, the final MatcherEvent is
     *        returned <p>
     *
     *   <li> if <code>matcherEvents</code> contains both constraining and
     *        relaxing MatcherEvents, the final MatcherEvent is returned with
     *        its type as {@link MatcherEditor.Event#CHANGED}
     * </ol>
     *
     * Note that <code>1, 2,</code> and <code>3</code> above merely represent
     * safe optimizations of the type of MatcherEvent that can be returned.
     * It could also have been returned as a MatcherEvent with a type of
     * {@link MatcherEditor.Event#CHANGED} and be assumed to work correctly, though
     * potentially less efficiently, since it is a more generic type of change.
     * <p>
     *
     * Subclasses with the ability to fire precise MatcherEvents with fine grain
     * types (i.e. <code>relaxed</code> or <code>constrained</code>) when
     * coalescing <code>matcherEvents</code> in situations not recounted above
     * may do so by overiding this method.
     *
     * @param matcherEvents an array of MatcherEvents recorded in the order
     *      they were received from the source MatcherEditor
     * @return a single MatcherEvent which, when fired, will result in the
     *      same state as if all <code>matcherEvents</code> had been fired
     *      sequentially
     */
    protected Event<E> coalesceMatcherEvents(List<Event<E>> matcherEvents) {
        boolean changeType = false;

        // fetch the last matcher event - it is the basis of the MatcherEvent which must be returned
        // all that remains is to determine the type of the MatcherEvent to return
        final Event<E> lastMatcherEvent = matcherEvents.get(matcherEvents.size()-1);
        final int lastMatcherEventType = lastMatcherEvent.getType();

        // if the last MatcherEvent is a MATCH_ALL or MATCH_NONE type, we can safely return it immediately
        if (lastMatcherEventType != Event.MATCH_ALL && lastMatcherEventType != Event.MATCH_NONE) {
            // otherwise determine if any constraining and/or relaxing MatcherEvents exist
            boolean constrained = false;
            boolean relaxed = false;

            for (Iterator<Event<E>> i = matcherEvents.iterator(); i.hasNext(); ) {
                switch (i.next().getType()) {
                    case Event.MATCH_ALL: relaxed = true; break;
                    case Event.MATCH_NONE: constrained = true; break;
                    case Event.RELAXED: relaxed = true; break;
                    case Event.CONSTRAINED: constrained = true; break;
                    case Event.CHANGED: constrained = relaxed = true; break;
                }
            }

            changeType = constrained && relaxed;
        }

        // if both constraining and relaxing MatcherEvents exist, ensure we must return a CHANGED MatcherEvent
        // otherwise the last MatcherEvent must represent the coalesced MatcherEvent
        return new MatcherEditor.Event<E>(this, changeType ? Event.CHANGED : lastMatcherEventType, lastMatcherEvent.getMatcher());
    }

    /**
     * This method executes the given <code>runnable</code> on a Thread. The
     * particular Thread chosen to execute the Runnable is left as an
     * implementation detail. By default, a new Thread named
     * <code>MatcherQueueThread</code> is constructed to execute the
     * <code>runnable</code> each time this method is called. Subclasses may
     * override this method to use any Thread selection strategy they wish.
     *
     * @param runnable a Runnable to execute on an alternate Thread
     */
    protected void executeMatcherEventQueueRunnable(Runnable runnable) {
        new Thread(runnable, "MatcherQueueThread").start();
    }

    /**
     * This MatcherEditorListener enqueues each MatcherEvent it receives in the
     * order it is received and then schedules a Runnable to drain the queue of
     * MatcherEvents as soon as possible.
     */
    private class QueuingMatcherEditorListener implements MatcherEditor.Listener<E> {
        public void changedMatcher(Event<E> matcherEvent) {
            synchronized(matcherEventQueue) {
                matcherEventQueue.add(matcherEvent);

                // if necessary, start a Thread to drain the queue
                if (!isDrainingQueue) {
                    isDrainingQueue = true;
                    executeMatcherEventQueueRunnable(drainMatcherEventQueueRunnable);
                }
            }
        }
    }

    /**
     * This Runnable contains logic which continues to process batches of
     * MatcherEvents from the matcherEventQueue until the queue is empty. Each
     * batch of MatcherEvents includes all MatcherEvents available at the time
     * the queue is inspected. The MatcherEvents are then coalesced and the
     * resulting singular MatcherEvent is fired to MatcherEditorListeners
     * attached to this ThreadedMatcherEditor on a different Thread. When the
     * fire method returns, the queue is drained again if it has accumulated
     * MatcherEvents otherwise the DrainMatcherEventQueueRunnable exits.
     */
    private class DrainMatcherEventQueueRunnable implements Runnable {
        public void run() {
            while (true) {
                // acquire the monitor that guards assigning the drainMatcherEventQueueRunnable
                // to a processing Thread as well as exiting the drainMatcherEventQueueRunnable
                final Event<E> matcherEvent;
                synchronized (matcherEventQueue) {
                    // if no work exists in the queue, exit the Runnable
                    if (matcherEventQueue.isEmpty()) {
                        // no matter the circumstance for us exiting the Runnable,
                        // ensure we indicate we are no longer draining the queue
                        isDrainingQueue = false;
                        return;
                    }

                    // fetch a copy of all MatcherEvents currently in the queue
                    matcherEvent = coalesceMatcherEvents(matcherEventQueue);
                    matcherEventQueue.clear();
                }

                try {
                    // coalesce all of the current MatcherEvents to a single representative MatcherEvent
                    // and fire the single coalesced MatcherEvent
                    fireChangedMatcher(matcherEvent);

                } catch(Error e) {
                    synchronized(matcherEventQueue) { isDrainingQueue = false; }
                    throw e;
                } catch(RuntimeException e) {
                    synchronized(matcherEventQueue) { isDrainingQueue = false; }
                    throw e;
                }
            }
        }
    }
}