/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.ThresholdList;

import javax.swing.*;

/**
 * A LowerThresholdRangeModel provides an implementation of a bounded-range
 * model for a slider widget that binds a JSlider to a ThresholdList.  This
 * implementation maps the slider value to the lower threshold of the
 * ThresholdList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class LowerThresholdRangeModel extends DefaultBoundedRangeModel implements BoundedRangeModel {

    /** the list to connect a slider widget to */
    private ThresholdList target = null;

    /**
     * Creates a new range that controls specified ThresholdList.
     */
    public LowerThresholdRangeModel(ThresholdList target) {
        this.target = target;
    }

    /**
     * Returns the model's maximum.
     */
    @Override
    public int getMaximum() {
        target.getReadWriteLock().readLock().lock();
        try {
            return target.getUpperThreshold();
        } finally {
            target.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Returns the model's current value.
     */
    @Override
    public int getValue() {
        target.getReadWriteLock().readLock().lock();
        try {
            return target.getLowerThreshold();
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
    @Override
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
                (newMin != getMinimum()) ||
                (adjusting != getValueIsAdjusting());

            // Set the lower threshold if applicable
            if(newValue != getValue()) {
                target.setLowerThreshold(newValue);
                changed = true;
            }

            // Set the upper threshold if applicable
            if(newMax != getMaximum()) {
                target.setUpperThreshold(newMax);
                changed = true;
            }

            // Update all of the range properties if there was a change
            if(changed) super.setRangeProperties(newValue, newExtent, newMin, newMax, adjusting);

        } finally {
            target.getReadWriteLock().writeLock().unlock();
        }
    }
}