/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.calculation.Calculation;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;

public final class SwingCalculations {

    private SwingCalculations() {}

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
    public static void bind(JLabel label, Calculation<? extends Number> calculation, Format formatter) {
        calculation.addPropertyChangeListener(new CalculationToLabelBinder(label, formatter, calculation.getValue()));
    }

    /**
     * Updates the given label with the latest value of a Calculation each time
     * it reports a change. The value displayed in the label can optionally be
     * formatted using a specified NumberFormat.
     */
    private static final class CalculationToLabelBinder implements PropertyChangeListener {

        private final JLabel label;
        private final Format formatter;

        private CalculationToLabelBinder(JLabel label, Format formatter, Number initialValue) {
            this.label = label;
            this.formatter = formatter;
            update(initialValue);
        }

        private void update(Number value) {
            final String text = formatter == null ? (isNumberFormattable(value) ? String.valueOf(value) : "") : formatter.format(value);
            label.setText(text);
        }

        private static boolean isNumberFormattable(Number n) {
            return n != null && !n.equals(new Float(Float.NaN)) && !n.equals(new Double(Double.NaN));
        }

        public void propertyChange(PropertyChangeEvent evt) {
            update((Number) evt.getNewValue());
        }
    }
}