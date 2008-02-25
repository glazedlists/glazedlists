/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Advanced Calculations can be derived by combining many smaller Calculations
 * together using a formula. This abstract class advances toward the goal of
 * combining multiple input Calculations using any arithmetic expression to
 * produce the value of this calculation.
 *
 * <p>When any of the input Calculations change their value, this composite
 * Calculation responds by recomputing its own value.
 *
 * @author James Lemieux
 */
public abstract class AbstractCompositeCalculation<N extends Number> extends AbstractCalculation<N> implements PropertyChangeListener {

    /** the inputs which must be combined by {@link #recompute} to produce the value of this Calculation */
    private final Calculation<? extends Number>[] inputs;

    /**
     * Combines the given <code>inputs</code> with the logic in {@link #recompute}
     * to produce the value of this Calculation.
     *
     * @param inputs smaller Calculations to combine to produce this Calculation
     */
    protected AbstractCompositeCalculation(Calculation<? extends Number>... inputs) {
        super(null);
        this.inputs = inputs;

        // compute the first real value of this Calculation from the input values
        setValue(recompute(getInputValues()));

        // begin listening to the input Calculations for changes
        for (int i = 0; i < inputs.length; i++)
            inputs[i].addPropertyChangeListener(this);
    }

    /** @inheritDoc */
    public void dispose() {
        // stop listening to the input Calculations for changes
        for (int i = 0; i < inputs.length; i++)
            inputs[i].removePropertyChangeListener(this);
    }

    /**
     * A convenience method to fetch a snapshot of the values of each of the
     * smaller Calculations being combined by this composite calculation.
     *
     * @return the values of the composed Calculations
     */
    private Number[] getInputValues() {
        final Number[] inputValues = new Number[inputs.length];
        for (int i = 0; i < inputs.length; i++)
            inputValues[i] = inputs[i].getValue();

        return inputValues;
    }

    /**
     * Provides the logic to combine the <code>inputs</code> into the single
     * numeric value of this composite calculation. The inputs can be combined
     * using any desirable arithmetic expression.
     *
     * @param inputs the values of the composed Calculations
     * @return the single numeric value of this composite calculation
     */
    protected abstract N recompute(Number[] inputs);

    /**
     * When any of the input Calculations report a change, this composite
     * calculation is also recalculated in response.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        final N oldValue = getValue();
        final N newValue = recompute(getInputValues());
        setValue(newValue);
        fireValueChange(oldValue, newValue);
    }
}