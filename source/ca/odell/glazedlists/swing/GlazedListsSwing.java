/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.swing;

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
import ca.odell.glazedlists.impl.swing.*;
// implemented interfaces
import ca.odell.glazedlists.io.ByteCoder;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.ThresholdEvaluator;
import java.util.Comparator;
// swing
import javax.swing.BoundedRangeModel;

/**
 * A factory for creating all sorts of objects to be used with Glazed Lists.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
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
     * Wraps the source in an {@link EventList} that fires all of its update events
     * from the Swing event dispatch thread.
     */
    public static TransformedList swingThreadProxyList(EventList source) {
        return new SwingThreadProxyEventList(source);
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