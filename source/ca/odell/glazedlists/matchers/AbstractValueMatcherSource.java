/*
 * Copyright(c) 2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Mar 22, 2005 - 4:39:59 PM
 */
package ca.odell.glazedlists.matchers;


import ca.odell.glazedlists.Matcher;

/**
 * // TODO: comment
 *
 * @see AbstractMatcherSource
 * @see ValueMatcherSource
 * @see ca.odell.glazedlists.MatcherSource
 *
 * @author <a href="mailto:rob@starlight-systems.com>Rob Eden</a>
 */
public abstract class AbstractValueMatcherSource extends AbstractMatcherSource
	implements ValueMatcherSource {

	private volatile Object value = null;

	private volatile boolean logic_inverted = false;


	// TODO: comment
	protected AbstractValueMatcherSource(Matcher initial_matcher, boolean logic_inverted,
		Object initial_value) {

		super(initial_matcher);

		setLogicInverted(logic_inverted);
		setValue(initial_value);
	}


	protected abstract Matcher createMatcher(Object value);


	/**
	 * {@inheritDoc}
	 */
	public boolean isLogicInverted() {
		return logic_inverted;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLogicInverted(boolean logic_inverted) {
		boolean old_state = this.logic_inverted;
		if (old_state == logic_inverted) return;		// no change

		this.logic_inverted = logic_inverted;

		fireChanged(getCurrentMatcher());
	}


	public final Matcher getCurrentMatcher() {
		Object value = this.value;

		if (value == null) return TrueMatcher.getInstance();

		Matcher parent = createMatcher(value);
		if (logic_inverted) {
			return new NotMatcher(parent);
		} else {
			return parent;
		}
	}


	/**
	 * Set the value the matcher will compare against.
	 * <p>
	 * This must be called <strong>after</strong> the internal state of the extending
	 * class has been updated such that {@link #createMatcher(Object)} will return a
	 * correct value. Note that {@link #getValue()} will return the value set here when
	 * {@link #createMatcher(Object)} is called (and will be passed in as an argument),
	 * so if that is the only state change, nothing special will need to be done.
	 *
	 * @param value		The new value that will be matched against.
	 *
	 * @return		true if the update has already been fired (because the current or
	 * 				previous value was null), otherwise the extending
	 * 				class should determine what event to fire.
	 */
	protected final boolean setValue(Object value) {
		Object old_value = this.value;
		if (old_value == null && value == null) return false;		// no change

		this.value = value;

		if (value == null) {
			fireCleared();
			return false;
		} else if (old_value == null) {
			fireConstrained(getCurrentMatcher());
			return false;
		}

		return true;
	}

	/**
	 * @see #getValue()
	 */
	protected final Object getValue() {
		return value;
	}
}
