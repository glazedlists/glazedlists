/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import org.junit.Test;

import static org.junit.Assert.*;

public final class SubtractionTest {

    @Test
    public void testSubtractionFloat() {
        final EventList<Object> aList = new BasicEventList<>();
        final EventList<Object> bList = new BasicEventList<>();

        final Calculation<Integer> a = Calculations.count(aList);
        final Calculation<Integer> b = Calculations.count(bList);
        final Calculation<Float> diff = Calculations.subtractFloats(a, b);

        assertEquals(Float.valueOf(0), diff.getValue());

        aList.add(null);
        assertEquals(Float.valueOf(1), diff.getValue());

        bList.add(null);
        assertEquals(Float.valueOf(0), diff.getValue());
    }

    @Test
    public void testSubtractionDouble() {
        final EventList<Object> aList = new BasicEventList<>();
        final EventList<Object> bList = new BasicEventList<>();

        final Calculation<Integer> a = Calculations.count(aList);
        final Calculation<Integer> b = Calculations.count(bList);
        final Calculation<Double> diff = Calculations.subtractDoubles(a, b);

        assertEquals(Double.valueOf(0), diff.getValue());

        aList.add(null);
        assertEquals(Double.valueOf(1), diff.getValue());

        bList.add(null);
        assertEquals(Double.valueOf(0), diff.getValue());
    }

    @Test
    public void testSubtractionInteger() {
        final EventList<Object> aList = new BasicEventList<>();
        final EventList<Object> bList = new BasicEventList<>();

        final Calculation<Integer> a = Calculations.count(aList);
        final Calculation<Integer> b = Calculations.count(bList);
        final Calculation<Integer> diff = Calculations.subtractIntegers(a, b);

        assertEquals(Integer.valueOf(0), diff.getValue());

        aList.add(null);
        assertEquals(Integer.valueOf(1), diff.getValue());

        bList.add(null);
        assertEquals(Integer.valueOf(0), diff.getValue());
    }

    @Test
    public void testSubtractionLong() {
        final EventList<Object> aList = new BasicEventList<>();
        final EventList<Object> bList = new BasicEventList<>();

        final Calculation<Integer> a = Calculations.count(aList);
        final Calculation<Integer> b = Calculations.count(bList);
        final Calculation<Long> diff = Calculations.subtractLongs(a, b);

        assertEquals(Long.valueOf(0), diff.getValue());

        aList.add(null);
        assertEquals(Long.valueOf(1), diff.getValue());

        bList.add(null);
        assertEquals(Long.valueOf(0), diff.getValue());
    }
}
