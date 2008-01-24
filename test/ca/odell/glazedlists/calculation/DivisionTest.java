/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import junit.framework.TestCase;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;

public class DivisionTest extends TestCase {

    public void testMeanFloat() {
        final EventList<Float> source = new BasicEventList<Float>();
        source.add(1f);

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Integer> count = Calculations.count(source);
        final Calculation<Float> sum = Calculations.sumFloats(source);
        final Calculation<Float> division = Calculations.divideFloats(sum, count);
        division.addPropertyChangeListener(counter);

        // check the initial Division state
        assertEquals(1f, division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(2f);
        assertEquals(1.5f, division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test update with sum change
        source.set(1, 3f);
        assertEquals(2f, division.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(2f, division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(1f, division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test remove all
        source.remove(0);
        assertEquals(Float.NaN, division.getValue());
        assertEquals(2, counter.getCountAndReset());
    }

    public void testMeanDouble() {
        final EventList<Double> source = new BasicEventList<Double>();
        source.add(1d);

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Integer> count = Calculations.count(source);
        final Calculation<Double> sum = Calculations.sumDoubles(source);
        final Calculation<Double> division = Calculations.divideDoubles(sum, count);
        division.addPropertyChangeListener(counter);

        // check the initial Division state
        assertEquals(1d, division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(2d);
        assertEquals(1.5d, division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test update with sum change
        source.set(1, 3d);
        assertEquals(2d, division.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(2d, division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(1d, division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test remove all
        source.remove(0);
        assertEquals(Double.NaN, division.getValue());
        assertEquals(2, counter.getCountAndReset());
    }
}