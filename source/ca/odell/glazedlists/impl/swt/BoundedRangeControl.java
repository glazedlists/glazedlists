/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.swt;

// to proxy adding of SelectionListeners
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.*;

/**
 * This interface is used by the Viewer classes for ThresholdList.
 * Despite both {@link Scale} and {@link Slider} having similar functional
 * and API requirements, no interface exists to allow them to be used
 * interchangeably.  This interface is designed to allow for that, within
 * the limited scope of what the ThresholdList Viewers need access to.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public interface BoundedRangeControl {

    /**
     * Adds a listener to SelectionEvents on the underlying widget.
     */
    public void addSelectionListener(SelectionListener listener);

    /**
     * Returns the minimum value set on the widget.
     */
    public int getMinimum();

    /**
     * Returns the maximum value set on the widget.
     */
    public int getMaximum();

    /**
     * Returns the value that is currently selected on the widget.
     */
    public int getSelection();

}