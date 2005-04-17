/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swt;

// to interact with Sliders
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
// to access the ThresholdList
import ca.odell.glazedlists.ThresholdList;

/**
 * A Viewer class that binds a BoundedRangeControl to the lower threshold on a
 * ThresholdList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class LowerThresholdViewer implements SelectionListener {

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
    public LowerThresholdViewer(ThresholdList target, BoundedRangeControl control) {
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