/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.impl.swing.SwingThreadProxyCalculation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;

import javax.swing.JLabel;

public final class CalculationsSwing {
    private static final Float FLOAT_NAN = new Float(Float.NaN);
    private static final Double DOUBLE_NAN = new Double(Double.NaN);

    private CalculationsSwing() {}

    /**
     * Wraps the source in a {@link Calculation} that fires all of its update
     * events from the Swing event dispatch thread.
     */
    public static <E> Calculation<E> swingThreadProxyCalculation(Calculation<? extends E> source) {
        return new SwingThreadProxyCalculation<E>(source);
    }

    /**
     * Returns <code>true</code> if <code>calc</code> is a {@link Calculation} that fires
     * all of its update events from the Swing event dispatch thread.
     */
    public static boolean isSwingThreadProxyCalculation(Calculation calc) {
        return calc instanceof SwingThreadProxyCalculation;
    }

    /**
     * Updates the given label with the latest value of a Calculation each time
     * it reports a change.
     *
     * @param label the JLabel displaying the value of the calculation
     * @param calculation a source of a value that changes over time
     */
    public static void bind(JLabel label, Calculation<?> calculation) {
        bind(label, calculation, null);
    }

    /**
     * Updates the given label with the latest value of a Calculation each time
     * it reports a change.
     *
     * @param label the JLabel displaying the value of the calculation
     * @param calculation a source of a value that changes over time
     * @param formatter used to format the raw value of the calculation into pretty display text
     */
    public static void bind(JLabel label, Calculation<?> calculation, Format formatter) {
        calculation.addPropertyChangeListener(new CalculationToLabelBinder(label, formatter, calculation.getValue()));
    }

    /**
     * Updates the given label with the latest value of a Calculation each time
     * it reports a change. The value displayed in the label can optionally be
     * formatted using a specified Format.
     */
    private static final class CalculationToLabelBinder implements PropertyChangeListener {

        private final JLabel label;
        private final Format formatter;

        private CalculationToLabelBinder(JLabel label, Format formatter, Object initialValue) {
            this.label = label;
            this.formatter = formatter;
            update(initialValue);
        }

        private void update(Object value) {
            // special case out null and NaN 'cause otherwise they're ugly!
            if (null == value || FLOAT_NAN.equals(value) || DOUBLE_NAN.equals(value)) {
                label.setText("");
            } else {
                label.setText(formatter != null ? formatter.format(value) : String.valueOf(value));
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            update(evt.getNewValue());
        }
    }
}