package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.calculation.Count;
import ca.odell.glazedlists.calculation.Division;
import ca.odell.glazedlists.calculation.Sum;
import junit.framework.TestCase;

import javax.swing.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CalculationsTest extends TestCase {

    public void testCalculationLabel() {
        final EventList<Float> source = new BasicEventList<Float>();
        source.add(1f);

        final Count count = new Count(source);
        final Sum.SumFloat sum = new Sum.SumFloat(source);
        final Division.DivisionFloat division = new Division.DivisionFloat(sum, count);

        final JLabel label = new JLabel();
        final JLabel formattedLabel = new JLabel();
        final NumberFormat formatter = DecimalFormat.getNumberInstance();
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        Calculations.bind(label, division);
        Calculations.bind(formattedLabel, division, formatter);

        assertEquals("1.0", label.getText());
        assertEquals("1.00", formattedLabel.getText());

        // alter the value
        source.add(12f);
        assertEquals("6.5", label.getText());
        assertEquals("6.50", formattedLabel.getText());
    }
}