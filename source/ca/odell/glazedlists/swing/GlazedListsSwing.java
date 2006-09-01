/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ThresholdList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.impl.swing.LowerThresholdRangeModel;
import ca.odell.glazedlists.impl.swing.SwingThreadProxyEventList;
import ca.odell.glazedlists.impl.swing.UpperThresholdRangeModel;

import javax.swing.*;

/**
 * A factory for creating all sorts of objects to be used with Glazed Lists.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class GlazedListsSwing {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedListsSwing() {
        throw new UnsupportedOperationException();
    }

    // EventLists // // // // // // // // // // // // // // // // // // // // //

    /**
     * Wraps the source in an {@link EventList} that fires all of its update
     * events from the Swing event dispatch thread.
     */
    public static <E> TransformedList<E, E> swingThreadProxyList(EventList<E> source) {
        return new SwingThreadProxyEventList<E>(source);
    }

    /**
     * Returns true iff <code>list</code> is an {@link EventList} that fires
     * all of its update events from the Swing event dispatch thread.
     */
    public static boolean isSwingThreadProxyList(EventList list) {
        return list instanceof SwingThreadProxyEventList;
    }

    // ThresholdRangeModels // // // // // // // // // // // // // // // // //

    /**
     * Creates a model that manipulates the lower bound of the specified
     * ThresholdList.  The ThresholdList linked to this model type will contain
     * a range of Objects between the results of getValue() and getMaximum()
     * on the BoundedRangeModel.
     */
    public static BoundedRangeModel lowerRangeModel(ThresholdList target) {
        return new LowerThresholdRangeModel(target);
    }

    /**
     * Creates a model that manipulates the upper bound of the specified
     * ThresholdList.  The ThresholdList linked to this model type will contain
     * a range of Objects between the results of getMinimum() and getValue()
     * on the BoundedRangeModel.
     */
    public static BoundedRangeModel upperRangeModel(ThresholdList target) {
        return new UpperThresholdRangeModel(target);
    }
}