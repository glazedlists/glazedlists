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
    
    /** require all matchers in the {@link MatcherEditor} to match */
    public static final int AND = 42;
    /** require any matchers in the {@link MatcherEditor} to match */
    public static final int OR = 24;
    
    /** the delegates */
    private EventList matcherEditors;
    
    /** whether to match with AND or OR */
    private int mode = AND;

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
        if(mode == AND) return new AndMatcher(matchers);
        else if(mode == OR) return new OrMatcher(matchers);
        else throw new IllegalStateException();
    }
    
    /**
     * Handle changes to the MatcherEditors.
     */
    private class MatcherEditorsListListener implements ListEventListener {
        public void listChanged(ListEvent listChanges) {
            // update listeners for the list change
            boolean inserts = false;
            boolean deletes = false;
            boolean wasEmpty = matcherEditorListeners.isEmpty();
            while (listChanges.next()) {
                int index = listChanges.getIndex();
                int type = listChanges.getType();
                
                // when a MatcherEditor is added, listen to it
                if(type == ListEvent.INSERT) {
                    MatcherEditor inserted = (MatcherEditor)matcherEditors.get(index);
                    matcherEditorListeners.add(new DelegateMatcherEditorListener(inserted));
                    inserts = true;
                    
                // when a MatcherEditor is removed, stop listening to it
                } else if(type == ListEvent.DELETE) {
                    DelegateMatcherEditorListener listener = (DelegateMatcherEditorListener)matcherEditorListeners.remove(index);
                    listener.stopListening();
                    deletes = true;
                    
                // when a MatcherEditor is updated, update the listener
                } else if(type == ListEvent.UPDATE) {
                    MatcherEditor updated = (MatcherEditor)matcherEditors.get(index);
                    DelegateMatcherEditorListener listener = (DelegateMatcherEditorListener)matcherEditorListeners.remove(index);
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
        
        // don't fire events if there's no filters
        if(matcherEditors.isEmpty()) {
            return;
        
        // requiring all to requiring any is a relax
        } else if(oldMode == AND && mode == OR) {
            fireRelaxed(rebuildMatcher());
            
        // requiring any to requiring all is a constrain
        } else if(oldMode == OR && mode == AND) {
            fireConstrained(rebuildMatcher());
            
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
     * Models a Matcher that matches if all child elements match.
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
    
    /**
     * Models a Matcher that matches if any child elements match.
     */
    private class OrMatcher implements Matcher {
        private List matchers = new ArrayList();
        public OrMatcher(List matchers) {
            this.matchers.addAll(matchers);
        }
        /** {@inheritDoc} */
        public boolean matches(Object item) {
            // true only if everything matches
            for(Iterator i = matchers.iterator(); i.hasNext(); ) {
                Matcher matcher = (Matcher)i.next();
                if(matcher.matches(item)) return true;
            }
            return false;
        }
    }
}
