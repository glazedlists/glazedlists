/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// to interact with Sliders
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
// to access the ThresholdList
import ca.odell.glazedlists.ThresholdList;

/**
 * A factory for creating a viewer for a Slider that binds the Slider to a ThresholdList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ThresholdSliderViewerFactory {

    private ThresholdSliderViewerFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a viewer that manipulates the lower bound of the specified
     * ThresholdList based on changes to the Slider.  The ThresholdList linked
     * to this viewer type will contain a range of Objects between the results
     * of getSelection() and getMaximum() on the Slider.
     */
    public static SelectionListener createLower(ThresholdList thresholdList, Slider slider) {
		return new LowerThresholdSliderViewer(thresholdList, slider);
	}

    /**
     * Creates a viewer that manipulates the upper bound of the specified
     * ThresholdList based on changes to the Slider.  The ThresholdList linked
     * to this model type will contain a range of Objects between the results
     * of getMinimum() and getSelection() on the Slider.
     */
    public static SelectionListener createUpper(ThresholdList thresholdList, Slider slider) {
		return new UpperThresholdSliderViewer(thresholdList, slider);
	}

	/**
	 * A Viewer class that binds a Slider to the lower threshold on a
	 * ThresholdList.
	 */
	private static class LowerThresholdSliderViewer implements SelectionListener {

		/** the ThresholdList that is the target for changes */
		private ThresholdList target = null;

		/** the Slider that manipulates the lower threshold on the target list */
		private Slider slider = null;

		/**
		 * Creates a SliderViewer that binds a Slider to the lower threshold
		 * on a ThresholdList.
		 */
		LowerThresholdSliderViewer(ThresholdList target, Slider slider) {
		    this.target = target;
		    this.slider = slider;
		    slider.addSelectionListener(this);
		}

		/**
		 * Allows this Viewer to respond to changes to the Slider
		 */
		public void widgetSelected(SelectionEvent e) {
            target.getReadWriteLock().writeLock().lock();
            try {
				target.setLowerThreshold(slider.getSelection());
				target.setUpperThreshold(slider.getMaximum());
			} finally {
                target.getReadWriteLock().writeLock().unlock();
            }
		}

		/**
		 * No-op on a Slider
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * A Viewer class that binds a Slider to the upper threshold on a
	 * ThresholdList.
	 */
	private static class UpperThresholdSliderViewer implements SelectionListener {

		/** the ThresholdList that is the target for changes */
		private ThresholdList target = null;

		/** the Slider that manipulates the lower threshold on the target list */
		private Slider slider = null;

		/**
		 * Creates a SliderViewer that binds a Slider to the upper threshold
		 * on a ThresholdList.
		 */
		UpperThresholdSliderViewer(ThresholdList target, Slider slider) {
		    this.target = target;
		    this.slider = slider;
		    slider.addSelectionListener(this);
		}

		/**
		 * Allows this Viewer to respond to changes to the Slider
		 */
		public void widgetSelected(SelectionEvent e) {
            target.getReadWriteLock().writeLock().lock();
            try {
				target.setLowerThreshold(slider.getMinimum());
				target.setUpperThreshold(slider.getSelection());
			} finally {
                target.getReadWriteLock().writeLock().unlock();
            }
		}

		/**
		 * No-op on a Slider
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
			throw new UnsupportedOperationException();
		}
	}
}