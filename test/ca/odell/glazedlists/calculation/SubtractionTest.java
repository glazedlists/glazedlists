/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.calculation;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import junit.framework.TestCase;

public final class SubtractionTest extends TestCase {

    public void testSubtractionFloat() {
        final EventList<Object> aList = new BasicEventList<Object>();
        final EventList<Object> bList = new BasicEventList<Object>();
        
        final Calculation<Integer> a = Calculations.count(aList);
        final Calculation<Integer> b = Calculations.count(bList);
        final Calculation<Float> diff = Calculations.subtractFloats(a, b);

        assertEquals(0f, diff.getValue());

        aList.add(null);
        assertEquals(1f, diff.getValue());

        bList.add(null);
        assertEquals(0f, diff.getValue());
    }

    public void testSubtractionDouble() {
        final EventList<Object> aList = new BasicEventList<Object>();
        final EventList<Object> bList = new BasicEventList<Object>();

        final Calculation<Integer> a = Calculations.count(aList);
        final Calculation<Integer> b = Calculations.count(bList);
        final Calculation<Double> diff = Calculations.subtractDoubles(a, b);

        assertEquals(0d, diff.getValue());

        aList.add(null);
        assertEquals(1d, diff.getValue());

        bList.add(null);
        assertEquals(0d, diff.getValue());
    }

    public void testSubtractionInteger() {
        final EventList<Object> aList = new BasicEventList<Object>();
        final EventList<Object> bList = new BasicEventList<Object>();

        final Calculation<Integer> a = Calculations.count(aList);
        final Calculation<Integer> b = Calculations.count(bList);
        final Calculation<Integer> diff = Calculations.subtractIntegers(a, b);

        assertEquals(new Integer(0), new Integer(diff.getValue()));

        aList.add(null);
        assertEquals(new Integer(1), new Integer(diff.getValue()));

        bList.add(null);
        assertEquals(new Integer(0), new Integer(diff.getValue()));
    }

    public void testSubtractionLong() {
        final EventList<Object> aList = new BasicEventList<Object>();
        final EventList<Object> bList = new BasicEventList<Object>();

        final Calculation<Integer> a = Calculations.count(aList);
        final Calculation<Integer> b = Calculations.count(bList);
        final Calculation<Long> diff = Calculations.subtractLongs(a, b);

        assertEquals(new Long(0), new Long(diff.getValue()));

        aList.add(null);
        assertEquals(new Long(1), new Long(diff.getValue()));

        bList.add(null);
        assertEquals(new Long(0), new Long(diff.getValue()));
    }
}