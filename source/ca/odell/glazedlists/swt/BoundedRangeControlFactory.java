/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// to proxy adding of SelectionListeners
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.*;

/**
 * A Factory to simplify the wrapping of {@link Scale} and {@link Slider}
 * widgets in an Adapter to allow the use of the common
 * {@link BoundedRangeControl} interface for polymorphism.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
class BoundedRangeControlFactory {

    /**
     * Don't let this Factory be created.
     */
    private BoundedRangeControlFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Thinly wrap a {@link Slider} Object to allow use of the
     * {@link BoundedRangeControl} interface.
     */
    public static BoundedRangeControl slider(Slider slider) {
        return new SliderBoundedRangeControl(slider);
    }

    /**
     * Thinly wrap a {@link Scale} Object to allow use of the
     * {@link BoundedRangeControl} interface.
     */
    public static BoundedRangeControl scale(Scale scale) {
        return new ScaleBoundedRangeControl(scale);
    }

    /**
     * This class exists as a proxy to methods on the Slider interface
     * via the BoundedRangeControl interface.
     */
    private static final class SliderBoundedRangeControl implements BoundedRangeControl {

        /** the slider to proxy calls to */
        private Slider slider;

        /**
         * Creates a new proxy to a {@link Slider} widget that implements
         * the {@link BoundedRangeControl}.
         */
        SliderBoundedRangeControl(Slider slider) {
            this.slider = slider;
        }

        /** {@inheritDoc} */
        public void addSelectionListener(SelectionListener listener) {
            slider.addSelectionListener(listener);
        }

        /** {@inheritDoc} */
        public int getMinimum() {
            return slider.getMinimum();
        }

        /** {@inheritDoc} */
        public int getMaximum() {
            return slider.getMaximum();
        }

        /** {@inheritDoc} */
        public int getSelection() {
            return slider.getSelection();
        }
    }

    /**
     * This class exists as a proxy to methods on the Scale interface
     * via the BoundedRangeControl interface.
     */
    private static final class ScaleBoundedRangeControl implements BoundedRangeControl {

        /** the slider to proxy calls to */
        private Scale scale;

        /**
         * Creates a new proxy to a {@link Scale} widget that implements
         * the {@link BoundedRangeControl}.
         */
        ScaleBoundedRangeControl(Scale scale) {
            this.scale = scale;
        }

        /** {@inheritDoc} */
        public void addSelectionListener(SelectionListener listener) {
            scale.addSelectionListener(listener);
        }

        /** {@inheritDoc} */
        public int getMinimum() {
            return scale.getMinimum();
        }

        /** {@inheritDoc} */
        public int getMaximum() {
            return scale.getMaximum();
        }

        /** {@inheritDoc} */
        public int getSelection() {
            return scale.getSelection();
        }
    }
}