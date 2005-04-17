/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import java.util.*;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;

/**
 * A {@link MatcherEditor} composed of zero or more delegate
 * {@link MatcherEditor}s.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CompositeMatcherEditor extends AbstractMatcherEditor {
    
    /** the delegates */
    private EventList matcherEditors;

    /** listeners for each delegate */
    private List matcherEditorListeners = new ArrayList();
    
    /**
     * Create a {@link CompositeMatcherEditor} that creates Matchers from the union
     * of the specified {@link EventList} of {@link MatcherEditor}s. The {@link EventList}
     * must not contain any <code>null</code> values and all elements must
     * implement {@link MatcherEditor}.
     */
    public CompositeMatcherEditor(EventList matcherEditors) {
        this.matcherEditors =  matcherEditors;
        
        // prepare the initial set
        for(Iterator i = matcherEditors.iterator(); i.hasNext(); ) {
            MatcherEditor matcherEditor = (MatcherEditor)i.next();
            matcherEditorListeners.add(new DelegateMatcherEditorListener(matcherEditor));
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
        this(new BasicEventList());
    }
    
    /**
     * Get the {@link EventList} of {@link MatcherEditor}s that make up this
     * {@link CompositeMatcherEditor}.  The {@link EventList}
     * must never contain any <code>null</code> values and all elements must
     * implement {@link MatcherEditor}.
     */
    public EventList getMatcherEditors() {
        return matcherEditors;
    }
    
    /**
     * Rebuild the CompositeMatcher modelled by this editor.
     */
    private Matcher rebuildMatcher() {
        List matchers = new ArrayList();
        for(Iterator i = matcherEditors.iterator(); i.hasNext(); ) {
            MatcherEditor matcherEditor = (MatcherEditor)i.next();
            matchers.add(matcherEditor.getMatcher());
        }
        return new AndMatcher(matchers);
    }
    
    /**
     * Handle changes to the MatcherEditors.
     */
    private class MatcherEditorsListListener implements ListEventListener {
        public void listChanged(ListEvent listChanges) {
            // update listeners for the list change
            boolean relaxed = true;
            boolean constrained = true;
            while (listChanges.next()) {
                int index = listChanges.getIndex();
                int type = listChanges.getType();
                
                // when a MatcherEditor is added, listen to it
                if(type == ListEvent.INSERT) {
                    MatcherEditor inserted = (MatcherEditor)matcherEditors.get(index);
                    matcherEditorListeners.add(new DelegateMatcherEditorListener(inserted));
                    relaxed = false;
                    
                // when a MatcherEditor is removed, stop listening to it
                } else if(type == ListEvent.DELETE) {
                    DelegateMatcherEditorListener listener = (DelegateMatcherEditorListener)matcherEditorListeners.remove(index);
                    listener.stopListening();
                    constrained = false;
                    
                // when a MatcherEditor is updated, update the listener
                } else if(type == ListEvent.UPDATE) {
                    MatcherEditor updated = (MatcherEditor)matcherEditors.get(index);
                    DelegateMatcherEditorListener listener = (DelegateMatcherEditorListener)matcherEditorListeners.remove(index);
                    listener.setMatcherEditor(updated);
                    relaxed = false;
                    constrained = false;
                    
                }
            }
            
            // fire events
            if(matcherEditors.isEmpty()) {
                fireMatchAll(); // optimization
            } else if(relaxed) {
                fireRelaxed(rebuildMatcher());
            } else if(constrained) {
                fireConstrained(rebuildMatcher());
            } else {
                fireChanged(rebuildMatcher());
            }
        }
    }
    
    /**
     * Listens to a specific MatcherEditor and fires events as that MatcherEditor changes.
     */
    private class DelegateMatcherEditorListener implements MatcherEditorListener {
        /** the matcher editor this listens to */
        private MatcherEditor source;
        /**
         * Create a new listener for the specified MatcherEditor. Listening is
         * started automatically and should be stopped using {@link stopListening()}.
         */
        public DelegateMatcherEditorListener(MatcherEditor source) {
            this.source = source;
            source.addMatcherEditorListener(this);
        }
        /** {@inheritDoc} */
        public void matchAll(MatcherEditor source) {
            fireMatchAll();
        }
        /** {@inheritDoc} */
        public void matchNone(MatcherEditor source) {
            if(matcherEditors.size() == 1) fireMatchNone(); // optimization
            else fireConstrained(rebuildMatcher());
        }
        /** {@inheritDoc} */
        public void changed(MatcherEditor source, Matcher matcher) {
            fireChanged(rebuildMatcher());
        }
        /** {@inheritDoc} */
        public void constrained(MatcherEditor source, Matcher matcher) {
            fireConstrained(rebuildMatcher());
        }
        /** {@inheritDoc} */
        public void relaxed(MatcherEditor source, Matcher matcher) {
            fireRelaxed(rebuildMatcher());
        }
        /**
         * Start listening to events from the MatcherEditor.
         */
        public void setMatcherEditor(MatcherEditor source) {
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
    
    /**
     * Models a Matcher that only matches if all child elements match.
     */
    private class AndMatcher implements Matcher {
        private List matchers = new ArrayList();
        public AndMatcher(List matchers) {
            this.matchers.addAll(matchers);
        }
        /** {@inheritDoc} */
        public boolean matches(Object item) {
            // true only if everything matches
            for(Iterator i = matchers.iterator(); i.hasNext(); ) {
                Matcher matcher = (Matcher)i.next();
                if(!matcher.matches(item)) return false;
            }
            return true;
        }
    }
}
