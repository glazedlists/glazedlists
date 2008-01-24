package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.calculation.Calculation;
import ca.odell.glazedlists.calculation.Calculations;
import junit.framework.TestCase;

import javax.swing.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CalculationsTest extends TestCase {

    public void testCalculationLabel() {
        final EventList<Float> source = new BasicEventList<Float>();
        source.add(1f);

        final Calculation<Float> mean = Calculations.meanFloats(source);

        final JLabel label = new JLabel();
        final JLabel formattedLabel = new JLabel();
        final NumberFormat formatter = DecimalFormat.getNumberInstance();
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        SwingCalculations.bind(label, mean);
        SwingCalculations.bind(formattedLabel, mean, formatter);

        assertEquals("1.0", label.getText());
        assertEquals("1.00", formattedLabel.getText());

        // alter the value
        source.add(12f);
        assertEquals("6.5", label.getText());
        assertEquals("6.50", formattedLabel.getText());

        // remove all values
        source.clear();
        assertEquals("", label.getText());
    }
}