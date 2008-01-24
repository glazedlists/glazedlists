/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Eases the burdens of implementing any type of Calculation. It stores the
 * last numeric result which represents the Calculation. It provides support
 * for managing the PropertyChangeListeners and firing value changes to them.
 *
 * @author James Lemieux
 */
public abstract class AbstractCalculation<N extends Number> implements Calculation<N> {

    /** manages the registered PropertyChangeListeners */
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    /** the latest calculated value */
    private N value;

    /**
     * @param initialValue the value that should immediately be reported as the
     *      value of this Calculation
     */
    public AbstractCalculation(N initialValue) {
        this.value = initialValue;
    }

    /** @inheritDoc */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    /** @inheritDoc */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    /** @inheritDoc */
    public N getValue() {
        return value;
    }

    /**
     * Subclasses should call this method in order to update the value that is
     * reported from this calculation. Note: this method does NOT fire a
     * PropertyChangeEvent. Subclasses must do that at appropriate times using
     * {@link #fireValueChange(Number, Number)}.
     *
     * @param value the new value of this calculation
     */
    protected void setValue(N value) {
        this.value = value;
    }

    /**
     * A convenience method for firing a PropertyChangeEvent describing a
     * change in the value of this calculation.
     *
     * @param oldValue the old value reported from this calculation
     * @param newValue the new value reported from this calculation
     */
    protected void fireValueChange(N oldValue, N newValue) {
        support.firePropertyChange("value", oldValue, newValue);
    }
}