/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
// Swing toolkit stuff for implementing models
import javax.swing.*;
import java.util.EventListener;
// for responding to user actions
import javax.swing.event.*;

/**
 * A factory for creating BoundedRangeModels that are linked to a ThresholdList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ThresholdRangeModelFactory {

    private ThresholdRangeModelFactory() {
        // don't make one of these
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a model that manipulates the lower bound of the specified
     * ThresholdList.  The ThresholdList linked to this model type will contain
     * a range of Objects between the results of getValue() and getMaximum()
     * on the BoundedRangeModel.
     */
    public static BoundedRangeModel createLower(ThresholdList target) {
        return new LowerThresholdRangeModel(target);
    }

    /**
     * Creates a model that manipulates the upper bound of the specified
     * ThresholdList.  The ThresholdList linked to this model type will contain
     * a range of Objects between the results of getMinimum() and getValue()
     * on the BoundedRangeModel.
     */
    public static BoundedRangeModel createUpper(ThresholdList target) {
        return new UpperThresholdRangeModel(target);
    }

    /**
     * A LowerThresholdRangeModel provides an implementation of a bounded-range
     * model for a slider widget that binds a JSlider to a ThresholdList.  This
     * implementation maps the slider value to the lower threshold of the
     * ThresholdList.
     */
    private static class LowerThresholdRangeModel extends DefaultBoundedRangeModel implements BoundedRangeModel {

        /** the list to connect a slider widget to */
        private ThresholdList target = null;

        /**
         * Creates a new range that controls specified ThresholdList.
         */
        LowerThresholdRangeModel(ThresholdList target) {
            this.target = target;
        }

        /**
         * Returns the model's maximum.
         */
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


    /**
     * A UpperThresholdRangeModel provides an implementation of a bounded-range
     * model for a slider widget that binds a JSlider to a ThresholdList.  This
     * implementation maps the slider value to the upper threshold of the
     * ThresholdList.
     */
    private static class UpperThresholdRangeModel extends DefaultBoundedRangeModel implements BoundedRangeModel {

        /** the list to connect a slider widget to */
        private ThresholdList target = null;

        /**
         * Creates a new range that controls specified ThresholdList.
         */
        UpperThresholdRangeModel(ThresholdList target) {
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

}
