/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 7:39:25 AM
 */
package ca.odell.glazedlists.impl.matchers;


/**
 * A {@link ca.odell.glazedlists.Matcher} that filters elements based on whether they are
 * equal to a given value. Either {@link Object#equals} or <tt>==</tt> can be used for
 * comparison and the logic can be flipped (so that objects match if they are not equal).
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class EqualsMatcher extends AbstractMatcher {
    private volatile Object match_value;
    /**
     * If false, "==" is used rather than "equals()"
     */
    private volatile boolean use_equals_method;
    /**
     * If false, objects match if they are not equal to the match value.
     */
    private volatile boolean match_on_equal;
    /**
     * If true, all items will match if no match value is set. Otherwise, nothing will.
     */
    private volatile boolean match_on_no_threshold;


    /**
     * Construct an instance with no initial match value that use {@link Object#equals}
     * for comparisons.
     */
    public EqualsMatcher() {
        this(null, true, true, true);
    }

    /**
     * Construct an instance with an initial match value that use {@link Object#equals}
     * for comparisons.
     *
     * @param match_value The initial value to match, or null if none. See {@link
     *                    #setMatchValue(Object)} for complete rules.
     */
    public EqualsMatcher(Object match_value) {
        this(match_value, true, true, true);
    }

    /**
     * Construct an instance.
     *
     * @param match_value           The initial value to match, or null if none. See
     *                              {@link #setMatchValue(Object)} for complete rules.
     * @param use_equals_method     If true {@link Object#equals} will  be used to
     *                              determine equality, otherwise <tt>==</tt> will be
     *                              used.
     * @param match_on_equal        If true, items will match if they are equal to the
     *                              <tt>match_value</tt>, otherwise they will match if
     *                              they are not equal.
     * @param match_on_no_threshold Determines whether a null threshold causes all
     *                              elements to match (true) or be hidden (false).
     */
    public EqualsMatcher(Object match_value, boolean use_equals_method,
        boolean match_on_equal, boolean match_on_no_threshold) {

        this.match_value = match_value;
        this.use_equals_method = use_equals_method;
        this.match_on_equal = match_on_equal;
        this.match_on_no_threshold = match_on_no_threshold;
    }


    /**
     * {@inheritDoc
     */
    public boolean matches(Object item) {
        // Grab a reference so it doesn't change out from under us
        Object match_value = this.match_value;

        // If there is no match value set...
        if (match_value == null) return match_on_no_threshold;

        boolean equal;
        // If item is null, can't be equal
        if (item == null)
            equal = false;
        // Do equals() check
        else if (use_equals_method)
            equal = match_value.equals(item);
        // Do identity check
        else
            equal = match_value == item;

        // Possibly flip logic
        return match_on_equal ? equal : !equal;
    }


    /**
     * Update the match value. The object used may not change its behavior while used by
     * the matcher. That is, equals() must always return the same value for a given
     * object. Immutable objects are highly recommended to avoid unintended, unpredicatble
     * behavior caused by accidentally changing the state of the value.
     */
    public synchronized void setMatchValue(Object match_value) {
        Object old_threshold = this.match_value;
        this.match_value = match_value;

        if (match_value == null) {
            // If there didn't used to be a match value, it's a noop
            if (old_threshold == null) return;

            // Either cleared filter or restricted it (all the way!)
            if (match_on_no_threshold)
                fireCleared();
            else
                fireConstrained();
        } else if (old_threshold == null) {
            if (match_on_no_threshold)
                fireConstrained();
            else
                fireRelaxed();
        } else {
            fireChanged();
        }
    }

    /**
     * @see #setMatchValue(Object)
     */
    public Object getMatchValue() {
        return match_value;
    }


    /**
     * Update whether {@link Object#equals} is used for comparison (when <tt>true</tt>) or
     * whether <tt>==</tt> is used (when <tt>false</tt>).
     */
    public synchronized void setUseEquals(boolean use_equals_method) {
        boolean old_value = this.use_equals_method;
        this.use_equals_method = use_equals_method;

        if (use_equals_method != old_value) fireChanged();
    }

    /**
     * @see #setUseEquals(boolean)
     */
    public boolean getUseEquals() {
        return use_equals_method;
    }


    /**
     * Update whether items match if they are equal to (when <tt>true</tt>) or not equal
     * to (when <tt>false</tt>) the {@link #setMatchValue match value}.
     */
    public synchronized void setMatchOnEqual(boolean match_on_equal) {
        boolean old_value = this.match_on_equal;
        this.match_on_equal = match_on_equal;

        if (match_on_equal != old_value) fireChanged();
    }

    /**
     * @see #setMatchOnEqual(boolean)
     */
    public boolean getMatchOnEqual() {
        return match_on_equal;
    }


    /**
     * Update whether or not an empty threshold indicates that all elements should match
     * (true) or be hidden (false).
     */
    public synchronized void setMatchOnNoThreshold(boolean match_on_no_threshold) {
        this.match_on_no_threshold = match_on_no_threshold;

        // Only need to update if there is current no match value
        if (match_value == null) fireChanged();
    }

    /**
     * @see #setMatchOnNoThreshold(boolean)
     */
    public boolean getMatchOnThreshold() {
        return match_on_no_threshold;
    }
}
