/* Glazed Lists                                                      (c) 2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.impl.matchers.TrueMatcher;


/**
 * Convenience class that makes implementing {@link ValueMatcherEditor} simpler and helps
 * to make sure some the requirement of matching all elements when no value is set is
 * fulfilled.
 *
 * @author <a href="mailto:rob@starlight-systems.com>Rob Eden</a>
 *
 * @see AbstractMatcherEditor
 * @see ValueMatcherEditor
 * @see MatcherEditor
 */
public abstract class AbstractValueMatcherEditor extends AbstractMatcherEditor
	implements ValueMatcherEditor {

	private volatile Object value = null;

	private volatile boolean logicInverted = false;


	/**
	 * Basic constructor.
	 *
	 * @param logicInverted		See {@link ValueMatcherEditor#setLogicInverted(boolean)}
	 * @param initialValue			The initial value for the editor, see
	 * 							{@link #setValue(Object)}.
	 */
	protected AbstractValueMatcherEditor(boolean logicInverted, Object initialValue) {
		setLogicInverted(logicInverted);
		setValue(initialValue);
	}


	/**
	 * Must be implemented by extending classes to create a matcher that is based
	 * on the given value. This method is intended for internal use by the
	 * AbstractValueMatcherEditor class and should not be used directly by extending
	 * classes because logic inversion will not be handled correctly. Instead they
	 * should use {@link #getMatcher()}.
     *
	 * <p>Note that this method will not be called with a null value.
	 *
	 * @param value		The current value. This will always be non-null.
	 */
	protected abstract Matcher createMatcher(Object value);


	/**
	 * {@inheritDoc}
	 */
	public Matcher getMatcher() {
		Object value = this.value;

		// Always match if the value is null
		if (value == null) return TrueMatcher.getInstance();

		Matcher parent = createMatcher(value);
		if (logicInverted) {
			return Matchers.invert(parent);
		} else {
			return parent;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isLogicInverted() {
		return logicInverted;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLogicInverted(boolean logic_inverted) {
		boolean old_state = this.logicInverted;
		if (old_state == logic_inverted) return;		// no change

		this.logicInverted = logic_inverted;

		fireChanged(getMatcher());
	}


	/**
	 * Set the value the matcher will compare against.
     *
	 * <p>This must be called <strong>after</strong> the internal state of the extending class
	 * has been updated such that {@link #createMatcher(Object)} will return a correct value.
	 * Note that {@link #getValue()} will return the value set here when {@link
	 * #createMatcher(Object)} is called (and will be passed in as an argument), so if that
	 * is the only state change, nothing special will need to be done.
	 *
	 * @param value The new value that will be matched against.
	 *
	 * @return true if the update has already been fired (because the current or previous
	 * value was null), otherwise the extending class should determine what event to fire.
	 */
	protected final boolean setValue(Object value) {
		Object old_value = this.value;
		if (old_value == null && value == null) return false;		// no change

		this.value = value;

		if (value == null) {
			fireMatchAll();
			return false;
		} else if (old_value == null) {
			fireConstrained(getMatcher());
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