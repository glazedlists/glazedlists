/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

import java.util.*;
// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.event.*;
// for access to volatile classes
import ca.odell.glazedlists.impl.*;
import ca.odell.glazedlists.impl.sort.*;
import ca.odell.glazedlists.impl.io.*;
import ca.odell.glazedlists.impl.beans.*;
import ca.odell.glazedlists.impl.gui.*;
import ca.odell.glazedlists.impl.swt.*;
// implemented interfaces
import ca.odell.glazedlists.io.ByteCoder;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.ThresholdEvaluator;
import java.util.Comparator;
import org.eclipse.jface.viewers.ILabelProvider;
// SWT components
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Scale;


/**
 * A factory for creating all sorts of objects to be used with Glazed Lists.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class GlazedListsSWT {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedListsSWT() {
        throw new UnsupportedOperationException();
    }

    // EventLists // // // // // // // // // // // // // // // // // // // // //

    /**
     * Wraps the source in an {@link EventList} that fires all of its update events
     * from the SWT user interface thread.
     */
    public static TransformedList swtThreadProxyList(EventList source, Display display) {
        return new SWTThreadProxyEventList(source, display);
    }

    
    // LabelProviders // // // // // // // // // // // // // // // // // // //

    /**
     * Creates an {@link ILabelProvider} that returns labels for Objects via
     * Relection. The label returned will be the String value of specified
     * JavaBean property.
     */
    public static ILabelProvider beanLabelProvider(String property) {
        return new BeanLabelProvider(property);
    }
    

    // ThresholdViewers // // // // // // // // // // // // // // // // // // //

    /**
     * Creates a viewer that manipulates the lower bound of the specified
     * ThresholdList based on changes to the Slider.  The ThresholdList linked
     * to this viewer type will contain a range of Objects between the results
     * of getSelection() and getMaximum() on the Slider.
     */
    public static SelectionListener lowerThresholdViewer(ThresholdList thresholdList, Slider slider) {
        return new LowerThresholdViewer(thresholdList, BoundedRangeControlFactory.slider(slider));
    }

    /**
     * Creates a viewer that manipulates the lower bound of the specified
     * ThresholdList based on changes to Scale selection.  The ThresholdList
     * linked to this viewer type will contain a range of Objects between the
     * results of getSelection() and getMaximum() on the Scale.
     */
    public static SelectionListener lowerThresholdViewer(ThresholdList thresholdList, Scale scale) {
        return new LowerThresholdViewer(thresholdList, BoundedRangeControlFactory.scale(scale));
    }

    /**
     * Creates a viewer that manipulates the upper bound of the specified
     * ThresholdList based on changes to the Slider.  The ThresholdList linked
     * to this model type will contain a range of Objects between the results
     * of getMinimum() and getSelection() on the Slider.
     */
    public static SelectionListener upperThresholdViewer(ThresholdList thresholdList, Slider slider) {
        return new UpperThresholdViewer(thresholdList, BoundedRangeControlFactory.slider(slider));
    }

    /**
     * Creates a viewer that manipulates the upper bound of the specified
     * ThresholdList based on changes to Scale selection.  The ThresholdList
     * linked to this viewer type will contain a range of Objects between the
     * results of getMinimum() and getSelection() on the Scale.
     */
    public static SelectionListener upperThresholdViewer(ThresholdList thresholdList, Scale scale) {
        return new UpperThresholdViewer(thresholdList, BoundedRangeControlFactory.scale(scale));
    }
}