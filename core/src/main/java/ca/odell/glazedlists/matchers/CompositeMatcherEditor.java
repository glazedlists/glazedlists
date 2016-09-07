/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link MatcherEditor} composed of zero or more delegate
 * {@link MatcherEditor}s.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class CompositeMatcherEditor<E> extends AbstractMatcherEditor<E> {

    /** require all matchers in the {@link MatcherEditor} to match */
    public static final int AND = 42;
    /** require any matchers in the {@link MatcherEditor} to match */
    public static final int OR = 24;

    /** the delegates */
    private EventList<MatcherEditor<E>> matcherEditors;

    /** whether to match with AND or OR */
    private int mode = AND;

    /** listeners for each delegate */
    private List<DelegateMatcherEditorListener> matcherEditorListeners = new ArrayList<DelegateMatcherEditorListener>();

    /**
     * Create a {@link CompositeMatcherEditor} that creates Matchers from the union
     * of the specified {@link EventList} of {@link MatcherEditor}s. The {@link EventList}
     * must not contain any <code>null</code> values and all elements must
     * implement {@link MatcherEditor}.
     */
    public CompositeMatcherEditor(EventList<MatcherEditor<E>> matcherEditors) {
        this.matcherEditors = matcherEditors;

        // prepare the initial set
        for(Iterator<MatcherEditor<E>> i = matcherEditors.iterator(); i.hasNext(); ) {
            matcherEditorListeners.add(new DelegateMatcherEditorListener(i.next()));
        }

        // handle changes to the list of matchers
        matcherEditors.addListEventListener(new MatcherEditorsListListener());

        // use the initial matcher
        fireChanged(rebuildMatcher());
    }

    /**
     * Create a {@link CompositeMatcherEditor}.
     */
    public CompositeMatcherEditor() {
        this(new BasicEventList<MatcherEditor<E>>());
    }

    /**
     * Get the {@link EventList} of {@link MatcherEditor}s that make up this
     * {@link CompositeMatcherEditor}.  The {@link EventList}
     * must never contain any <code>null</code> values and all elements must
     * implement {@link MatcherEditor}.
     */
    public EventList<MatcherEditor<E>> getMatcherEditors() {
        return matcherEditors;
    }

    /**
     * Rebuild the CompositeMatcher modelled by this editor.
     */
    private Matcher<E> rebuildMatcher() {
        final Matcher[] matchers = new Matcher[matcherEditors.size()];
        for (int i = 0, n = matcherEditors.size(); i < n; i++) {
            matchers[i] = matcherEditors.get(i).getMatcher();
        }

        if(mode == AND) return Matchers.and(matchers);
        else if(mode == OR) return Matchers.or(matchers);
        else throw new IllegalStateException();
    }

    /**
     * Handle changes to the MatcherEditors.
     */
    private class MatcherEditorsListListener implements ListEventListener<MatcherEditor<E>> {
        @Override
        public void listChanged(ListEvent<MatcherEditor<E>> listChanges) {
            // update listeners for the list change
            boolean inserts = false;
            boolean deletes = false;
            boolean wasEmpty = matcherEditorListeners.isEmpty();
            while (listChanges.next()) {
                int index = listChanges.getIndex();
                int type = listChanges.getType();

                // when a MatcherEditor is added, listen to it
                if(type == ListEvent.INSERT) {
                    MatcherEditor<E> inserted = matcherEditors.get(index);
                    matcherEditorListeners.add(new DelegateMatcherEditorListener(inserted));
                    inserts = true;

                // when a MatcherEditor is removed, stop listening to it
                } else if(type == ListEvent.DELETE) {
                    DelegateMatcherEditorListener listener = matcherEditorListeners.remove(index);
                    listener.stopListening();
                    deletes = true;

                // when a MatcherEditor is updated, update the listener
                } else if(type == ListEvent.UPDATE) {
                    MatcherEditor<E> updated = matcherEditors.get(index);
                    DelegateMatcherEditorListener listener = matcherEditorListeners.get(index);
                    listener.setMatcherEditor(updated);
                    inserts = true;
                    deletes = true;

                }
            }
            boolean isEmpty = matcherEditorListeners.isEmpty();

            // fire events
            if(mode == AND) {
                if(inserts && deletes) {
                    fireChanged(rebuildMatcher());
                } else if(inserts) {
                    fireConstrained(rebuildMatcher());
                } else if(deletes) {
                    if(isEmpty) fireMatchAll();
                    else fireRelaxed(rebuildMatcher());
                }
            } else if(mode == OR) {
                if(inserts && deletes) {
                    fireChanged(rebuildMatcher());
                } else if(inserts) {
                    if(wasEmpty) fireConstrained(rebuildMatcher());
                    else fireRelaxed(rebuildMatcher());
                } else if(deletes) {
                    if(isEmpty) fireMatchAll();
                    else fireConstrained(rebuildMatcher());
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Set the match mode for this {@link CompositeMatcherEditor}.
     *
     * @param mode either <code>CompositeMatcherEditor.AND</code> to match all
     *      <code>CompositeMatcherEditor.OR</code> to match any.
     */
    public void setMode(int mode) {
        if(this.mode == mode) return;
        int oldMode = this.mode;
        this.mode = mode;

        // requiring all to requiring any is a relax
        if(oldMode == AND && mode == OR) {
            if(matcherEditors.isEmpty()) {
                fireMatchNone();
            } else if(matcherEditors.size() > 1) {
                fireRelaxed(rebuildMatcher());
            }

        // requiring any to requiring all is a constrain
        } else if(oldMode == OR && mode == AND) {
            if(matcherEditors.isEmpty()) {
                fireMatchAll();
            } else if(matcherEditors.size() > 1) {
                fireConstrained(rebuildMatcher());
            }

        // we don't support this mode
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Get the match mode for this {@link CompositeMatcherEditor}.
     *
     * @return either <code>CompositeMatcherEditor.AND</code> for match all
     *      <code>CompositeMatcherEditor.OR</code> for match any.
     */
    public int getMode() {
        return mode;
    }

    /**
     * Listens to a specific MatcherEditor and fires events as that MatcherEditor changes.
     */
    private class DelegateMatcherEditorListener implements Listener<E> {
        /** the matcher editor this listens to */
        private MatcherEditor<E> source;

        /**
         * This implementation of this method simply delegates the handling of
         * the given <code>matcherEvent</code> to one of the protected methods
         * defined by this class. This clearly separates the logic for each
         * type of Matcher change.
         *
         * @param matcherEvent a MatcherEvent describing the change in the
         *      Matcher produced by the MatcherEditor
         */
        @Override
        public void changedMatcher(MatcherEditor.Event<E> matcherEvent) {
            switch (matcherEvent.getType()) {
                case Event.CONSTRAINED: this.constrained(); break;
                case Event.RELAXED: this.relaxed(); break;
                case Event.CHANGED: this.changed(); break;
                case Event.MATCH_ALL: this.matchAll(); break;
                case Event.MATCH_NONE: this.matchNone(); break;
            }
        }

        /**
         * Create a new listener for the specified MatcherEditor. Listening is
         * started automatically and should be stopped using {@link #stopListening()}.
         */
        private DelegateMatcherEditorListener(MatcherEditor<E> source) {
            this.source = source;
            source.addMatcherEditorListener(this);
        }
        private void matchAll() {
            if(matcherEditors.size() == 1) fireMatchAll(); // optimization
            else fireRelaxed(rebuildMatcher());
        }
        private void matchNone() {
            if(matcherEditors.size() == 1) fireMatchNone(); // optimization
            else fireConstrained(rebuildMatcher());
        }
        private void changed() {
            fireChanged(rebuildMatcher());
        }
        private void constrained() {
            fireConstrained(rebuildMatcher());
        }
        private void relaxed() {
            fireRelaxed(rebuildMatcher());
        }
        /**
         * Start listening to events from the MatcherEditor.
         */
        public void setMatcherEditor(MatcherEditor<E> source) {
            if(this.source == source) return;
            stopListening();
            this.source = source;
            source.addMatcherEditorListener(this);
        }
        /**
         * Stop listening to events from the MatcherEditor.
         */
        public void stopListening() {
            source.removeMatcherEditorListener(this);
        }
    }
}