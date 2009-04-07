/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

import javax.swing.event.EventListenerList;
import java.lang.ref.WeakReference;

/**
 * This {@link MatcherEditor} exists to aid with garbage collection of
 * {@link Listener} objects. It is particularly useful when a long-lived
 * {@link MatcherEditor} exists and many short-lived {@link Listener} objects
 * must be added and removed.
 *
 * <p>Rather than attaching each {@link Listener} to the long-lived
 * {@link MatcherEditor} with hard references and managing the {@link Listener}
 * registrations manually, it is considerably easier to contruct a
 * {@link WeakReferenceMatcherEditor} which removes {@link Listener}s after
 * they are unreachable and have been garbage collected.
 *
 * <p>Common usage of this class resembles:
 * <pre>
 * MatcherEditor myCustomMatcherEditor = ...
 * MatcherEditor weakRefMatcherEditor = Matchers.weakReferenceProxy(myCustomMatcherEditor);
 *
 * // customMatcherEditorListener will be removed when it is garbage collected
 * MatcherEditor.Listener customMatcherEditorListener = ...
 * weakRefMatcherEditor.addMatcherEditorListener(customMatcherEditorListener);
 * </pre>
 *
 * @author James Lemieux
 */
public final class WeakReferenceMatcherEditor<E> implements MatcherEditor<E>, MatcherEditor.Listener<E> {

    /** The Listeners for this MatcherEditor. */
    private final EventListenerList listenerList = new EventListenerList();

    /** The last Matcher that was broadcast from this MatcherEditor. */
    private MatcherEditor<E> source;

    /**
     * Construct a MatcherEditor which acts as a weak proxy for the given
     * <code>source</code>. That is, it rebroadcasts MatcherEvents it receives
     * from the <code>source</code> to its own weak listeners until it, itself,
     * is no longer reachable, at which time it stops listening to the
     * <code>source</code>.
     *
     * @param source the MatcherEditor to decorate with weak proxying
     */
    public WeakReferenceMatcherEditor(MatcherEditor<E> source) {
        this.source = source;

        // listen to the source weakly so we clean ourselves up when we're extinct
        source.addMatcherEditorListener(new WeakMatcherEditorListener<E>(source, this));
    }

    /**
     * Return the current {@link Matcher} specified by the decorated
     * {@link MatcherEditor}.
     *
     * @return a non-null {@link Matcher}
     */
    public Matcher<E> getMatcher() {
        return this.source.getMatcher();
    }

    /**
     * Wrap the given <code>listener</code> in a {@link WeakReference} and
     * notify it when the decorated {@link MatcherEditor} fires {@link Matcher}
     * changes. The weak listener will only be notified while it is reachable
     * via hard references, and will be cleaned up the next time a new
     * {@link MatcherEditor.Event} is fired.
     */
    public void addMatcherEditorListener(Listener<E> listener) {
        this.listenerList.add(Listener.class, new WeakMatcherEditorListener<E>(this, listener));
    }

    /** {@inheritDoc} */
    public void removeMatcherEditorListener(Listener<E> listener) {
        final Object[] listeners = this.listenerList.getListenerList();

        // we remove the given listener by identity
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            final Object currentObject = listeners[i+1];
            if (currentObject == listener)
                this.listenerList.remove(MatcherEditor.Listener.class, listener);

            // if the given listener is a WeakMatcherEditorListener, check if
            // the currentObject is actually its referent
            if (currentObject instanceof WeakMatcherEditorListener) {
                final WeakMatcherEditorListener<E> weakMatcherEditorListener = (WeakMatcherEditorListener<E>) currentObject;
                final Listener<E> currentListener = weakMatcherEditorListener.getDecoratedListener();
                if (currentListener == listener)
                    this.listenerList.remove(MatcherEditor.Listener.class, weakMatcherEditorListener);
            }
        }
    }

    /**
     * Indicates a changes has occurred in the delegate Matcher produced by the
     * MatcherEditor.
     *
     * @param matcherEvent a MatcherEditor.Event describing the change in the
     *      delegate Matcher produced by the MatcherEditor
     */
    public void changedMatcher(Event<E> matcherEvent) {
        final Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            ((Listener<E>) listeners[i+1]).changedMatcher(matcherEvent);
    }

    /**
     * This is the crux of this MatcherEditor. It wraps a {@link Listener} in a
     * {@link WeakReference} so that its garbage collection is not affected by
     * being registered with a {@link MatcherEditor}. Instead, each time it is
     * notified that the {@link Matcher} changed it must test the availability
     * of the underlying listener. If it is available, it is notified. If it is
     * unavailable, it removes itself from listening.
     */
    private class WeakMatcherEditorListener<E> implements Listener<E> {

        /** The WeakReference housing the true MatcherEditor.Listener. */
        private final WeakReference<Listener<E>> weakListener;

        /** The editor that this Listener is listening to. */
        private final MatcherEditor<E> editor;

        /**
         * Construct a WeakMatcherEditorListener which wraps the given
         * <code>listener</code>, which is assumed to listen to the given
         * <code>editor</code>, in a {@link WeakReference}.
         *
         * @param editor the {@link MatcherEditor} from which to remove the
         *      listener after it has been garbage collected
         * @param listener the {@link Listener} containing the true logic for
         *      reacting to matcher changes
         */
        public WeakMatcherEditorListener(MatcherEditor<E> editor, Listener<E> listener) {
            this.weakListener = new WeakReference<Listener<E>>(listener);
            this.editor = editor;
        }

        /**
         * Return the underlying {@link Listener} from the {@link WeakReference}.
         */
        public Listener<E> getDecoratedListener() {
            return this.weakListener.get();
        }

        /**
         * This method tests for the existence of the underlying {@link Listener}
         * and if it still exists (i.e. has not been garbage collected) it is
         * notified of the <code>matcherEvent</code>. Otherwise, it is removed
         * from the {@link MatcherEditor} and will never be notified again.
         *
         * @param matcherEvent a MatcherEditor.Event describing the change in the
         *      Matcher produced by the MatcherEditor
         */
        public void changedMatcher(Event<E> matcherEvent) {
            // fetch the underlying MatcherEditor.Listener
            final Listener<E> matcherEditorListener = this.weakListener.get();

            // if it has been garbage collected, stop listening to the MatcherEditor
            if (matcherEditorListener == null) {
                this.editor.removeMatcherEditorListener(this);
            } else {
                // otherwise fire the event as though it originated from this WeakReferenceMatcherEditor
                matcherEvent = new MatcherEditor.Event(WeakReferenceMatcherEditor.this, matcherEvent.getType(), matcherEvent.getMatcher());
                matcherEditorListener.changedMatcher(matcherEvent);
            }
        }
    }
}