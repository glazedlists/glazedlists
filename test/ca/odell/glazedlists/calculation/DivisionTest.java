/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import junit.framework.TestCase;
import junit.framework.Assert;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;

public class DivisionTest extends TestCase {

    public void testMeanFloat() {
        final EventList<Float> source = new BasicEventList<Float>();
        source.add(new Float(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Integer> count = Calculations.count(source);
        final Calculation<Float> sum = Calculations.sumFloats(source);
        final Calculation<Float> division = Calculations.divideFloats(sum, count);
        division.addPropertyChangeListener(counter);

        // check the initial Division state
        assertEquals(new Float(1), division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(new Float(2));
        assertEquals(new Float(1.5), division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test update with sum change
        source.set(1, new Float(3));
        assertEquals(new Float(2), division.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(new Float(2), division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(new Float(1), division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test remove all
        source.remove(0);
        assertEquals(new Float(Float.NaN), division.getValue());
        assertEquals(2, counter.getCountAndReset());
    }

    public void testMeanDouble() {
        final EventList<Double> source = new BasicEventList<Double>();
        source.add(new Double(1));

        final PropertyChangeCounter counter = new PropertyChangeCounter();
        final Calculation<Integer> count = Calculations.count(source);
        final Calculation<Double> sum = Calculations.sumDoubles(source);
        final Calculation<Double> division = Calculations.divideDoubles(sum, count);
        division.addPropertyChangeListener(counter);

        // check the initial Division state
        assertEquals(new Double(1), division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test add
        source.add(new Double(2));
        assertEquals(new Double(1.5), division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test update with sum change
        source.set(1, new Double(3));
        assertEquals(new Double(2), division.getValue());
        assertEquals(1, counter.getCountAndReset());

        // test update without sum change
        source.set(1, source.get(1));
        assertEquals(new Double(2), division.getValue());
        assertEquals(0, counter.getCountAndReset());

        // test remove
        source.remove(1);
        assertEquals(new Double(1), division.getValue());
        assertEquals(2, counter.getCountAndReset());

        // test remove all
        source.remove(0);
        assertEquals(new Double(Double.NaN), division.getValue());
        assertEquals(2, counter.getCountAndReset());
    }
}