/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// to interact with Sliders
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
// to access the ThresholdList
import ca.odell.glazedlists.ThresholdList;

/**
 * A factory for creating a Viewer for Sliders and Scales that binds to a
 * ThresholdList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ThresholdViewerFactory {

    private ThresholdViewerFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a viewer that manipulates the lower bound of the specified
     * ThresholdList based on changes to the Slider.  The ThresholdList linked
     * to this viewer type will contain a range of Objects between the results
     * of getSelection() and getMaximum() on the Slider.
     */
    public static SelectionListener createLower(ThresholdList thresholdList, Slider slider) {
        return new LowerThresholdViewer(thresholdList, BoundedRangeControlFactory.slider(slider));
    }

    /**
     * Creates a viewer that manipulates the lower bound of the specified
     * ThresholdList based on changes to Scale selection.  The ThresholdList
     * linked to this viewer type will contain a range of Objects between the
     * results of getSelection() and getMaximum() on the Scale.
     */
    public static SelectionListener createLower(ThresholdList thresholdList, Scale scale) {
        return new LowerThresholdViewer(thresholdList, BoundedRangeControlFactory.scale(scale));
    }

    /**
     * Creates a viewer that manipulates the upper bound of the specified
     * ThresholdList based on changes to the Slider.  The ThresholdList linked
     * to this model type will contain a range of Objects between the results
     * of getMinimum() and getSelection() on the Slider.
     */
    public static SelectionListener createUpper(ThresholdList thresholdList, Slider slider) {
        return new UpperThresholdViewer(thresholdList, BoundedRangeControlFactory.slider(slider));
    }

    /**
     * Creates a viewer that manipulates the upper bound of the specified
     * ThresholdList based on changes to Scale selection.  The ThresholdList
     * linked to this viewer type will contain a range of Objects between the
     * results of getMinimum() and getSelection() on the Scale.
     */
    public static SelectionListener createUpper(ThresholdList thresholdList, Scale scale) {
        return new UpperThresholdViewer(thresholdList, BoundedRangeControlFactory.scale(scale));
    }

    /**
     * A Viewer class that binds a BoundedRangeControl to the lower threshold on a
     * ThresholdList.
     */
    private static class LowerThresholdViewer implements SelectionListener {

        /** the ThresholdList that is the target for changes */
        private ThresholdList target = null;

        /** the BoundedRangeControl that manipulates the lower threshold on the target list */
        private BoundedRangeControl control = null;

        /** a cache of the maximum value which will likely not change much */
        private int maximum = -1;

        /**
         * Creates a Viewer that binds a BoundedRangeControl to the lower
         * threshold on a ThresholdList.
         */
        LowerThresholdViewer(ThresholdList target, BoundedRangeControl control) {
            this.target = target;
            this.control = control;
            widgetSelected(null);
            control.addSelectionListener(this);
        }

        /**
         * Allows this Viewer to respond to changes to the BoundedRangeControl
         */
        public void widgetSelected(SelectionEvent e) {
            target.getReadWriteLock().writeLock().lock();
            try {
                target.setLowerThreshold(control.getSelection());
                if(maximum != control.getMaximum()) {
                    maximum = control.getMaximum();
                    target.setUpperThreshold(maximum);
                }
            } finally {
                target.getReadWriteLock().writeLock().unlock();
            }
        }

        /**
         * No-op on a Slider, but the SWT documentation excludes information
         * on whether or not it is called for Scale.
         */
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }

    /**
     * A Viewer class that binds a BoundedRangeControl to the upper threshold on a
     * ThresholdList.
     */
    private static class UpperThresholdViewer implements SelectionListener {

        /** the ThresholdList that is the target for changes */
        private ThresholdList target = null;

        /** the BoundedRangeControl that manipulates the lower threshold on the target list */
        private BoundedRangeControl control = null;

        /** a cache of the minimum value which will likely not change much */
        private int minimum = -1;

        /**
         * Creates a Viewer that binds a BoundedRangeControl to the upper
         * threshold on a ThresholdList.
         */
        UpperThresholdViewer(ThresholdList target, BoundedRangeControl control) {
            this.target = target;
            this.control = control;
            widgetSelected(null);
            control.addSelectionListener(this);
        }

        /**
         * Allows this Viewer to respond to changes to the BoundedRangeControl
         */
        public void widgetSelected(SelectionEvent e) {
            target.getReadWriteLock().writeLock().lock();
            try {
                if(minimum != control.getMinimum()) {
                    minimum = control.getMinimum();
                    target.setLowerThreshold(minimum);
                }
                target.setUpperThreshold(control.getSelection());
            } finally {
                target.getReadWriteLock().writeLock().unlock();
            }
        }

        /**
         * No-op on a Slider, but the SWT documentation excludes information
         * on whether or not it is called for Scale.
         */
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }
}