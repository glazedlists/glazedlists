/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.*;
import java.util.*;

/**
 * Basic building block for {@link MatcherEditor} implementations that
 * handles the details of dealing with listeners. All {@link MatcherEditor}
 * implementators are encouraged to extends this class for its convenience methods.
 *
 * <p>Extending classes can fire events to listener using "fire" methods: 
 * <ul>
 *    <li>{@link #fireMatchNone(Matcher)}</li>
 *    <li>{@link #fireConstrained(Matcher)}</li>
 *    <li>{@link #fireChanged(Matcher)}</li>
 *    <li>{@link #fireRelaxed(Matcher)}</li>
 *    <li>{@link #fireMatchAll(Matcher)}</li>
 * </ul>
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public abstract class AbstractMatcherEditor implements MatcherEditor {
    
    /** listeners for this Editor */
    private final List listeners = new ArrayList(1);  // normally only one listener

	/** the current Matcher in effect */
	private volatile Matcher currentMatcher = Matchers.trueMatcher();

	/** {@inheritDoc} */
	public Matcher getMatcher() {
		return currentMatcher;
	}

	/** {@inheritDoc} */
    public final void addMatcherEditorListener(MatcherEditorListener listener) {
        listeners.add(listener);
    }

    /** {@inheritDoc} */
    public final void removeMatcherEditorListener(MatcherEditorListener listener) {
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            if(i.next() == listener) {
                i.remove();
                break;
            }
        }
        throw new IllegalStateException("Listener not found: " + listener);
    }

    /**
     * Indicates that the filter matches all.
     */
    protected final void fireMatchAll() {
		this.currentMatcher = Matchers.trueMatcher();

        // notify all listeners
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            MatcherEditorListener listener = (MatcherEditorListener)i.next();
            listener.matchAll(this);
        }
    }

    /**
     * Indicates that the filter has changed in an inditerminate way.
     */
    protected final void fireChanged(Matcher matcher) {
		if(matcher == null) throw new NullPointerException();
		this.currentMatcher = matcher;

        // notify all listeners
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            MatcherEditorListener listener = (MatcherEditorListener)i.next();
            listener.changed(this, currentMatcher);
        }
    }

    /**
     * Indicates that the filter has changed to be more restrictive. This should only be
     * called if all currently filtered items will remain filtered.
     */
    protected final void fireConstrained(Matcher matcher) {
		if(matcher == null) throw new NullPointerException();
		this.currentMatcher = matcher;

        // notify all listeners
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            MatcherEditorListener listener = (MatcherEditorListener)i.next();
            listener.constrained(this, currentMatcher);
        }
    }

    /**
     * Indicates that the filter has changed to be less restrictive. This should only be
     * called if all currently unfiltered items will remain unfiltered.
     */
    protected final void fireRelaxed(Matcher matcher) {
		if(matcher == null) throw new NullPointerException();
		this.currentMatcher = matcher;

        // notify all listeners
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            MatcherEditorListener listener = (MatcherEditorListener)i.next();
            listener.relaxed(this, currentMatcher);
        }
    }
    
    /**
     * Indicates that the filter matches none.
     */
    protected final void fireMatchNone() {
		this.currentMatcher = Matchers.falseMatcher();

        // notify all listeners
        for(Iterator i = listeners.iterator(); i.hasNext(); ) {
            MatcherEditorListener listener = (MatcherEditorListener)i.next();
            listener.matchNone(this);
        }
    }
}
