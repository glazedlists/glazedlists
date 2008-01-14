/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ThresholdList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.impl.swing.LowerThresholdRangeModel;
import ca.odell.glazedlists.impl.swing.SwingThreadProxyEventList;
import ca.odell.glazedlists.impl.swing.UpperThresholdRangeModel;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.NumberFormat;

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

    // Calculations // // // // // // // // // // // // // // // // // // // //

    /**
     * Updates the given label with the latest value of a Calculation each time
     * it reports a change.
     *
     * @param label the JLabel displaying the value of the calculation
     * @param calculation a source of a numeric value that changes over time
     */
    public static void bind(JLabel label, Calculation calculation) {
        bind(label, calculation, null);
    }

    /**
     * Updates the given label with the latest value of a Calculation each time
     * it reports a change.
     *
     * @param label the JLabel displaying the value of the calculation
     * @param calculation a source of a numeric value that changes over time
     * @param formatter used to format the raw numeric value of the calculation
     *      into pretty display text
     */
    public static void bind(JLabel label, Calculation calculation, NumberFormat formatter) {
        calculation.addPropertyChangeListener(new CalculationToLabelBinder(label, formatter, calculation.getValue()));
    }

    /**
     * Updates the given label with the latest value of a Calculation each time
     * it reports a change. The value displayed in the label can optionally be
     * formatted using a specified NumberFormat.
     */
    private static final class CalculationToLabelBinder implements PropertyChangeListener {

        private final JLabel label;
        private final NumberFormat formatter;

        private CalculationToLabelBinder(JLabel label, NumberFormat formatter, Number initialValue) {
            this.label = label;
            this.formatter = formatter;
            update(initialValue);
        }

        private void update(Number value) {
            final String text = formatter == null ? (value == null ? "" : String.valueOf(value)) : formatter.format(value);
            label.setText(text);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            update((Number) evt.getNewValue());
        }
    }
}