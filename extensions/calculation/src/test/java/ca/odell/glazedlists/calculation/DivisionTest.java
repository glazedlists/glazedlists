/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import org.junit.Test;

import static org.junit.Assert.*;

public class DivisionTest {

    @Test
    public void testMeanFloat() {
        final EventList<Float> source = new BasicEventList<>();
        source.add(Float.valueOf(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Integer> count = Calculations.count(source);
        final Calculation<Float> sum = Calculations.sumFloats(source);
        final Calculation<Float> division = Calculations.divideFloats(sum, count);
        division.addPropertyChangeListener(counter);

        // check the initial Division state
        assertEquals(Float.valueOf(1), division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(Float.valueOf(2));
        assertEquals(Float.valueOf(1.5f), division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test update with sum change
        source.set(1, Float.valueOf(3));
        assertEquals(Float.valueOf(2), division.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(Float.valueOf(2), division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(Float.valueOf(1), division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test remove all
        source.remove(0);
        assertEquals(Float.valueOf(Float.NaN), division.getValue());
        assertEquals(2, counter.getCountAndReset());
    }

    @Test
    public void testMeanDouble() {
        final EventList<Double> source = new BasicEventList<>();
        source.add(Double.valueOf(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Integer> count = Calculations.count(source);
        final Calculation<Double> sum = Calculations.sumDoubles(source);
        final Calculation<Double> division = Calculations.divideDoubles(sum, count);
        division.addPropertyChangeListener(counter);

        // check the initial Division state
        assertEquals(Double.valueOf(1), division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(Double.valueOf(2));
        assertEquals(Double.valueOf(1.5), division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test update with sum change
        source.set(1, Double.valueOf(3));
        assertEquals(Double.valueOf(2), division.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(Double.valueOf(2), division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(Double.valueOf(1), division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test remove all
        source.remove(0);
        assertEquals(Double.valueOf(Double.NaN), division.getValue());
        assertEquals(2, counter.getCountAndReset());
    }
}
