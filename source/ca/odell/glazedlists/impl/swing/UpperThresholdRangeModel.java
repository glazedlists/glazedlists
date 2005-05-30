/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
// Swing toolkit stuff for implementing models
import javax.swing.*;
// for responding to user actions
import javax.swing.event.*;

/**
 * A UpperThresholdRangeModel provides an implementation of a bounded-range
 * model for a slider widget that binds a JSlider to a ThresholdList.  This
 * implementation maps the slider value to the upper threshold of the
 * ThresholdList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class UpperThresholdRangeModel extends DefaultBoundedRangeModel implements BoundedRangeModel {

    /** the list to connect a slider widget to */
    private ThresholdList target = null;

    /**
     * Creates a new range that controls specified ThresholdList.
     */
    public UpperThresholdRangeModel(ThresholdList target) {
        this.target = target;
    }

    /**
     * Returns the model's minimum.
     */
    public int getMinimum() {
        target.getReadWriteLock().readLock().lock();
        try {
            return target.getLowerThreshold();
        } finally {
            target.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns the model's current value.
     */
    public int getValue() {
        target.getReadWriteLock().readLock().lock();
        try {
            return target.getUpperThreshold();
        } finally {
            target.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Sets all of the properties for this bounded-range.
     *
     * <p>This is a tweaked version of the setRangeProperties method to be found
     * in the JDK source for DefaultBoundedRangeModel.  Just giving credit where
     * credit is due.
     */
    public void setRangeProperties(int newValue, int newExtent, int newMin, int newMax, boolean adjusting) {
        target.getReadWriteLock().writeLock().lock();
        try {
            // Correct invalid values
            if(newMin > newMax) newMin = newMax;
            if(newValue > newMax) newMax = newValue;
            if(newValue < newMin) newMin = newValue;

            // See if non-threshold model changes are necessary
            boolean changed =
                (newExtent != getExtent()) ||
                (newMax != getMaximum()) ||
                (adjusting != getValueIsAdjusting());

            // Set the lower threshold if applicable
            if(newMin != getMinimum()) {
                target.setLowerThreshold(newMin);
                changed = true;
            }

            // Set the upper threshold if applicable
            if(newValue != getValue()) {
                target.setUpperThreshold(newValue);
                changed = true;
            }

            // Update all of the range properties if there was a change
            if(changed) super.setRangeProperties(newValue, newExtent, newMin, newMax, adjusting);
        } finally {
            target.getReadWriteLock().writeLock().unlock();
        }
    }
}
